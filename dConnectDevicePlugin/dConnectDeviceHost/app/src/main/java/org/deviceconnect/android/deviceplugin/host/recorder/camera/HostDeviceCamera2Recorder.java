package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.mediaplayer.VideoConst;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.video.HostDeviceVideoRecorder;
import org.deviceconnect.android.provider.FileManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HostDeviceCamera2Recorder extends AbstractPreviewServerProvider implements HostDevicePhotoRecorder, HostDeviceStreamRecorder {

    interface OpenCallback {

        void onOpen(final @NonNull CameraDevice camera);

        void onError(final @NonNull Exception ex);
    }

    /**
     * カメラターゲットIDの定義.
     */
    private static final String ID_BASE = "photo";

    /**
     * カメラ名の定義.
     */
    private static final String NAME_BASE = "Camera";

    /**
     * NotificationIDを定義.
     */
    private static final int NOTIFICATION_ID = 1010;

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
     * {@link CameraManager} のインスタンス.
     */
    private final CameraManager mCameraManager;

    /**
     * カメラID.
     *
     * @see CameraManager#getCameraIdList()
     */
    private final String mCameraId;

    /**
     * カメラのインスタンス.
     */
    private CameraDevice mCamera;

    /**
     * カメラオプション.
     */
    private final Options mOptions = new Options();

    /**
     * Android フレームワークから通知を受け取るスレッド.
     */
    private final Handler mHandler = new Handler();

    /**
     * カメラの位置.
     */
    private final CameraFacing mFacing;

    /**
     * マイムタイプ一覧.
     */
    private final List<String> mMimeTypes = new ArrayList<String>() {
        {
            add("image/jpg");
            add("video/x-mjpeg");
            add("video/x-rtp");
        }
    };

    private final FileManager mFileManager;

    /**
     * 初期化状態.
     */
    private boolean mIsInitialized;

    /**
     * レコーディング状態.
     */
    private RecorderState mRecorderState = RecorderState.INACTTIVE;

    /**
     * プレビュー配信サーバーのリスト.
     */
    private final List<PreviewServer> mPreviewServers = new ArrayList<>();

    private final Camera2MJPEGPreviewServer mMjpegServer;

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
     * ログ出力用タグ.
     */
    private static final String TAG = "host.dplugin";

    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param cameraId カメラID
     */
    public HostDeviceCamera2Recorder(final @NonNull Context context,
                                     final @NonNull String cameraId,
                                     final @NonNull FileManager fileManager) {
        super(context, NOTIFICATION_ID + cameraId.hashCode());

        mCameraId = cameraId;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mFileManager = fileManager;
        mFacing = detectFacing();

        mMjpegServer = new Camera2MJPEGPreviewServer(context, this);
        mMjpegServer.setPreviewServerListener(new Camera2MJPEGPreviewServer.PreviewServerListener() {
            @Override
            public boolean onAccept() {
                return onAcceptPreviewRequest();
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onStop() {
            }
        });
        mPreviewServers.add(mMjpegServer);
        // TODO RTSPサーバーの追加
    }

    private boolean checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public void openCamera(final @NonNull OpenCallback callback) {
        if (DEBUG) {
            Log.d(TAG, "openCamera: id" + mCameraId);
        }
        CameraDevice camera = mCamera;
        if (camera != null) {
            callback.onOpen(camera);
            return;
        }

        if (checkCameraPermission()) {
            openCameraInternal(callback);
        } else {
            String[] permissions = {
                    Manifest.permission.CAMERA
            };
            PermissionUtility.requestPermissions(getContext(), mHandler,
                    permissions,
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {
                            openCameraInternal(callback);
                        }

                        @Override
                        public void onFail(final @NonNull String permission) {
                            callback.onError(new RecorderException(RecorderException.REASON_NOT_ALLOWED));
                        }
                    });
        }
    }

    private void openCameraInternal(final @NonNull OpenCallback callback) {
        try {
            mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(final @NonNull CameraDevice camera) {
                    mCamera = camera;

                    callback.onOpen(camera);
                }

                @Override
                public void onDisconnected(final @NonNull CameraDevice camera) {
                    callback.onError(new RecorderException(RecorderException.REASON_DISABLED));
                }

                @Override
                public void onError(final @NonNull CameraDevice camera, final int error) {
                    int reason;
                    switch (error) {
                        case CameraDevice.StateCallback.ERROR_CAMERA_DISABLED:
                            reason = RecorderException.REASON_DISABLED;
                            break;
                        case CameraDevice.StateCallback.ERROR_CAMERA_IN_USE:
                            reason = RecorderException.REASON_IN_USE;
                            break;
                        case CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE:
                            reason = RecorderException.REASON_TOO_MANY;
                            break;
                        case CameraDevice.StateCallback.ERROR_CAMERA_DEVICE:
                        case CameraDevice.StateCallback.ERROR_CAMERA_SERVICE:
                        default:
                            reason = RecorderException.REASON_FATAL;
                            break;
                    }
                    callback.onError(new RecorderException(reason));
                }
            }, mHandler);
        } catch (SecurityException e) {
            callback.onError(new RecorderException(RecorderException.REASON_NOT_ALLOWED, e));
        } catch (CameraAccessException e) {
            int error = e.getReason();
            int reason;
            switch (error) {
                case CameraAccessException.CAMERA_DISABLED:
                    reason = RecorderException.REASON_DISABLED;
                    break;
                case CameraAccessException.CAMERA_DISCONNECTED:
                    reason = RecorderException.REASON_DISCONNECTED;
                    break;
                case CameraAccessException.CAMERA_IN_USE:
                    reason = RecorderException.REASON_IN_USE;
                    break;
                case CameraAccessException.MAX_CAMERAS_IN_USE:
                    reason = RecorderException.REASON_TOO_MANY;
                    break;
                case CameraAccessException.CAMERA_ERROR:
                default:
                    reason = RecorderException.REASON_FATAL;
                    break;
            }
            callback.onError(new RecorderException(reason, e));
        }
    }

    @Override
    public void takePhoto(final @NonNull OnPhotoEventListener listener) {
        openCamera(new OpenCallback() {
            @Override
            public void onOpen(final @NonNull CameraDevice camera) {
                if (DEBUG) {
                    Log.d(TAG, "onOpen: id=" + camera.getId());
                }

                try {
                    takePhotoInternal(camera, listener);
                } catch (CameraAccessException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to take photo", e);
                    }
                    listener.onFailedTakePhoto(e.getMessage());
                }
            }

            @Override
            public void onError(final @NonNull Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to open camera", e);
                }
                listener.onFailedTakePhoto(e.getMessage());
            }
        });
    }

    private void takePhotoInternal(final @NonNull CameraDevice camera,
                                   final @NonNull OnPhotoEventListener listener) throws CameraAccessException {
        final ImageReader imageReader = createImageReader(mOptions.getPictureSize());

        List<Surface> targets = new ArrayList<>();
        targets.add(imageReader.getSurface());
        camera.createCaptureSession(targets, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(final @NonNull CameraCaptureSession session) {
                if (DEBUG) {
                    Log.d(TAG, "onConfigured: " + session.getDevice().getId());
                }

                try {
                    imageReader.setOnImageAvailableListener(reader -> {
                        if (DEBUG) {
                            Log.d(TAG, "OnImageAvailable: reader=" + reader);
                        }

                        // ビットマップ取得
                        final Image image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        image.close();

                        // ビットマップ -> JPEG に変換
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] jpeg = baos.toByteArray();
                        bitmap.recycle();

                        // ファイル保存
                        mFileManager.saveFile(createNewFileName(), jpeg, true, new FileManager.SaveFileCallback() {
                            @Override
                            public void onSuccess(@NonNull final String uri) {
                                if (DEBUG) {
                                    Log.d(TAG, "Saved photo: uri=" + uri);
                                }

                                String filePath = mFileManager.getBasePath().getAbsolutePath() + "/" + uri;
                                listener.onTakePhoto(uri, filePath);
                            }

                            @Override
                            public void onFail(@NonNull final Throwable e) {
                                if (DEBUG) {
                                    Log.e(TAG, "Failed to save photo", e);
                                }
                                listener.onFailedTakePhoto(e.getMessage());
                            }
                        });
                    }, mHandler);

                    CaptureRequest.Builder captureBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    captureBuilder.addTarget(imageReader.getSurface());
                    captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(Camera2Helper.getDisplayRotation(getContext())));

                    session.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            if (DEBUG) {
                                Log.d(TAG, "onCaptureCompleted");
                            }
                        }

                        @Override
                        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                            if (DEBUG) {
                                Log.d(TAG, "onCaptureFailed: reason=" + failure.getReason());
                            }
                            listener.onFailedTakePhoto("Failed to capture photo");
                        }
                    }, mHandler);
                } catch (CameraAccessException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to request capture", e);
                    }
                    listener.onFailedTakePhoto(e.getMessage());
                }
            }

            @Override
            public void onConfigureFailed(final @NonNull CameraCaptureSession session) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to configure capture session");
                }
                listener.onFailedTakePhoto("Failed to configure capture session");
            }
        }, mHandler);
    }

    /**
     * 新規のファイル名を作成する.
     *
     * @return ファイル名
     */
    private String createNewFileName() {
        return FILENAME_PREFIX + DATE_FORMAT.format(new Date()) + FILE_EXTENSION;
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

    private ImageReader createImageReader(final Size pictureSize) {
        return Camera2Helper.createImageReader(pictureSize.getWidth(), pictureSize.getHeight(), ImageFormat.JPEG);
    }

    private boolean onAcceptPreviewRequest() {
        final CountDownLatch lock = new CountDownLatch(1);
        final CameraDevice[] camera = new CameraDevice[1];
        openCamera(new HostDeviceCamera2Recorder.OpenCallback() {
            @Override
            public void onOpen(final @NonNull CameraDevice c) {
                camera[0] = c;
                lock.countDown();
            }

            @Override
            public void onError(final @NonNull Exception ex) {
                lock.countDown();
            }
        });

        try {
            lock.await(10 * 1000, TimeUnit.MILLISECONDS);
            if (camera[0] == null) {
                return false;
            }
            startPreview(camera[0]);
            return true;
        } catch (InterruptedException e) {
            return false;
        } catch (CameraAccessException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to start preview", e);
            }
            return false;
        }
    }

    private void startPreview(final @NonNull CameraDevice camera) throws CameraAccessException {
        final ImageReader imageReader = createImageReader(mOptions.getPreviewSize());

        List<Surface> targets = new ArrayList<>();
        targets.add(imageReader.getSurface());
        camera.createCaptureSession(targets, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(final @NonNull CameraCaptureSession session) {
                if (DEBUG) {
                    Log.d(TAG, "onConfigured: " + session.getDevice().getId());
                }

                try {
                    imageReader.setOnImageAvailableListener(reader -> {
                        if (DEBUG) {
                            Log.d(TAG, "OnImageAvailable: reader=" + reader);
                        }

                        // ビットマップ取得
                        final Image image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        image.close();

                        // ビットマップ -> JPEG に変換
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] jpeg = baos.toByteArray();
                        bitmap.recycle();

                        mMjpegServer.offerMedia(jpeg);

                        if (DEBUG) {
                            Log.d(TAG, "OnImageAvailable: bitmap=" + bitmap.getByteCount());
                        }
                    }, mHandler);

                    CaptureRequest.Builder captureBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    captureBuilder.addTarget(imageReader.getSurface());
                    captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(Camera2Helper.getDisplayRotation(getContext())));

                    session.setRepeatingRequest(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                            if (DEBUG) {
                                Log.d(TAG, "onCaptureStarted: No." + frameNumber);
                            }
                        }

                        @Override
                        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                            if (DEBUG) {
                                Log.d(TAG, "onCaptureFailed: reason=" + failure.getReason());
                            }
                        }
                    }, mHandler);
                } catch (CameraAccessException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to start preview", e);
                    }
                }
            }

            @Override
            public void onConfigureFailed(final @NonNull CameraCaptureSession session) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to configure capture");
                }
            }
        }, mHandler);
    }

    @Override
    public boolean isBack() {
        return mFacing == CameraFacing.BACK;
    }

    @Override
    public void turnOnFlashLight() {
        // TODO
    }

    @Override
    public void turnOffFlashLight() {
        // TODO
    }

    @Override
    public boolean isFlashLightState() {
        return false;  // TODO
    }

    @Override
    public boolean isUseFlashLight() {
        return false;  // TODO
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
        return mRecorderState;
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
        return mPreviewServers;
    }

    @Override
    public boolean canPauseRecording() {
        return false;
    }

    @Override
    public void startRecording(final @NonNull String serviceId,
                               final @NonNull RecordingListener listener) {

        openCamera(new OpenCallback() {
            @Override
            public void onOpen(final @NonNull CameraDevice camera) {
                try {
                    synchronized (HostDeviceCamera2Recorder.this) {
                        if (mMediaRecorder != null) {
                            listener.onFailed(HostDeviceCamera2Recorder.this, "Video recording has started already.");
                            return;
                        }
                        mSurface = MediaCodec.createPersistentInputSurface();
                        mMediaRecorder = createMediaRecorder(mOptions);
                        mMediaRecorder.prepare();
                        mMediaRecorder.start();
                        createVideoCaptureSession(camera, mMediaRecorder, listener);
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to prepare video recording.", e);
                    }
                    listener.onFailed(HostDeviceCamera2Recorder.this, "Failed to prepare video recording.");
                } catch (CameraAccessException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to create capture request for video.", e);
                    }
                    listener.onFailed(HostDeviceCamera2Recorder.this, "Failed to prepare video recording.");
                }
            }

            @Override
            public void onError(final @NonNull Exception ex) {
                listener.onFailed(HostDeviceCamera2Recorder.this, ex.getMessage());
            }
        });
    }

    private CameraCaptureSession mVideoCaptureSession;

    private void createVideoCaptureSession(final @NonNull CameraDevice camera,
                                           final @NonNull MediaRecorder mediaRecorder,
                                           final @NonNull RecordingListener listener) throws CameraAccessException {
        final HostDeviceStreamRecorder recorder = this;
        List<Surface> outputs = new ArrayList<>();
        outputs.add(mSurface);
        camera.createCaptureSession(outputs, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(final @NonNull CameraCaptureSession session) {
                if (DEBUG) {
                    Log.d(TAG, "onConfigured: " + session.getDevice().getId());
                }
                mVideoCaptureSession = session;

                try {
                    CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                    builder.addTarget(mSurface);
                    builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    session.setRepeatingRequest(builder.build(), new CameraCaptureSession.CaptureCallback() {
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

                            // TODO 後始末
                        }
                    }, mHandler);
                    mRecorderState = RecorderState.RECORDING;
                    listener.onRecorded(HostDeviceCamera2Recorder.this, mVideoFile.getAbsolutePath());
                } catch (CameraAccessException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to create capture request for video.", e);
                    }
                    listener.onFailed(recorder, "Failed to create capture request for video.");
                }
            }

            @Override
            public void onConfigureFailed(final @NonNull CameraCaptureSession session) {
                listener.onFailed(recorder, "Failed to configure capture session for video.");
            }
        }, mHandler);
    }

    private MediaRecorder mMediaRecorder;
    private Surface mSurface;
    private File mVideoFile;

    private MediaRecorder createMediaRecorder(final Options options) {
        MediaRecorder mediaRecorder = new MediaRecorder();

        // Inputs
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setInputSurface(mSurface);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        // Output
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mVideoFile = new File(mFileManager.getBasePath(), generateVideoFileName());
        mediaRecorder.setOutputFile(mVideoFile.toString());

        // Video Options
        mediaRecorder.setVideoEncodingBitRate(10000000); // TODO 変更可能にする
        mediaRecorder.setVideoFrameRate(30); // TODO 変更可能にする
        Size videoSize = options.getPictureSize();
        mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        return mediaRecorder;
    }

    private String generateVideoFileName() {
        return "video-" + DATE_FORMAT.format(new Date()) + ".mp4";
    }

    @Override
    public void stopRecording(final @NonNull StoppingListener listener) {
        File videoFile;
        synchronized (this) {
            if (mMediaRecorder != null) {
                mRecorderState = RecorderState.INACTTIVE;

                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            try {
                if (mVideoCaptureSession != null) {
                    mVideoCaptureSession.stopRepeating();
                }
            } catch (CameraAccessException e) {
                if (DEBUG) {
                    Log.w(TAG, "Failed to stop video capture.", e);
                }
            }
            videoFile = mVideoFile;

            if (checkVideoFile(videoFile)) {
                // Content Providerに登録する.
                MediaMetadataRetriever mediaMeta = new MediaMetadataRetriever();
                mediaMeta.setDataSource(videoFile.toString());
                ContentResolver resolver = getContext().getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.TITLE, videoFile.getName());
                values.put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.getName());
                values.put(MediaStore.Video.Media.ARTIST, "DeviceConnect");
                values.put(MediaStore.Video.Media.MIME_TYPE, VideoConst.FORMAT_TYPE);
                values.put(MediaStore.Video.Media.DATA, videoFile.toString());
                resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                listener.onFailed(this, "Failed to store video file: " + videoFile.getName());
                return;
            }
        }
        listener.onStopped(this, videoFile.getAbsolutePath());
    }

    private boolean checkVideoFile(final @NonNull File file) {
        return file.exists() && file.length() > 0;
    }

    @Override
    public void pauseRecording() {

    }

    @Override
    public void resumeRecording() {

    }

    /**
     * カメラの位置を判定する.
     * @return カメラの位置
     */
    private CameraFacing detectFacing() {
        try {
            int facing = Camera2Helper.getFacing(mCameraManager, mCameraId);
            switch (facing) {
                case CameraCharacteristics.LENS_FACING_BACK:
                    return CameraFacing.BACK;
                case CameraCharacteristics.LENS_FACING_FRONT:
                    return CameraFacing.FRONT;
                case CameraCharacteristics.LENS_FACING_EXTERNAL:
                    return CameraFacing.EXTERNAL;
                default:
                    return CameraFacing.UNKNOWN;
            }
        } catch (CameraAccessException e) {
            return CameraFacing.UNKNOWN;
        }
    }

    /**
     * カメラの位置.
     */
    public enum CameraFacing {
        /** スマートフォンの裏側. */
        BACK("back"),

        /** スマートフォンの正面. */
        FRONT("front"),

        /** 外部接続. (e.g. OTG 接続されている USB カメラ) */
        EXTERNAL("external"),

        /** 不明. */
        UNKNOWN("unknown");

        /** カメラの位置を表現する名前. */
        private final String mName;

        /**
         * コンストラクタ.
         * @param name カメラの位置を表現する名前
         */
        CameraFacing(final String name) {
            mName = name;
        }

        /**
         * カメラの位置を表現する名前を取得する.
         * @return 名前
         */
        public String getName() {
            return mName;
        }
    }

    /**
     * カメラオプションを保持するクラス.
     */
    static class Options {

        private Size mPictureSize;
        private Size mPreviewSize;
        private List<Size> mSupportedPictureSizeList = new ArrayList<>();
        private List<Size> mSupportedPreviewSizeList = new ArrayList<>();

        Size getPictureSize() {
            return mPictureSize;
        }

        void setPictureSize(final Size pictureSize) {
            mPictureSize = pictureSize;
        }

        Size getPreviewSize() {
            return mPreviewSize;
        }

        void setPreviewSize(final Size previewSize) {
            mPreviewSize = previewSize;
        }

        List<Size> getSupportedPictureSizeList() {
            return mSupportedPictureSizeList;
        }

        void setSupportedPictureSizeList(final List<Size> supportedPictureSizeList) {
            mSupportedPictureSizeList = new ArrayList<>(supportedPictureSizeList);
        }

        List<Size> getSupportedPreviewSizeList() {
            return mSupportedPreviewSizeList;
        }

        void setSupportedPreviewSizeList(final List<Size> supportedPreviewSizeList) {
            mSupportedPreviewSizeList = new ArrayList<>(supportedPreviewSizeList);
        }
    }
}
