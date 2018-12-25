/*
 Camera2PhotoRecorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.provider.FileManager;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Camera2PhotoRecorder extends AbstractCamera2Recorder implements HostDevicePhotoRecorder {

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
    private static final String ID_BASE = "photo";

    /**
     * カメラ名の定義.
     */
    private static final String NAME_BASE = "Photo Camera";

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
            add("image/jpg");
            add("video/x-mjpeg");
            add("video/x-rtp");
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
     * MotionJPEG サーバー.
     */
    private final Camera2MJPEGPreviewServer mMjpegServer;

    private ImageReader mPhotoImageReader;

    private ImageReader mPreviewImageReader;

    /**
     * 初期化状態.
     */
    private boolean mIsInitialized;

    private boolean mIsPreviewStarted;

    private CameraCaptureSession mCaptureSession;

    private CaptureRequest mPreviewRequest;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param cameraId カメラID
     */
    public Camera2PhotoRecorder(final @NonNull Context context,
                                final @NonNull String cameraId,
                                final @NonNull FileManager fileManager) {
        super(context, cameraId);
        mFileManager = fileManager;

        mMjpegServer = new Camera2MJPEGPreviewServer(this);
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
                synchronized (Camera2PhotoRecorder.this) {
                    mIsPreviewStarted = false;
                    mPreviewRequest = null;

                    releaseCamera();
                }
            }
        });
        mPreviewServers.add(mMjpegServer);

        // TODO RTSPサーバーの追加
    }

    private interface CaptureSessionCallback {
        void onSessionCreated(@NonNull CameraCaptureSession session);
        void onSessionFailed(@NonNull Exception e);
    }

    private void prepareSession(final @NonNull CaptureSessionCallback callback) {
        if (DEBUG) {
            Log.d(TAG, "prepareSession: id=" + mCameraId);
        }
        openCamera(new CameraOpenCallback() {
            @Override
            public void onOpen(final @NonNull CameraDevice camera, final boolean isNew) {
                synchronized (Camera2PhotoRecorder.this) {
                    CameraCaptureSession session = mCaptureSession;
                    if (session != null) {
                        callback.onSessionCreated(session);
                        return;
                    }
                }

                try {
                    Options options = mOptions;
                    if (DEBUG) {
                        Size pictureSize = options.getPictureSize();
                        Log.d(TAG, "Picture size: " + pictureSize.getWidth() + "x" + pictureSize.getHeight());
                    }
                    mPreviewImageReader = createImageReader(options.getPreviewSize(), ImageFormat.YUV_420_888);
                    mPhotoImageReader = createImageReader(options.getPictureSize(), ImageFormat.JPEG);
                    if (DEBUG) {
                        Log.d(TAG, "Created resources.");
                    }

                    List<Surface> outputs = new ArrayList<>();
                    outputs.add(mPreviewImageReader.getSurface());
                    outputs.add(mPhotoImageReader.getSurface());

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
        if (mPhotoImageReader != null) {
            mPhotoImageReader.close();
            mPhotoImageReader = null;
        }
        if (mPreviewImageReader != null) {
            mPreviewImageReader.close();
            mPreviewImageReader = null;
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

    @Override
    public void takePhoto(final @NonNull OnPhotoEventListener listener) {
        prepareSession(new CaptureSessionCallback() {
            @Override
            public void onSessionCreated(final @NonNull CameraCaptureSession session) {
                try {
                    ImageReader imageReader = mPhotoImageReader;
                    imageReader.setOnImageAvailableListener(reader -> {
                        if (DEBUG) {
                            Log.d(TAG, "OnImageAvailable: reader=" + reader);
                        }

                        final Image image = reader.acquireLatestImage();
                        if (image == null) {
                            listener.onFailedTakePhoto("Failed to access image.");
                            return;
                        }
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

                        onTakePhotoFinish(session);

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

                    CaptureRequest.Builder requestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    requestBuilder.addTarget(imageReader.getSurface());
                    requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(Camera2Helper.getDisplayRotation(getContext())));

                    CaptureRequest request = requestBuilder.build();
                    session.capture(request, new CameraCaptureSession.CaptureCallback() {
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
            public void onSessionFailed(final @NonNull Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to configure capture session", e);
                }
                listener.onFailedTakePhoto("Failed to configure capture session");
            }
        });
    }

    private void onTakePhotoFinish(final CameraCaptureSession session) {
        synchronized (this) {
            if (mPreviewRequest != null) {
                try {
                    session.setRepeatingRequest(mPreviewRequest, null, null);
                } catch (CameraAccessException e) {
                    if (DEBUG) {
                        Log.w(TAG, "Failed to resume preview.", e);
                    }
                }
            } else {
                releaseCamera();
            }
        }
    }

    private static byte[] NV21toJPEG(byte[] nv21, int width, int height, int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), quality, out);
        return out.toByteArray();
    }

    private static byte[] YUV420toNV21(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }

            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
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

    private ImageReader createImageReader(final Size pictureSize, final int format) {
        return Camera2Helper.createImageReader(pictureSize.getWidth(), pictureSize.getHeight(), format);
    }

    private boolean onAcceptPreviewRequest() {
        final CountDownLatch lock = new CountDownLatch(1);
        final CameraCaptureSession[] session = new CameraCaptureSession[1];
        prepareSession(new CaptureSessionCallback() {
            @Override
            public void onSessionCreated(final @NonNull CameraCaptureSession s) {
                if (DEBUG) {
                    Log.d(TAG, "onAcceptPreviewRequest: onSessionCreated:");
                }
                session[0] = s;
                lock.countDown();
            }

            @Override
            public void onSessionFailed(final @NonNull Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "onAcceptPreviewRequest: onSessionFailed:", e);
                }
                lock.countDown();
            }
        });

        try {
            lock.await(10 * 1000, TimeUnit.MILLISECONDS);
            if (session[0] == null) {
                return false;
            }
            startPreview(session[0]);
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

    private void startPreview(final @NonNull CameraCaptureSession session) throws CameraAccessException {
        synchronized (this) {
            if (mIsPreviewStarted) {
                return;
            }
            mIsPreviewStarted = true;
        }

        final ImageReader imageReader = mPreviewImageReader;
        imageReader.setOnImageAvailableListener(reader -> {
           // ビットマップ取得
            final Image image = reader.acquireLatestImage();
            if (image != null) {
                Image.Plane[] planes = image.getPlanes();
                if (planes != null) {
                    // ビットマップ取得
                    int width = image.getWidth();
                    int height = image.getHeight();
                    byte[] jpeg = NV21toJPEG(YUV420toNV21(image), width, height, 100);
                    image.close();

                    mMjpegServer.offerMedia(jpeg);
                }
            }
        }, mHandler);

        CameraDevice camera = session.getDevice();
        CaptureRequest.Builder captureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureBuilder.addTarget(imageReader.getSurface());

        mPreviewRequest = captureBuilder.build();
        session.setRepeatingRequest(mPreviewRequest, null, null);
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

        Size defaultSize = mOptions.getDefaultPictureSize();
        if (defaultSize != null) {
            mOptions.setPictureSize(defaultSize);
        }
        defaultSize = mOptions.getDefaultPreviewSize();
        if (defaultSize != null) {
            mOptions.setPreviewSize(defaultSize);
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
        if (mIsPreviewStarted) {
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
        return mPreviewServers;
    }

}
