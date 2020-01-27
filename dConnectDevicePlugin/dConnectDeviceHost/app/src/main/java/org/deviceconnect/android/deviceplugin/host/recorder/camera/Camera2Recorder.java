/*
 Camera2Recorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.camera.Camera2Helper;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapper;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperException;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.DefaultSurfaceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.ImageUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaSharing;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSettingData;
import org.deviceconnect.android.deviceplugin.host.recorder.util.SurfaceRecorder;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;

public class Camera2Recorder extends AbstractCamera2Recorder implements HostDevicePhotoRecorder, HostDeviceStreamRecorder {
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
    private static final String ID_BASE = "camera";

    /**
     * カメラ名の定義.
     */
    private static final String NAME_BASE = "Camera";

    /**
     * ファイル名に付けるプレフィックス.
     */
    private static final String FILENAME_PREFIX = "android_camera_";

    /**
     * ファイルの拡張子.
     */
    private static final String FILE_EXTENSION = ".jpg";

    /**
     * 日付のフォーマット.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    /**
     * 写真の JPEG 品質.
     */
    private static final int PHOTO_JPEG_QUALITY = 100;

    /**
     * 端末の回転方向を格納するリスト.
     */
    private static final SparseIntArray ROTATIONS = new SparseIntArray();
    static {
        ROTATIONS.append(Surface.ROTATION_0, 0);
        ROTATIONS.append(Surface.ROTATION_90, 90);
        ROTATIONS.append(Surface.ROTATION_180, 180);
        ROTATIONS.append(Surface.ROTATION_270, 270);
    }

    /**
     * マイムタイプ一覧.
     */
    private final List<String> mMimeTypes = new ArrayList<String>() {
        {
            add(MIME_TYPE_JPEG);
            add("video/x-mjpeg");
            add("video/x-rtp");
            add("video/mp4");
        }
    };

    /**
     * ファイルマネージャ.
     */
    private final FileManager mFileManager;

    /**
     * プレビュー配信サーバーのリスト.
     */
    private final List<PreviewServer> mPreviewServers = new ArrayList<>();

    /**
     * {@link SurfaceRecorder} のインスタンス.
     */
    private SurfaceRecorder mSurfaceRecorder;

    private final CameraWrapper mCameraWrapper;

    /**
     * リクエストの処理を実行するハンドラ.
     */
    private final Handler mRequestHandler;

    /**
     * 写真撮影の処理を実行するハンドラ.
     */
    private final Handler mPhotoHandler;

    /**
     * 現在の端末の回転方向.
     *
     * @see Surface#ROTATION_0
     * @see Surface#ROTATION_90
     * @see Surface#ROTATION_180
     * @see Surface#ROTATION_270
     */
    private int mCurrentRotation;

    private final MediaSharing mMediaSharing = MediaSharing.getInstance();

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param camera カメラ
     * @param fileManager ファイルマネージャ
     */
    public Camera2Recorder(final @NonNull Context context,
                           final @NonNull CameraWrapper camera,
                           final @NonNull FileManager fileManager) {
        super(context, camera.getId());
        mCameraWrapper = camera;
        mCameraWrapper.setCameraEventListener(this::notifyEventToUser, new Handler(Looper.getMainLooper()));

        HandlerThread photoThread = new HandlerThread("host-camera-photo");
        photoThread.start();
        mPhotoHandler = new Handler(photoThread.getLooper());

        HandlerThread requestThread = new HandlerThread("host-camera-request");
        requestThread.start();
        mRequestHandler = new Handler(requestThread.getLooper());

        mFileManager = fileManager;

        Camera2MJPEGPreviewServer mjpegServer = new Camera2MJPEGPreviewServer(getContext(), this, this);
        mjpegServer.setQuality(RecorderSettingData.getInstance(getContext()).readPreviewQuality(camera.getId()));
        Camera2RTSPPreviewServer rtspServer = new Camera2RTSPPreviewServer(getContext(), this, this);
        mPreviewServers.add(mjpegServer);
        mPreviewServers.add(rtspServer);
    }

    private CameraWrapper getCameraWrapper() {
        return mCameraWrapper;
    }

    private void notifyEventToUser(final CameraWrapper.CameraEvent event) {
        switch (event) {
            case SHUTTERED:
                showToast(getString(R.string.shuttered));
                break;
            case STARTED_VIDEO_RECORDING:
                showToast(getString(R.string.started_video_recording));
                break;
            case STOPPED_VIDEO_RECORDING:
                showToast(getString(R.string.stopped_video_recording));
                break;
            default:
                break;
        }
    }

    private String getString(final int stringId) {
        return getContext().getString(stringId);
    }

    private void showToast(final String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void takePhoto(final @NonNull OnPhotoEventListener listener) {
        mRequestHandler.post(() -> takePhotoInternal(listener));
    }

    @Override
    public synchronized void startRecording(final String serviceId, final RecordingListener listener) {
        mRequestHandler.post(() -> startRecordingInternal(serviceId, listener));
    }

    @Override
    public synchronized void stopRecording(final StoppingListener listener) {
        mRequestHandler.post(() -> stopRecordingInternal(listener));
    }

    @Override
    public boolean canPauseRecording() {
        // 録画の一時停止はサポートしない
        return false;
    }

    @Override
    public void pauseRecording() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resumeRecording() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBack() {
        return mFacing == CameraFacing.BACK;
    }

    @Override
    public void turnOnFlashLight(final @NonNull TurnOnFlashLightListener listener,
                                 final @NonNull Handler handler) {
        mRequestHandler.post(() -> {
            try {
                CameraWrapper camera = getCameraWrapper();
                camera.turnOnTorch(listener::onTurnOn, handler);
                handler.post(listener::onRequested);
            } catch (CameraWrapperException e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to turn on flash light.", e);
                }
                handler.post(() -> listener.onError(Error.FATAL_ERROR));
            }
        });
    }

    @Override
    public void turnOffFlashLight(final @NonNull TurnOffFlashLightListener listener,
                                  final @NonNull Handler handler) {
        mRequestHandler.post(() -> {
            CameraWrapper camera = getCameraWrapper();
            camera.turnOffTorch(listener::onTurnOff, handler);
            handler.post(listener::onRequested);
        });
    }

    @Override
    public boolean isFlashLightState() {
        return mCameraWrapper.isTorchOn();
    }

    @Override
    public boolean isUseFlashLight() {
        return mCameraWrapper.isUseTorch();
    }

    @Override
    public synchronized void initialize() {
    }

    @Override
    public synchronized void clean() {
        for (PreviewServer server : getServers()) {
            server.stopWebServer();
        }
    }

    @Override
    public void destroy() {
        mPhotoHandler.getLooper().quit();
        mRequestHandler.getLooper().quit();
    }

    @Override
    protected int getDefaultPreviewQuality(String mimeType) {
        return PHOTO_JPEG_QUALITY;
    }

    @Override
    public String getId() {
        return ID_BASE + "_" + mCameraId;
    }

    @Override
    public String getName() {
        return NAME_BASE + " " + mCameraId + " (" + mFacing.getName() + ")";
    }

    @Override
    public String getMimeType() {
        return mMimeTypes.get(0);
    }

    @Override
    public String getStreamMimeType() {
        return "video/mp4";
    }

    @Override
    public RecorderState getState() {
        if (mCameraWrapper.isRecording() || mCameraWrapper.isTakingStillImage()) {
            return RecorderState.RECORDING;
        }
        return RecorderState.INACTTIVE;
    }

    @Override
    public PictureSize getPictureSize() {
        return new PictureSize(getCameraWrapper().getOptions().getPictureSize());
    }

    @Override
    public void setPictureSize(final PictureSize size) {
        Size newSize = new Size(size.getWidth(), size.getHeight());
        mCameraWrapper.getOptions().setPictureSize(newSize);
    }

    @Override
    public PictureSize getPreviewSize() {
        return new PictureSize(mCameraWrapper.getOptions().getPreviewSize());
    }

    @Override
    public void setPreviewSize(final PictureSize size) {
        Size newSize = new Size(size.getWidth(), size.getHeight());
        mCameraWrapper.getOptions().setPreviewSize(newSize);
    }

    @Override
    public double getMaxFrameRate() {
        return mCameraWrapper.getOptions().getPreviewMaxFrameRate();
    }

    @Override
    public void setMaxFrameRate(final double frameRate) {
        mCameraWrapper.getOptions().setPreviewMaxFrameRate(frameRate);
    }

    @Override
    public int getPreviewBitRate() {
        return mCameraWrapper.getOptions().getPreviewBitRate();
    }

    @Override
    public void setPreviewBitRate(final int bitRate) {
        mCameraWrapper.getOptions().setPreviewBitRate(bitRate);
    }

    @Override
    public List<PictureSize> getSupportedPictureSizes() {
        List<PictureSize> result = new ArrayList<>();
        for (Size size : mCameraWrapper.getOptions().getSupportedPictureSizeList()) {
            result.add(new PictureSize(size));
        }
        return result;
    }

    @Override
    public List<PictureSize> getSupportedPreviewSizes() {
        List<PictureSize> result = new ArrayList<>();
        for (Size size : mCameraWrapper.getOptions().getSupportedPreviewSizeList()) {
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

    public void setWhiteBalance(final String whiteBalance) {
        getCameraWrapper().getOptions().setWhiteBalance(whiteBalance);
    }

    public String getWhiteBalance() {
        return getCameraWrapper().getOptions().getWhiteBalance();
    }

    @Override
    public void requestPermission(final PermissionCallback callback) {
        CapabilityUtil.requestPermissions(getContext(), new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                callback.onAllowed();
            }

            @Override
            public void onFail(final @NonNull String deniedPermission) {
                callback.onDisallowed();
            }
        });
    }

    @Override
    public List<PreviewServer> getServers() {
        return mPreviewServers;
    }

    @Override
    public PreviewServer getServerForMimeType(final String mimeType) {
        for (PreviewServer server : getServers()) {
            if (server.getMimeType().equals(mimeType)) {
                return server;
            }
        }
        return null;
    }

    @Override
    public void onDisplayRotation(final int degree) {
        mCurrentRotation = degree;
//        for (PreviewServer server : getServers()) {
//            server.onDisplayRotation(degree);
//        }
    }

    /**
     * 新規のファイル名を作成する.
     *
     * @return ファイル名
     */
    private String createNewFileName() {
        return FILENAME_PREFIX + DATE_FORMAT.format(new Date()) + FILE_EXTENSION;
    }

    int getDisplayRotation() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            throw new RuntimeException("WindowManager is not supported.");
        }
        return wm.getDefaultDisplay().getRotation();
    }

    PictureSize getRotatedPreviewSize() {
        Size original = getCameraWrapper().getOptions().getPreviewSize();
        Size rotated;
        int rotation = getDisplayRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                rotated = new Size(original.getHeight(), original.getWidth());
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
            default:
                rotated = original;
                break;
        }
        return new PictureSize(rotated.getWidth(), rotated.getHeight());
    }

    boolean isStartedPreview() {
        CameraWrapper camera = getCameraWrapper();
        return camera.isPreview();
    }

    void startPreview(final Surface previewSurface) throws CameraWrapperException {
        CameraWrapper camera = getCameraWrapper();
        camera.startPreview(previewSurface, false);
    }

    void stopPreview() throws CameraWrapperException {
        CameraWrapper camera = getCameraWrapper();
        camera.stopPreview();
    }

    private void takePhotoInternal(final @NonNull OnPhotoEventListener listener) {
        try {
            final CameraWrapper camera = getCameraWrapper();
            final ImageReader stillImageReader = camera.createStillImageReader(ImageFormat.JPEG);
            if (DEBUG) {
                int w = stillImageReader.getWidth();
                int h = stillImageReader.getHeight();
                Log.d(TAG, "takePhoto: surface: " + w + "x" + h);
            }
            stillImageReader.setOnImageAvailableListener((reader) -> {
                Image photo = reader.acquireNextImage();
                if (photo == null) {
                    listener.onFailedTakePhoto("Failed to acquire image.");
                    return;
                }
                storePhoto(photo, listener);
                photo.close();
            }, mPhotoHandler);

            camera.takeStillImage(stillImageReader.getSurface());
        } catch (CameraWrapperException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to take photo.", e);
            }
            listener.onFailedTakePhoto("Failed to take photo.");
        }
    }

    private void storePhoto(final Image image, final OnPhotoEventListener listener) {
        if (DEBUG) {
            Log.d(TAG, "storePhoto: screen orientation: " + Camera2Helper.getScreenOrientation(getContext()));
        }

        byte[] jpeg = ImageUtil.convertToJPEG(image);
        int deviceRotation = ROTATIONS.get(mCurrentRotation);
        int cameraRotation = Camera2Helper.getSensorOrientation(mCameraManager, mCameraId);
        int degrees = (360 - deviceRotation + cameraRotation) % 360;
        if (mFacing == CameraFacing.FRONT) {
            degrees = (180 - degrees) % 360;
        }

        jpeg = ImageUtil.rotateJPEG(jpeg, PHOTO_JPEG_QUALITY, degrees);

        final String filename = createNewFileName();
        mFileManager.saveFile(filename, jpeg, true, new FileManager.SaveFileCallback() {
            @Override
            public void onSuccess(@NonNull final String uri) {
                if (DEBUG) {
                    Log.d(TAG, "Saved photo: uri=" + uri);
                }

                String photoFilePath = mFileManager.getBasePath().getAbsolutePath() + "/" + uri;
                registerPhoto(new File(mFileManager.getBasePath(), filename));
                listener.onTakePhoto(uri, photoFilePath, MIME_TYPE_JPEG);
            }

            @Override
            public void onFail(@NonNull final Throwable e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to save photo", e);
                }

                listener.onFailedTakePhoto(e.getMessage());
            }
        });
    }

    private synchronized void startRecordingInternal(final String serviceId, final RecordingListener listener) {
        if (mSurfaceRecorder != null) {
            listener.onFailed(this, "Recording has started already.");
            return;
        }

        try {
            final CameraWrapper camera = getCameraWrapper();
            mSurfaceRecorder = new DefaultSurfaceRecorder(
                    getContext(),
                    mFacing,
                    camera.getSensorOrientation(),
                    camera.getOptions().getPictureSize(),
                    mFileManager.getBasePath());
            mSurfaceRecorder.start(new SurfaceRecorder.OnRecordingStartListener() {
                @Override
                public void onRecordingStart() {
                    try {
                        camera.startRecording(mSurfaceRecorder.getInputSurface(), false);
                        listener.onRecorded(Camera2Recorder.this, mSurfaceRecorder.getOutputFile().getAbsolutePath());
                    } catch (CameraWrapperException e) {
                        listener.onFailed(Camera2Recorder.this,
                                "Failed to start recording because of camera problem: " + e.getMessage());
                    }
                }

                @Override
                public void onRecordingStartError(final Throwable e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to start recording for unexpected problem: ", e);
                    }
                    listener.onFailed(Camera2Recorder.this,
                            "Failed to start recording for unexpected problem: " + e.getMessage());
                }
            });
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to start recording for unexpected problem: ", e);
            }
            listener.onFailed(this, "Failed to start recording for unexpected problem: " + e.getMessage());
        }
    }

    private void stopRecordingInternal(final StoppingListener listener) {
        if (mSurfaceRecorder == null) {
            listener.onFailed(this, "Recording has stopped already.");
            return;
        }

        try {
            CameraWrapper camera = getCameraWrapper();
            camera.stopRecording();
            mSurfaceRecorder.stop(new SurfaceRecorder.OnRecordingStopListener() {
                @Override
                public void onRecordingStop() {
                    File videoFile = mSurfaceRecorder.getOutputFile();
                    mSurfaceRecorder = null;

                    registerVideo(videoFile);
                    listener.onStopped(Camera2Recorder.this, videoFile.getName());
                }

                @Override
                public void onRecordingStopError(Throwable e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to stop recording for unexpected error.", e);
                    }
                    listener.onFailed(Camera2Recorder.this,
                            "Failed to stop recording for unexpected error: " + e.getMessage());
                }
            });
        } catch (CameraWrapperException e) {
            if (DEBUG) {
                Log.w(TAG, "Failed to stop recording.", e);
            }
            listener.onFailed(this, "Failed to stop recording: " + e.getMessage());
        }
    }

    private void registerVideo(final File videoFile) {
        Uri uri = mMediaSharing.shareVideo(getContext(), videoFile, mFileManager);
        if (DEBUG) {
            String filePath = videoFile.getAbsolutePath();
            if (uri != null) {
                Log.d(TAG, "Registered video: filePath=" + filePath + ", uri=" + uri.getPath());
            } else {
                Log.e(TAG, "Failed to register video: file=" + filePath);
            }
        }
    }

    private void registerPhoto(final File photoFile) {
        Uri uri = mMediaSharing.sharePhoto(getContext(), photoFile);
        if (DEBUG) {
            if (uri != null) {
                Log.d(TAG, "Registered photo: uri=" + uri.getPath());
            } else {
                Log.e(TAG, "Failed to register photo: file=" + photoFile.getAbsolutePath());
            }
        }
    }
}
