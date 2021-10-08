/*
 Camera2Recorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.Manifest;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapper;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperException;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.ImageUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MP4Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaProjectionProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.SurfaceMP4Recorder;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Camera2Recorder extends AbstractMediaRecorder {
    protected static final List<Size> ENCODE_SIZE_LIST = Arrays.asList(
            new Size(128, 96),
            new Size(320, 240),
            new Size(640, 480),
            new Size(800, 600),
            new Size(1024, 768),
            new Size(1280, 960),
            new Size(1440, 1080),
            new Size(1600, 1200),
            new Size(320, 180),
            new Size(640, 360),
            new Size(854, 480),
            new Size(1280, 720),
            new Size(1366, 768),
            new Size(1920, 1080),
            new Size(2560, 1440),
            new Size(3840, 2160),
            new Size(5760, 3240),
            new Size(7680, 4320)
    );

    /**
     * カメラターゲットIDの定義.
     */
    private static final String ID_BASE = "camera";

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
     * カメラ操作オブジェクト.
     */
    private final CameraWrapper mCameraWrapper;

    /**
     * カメラの位置.
     */
    private final Camera2Recorder.CameraFacing mFacing;

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

    /**
     * プレビュー配信サーバを管理するクラス.
     */
    private final Camera2PreviewServerProvider mCamera2PreviewServerProvider;

    /**
     * カメラのプレビューを配信するクラス.
     */
    private final Camera2BroadcasterProvider mCamera2BroadcasterProvider;

    /**
     * カメラの映像を Surface に描画を行うためのクラス.
     */
    private final CameraSurfaceDrawingThread mCameraSurfaceDrawingThread;

    /**
     * レコーダの設定.
     */
    private final CameraSettings mSettings;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param camera カメラ
     * @param fileManager ファイルマネージャ
     */
    public Camera2Recorder(Context context, CameraWrapper camera, FileManager fileManager, MediaProjectionProvider provider) {
        super(context, fileManager, provider);
        mCameraWrapper = camera;
        mCameraWrapper.setCameraEventListener(this::notifyEventToUser, new Handler(Looper.getMainLooper()));
        mFacing = CameraFacing.detect(mCameraWrapper);
        mSettings = new CameraSettings(context, this);

        initSupportedSettings();

        HandlerThread photoThread = new HandlerThread("host-camera-photo");
        photoThread.start();
        mPhotoHandler = new Handler(photoThread.getLooper());

        mCameraSurfaceDrawingThread = new CameraSurfaceDrawingThread(this);
        mCamera2PreviewServerProvider = new Camera2PreviewServerProvider(context, this);
        mCamera2BroadcasterProvider = new Camera2BroadcasterProvider(context, this);
    }

    /**
     * サイズの小さい方からソートを行うための比較演算子.
     */
    private static final Comparator<Size> SIZE_COMPARATOR = (lhs, rhs) -> {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    };

    private List<Size> getEncoderSizeList() {
        Size maxSize = CapabilityUtil.getSupportedMaxSize(VideoCodec.H264.getMimeType());
        List<Size> sizes = new ArrayList<>();
        for (Size size : ENCODE_SIZE_LIST) {
            if (size.getWidth() <= maxSize.getWidth() && size.getHeight() <= maxSize.getHeight()) {
                sizes.add(size);
            }
        }
        Collections.sort(sizes, SIZE_COMPARATOR);
        return sizes;
    }

    /**
     * レコーダの設定を初期化します.
     */
    private void initSupportedSettings() {
        CameraWrapper.Options options = mCameraWrapper.getOptions();

        List<Range<Integer>> supportedFpsList = options.getSupportedFpsList();
        mSettings.mSupportedPictureSize = new ArrayList<>(options.getSupportedPictureSizeList());
        mSettings.mSupportedPreviewSize = new ArrayList<>(options.getSupportedPreviewSizeList());
        mSettings.mSupportedEncoderSize = getEncoderSizeList();

        if (!mSettings.isInitialized()) {
            // カメラ設定
            mSettings.setPictureSize(options.getDefaultPictureSize());
            mSettings.setPreviewSize(options.getDefaultPreviewSize());
            mSettings.setPreviewAutoFocusMode(options.getAutoFocusMode());
            mSettings.setPreviewWhiteBalance(options.getAutoWhiteBalanceMode());
            mSettings.setPreviewWhiteBalanceTemperature(5600);
            if (supportedFpsList.size() > 0) {
                mSettings.setPreviewFps(supportedFpsList.get(supportedFpsList.size() - 1));
            }
            mSettings.setOrientation(Surface.ROTATION_90);

            // 音声設定
            mSettings.setPreviewAudioSource(null);
            mSettings.setPreviewAudioBitRate(128 * 1024);
            mSettings.setPreviewSampleRate(48000);
            mSettings.setPreviewChannel(1);
            mSettings.setUseAEC(true);

            // 各サーバ設定
            mSettings.addEncoder(getId() + "-MJPEG");
            EncoderSettings mjpeg = mSettings.getEncoderSetting(getId() + "-MJPEG");
            mjpeg.setName("MJPEG");
            mjpeg.setMimeType(MimeType.MJPEG);
            mjpeg.setPort(11000 + mFacing.mValue);
            mjpeg.setPreviewSize(options.getDefaultPreviewSize());
            mjpeg.setPreviewQuality(80);
            mjpeg.setPreviewMaxFrameRate(30);

            mSettings.addEncoder(getId() + "-RTSP");
            EncoderSettings rtsp = mSettings.getEncoderSetting(getId() + "-RTSP");
            rtsp.setName("RTSP");
            rtsp.setMimeType(MimeType.RTSP);
            rtsp.setPort(12000 + mFacing.mValue);
            rtsp.setPreviewSize(options.getDefaultPreviewSize());
            rtsp.setPreviewBitRate(2 * 1024 * 1024);
            rtsp.setPreviewMaxFrameRate(30);
            rtsp.setPreviewKeyFrameInterval(5);

            mSettings.addEncoder(getId() + "-SRT");
            EncoderSettings srt = mSettings.getEncoderSetting(getId() + "-SRT");
            srt.setName("SRT");
            srt.setMimeType(MimeType.SRT);
            srt.setPort(13000 + mFacing.mValue);
            srt.setPreviewSize(options.getDefaultPreviewSize());
            srt.setPreviewBitRate(2 * 1024 * 1024);
            srt.setPreviewMaxFrameRate(30);
            srt.setPreviewKeyFrameInterval(5);

            mSettings.addEncoder(getId() + "-RTMP");
            EncoderSettings rtmp = mSettings.getEncoderSetting(getId() + "-RTMP");
            rtmp.setName("RTMP");
            rtmp.setMimeType(MimeType.RTMP);
            rtmp.setPreviewSize(options.getDefaultPreviewSize());
            rtmp.setPreviewBitRate(2 * 1024 * 1024);
            rtmp.setPreviewMaxFrameRate(30);
            rtmp.setPreviewKeyFrameInterval(5);
            rtmp.setBroadcastURI("rtmp://localhost:1935");

            mSettings.finishInitialization();
        }
    }

    public CameraWrapper getCameraWrapper() {
        return mCameraWrapper;
    }

    // HostMediaRecorder

    @Override
    public synchronized void clean() {
        super.clean();
        mCamera2BroadcasterProvider.stop();
        mCamera2PreviewServerProvider.stop();
        mCameraSurfaceDrawingThread.stop(true);
    }

    @Override
    public void destroy() {
        super.destroy();
        mPhotoHandler.getLooper().quit();
    }

    @Override
    public String getId() {
        return ID_BASE + "_" + mCameraWrapper.getId().replaceAll("/", "_");
    }

    @Override
    public String getName() {
        if (mCameraWrapper.isDepth()) {
            return "Depth Camera " + mCameraWrapper.getId() + " (" + mFacing.getName() + ")";
        } else {
            return "Camera " + mCameraWrapper.getId() + " (" + mFacing.getName() + ")";
        }
    }

    @Override
    public String getMimeType() {
        // デフォルトのマイムタイプを返却
        return MIME_TYPE_JPEG;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        List<String> mimeTypes = mCamera2PreviewServerProvider.getSupportedMimeType();
        mimeTypes.addAll(mCamera2BroadcasterProvider.getSupportedMimeType());
        mimeTypes.add(0, MIME_TYPE_JPEG);
        return mimeTypes;
    }

    @Override
    public Settings getSettings() {
        return mSettings;
    }

    @Override
    public PreviewServerProvider getServerProvider() {
        return mCamera2PreviewServerProvider;
    }

    @Override
    public BroadcasterProvider getBroadcasterProvider() {
        return mCamera2BroadcasterProvider;
    }

    @Override
    public EGLSurfaceDrawingThread getSurfaceDrawingThread(){
        return mCameraSurfaceDrawingThread;
    }

    @Override
    public void onDisplayRotation(int rotation) {
        mCurrentRotation = rotation;
        super.onDisplayRotation(rotation);
    }

    @Override
    public void requestPermission(final PermissionCallback callback) {
        requestPermission(new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, new PermissionCallback() {
            @Override
            public void onAllowed() {
                if (mSettings.getPreviewAudioSource() == AudioSource.APP) {
                    requestMediaProjection(callback);
                } else {
                    callback.onAllowed();
                }
            }

            @Override
            public void onDisallowed() {
                callback.onDisallowed();
            }
        });
    }

    // HostDevicePhotoRecorder

    @Override
    public void takePhoto(final @NonNull OnPhotoEventListener listener) {
        postRequestHandler(() -> takePhotoInternal(listener));
    }

    @Override
    public void turnOnFlashLight(final @NonNull TurnOnFlashLightListener listener,
                                 final @NonNull Handler handler) {
        postRequestHandler(() -> {
            try {
                mCameraWrapper.turnOnTorch(listener::onTurnOn, handler);
                handler.post(listener::onRequested);
            } catch (CameraWrapperException e) {
                handler.post(() -> listener.onError(Error.FATAL_ERROR));
            }
        });
    }

    @Override
    public void turnOffFlashLight(final @NonNull TurnOffFlashLightListener listener,
                                  final @NonNull Handler handler) {
        postRequestHandler(() -> {
            mCameraWrapper.turnOffTorch(listener::onTurnOff, handler);
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

    // Implements AbstractMediaRecorder method.

    @Override
    protected MP4Recorder createMP4Recorder() {
        File file = new File(getFileManager().getBasePath(), generateVideoFileName());
        return new SurfaceMP4Recorder(file, mSettings, mCameraSurfaceDrawingThread);
    }

    /**
     * トーストでカメラ操作のイベントをユーザに通知します.
     *
     * @param event カメラ操作のイベント
     */
    private void notifyEventToUser(final CameraWrapper.CameraEvent event) {
        switch (event) {
            case SHUTTERED:
                showToast(getContext().getString(R.string.shuttered));
                break;
            case STARTED_VIDEO_RECORDING:
                showToast(getContext().getString(R.string.started_video_recording));
                break;
            case STOPPED_VIDEO_RECORDING:
                showToast(getContext().getString(R.string.stopped_video_recording));
                break;
            default:
                break;
        }
    }

    /**
     * トーストを画面に表示します.
     *
     * @param message トーストに表示する文字列
     */
    private void showToast(final String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 新規の動画ファイル名を作成します.
     *
     * @return ファイル名
     */
    private String generateVideoFileName() {
        return FILENAME_PREFIX + DATE_FORMAT.format(new Date()) + ".mp4";
    }

    /**
     * 新規の静止画ファイル名を作成します.
     *
     * @return ファイル名
     */
    private String generateImageFileName() {
        return FILENAME_PREFIX + DATE_FORMAT.format(new Date()) + FILE_EXTENSION;
    }

    /**
     * 静止画の撮影を行います.
     *
     * @param listener 静止画の撮影結果を通知するリスナー
     */
    private void takePhotoInternal(final @NonNull OnPhotoEventListener listener) {
        try {
            if (mCameraWrapper.isTakingStillImage()) {
                listener.onFailedTakePhoto("Already taking a picture.");
                return;
            }

            mCameraWrapper.getOptions().setPictureSize(mSettings.getPictureSize());
            mCameraWrapper.takeStillImage((reader) -> {
                Image photo = reader.acquireNextImage();
                if (photo == null) {
                    listener.onFailedTakePhoto("Failed to acquire image.");
                    return;
                }

                int currentRotation = mCurrentRotation;
                if (getSettings().getOrientation() != -1) {
                    currentRotation = getSettings().getOrientation();
                }

                byte[] jpeg = ImageUtil.convertToJPEG(photo);
                int deviceRotation = ROTATIONS.get(currentRotation);
                int cameraRotation = mCameraWrapper.getSensorOrientation();
                int degrees = (360 - deviceRotation + cameraRotation) % 360;
                if (mFacing == CameraFacing.FRONT) {
                    degrees = (180 - degrees) % 360;
                }
                jpeg = ImageUtil.rotateJPEG(jpeg, PHOTO_JPEG_QUALITY, degrees);
                storePhoto(generateImageFileName(), jpeg, listener);

                photo.close();
            }, mPhotoHandler);
        } catch (CameraWrapperException e) {
            listener.onFailedTakePhoto("Failed to take photo.");
        }
    }

    /**
     * カメラの位置.
     */
    public enum CameraFacing {
        /** スマートフォンの裏側. */
        BACK("Back", 0),

        /** スマートフォンの正面. */
        FRONT("Front", 1),

        /** 外部接続. (e.g. OTG 接続されている USB カメラ) */
        EXTERNAL("External", 2),

        /** 不明. */
        UNKNOWN("Unknown", -1);

        /** カメラの位置を表現する名前. */
        private final String mName;

        /**
         * カメラの番号.
         */
        private final int mValue;

        /**
         * コンストラクタ.
         * @param name カメラの位置を表現する名前
         * @param value カメラの番号
         */
        CameraFacing(final String name, final int value) {
            mName = name;
            mValue = value;
        }

        /**
         * カメラの位置を表現する名前を取得する.
         * @return 名前
         */
        public String getName() {
            return mName;
        }

        /**
         * カメラの番号を取得します.
         *
         * @return カメラ番号
         */
        public int getValue() {
            return mValue;
        }

        public static CameraFacing detect(CameraWrapper cameraWrapper) {
            int facing = cameraWrapper.getFacing();
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
        }
    }

    private class CameraSettings extends Settings {
        private List<Size> mSupportedPictureSize = new ArrayList<>();
        private List<Size> mSupportedPreviewSize = new ArrayList<>();
        private List<Size> mSupportedEncoderSize;

        CameraSettings(Context context, HostMediaRecorder recorder) {
            super(context, recorder);
        }

        @Override
        protected EncoderSettings createEncoderSettings(String encoderId) {
            return new EncoderSettings(getContext(), encoderId) {
                @Override
                public List<Size> getSupportedEncoderSizes() {
                    return mSupportedEncoderSize;
                }
            };
        }

        @Override
        public List<Size> getSupportedPictureSizes() {
            return mSupportedPictureSize;
        }

        @Override
        public List<Size> getSupportedPreviewSizes() {
            return mSupportedPreviewSize;
        }

        @Override
        public List<Range<Integer>> getSupportedFps() {
            return mCameraWrapper.getOptions().getSupportedFpsList();
        }

        @Override
        public List<Integer> getSupportedAutoFocusModeList() {
            return mCameraWrapper.getOptions().getSupportedAutoFocusModeList();
        }

        @Override
        public List<Integer> getSupportedWhiteBalanceModeList() {
            return mCameraWrapper.getOptions().getSupportedWhiteBalanceList();
        }

        @Override
        public List<Integer> getSupportedAutoExposureModeList() {
            return mCameraWrapper.getOptions().getSupportedAutoExposureModeList();
        }

        @Override
        public List<Integer> getSupportedStabilizationList() {
            return mCameraWrapper.getOptions().getSupportedStabilizationList();
        }

        @Override
        public List<Integer> getSupportedOpticalStabilizationList() {
            return mCameraWrapper.getOptions().getSupportedOpticalStabilizationList();
        }

        @Override
        public Float getMaxDigitalZoom() {
            return mCameraWrapper.getOptions().getMaxDigitalZoom();
        }

        @Override
        public Range<Long> getSupportedSensorExposureTime() {
            return mCameraWrapper.getOptions().getSupportedExposureTimeRange();
        }

        @Override
        public Range<Integer> getSupportedSensorSensitivity() {
            return mCameraWrapper.getOptions().getSupportedSensitivityRange();
        }

        @Override
        public Long getMaxSensorFrameDuration() {
            return mCameraWrapper.getOptions().getMaxFrameDuration();
        }

        @Override
        public List<Integer> getSupportedNoiseReductionList() {
            return mCameraWrapper.getOptions().getSupportedNoiseReductionList();
        }

        @Override
        public List<Float> getSupportedFocalLengthList() {
            return mCameraWrapper.getOptions().getSupportedFocalLengthList();
        }

        @Override
        public Range<Integer> getSupportedWhiteBalanceTemperature() {
            return new Range<>(1000, 12000);
        }
    }
}
