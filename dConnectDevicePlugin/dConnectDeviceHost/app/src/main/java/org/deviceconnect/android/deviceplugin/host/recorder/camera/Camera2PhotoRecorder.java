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
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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

    private final CameraProxy mCameraProxy;

    private HandlerThread mPreviewThread = new HandlerThread("preview");

    private final HandlerThread mPhotoThread = new HandlerThread("photo");

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
        mCameraProxy = new CameraProxy(context, cameraId);
        mPreviewThread.start();
        mPhotoThread.start();
        mFileManager = fileManager;

        mMjpegServer = new Camera2MJPEGPreviewServer(this);
        mMjpegServer.setPreviewServerListener(new Camera2MJPEGPreviewServer.PreviewServerListener() {
            @Override
            public boolean onAccept() {
                boolean result = onAcceptPreviewRequest();
                if (DEBUG) {
                    Log.d(TAG, "Started preview.");
                }
                return result;
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onStop() {
                stopPreview();
            }
        });
        mPreviewServers.add(mMjpegServer);

        Camera2RTSPPreviewServer rtspServer = new Camera2RTSPPreviewServer(getContext(), this, this);
        mPreviewServers.add(rtspServer);
    }

    private CameraProxy getCameraProxy() {
        return mCameraProxy;
    }

    private void releaseCameraProxy(final CameraProxy camera) {
        camera.release();
    }

    public void setPreviewSurface(final Surface previewSurface) {
        mCameraProxy.setPreviewSurface(previewSurface);
    }

    @Override
    public void takePhoto(final @NonNull OnPhotoEventListener listener) {
        final CameraProxy camera = getCameraProxy();
        final Handler handler = new Handler(mPhotoThread.getLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                try {
                    switch (msg.what) {
                        case CameraProxy.MessageType.HELLO:
                            camera.open();
                            break;
                        case CameraProxy.MessageType.OPEN:
                            if (DEBUG) {
                                Log.d(TAG, "takePhoto: OPEN");
                            }
                            camera.createSession();
                            break;
                        case CameraProxy.MessageType.CREATE_SESSION:
                            if (DEBUG) {
                                Log.d(TAG, "takePhoto: CREATE_SESSION");
                            }
                            camera.startPreview();
                            break;
                        case CameraProxy.MessageType.START_PREVIEW:
                            if (DEBUG) {
                                Log.d(TAG, "takePhoto: START_PREVIEW");
                            }
                            camera.autoFocus();
                            break;
                        case CameraProxy.MessageType.AUTO_FOCUS:
                            if (DEBUG) {
                                Log.d(TAG, "takePhoto: AUTO_FOCUS");
                            }
                            camera.autoExposure();
                            break;
                        case CameraProxy.MessageType.AUTO_EXPOSURE:
                            if (DEBUG) {
                                Log.d(TAG, "takePhoto: AUTO_EXPOSURE");
                            }
                            camera.takePicture();
                            break;
                        case CameraProxy.MessageType.TAKE_PICTURE:
                            if (DEBUG) {
                                Log.d(TAG, "takePhoto: TAKE_PICTURE");
                            }
                            Image photo = (Image) msg.obj;
                            if (photo == null) {
                                releaseCameraProxy(camera);
                                listener.onFailedTakePhoto("Failed to access image.");
                                return;
                            }
                            storePhoto(photo, listener);
                            photo.close();
                            if (mIsPreviewStarted) {
                                camera.startPreview();
                            } else {
                                camera.destroy();
                            }
                            releaseCameraProxy(camera);
                            break;
                        default:
                            break;
                    }
                } catch (CameraAccessException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to take photo.", e);
                    }
                    releaseCameraProxy(camera);
                    listener.onFailedTakePhoto("Failed to take photo.");
                }
            }
        };
        final Handler errorHandler = new Handler(mPhotoThread.getLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                releaseCameraProxy(camera);
                listener.onFailedTakePhoto("Failed to take photo.");
            }
        };
        camera.start(handler, errorHandler);
    }

    private void storePhoto(final Image image, final OnPhotoEventListener listener) {
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] jpeg = NV21toJPEG(YUV420toNV21(image), width, height, 100);
        byte[] rotated = rotateJPEG(jpeg, width, height, 50);

        // ファイル保存
        mFileManager.saveFile(createNewFileName(), rotated, true, new FileManager.SaveFileCallback() {
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
    }

    private byte[] rotateJPEG(final byte[] jpeg, int width, int height, int quality) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

        //Log.d(TAG, "bitmap=" + bitmap.getWidth() + "x" + bitmap.getHeight() + " width=" + width + " height=" + height);

        int orientation = Camera2Helper.getSensorOrientation(mCameraManager, mCameraId);
        int degrees;
        Bitmap rotated;
        Matrix m = new Matrix();
        if (mFacing == CameraFacing.FRONT || mFacing == CameraFacing.BACK) {
            degrees = orientation;
        } else {
            degrees = 0;
        }
        m.postRotate(degrees);
        rotated = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotated.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] result = baos.toByteArray();
        bitmap.recycle();
        rotated.recycle();
        return result;
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

    public boolean onAcceptPreviewRequest() {
        final CountDownLatch lock = new CountDownLatch(1);
        final Boolean[] result = new Boolean[1];
        startPreview(new PreviewCallback() {
            @Override
            public void onStart() {
                result[0] = true;
                lock.countDown();
            }

            @Override
            public void onError() {
                result[0] = false;
                lock.countDown();
            }
        });
        try {
            lock.await(10 * 1000, TimeUnit.MILLISECONDS);
            if (result[0] == null) {
                return false;
            }
            return result[0];
        } catch (InterruptedException e) {
            return false;
        }
    }

    private ImageReader.OnImageAvailableListener mPreviewListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(final ImageReader reader) {
            // ビットマップ取得
            final Image image = reader.acquireNextImage();
            if (image == null || image.getPlanes() == null) {
                return;
            }
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] jpeg = new byte[buffer.remaining()];
            buffer.get(jpeg);
            byte[] rotated = rotateJPEG(jpeg, image.getHeight(), image.getWidth(), 100); // NOTE: swap width and height.
            image.close();

            mMjpegServer.offerMedia(rotated);
        }
    };

    interface PreviewCallback {
        void onStart();
        void onError();
    }

    private boolean mIsPreviewStarted;

    void startPreview(final @NonNull PreviewCallback callback) {
        final CameraProxy camera = getCameraProxy();
        camera.setPreviewListener(mPreviewListener);
        final Handler handler = new Handler(mPreviewThread.getLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                try {
                    switch (msg.what) {
                        case CameraProxy.MessageType.HELLO:
                            camera.open();
                            break;
                        case CameraProxy.MessageType.OPEN:
                            camera.createSession();
                            break;
                        case CameraProxy.MessageType.CREATE_SESSION:
                            camera.startPreview();
                            break;
                        case CameraProxy.MessageType.START_PREVIEW:
                            mIsPreviewStarted = true;
                            releaseCameraProxy(camera);
                            callback.onStart();
                            break;
                        default:
                            break;
                    }
                } catch (CameraAccessException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to take photo.", e);
                    }
                    releaseCameraProxy(camera);
                    callback.onError();
                }
            }
        };
        final Handler errorHandler = new Handler(mPreviewThread.getLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                releaseCameraProxy(camera);
                callback.onError();
            }
        };
        camera.start(handler, errorHandler);
    }

    void stopPreview() {
        CameraProxy camera = getCameraProxy();
        camera.stopPreview();
        mIsPreviewStarted = false;
    }

    @Override
    public boolean isBack() {
        return mFacing == CameraFacing.BACK;
    }

    private ImageReader mDummyImageReader;

    @Override
    public void turnOnFlashLight() {
        final CameraProxy camera = getCameraProxy();
        final Handler handler = new Handler(mPhotoThread.getLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                try {
                    switch (msg.what) {
                        case CameraProxy.MessageType.HELLO:
                            camera.open();
                            break;
                        case CameraProxy.MessageType.OPEN:
                            camera.createSession();
                            break;
                        case CameraProxy.MessageType.CREATE_SESSION:
                            camera.startPreview();
                            break;
                        case CameraProxy.MessageType.START_PREVIEW:
                            camera.turnOnTorch();
                            break;
                        default:
                            break;
                    }
                } catch (CameraAccessException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to take photo.", e);
                    }
                    releaseCameraProxy(camera);
                }
            }
        };
        final Handler errorHandler = new Handler(mPhotoThread.getLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                releaseCameraProxy(camera);
            }
        };
        camera.start(handler, errorHandler);
    }

    @Override
    public void turnOffFlashLight() {
        final CameraProxy camera = getCameraProxy();
        camera.turnOffTorch();
        releaseCameraProxy(camera);
    }

    @Override
    public boolean isFlashLightState() {
        return mCameraProxy.isTorchOn();
    }

    @Override
    public boolean isUseFlashLight() {
        return mCameraProxy.isUseTorch();
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
        return RecorderState.INACTTIVE;
    }

    @Override
    public PictureSize getPictureSize() {
        return new PictureSize(mCameraProxy.getOptions().getPictureSize());
    }

    @Override
    public void setPictureSize(final PictureSize size) {
        Size newSize = new Size(size.getWidth(), size.getHeight());
        mCameraProxy.getOptions().setPictureSize(newSize);
    }

    @Override
    public PictureSize getPreviewSize() {
        return new PictureSize(mCameraProxy.getOptions().getPreviewSize());
    }

    @Override
    public void setPreviewSize(final PictureSize size) {
        Size newSize = new Size(size.getWidth(), size.getHeight());
        mCameraProxy.getOptions().setPreviewSize(newSize);
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
        for (Size size : mCameraProxy.getOptions().getSupportedPictureSizeList()) {
            result.add(new PictureSize(size));
        }
        return result;
    }

    @Override
    public List<PictureSize> getSupportedPreviewSizes() {
        List<PictureSize> result = new ArrayList<>();
        for (Size size : mCameraProxy.getOptions().getSupportedPreviewSizeList()) {
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
