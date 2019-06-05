/*
 Camera2Recorder.java
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
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.ImageReader;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import org.deviceconnect.android.provider.FileManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private final HandlerThread mPreviewThread = new HandlerThread("preview");

    private final HandlerThread mPhotoThread = new HandlerThread("photo");

    private final Handler mRequestHandler;

    /**
     * 現在の端末の回転方向.
     *
     * @see Surface#ROTATION_0
     * @see Surface#ROTATION_90
     * @see Surface#ROTATION_180
     * @see Surface#ROTATION_270
     */
    private int mCurrentRotation;

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
        camera.setCameraEventListener(this::notifyEventToUser, new Handler(Looper.getMainLooper()));

        mPreviewThread.start();
        mPhotoThread.start();
        HandlerThread requestThread = new HandlerThread("request");
        requestThread.start();
        mRequestHandler = new Handler(requestThread.getLooper());
        mFileManager = fileManager;

        Camera2MJPEGPreviewServer mjpegServer = new Camera2MJPEGPreviewServer(this);
        mjpegServer.setQuality(readPreviewQuality(mjpegServer));
        Camera2RTSPPreviewServer rtspServer = new Camera2RTSPPreviewServer(getContext(), this, this);
        mPreviewServers.add(mjpegServer);
        mPreviewServers.add(rtspServer);
    }

    CameraWrapper getCameraWrapper() {
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
                if (DEBUG) {
                    int w = photo.getWidth();
                    int h = photo.getHeight();
                    Rect rect = photo.getCropRect();
                    Log.d(TAG, "takePhoto: onImageAvailable: image=" + w + "x" + h + " rect=" + rect.width() + "x" + rect.height());
                }

                storePhoto(photo, listener);
                photo.close();
            }, new Handler(mPhotoThread.getLooper()));

            camera.takeStillImage(stillImageReader.getSurface());
        } catch (CameraWrapperException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to take photo.", e);
            }
            listener.onFailedTakePhoto("Failed to take photo.");
        }
    }

    private void storePhoto(final Image image, final OnPhotoEventListener listener) {
        byte[] jpeg = convertToJPEG(image);
        if (DEBUG) {
            Log.d(TAG, "storePhoto: screen orientation: " + Camera2Helper.getScreenOrientation(getContext()));
        }
        jpeg = rotateJPEG(jpeg, PHOTO_JPEG_QUALITY);

        // ファイル保存
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

    private static byte[] readJPEG(final Image jpegImage) {
        ByteBuffer buffer = jpegImage.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data, 0, data.length);
        return data;
    }

    byte[] rotateJPEG(final byte[] jpeg, int quality) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
        int deviceRotation = ROTATIONS.get(mCurrentRotation);
        int cameraRotation = Camera2Helper.getSensorOrientation(mCameraManager, mCameraId);
        int degrees = (360 - deviceRotation + cameraRotation) % 360;
        Bitmap rotated;
        Matrix m = new Matrix();
        if (mFacing == CameraFacing.FRONT) {
            degrees = (180 - degrees) % 360;
        }
        m.postRotate(degrees);
        rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotated.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] result = baos.toByteArray();
        bitmap.recycle();
        rotated.recycle();
        return result;
    }

    static byte[] convertToJPEG(Image image) {
        byte[] jpeg;
        if (image.getFormat() == ImageFormat.JPEG) {
            jpeg = readJPEG(image);
        } else if (image.getFormat() == ImageFormat.YUV_420_888) {
            jpeg = NV21toJPEG(YUV420toNV21(image), image.getWidth(), image.getHeight(), 100);
        } else {
            throw new RuntimeException("Unsupported format: " + image.getFormat());
        }
        return jpeg;
    }

    static byte[] NV21toJPEG(byte[] nv21, int width, int height, int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), quality, out);
        return out.toByteArray();
    }

    static byte[] YUV420toNV21(Image image) {
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

    private int getRotation() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay().getRotation();
    }

    PictureSize getRotatedPreviewSize() {
        Size original = getCameraWrapper().getOptions().getPreviewSize();
        Size rotated;
        int rotation = getRotation();
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

    @Override
    public boolean canPauseRecording() {
        return false;
    }

    @Override
    public synchronized void startRecording(final String serviceId, final RecordingListener listener) {
        mRequestHandler.post(() -> startRecordingInternal(serviceId, listener));
    }

    public synchronized void startRecordingInternal(final String serviceId, final RecordingListener listener) {
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
                        listener.onFailed(Camera2Recorder.this, "Failed to start recording because of camera problem: " + e.getMessage());
                    }
                }

                @Override
                public void onRecordingStartError(final Throwable e) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to start recording for unexpected problem: ", e);
                    }
                    listener.onFailed(Camera2Recorder.this, "Failed to start recording for unexpected problem: " + e.getMessage());
                }
            });
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to start recording for unexpected problem: ", e);
            }
            listener.onFailed(this, "Failed to start recording for unexpected problem: " + e.getMessage());
        }
    }

    @Override
    public synchronized void stopRecording(final StoppingListener listener) {
        mRequestHandler.post(() -> stopRecordingInternal(listener));
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
                    listener.onFailed(Camera2Recorder.this, "Failed to stop recording for unexpected error: " + e.getMessage());
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
        if (checkMediaFile(videoFile)) {
            String filePath = videoFile.getAbsolutePath();
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.TITLE, videoFile.getName());
            values.put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.getName());
            values.put(MediaStore.Video.Media.ARTIST, "DeviceConnect");
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/avc");
            values.put(MediaStore.Video.Media.DATA, videoFile.toString());
            long thumbnailId = registerVideoThumbnail(videoFile);
            if (thumbnailId > -1) {
                values.put(MediaStore.Video.Media.MINI_THUMB_MAGIC, thumbnailId);
            }
            Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

            // 動画IDをサムネイルDBに挿入.
            try {
                if (uri != null && thumbnailId > -1) {
                    String id = uri.getLastPathSegment();
                    if (id != null) {
                        long videoId = Long.parseLong(id);
                        boolean updated = updateThumbnailInfo(thumbnailId, videoId);
                        if (updated) {
                            if (DEBUG) {
                                Log.d(TAG, "Updated videoID on thumbnail info: videoId="
                                        + videoId + ", thumbnailId=" + thumbnailId);
                            }
                        } else {
                            Log.w(TAG, "Failed to update videoID on thumbnail info: videoId="
                                    + videoId + ", thumbnailId=" + thumbnailId);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Failed to parse videoID as long type: video URI=" + uri, e);
            }
            if (DEBUG) {
                if (uri != null) {
                    Log.d(TAG, "Registered video: filePath=" + filePath + ", uri=" + uri.getPath());
                } else {
                    Log.e(TAG, "Failed to register video: file=" + filePath);
                }
            }
        }
    }

    private long registerVideoThumbnail(final File videoFile) {
        String videoFilePath = videoFile.getAbsolutePath();
        final int kind = MediaStore.Images.Thumbnails.MINI_KIND;
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFilePath, kind);

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, data);
        String fileName = videoFile.getName() + ".jpg";

        try {
            String thumbnailFilePath = mFileManager.saveFile(fileName, data.toByteArray());
            if (DEBUG) {
                Log.d(TAG, "Stored thumbnail file: path=" + thumbnailFilePath);
            }
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Thumbnails.DATA, thumbnailFilePath);
            values.put(MediaStore.Video.Thumbnails.WIDTH, thumbnail.getWidth());
            values.put(MediaStore.Video.Thumbnails.HEIGHT, thumbnail.getHeight());
            values.put(MediaStore.Video.Thumbnails.KIND, kind);
            ContentResolver resolver = getContext().getApplicationContext().getContentResolver();
            Uri uri = resolver.insert(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to register video thumbnail on content provider: videoFilePath=" + videoFilePath);
                }
                return -1;
            }
            if (DEBUG) {
                Log.d(TAG, "Registered video thumbnail: uri=" + uri.toString());
            }
            String id = uri.getLastPathSegment();
            if (id == null) {
                if (DEBUG) {
                    Log.e(TAG, "Thumbnail ID is not found in URI: " + uri);
                }
                return -1;
            }
            return Long.parseLong(id);
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to store video thumbnail by FileManager: videoFilePath=" + videoFilePath, e);
            }
            return -1;
        } catch (NumberFormatException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to parse thumbnail ID as long type: videoFilePath=" + videoFilePath);
            }
            return -1;
        } finally {
            thumbnail.recycle();
        }
    }

    private boolean updateThumbnailInfo(final long thumbnailId, final long videoId) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Thumbnails.VIDEO_ID, videoId);
        return updateThumbnailInfo(thumbnailId, values);
    }

    private boolean updateThumbnailInfo(final long thumbnailId, final ContentValues values) {
        ContentResolver resolver = getContext().getApplicationContext().getContentResolver();
        Uri uri = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;
        String where = MediaStore.Video.Thumbnails._ID + " =?";
        String[] args = { Long.toString(thumbnailId) };
        int result = resolver.update(uri, values, where, args);
        return result == 1;
    }

    private void registerPhoto(final File photoFile) {
        if (checkMediaFile(photoFile)) {
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, photoFile.getName());
            values.put(MediaStore.Images.Media.DISPLAY_NAME, photoFile.getName());
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, photoFile.toString());
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (DEBUG) {
                if (uri != null) {
                    Log.d(TAG, "Registered photo: uri=" + uri.getPath());
                } else {
                    Log.e(TAG, "Failed to register photo: file=" + photoFile.getAbsolutePath());
                }
            }
        }
    }

    private boolean checkMediaFile(final @NonNull File file) {
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

    @Override
    public boolean isBack() {
        return mFacing == CameraFacing.BACK;
    }

    @Override
    public void turnOnFlashLight(final @NonNull TurnOnFlashLightListener listener,
                                 final @NonNull Handler handler) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null.");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler is null.");
        }
        mRequestHandler.post(() -> {
            try {
                CameraWrapper camera = getCameraWrapper();
                camera.turnOnTorch(listener::onTurnOn, handler);
                handler.post(listener::onRequested);
            } catch (CameraWrapperException e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to turn on flash light.", e);
                }
                handler.post(() -> { listener.onError(Error.FATAL_ERROR); });
            }
        });
    }

    @Override
    public void turnOffFlashLight(final @NonNull TurnOffFlashLightListener listener,
                                  final @NonNull Handler handler) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null.");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler is null.");
        }
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
        mPreviewThread.quit();
        mPhotoThread.quit();
        mRequestHandler.getLooper().quit();
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
    protected int getDefaultPreviewQuality(final String mimeType) {
        return 40;
    }

    @Override
    public void onDisplayRotation(final int degree) {
        mCurrentRotation = degree;
        for (PreviewServer server : getServers()) {
            server.onDisplayRotation(degree);
        }
    }
}
