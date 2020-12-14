/*
 Camera2Recorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.camera.Camera2Helper;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapper;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperException;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.DefaultSurfaceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.ImageUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaSharing;
import org.deviceconnect.android.deviceplugin.host.recorder.util.SurfaceRecorder;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Camera2Recorder implements HostMediaRecorder, HostDevicePhotoRecorder, HostDeviceStreamRecorder {
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
     * ファイルマネージャ.
     */
    private final FileManager mFileManager;

    /**
     * {@link SurfaceRecorder} のインスタンス.
     */
    private SurfaceRecorder mSurfaceRecorder;

    /**
     * カメラ操作オブジェクト.
     */
    private final CameraWrapper mCameraWrapper;

    /**
     * カメラの位置.
     */
    private final Camera2Recorder.CameraFacing mFacing;

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

    /**
     * ファイルを Android 端末内で共有するためのクラス.
     */
    private final MediaSharing mMediaSharing = MediaSharing.getInstance();

    /**
     * プレビュー配信サーバを管理するクラス.
     */
    private Camera2PreviewServerProvider mCamera2PreviewServerProvider;

    /**
     * カメラのプレビューを配信するクラス.
     */
    private Camera2BroadcasterProvider mBroadcasterProvider;

    /**
     * カメラの映像を Surface に描画を行うためのクラス.
     */
    private CameraSurfaceDrawingThread mCameraSurfaceDrawingThread;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * レコーダの設定.
     */
    private final Settings mSettings = new Settings();

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
        mContext = context;
        mFileManager = fileManager;
        mCameraWrapper = camera;
        mCameraWrapper.setCameraEventListener(this::notifyEventToUser, new Handler(Looper.getMainLooper()));
        mFacing = CameraFacing.detect(mCameraWrapper);

        initSupportedSettings();

        HandlerThread photoThread = new HandlerThread("host-camera-photo");
        photoThread.start();
        mPhotoHandler = new Handler(photoThread.getLooper());

        HandlerThread requestThread = new HandlerThread("host-camera-request");
        requestThread.start();
        mRequestHandler = new Handler(requestThread.getLooper());

        mCameraSurfaceDrawingThread = new CameraSurfaceDrawingThread(this);
        mCamera2PreviewServerProvider = new Camera2PreviewServerProvider(context, this, mFacing.getValue());
        mBroadcasterProvider = new Camera2BroadcasterProvider(this);
    }

    /**
     * レコーダの設定を初期化します.
     */
    private void initSupportedSettings() {
        CameraWrapper.Options options = mCameraWrapper.getOptions();

        mSettings.setSupportedPreviewSizes(new ArrayList<>(options.getSupportedPreviewSizeList()));
        mSettings.setSupportedPictureSizes(new ArrayList<>(options.getSupportedPictureSizeList()));
        mSettings.setSupportedFps(options.getSupportedFpsList());
        mSettings.setSupportedWhiteBalances(options.getSupportedWhiteBalanceList());
    }

    public Context getContext() {
        return mContext;
    }

    public CameraWrapper getCameraWrapper() {
        return mCameraWrapper;
    }

    // テスト
    @Override
    public EGLSurfaceDrawingThread getSurfaceDrawingThread(){
        return mCameraSurfaceDrawingThread;
    }

    // HostMediaRecorder

    @Override
    public synchronized void initialize() {
        if (!mSettings.load(new File(mContext.getCacheDir(), getId()))) {
            CameraWrapper.Options options = mCameraWrapper.getOptions();
            mSettings.setPictureSize(options.getDefaultPictureSize());
            mSettings.setPreviewSize(options.getDefaultPreviewSize());
            mSettings.setPreviewBitRate(2 * 1024 * 1024);
            mSettings.setPreviewMaxFrameRate(30);
            mSettings.setPreviewKeyFrameInterval(1);

            mSettings.setAudioEnabled(false);
            mSettings.setPreviewAudioBitRate(64 * 1024);
            mSettings.setPreviewSampleRate(8000);
            mSettings.setPreviewChannel(1);
            mSettings.setUseAEC(true);
        }
    }

    @Override
    public synchronized void clean() {
        mCamera2PreviewServerProvider.stopServers();
    }

    @Override
    public void destroy() {
        mPhotoHandler.getLooper().quit();
        mRequestHandler.getLooper().quit();
    }

    @Override
    public String getId() {
        return ID_BASE + "_" + mCameraWrapper.getId();
    }

    @Override
    public String getName() {
        return NAME_BASE + " " + mCameraWrapper.getId() + " (" + mFacing.getName() + ")";
    }

    @Override
    public String getMimeType() {
        // デフォルトのマイムタイプを返却
        return MIME_TYPE_JPEG;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        List<String> mimeTypes = mCamera2PreviewServerProvider.getSupportedMimeType();
        mimeTypes.add(0, MIME_TYPE_JPEG);
        return mimeTypes;
    }

    @Override
    public State getState() {
        if (mCameraWrapper.isRecording() || mCameraWrapper.isTakingStillImage()) {
            return State.RECORDING;
        }
        // Preview用のNotificationが表示されている場合は、カメラをPreviewで占有しているものと判断する。
        if (mCamera2PreviewServerProvider.isRunning()) {
            return State.PREVIEW;
        }
        return State.INACTIVE;
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
        return mBroadcasterProvider;
    }

    @Override
    public void onDisplayRotation(final int degree) {
        mCurrentRotation = degree;
        mCamera2PreviewServerProvider.onConfigChange();
    }

    @Override
    public void requestPermission(final PermissionCallback callback) {
        CapabilityUtil.requestPermissions(mContext, new PermissionUtility.PermissionRequestCallback() {
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

    // HostDevicePhotoRecorder

    @Override
    public void takePhoto(final @NonNull OnPhotoEventListener listener) {
        mRequestHandler.post(() -> takePhotoInternal(listener));
    }

    @Override
    public void turnOnFlashLight(final @NonNull TurnOnFlashLightListener listener,
                                 final @NonNull Handler handler) {
        mRequestHandler.post(() -> {
            try {
                mCameraWrapper.turnOnTorch(listener::onTurnOn, handler);
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

    // HostDeviceStreamRecorder

    @Override
    public String getStreamMimeType() {
        return "video/mp4";
    }

    @Override
    public void startRecording(final RecordingListener listener) {
        mRequestHandler.post(() -> startRecordingInternal(listener));
    }

    @Override
    public void stopRecording(final StoppingListener listener) {
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
    public void muteTrack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unMuteTrack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMutedTrack() {
        throw new UnsupportedOperationException();
    }

    /**
     * トーストでカメラ操作のイベントをユーザに通知します.
     *
     * @param event カメラ操作のイベント
     */
    private void notifyEventToUser(final CameraWrapper.CameraEvent event) {
        switch (event) {
            case SHUTTERED:
                showToast(mContext.getString(R.string.shuttered));
                break;
            case STARTED_VIDEO_RECORDING:
                showToast(mContext.getString(R.string.started_video_recording));
                break;
            case STOPPED_VIDEO_RECORDING:
                showToast(mContext.getString(R.string.stopped_video_recording));
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
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
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
     * 画面の回転を取得します.
     *
     * <p>
     *  取得できる値は以下の通りです。
     *  <ul>
     *  <li>Surface.ROTATION_0</li>
     *  <li>Surface.ROTATION_90</li>
     *  <li>Surface.ROTATION_180</li>
     *  <li>Surface.ROTATION_270</li>
     *  </ul>
     * </p>
     *
     * @return 画面の回転
     */
    int getDisplayRotation() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            throw new RuntimeException("WindowManager is not supported.");
        }
        return wm.getDefaultDisplay().getRotation();
    }

    /**
     * 画面の回転に合わせて、カメラの解像度の横幅と縦幅をスワップするか確認します.
     *
     * @return 回転する場合はtrue、それ以外はfalse
     */
    boolean isSwappedDimensions() {
        int sensorOrientation = mCameraWrapper.getSensorOrientation();
        switch (getDisplayRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    return true;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * プレビュー確認用の Surface を設定します.
     *
     * @param surface Surface
     */
    void setTargetSurface(Surface surface) {
        mCameraWrapper.setTargetSurface(surface);
    }

    /**
     * プレビュー中か確認します.
     *
     * @return プレビュー中の場合はtrue、それ以外はfalse
     */
    boolean isPreview() {
        return mCameraWrapper.isPreview();
    }

    /**
     * プレビューを開始します.
     *
     * @param previewSurface プレビューを描画する Surface
     * @throws CameraWrapperException カメラの操作に失敗した場合に発生
     */
    void startPreview(final Surface previewSurface) throws CameraWrapperException {
        mCameraWrapper.startPreview(previewSurface, false);
    }

    /**
     * プレビューを停止します.
     *
     * @throws CameraWrapperException カメラの操作に失敗した場合に発生
     */
    void stopPreview() throws CameraWrapperException {
        mCameraWrapper.stopPreview();
    }

    /**
     * 静止画の撮影を行います.
     *
     * @param listener 静止画の撮影結果を通知するリスナー
     */
    private void takePhotoInternal(final @NonNull OnPhotoEventListener listener) {
        try {
            ImageReader stillImageReader = mCameraWrapper.createStillImageReader(ImageFormat.JPEG);
            stillImageReader.setOnImageAvailableListener((reader) -> {
                Image photo = reader.acquireNextImage();
                if (photo == null) {
                    listener.onFailedTakePhoto("Failed to acquire image.");
                    return;
                }
                storePhoto(photo, listener);
                photo.close();
            }, mPhotoHandler);
            mCameraWrapper.takeStillImage(stillImageReader.getSurface());
        } catch (CameraWrapperException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to take photo.", e);
            }
            listener.onFailedTakePhoto("Failed to take photo.");
        }
    }

    /**
     * 撮影した静止画をファイルに保存します.
     *
     * @param image 撮影された静止画
     * @param listener 静止画の撮影結果を通知するリスナー
     */
    private void storePhoto(final Image image, final OnPhotoEventListener listener) {
        if (DEBUG) {
            Log.d(TAG, "storePhoto: screen orientation: " + Camera2Helper.getScreenOrientation(mContext));
        }

        byte[] jpeg = ImageUtil.convertToJPEG(image);
        int deviceRotation = ROTATIONS.get(mCurrentRotation);
        int cameraRotation = mCameraWrapper.getSensorOrientation();
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

    /**
     * 録画を行います.
     *
     * @param listener 録画開始結果を通知するリスナー
     */
    private void startRecordingInternal(final RecordingListener listener) {
        if (mSurfaceRecorder != null) {
            listener.onFailed(this, "Recording has started already.");
            return;
        }

        try {
            mSurfaceRecorder = new DefaultSurfaceRecorder(
                    mContext,
                    mFacing,
                    mCameraWrapper.getSensorOrientation(),
                    mCameraWrapper.getOptions().getPictureSize(),
                    mFileManager.getBasePath());
            mSurfaceRecorder.start(new SurfaceRecorder.OnRecordingStartListener() {
                @Override
                public void onRecordingStart() {
                    try {
                        mCameraWrapper.startRecording(mSurfaceRecorder.getInputSurface(), false);
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

    /**
     * 録画停止を行います.
     *
     * @param listener 録画停止結果を通知するリスナー
     */
    private void stopRecordingInternal(final StoppingListener listener) {
        if (mSurfaceRecorder == null) {
            listener.onFailed(this, "Recording has stopped already.");
            return;
        }

        try {
            mCameraWrapper.stopRecording();
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
        Uri uri = mMediaSharing.shareVideo(mContext, videoFile, mFileManager);
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
        Uri uri = mMediaSharing.sharePhoto(mContext, photoFile);
        if (DEBUG) {
            if (uri != null) {
                Log.d(TAG, "Registered photo: uri=" + uri.getPath());
            } else {
                Log.e(TAG, "Failed to register photo: file=" + photoFile.getAbsolutePath());
            }
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

        public static CameraFacing facingOf(final int value) {
            for (CameraFacing facing : CameraFacing.values()) {
                if (facing.mValue == value) {
                    return facing;
                }
            }
            return null;
        }

        public static CameraFacing detect(CameraWrapper cameraWrapper) {
            try {
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
            } catch (CameraWrapperException e) {
                return CameraFacing.UNKNOWN;
            }
        }
    }
}
