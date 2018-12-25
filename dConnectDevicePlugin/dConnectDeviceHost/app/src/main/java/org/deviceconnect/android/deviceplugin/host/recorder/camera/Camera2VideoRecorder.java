/*
 Camera2VideoRecorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.provider.FileManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Camera2VideoRecorder extends AbstractCamera2Recorder implements HostDeviceStreamRecorder {

    /**
     * ログ出力用タグ.
     */
    private static final String TAG = "host.dplugin";

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * カメラターゲットIDの定義.
     */
    private static final String ID_BASE = "video";

    /**
     * カメラ名の定義.
     */
    private static final String NAME_BASE = "Video Camera";

    /**
     * 画面の向きを格納するリスト.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * マイムタイプ一覧.
     */
    private final List<String> mMimeTypes = new ArrayList<String>() {
        {
            add("video/mp4");
        }
    };

    /**
     * ファイルマネージャ.
     */
    private final FileManager mFileManager;

    /**
     * 初期化状態.
     */
    private boolean mIsInitialized;

    private boolean mIsVideoRecording;

    private CameraCaptureSession mCaptureSession;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param cameraId カメラID
     */
    public Camera2VideoRecorder(final @NonNull Context context,
                                final @NonNull String cameraId,
                                final @NonNull FileManager fileManager) {
        super(context, cameraId);
        mFileManager = fileManager;
    }

    private interface CaptureSessionCallback {
        void onSessionCreated(@NonNull CameraCaptureSession session);
        void onSessionFailed(@NonNull Exception e);
    }

    private SurfaceRecorder mSurfaceRecorder;

    private void prepareSession(final @NonNull CaptureSessionCallback callback) {
        if (DEBUG) {
            Log.d(TAG, "prepareSession: id=" + mCameraId);
        }
        openCamera(new CameraOpenCallback() {
            @Override
            public void onOpen(final @NonNull CameraDevice camera, final boolean isNew) {
                synchronized (Camera2VideoRecorder.this) {
                    if (mCaptureSession != null) {
                        callback.onSessionCreated(mCaptureSession);
                        return;
                    }
                }

                try {
                    Options options = mOptions;
                    if (DEBUG) {
                        Size pictureSize = options.getPictureSize();
                        Log.d(TAG, "Picture size: " + pictureSize.getWidth() + "x" + pictureSize.getHeight());
                    }
                    mSurfaceRecorder = new SurfaceRecorder(getContext(), options.getPictureSize());
                    if (DEBUG) {
                        Log.d(TAG, "Created resources.");
                    }

                    List<Surface> outputs = new ArrayList<>();
                    outputs.add(mSurfaceRecorder.getInputSurface());

                    camera.createCaptureSession(outputs, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(final @NonNull CameraCaptureSession session) {
                            mCaptureSession = session;
                            callback.onSessionCreated(session);
                        }

                        @Override
                        public void onConfigureFailed(final @NonNull CameraCaptureSession session) {
                            releaseCamera();
                            callback.onSessionFailed(new RecorderException(RecorderException.REASON_FATAL));
                        }
                    }, mHandler);
                } catch (Exception e) {
                    releaseCamera();
                    callback.onSessionFailed(e);
                }
            }

            @Override
            public void onError(final @NonNull Exception ex) {
                callback.onSessionFailed(ex);
            }
        });
    }

    private synchronized void releaseResources() {
        if (mSurfaceRecorder != null) {
            mSurfaceRecorder.release();
            mSurfaceRecorder = null;
        }
    }

    private synchronized void releaseCamera() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCamera != null) {
            mCamera.close();
            mCamera = null;
        }
        releaseResources();
    }

    /**
     * カメラの取り付けられた向きを取得します.
     *
     * @return カメラの取り付けられた向き
     */
    private int getSensorOrientation() {
        return Camera2Helper.getSensorOrientation(mCameraManager, mCameraId);
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + getSensorOrientation() + 270) % 360;
    }

    @Override
    public synchronized void initialize() {
        if (mIsInitialized) {
            return;
        }
        initOptions();
        mIsInitialized = true;
    }

    private void initOptions() {
        List<Size> supportedPictureList = Camera2Helper.getSupportedPictureSizes(mCameraManager, mCameraId);
        mOptions.setSupportedPictureSizeList(supportedPictureList);
        mOptions.setPictureSize(supportedPictureList.get(0));

        List<Size> supportedPreviewList = Camera2Helper.getSupportedPictureSizes(mCameraManager, mCameraId);
        mOptions.setSupportedPreviewSizeList(supportedPreviewList);
        mOptions.setPreviewSize(supportedPreviewList.get(0));

        Size defaultSize = mOptions.getDefaultPictureSize();
        if (defaultSize != null) {
            mOptions.setPictureSize(defaultSize);
        }
    }

    @Override
    public synchronized void clean() {
        for (PreviewServer server : getServers()) {
            server.stopWebServer();
        }
    }

    @Override
    public String getId() {
        return ID_BASE + "_" + mCameraId;
    }

    @Override
    public String getName() {
        return NAME_BASE + " - " + mFacing.getName();
    }

    @Override
    public String getMimeType() {
        return mMimeTypes.get(0);
    }

    @Override
    public RecorderState getState() {
        if (mIsVideoRecording) {
            return RecorderState.RECORDING;
        }
        return RecorderState.INACTTIVE;
    }

    @Override
    public PictureSize getPictureSize() {
        return new PictureSize(mOptions.getPictureSize());
    }

    @Override
    public void setPictureSize(final PictureSize size) {
        Size newSize = new Size(size.getWidth(), size.getHeight());
        mOptions.setPictureSize(newSize);
    }

    @Override
    public PictureSize getPreviewSize() {
        return new PictureSize(mOptions.getPreviewSize());
    }

    @Override
    public void setPreviewSize(final PictureSize size) {
        Size newSize = new Size(size.getWidth(), size.getHeight());
        mOptions.setPreviewSize(newSize);
    }

    @Override
    public double getMaxFrameRate() {
        return 0;  // TODO
    }

    @Override
    public void setMaxFrameRate(final double frameRate) {
        // TODO
    }

    @Override
    public int getPreviewBitRate() {
        return 0;  // TODO
    }

    @Override
    public void setPreviewBitRate(final int bitRate) {
        // TODO
    }

    @Override
    public List<PictureSize> getSupportedPictureSizes() {
        List<PictureSize> result = new ArrayList<>();
        for (Size size : mOptions.getSupportedPictureSizeList()) {
            result.add(new PictureSize(size));
        }
        return result;
    }

    @Override
    public List<PictureSize> getSupportedPreviewSizes() {
        List<PictureSize> result = new ArrayList<>();
        for (Size size : mOptions.getSupportedPreviewSizeList()) {
            result.add(new PictureSize(size));
        }
        return result;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return mMimeTypes;
    }

    @Override
    public boolean isSupportedPictureSize(final int width, final int height) {
        for (PictureSize size : getSupportedPictureSizes()) {
            if (size.getWidth() == width && size.getHeight() == height) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSupportedPreviewSize(final int width, final int height) {
        for (PictureSize size : getSupportedPreviewSizes()) {
            if (size.getWidth() == width && size.getHeight() == height) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void requestPermission(final PermissionCallback callback) {
        CapabilityUtil.requestPermissions(getContext(), new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                callback.onAllowed();
            }

            @Override
            public void onFail(final String deniedPermission) {
                callback.onDisallowed();
            }
        });
    }

    @Override
    public List<PreviewServer> getServers() {
        return new ArrayList<>();
    }

    @Override
    public boolean canPauseRecording() {
        return false;
    }

    @Override
    public void startRecording(final @NonNull String serviceId,
                               final @NonNull RecordingListener listener) {
        prepareSession(new CaptureSessionCallback() {
            @Override
            public void onSessionCreated(final @NonNull CameraCaptureSession session) {
                try {
                    SurfaceRecorder surfaceRecorder = mSurfaceRecorder;
                    surfaceRecorder.initMuxer(mFileManager.getBasePath());
                    surfaceRecorder.start();

                    CameraDevice camera = session.getDevice();
                    CaptureRequest.Builder requestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                    requestBuilder.addTarget(surfaceRecorder.getInputSurface());
                    requestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                    CaptureRequest request = requestBuilder.build();
                    session.setRepeatingRequest(request, new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(final @NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                            if (DEBUG) {
                                Log.d(TAG, "onCaptureStarted");
                            }
                        }

                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            if (DEBUG) {
                                Log.d(TAG, "onCaptureCompleted");
                            }
                        }

                        @Override
                        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                            if (DEBUG) {
                                Log.d(TAG, "onCaptureFailed: failure=" + failure);
                            }
                        }


                    }, mHandler);

                    mIsVideoRecording = true;

                    File outputFile = surfaceRecorder.getOutputFile();
                    listener.onRecorded(Camera2VideoRecorder.this, outputFile != null ? outputFile.getAbsolutePath() : null);
                } catch (CameraAccessException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to create capture request for video.", e);
                    }
                    listener.onFailed(Camera2VideoRecorder.this, "Failed to create capture request for video.");
                } catch (RecorderException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to create capture request for video.", e);
                    }
                    listener.onFailed(Camera2VideoRecorder.this, "Failed to create capture request for video.");
                }
            }

            @Override
            public void onSessionFailed(final @NonNull Exception ex) {
                listener.onFailed(Camera2VideoRecorder.this, ex.getMessage());
            }
        });
    }

    @Override
    public void stopRecording(final @NonNull StoppingListener listener) {
        synchronized (this) {
            mIsVideoRecording = false;

            if (mSurfaceRecorder != null) {
                File videoFile = mSurfaceRecorder.stop();
                registerVideo(videoFile);
                releaseCamera();
                listener.onStopped(this, videoFile.getAbsolutePath());
            } else {
                listener.onFailed(this, "Video recorder is already released.");
            }
        }
    }

    private void registerVideo(final File videoFile) {
        if (checkVideoFile(videoFile)) {
            // Content Providerに登録する.
            MediaMetadataRetriever mediaMeta = new MediaMetadataRetriever();
            mediaMeta.setDataSource(videoFile.toString());
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.TITLE, videoFile.getName());
            values.put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.getName());
            values.put(MediaStore.Video.Media.ARTIST, "DeviceConnect");
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/avc");
            values.put(MediaStore.Video.Media.DATA, videoFile.toString());
            resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    private boolean checkVideoFile(final @NonNull File file) {
        return file.exists() && file.length() > 0;
    }

    @Override
    public void pauseRecording() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resumeRecording() {
        throw new UnsupportedOperationException();
    }

}
