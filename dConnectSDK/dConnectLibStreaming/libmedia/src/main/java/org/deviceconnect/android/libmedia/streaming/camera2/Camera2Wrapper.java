package org.deviceconnect.android.libmedia.streaming.camera2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import org.deviceconnect.android.libmedia.BuildConfig;

/**
 * Camera2 API でカメラデバイスを制御するためのクラス.
 */
public class Camera2Wrapper {
    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "Camera2";

    /**
     * カメラデバイスと接続されていない状態.
     */
    private static final int STATE_NOT_OPEN = 0;

    /**
     * カメラデバイスの画像を表示する Surface を初期化している状態.
     */
    private static final int STATE_INIT_SURFACE = 1;

    /**
     * カメラデバイスと接続している状態.
     */
    private static final int STATE_OPEN_CAMERA = 2;

    /**
     * カメラデバイスとのキャプチャセッションを作成している状態.
     */
    private static final int STATE_CREATE_SESSION = 3;

    /**
     * カメラデバイスとのキャプチャセッションを破棄している状態.
     */
    private static final int STATE_DESTROY_SESSION = 4;

    /**
     * カメラデバイスがプレビューを開始している状態.
     */
    private static final int STATE_PREVIEW = 5;

    /**
     * カメラデバイスがオートフォーカスを行なっている状態.
     */
    private static final int STATE_AUTO_FOCUS = 6;

    /**
     * カメラデバイスがオート露出を行なっている状態.
     */
    private static final int STATE_AUTO_EXPOSURE = 7;

    /**
     * カメラデバイスが写真撮影を行なっている状態.
     */
    private static final int STATE_TAKE_PICTURE = 8;

    /**
     * カメラデバイスが動画撮影を行なっている状態.
     */
    private static final int STATE_RECORDING = 9;

    /**
     * カメラデバイスと切断している状態.
     */
    private static final int STATE_ABORT = 10;

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
     * カメラの回転方向を定義します.
     */
    public enum Rotation {
        ROTATION_0,
        ROTATION_90,
        ROTATION_180,
        ROTATION_270,
        FREE
    }

    /**
     * カメラデバイス管理クラス.
     */
    private CameraManager mCameraManager;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * 接続しているカメラデバイス.
     */
    private CameraDevice mCameraDevice;

    /**
     * プレビュー、撮影を行うセッション.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * 撮影結果を読み込むクラス.
     */
    private ImageReader mImageReader;

    /**
     * プレビュー要求ビルダー.
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * プレビューを表示するTextureView.
     */
    private AutoFitTextureView mTextureView;

    /**
     * プレビューを表示するSurfaceTexture.
     */
    private SurfaceTexture mSurfaceTexture;

    /**
     * 描画先の Surface のリスト.
     */
    private final List<Surface> mSurfaces = new ArrayList<>();

    /**
     * Camera2 に設定する Surface のリスト.
     */
    private final List<Surface> mTargetSurfaces = new ArrayList<>();

    /**
     * ハンドラー.
     */
    private Handler mBackgroundHandler = new Handler(Looper.getMainLooper());

    /**
     * 撮影結果を通知するリスナー.
     */
    private ImageReader.OnImageAvailableListener mTakePictureListener;

    /**
     * カメラデバイスへの設定.
     */
    private Settings mSettings;

    /**
     * カメラデバイスの状態.
     */
    private State mState;

    /**
     * エラー通知用コールバック.
     */
    private CameraEventListener mCameraEventListener;

    private final State mInitSurfaceState = new InitSurfaceState();
    private final State mOpenCameraState = new OpenCameraState();
    private final CreateSessionState mCreateSessionState = new CreateSessionState();
    private final State mDestroySessionState = new DestroySessionState();
    private final State mPreviewState = new PreviewState();
    private final State mRecordingState = new RecordingState();
    private final State mAutoFocusState = new AutoFocusState();
    private final State mAutoExposureState = new AutoExposureState();
    private final TakePictureState mTakePictureState = new TakePictureState();
    private final State mAbortState = new AbortState();

    /**
     * コンストラクタ.
     * @param context コンテキスト
     * @param cameraId カメラID
     */
    Camera2Wrapper(@NonNull Context context, String cameraId) {
        mContext = context;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (mCameraManager == null) {
            throw new UnsupportedOperationException("Not supported a Camera.");
        }
        initSettings(cameraId);
    }

    /**
     * カメラのイベントを通知するリスナー.
     *
     * @param listener リスナー
     */
    public void setCameraEventListener(CameraEventListener listener) {
        mCameraEventListener = listener;
    }

    /**
     * カメラデバイスを開きます.
     *
     * @param surfaces カメラの映像を描画するSurfaceのリスト
     * @IllegalStateException 既にカメラデバイスがオープンされていた場合に発生
     */
    public void open(List<Surface> surfaces) {
        if (mState != null) {
            throw new IllegalStateException("Camera2 has already started. state=" + mState);
        }

        mSurfaces.clear();
        mSurfaces.addAll(surfaces);
        nextState(mOpenCameraState);
    }

    /**
     * カメラデバイスを開きます.
     * <p>
     * TextView を指定して接続する場合には、{@link #STATE_INIT_SURFACE} → {@link #STATE_OPEN_CAMERA} と遷移します。
     * </p>
     * @param textureView カメラデバイスの映像を描画するView
     * @IllegalStateException 既にカメラデバイスがオープンされていた場合に発生
     */
    public void open(@NonNull AutoFitTextureView textureView) {
        if (mState != null) {
            throw new IllegalStateException("Camera2 has already started. state=" + mState);
        }

        mTextureView = textureView;
        nextState(mInitSurfaceState);
    }

    /**
     * カメラデバイスを開きます.
     * <p>
     * TextView を指定して接続する場合には、{@link #STATE_INIT_SURFACE} → {@link #STATE_OPEN_CAMERA} と遷移します。
     * </p>
     * @param textureView カメラデバイスの映像を描画するView
     * @param surfaces カメラの映像を描画するSurfaceのリスト
     * @IllegalStateException 既にカメラデバイスがオープンされていた場合に発生
     */
    public void open(@NonNull AutoFitTextureView textureView, List<Surface> surfaces) {
        if (mState != null) {
            throw new IllegalStateException("Camera2 has already started. state=" + mState);
        }

        mTextureView = textureView;
        mSurfaces.clear();
        mSurfaces.addAll(surfaces);
        nextState(mInitSurfaceState);
    }

    /**
     * カメラデバイスを開きます.
     *
     * @param surfaceTexture カメラデバイスの映像を描画するSurfaceTexture
     * @IllegalStateException 既にカメラデバイスがオープンされていた場合に発生
     */
    public void open(@NonNull SurfaceTexture surfaceTexture) {
        if (mState != null) {
            throw new IllegalStateException("Camera2 has already started. state=" + mState);
        }

        mSurfaceTexture = surfaceTexture;
        nextState(mOpenCameraState);
    }

    /**
     * カメラデバイスを開きます.
     *
     * @param surfaceTexture カメラデバイスの映像を描画するSurfaceTexture
     * @param surfaces カメラの映像を描画するSurfaceのリスト
     * @IllegalStateException 既にカメラデバイスがオープンされていた場合に発生
     */
    public void open(@NonNull SurfaceTexture surfaceTexture, List<Surface> surfaces) {
        if (mState != null) {
            throw new IllegalStateException("Camera2 has already started. state=" + mState);
        }

        mSurfaceTexture = surfaceTexture;
        mSurfaces.clear();
        mSurfaces.addAll(surfaces);
        nextState(mOpenCameraState);
    }

    /**
     * カメラデバイスの状態を取得します.
     *
     * 状態は以下の値を返却します。
     * <ul>
     *     <li>{@link #STATE_NOT_OPEN}</li>
     *     <li>{@link #STATE_INIT_SURFACE}</li>
     *     <li>{@link #STATE_OPEN_CAMERA}</li>
     *     <li>{@link #STATE_CREATE_SESSION}</li>
     *     <li>{@link #STATE_DESTROY_SESSION}</li>
     *     <li>{@link #STATE_PREVIEW}</li>
     *     <li>{@link #STATE_AUTO_FOCUS}</li>
     *     <li>{@link #STATE_AUTO_EXPOSURE}</li>
     *     <li>{@link #STATE_TAKE_PICTURE}</li>
     *     <li>{@link #STATE_RECORDING}</li>
     *     <li>{@link #STATE_ABORT}</li>
     * </ul>
     * @return カメラデバイスの状態
     */
    public int getState() {
        if (mState == null) {
            return STATE_NOT_OPEN;
        } else {
            return mState.getState();
        }
    }

    /**
     * カメラデバイスの設定を取得します.
     *
     * @return {@link Settings}のインスタンス
     */
    public Settings getSettings() {
        return mSettings;
    }

    /**
     * カメラデバイスのプレビューを開始します.
     *
     * @return プレビューの開始に成功した場合はtrue、それ以外はfalse
     */
    public boolean startPreview() {
        if (mState != mOpenCameraState) {
            if (DEBUG) {
                Log.w(TAG, "It is invalid state to start a preview. state=" + mState);
            }
            return false;
        }

        mCreateSessionState.setNextState(mPreviewState);
        nextState(mCreateSessionState);
        return true;
    }

    /**
     * カメラデバイスのプレビューを停止します.
     *
     * @return プレビューの停止に成功した場合はtrue、それ以外はfalse
     */
    public boolean stopPreview() {
        if (mState != mPreviewState) {
            if (DEBUG) {
                Log.w(TAG, "It is invalid state to stop a preview. state=" + mState);
            }
            return false;
        }

        nextState(mDestroySessionState);
        return true;
    }

    /**
     * カメラデバイスのレコーディングを開始します.
     *
     * @return レコーディングの開始に成功した場合はtrue、それ以外はfalse
     */
    public boolean startRecording() {
        if (mState != mOpenCameraState) {
            if (DEBUG) {
                Log.w(TAG, "It is invalid state to start a recording. state=" + mState);
            }
            return false;
        }

        mCreateSessionState.setNextState(mRecordingState);
        nextState(mCreateSessionState);
        return true;
    }

    /**
     * カメラデバイスのレコーディングを停止します.
     *
     * @return レコーディングの停止に成功した場合はtrue、それ以外はfalse
     */
    public boolean stopRecording() {
        if (mState != mRecordingState) {
            if (DEBUG) {
                Log.w(TAG, "It is invalid state to stop a recording. state=" + mState);
            }
            return false;
        }

        nextState(mDestroySessionState);
        return true;
    }

    /**
     * カメラデバイスの写真撮影を行います.
     * <p>
     * 写真撮影を行う場合には、{@link #startPreview()} を行い状態を {@link #STATE_PREVIEW} にしておく必要があります。
     * </p>
     * <p>
     * プレビューサイズ、写真サイズを切り替える場合には、プレビューを停止してから、プレビューを開始する必要があります。
     * </p>
     * @param listener 写真撮影した結果を通知するリスナー
     * @return 撮影開始に成功したらtrue、それ以外はfalse
     */
    public boolean takePicture(final ImageReader.OnImageAvailableListener listener) {
        if (mState != mPreviewState) {
            if (DEBUG) {
                Log.w(TAG, "It is invalid state to take a picture. state=" + mState);
            }
            return false;
        }

        mTakePictureListener = listener;
        mTakePictureState.setReturnState(mState);
        nextState(mAutoFocusState);
        return true;
    }

    /**
     * カメラデバイスをクローズします.
     */
    public void close() {
        nextState(mAbortState);
    }

    /**
     * カメラデバイスの設定を初期化します.
     */
    private void initSettings(String cameraId) {
        mSettings = new Settings(cameraId);

        List<Size> pictureSizes = mSettings.getSupportedPictureSizes();
        if (!pictureSizes.isEmpty()) {
            mSettings.setPictureSize(pictureSizes.get(0));
        }

        List<Size> previewSizes = mSettings.getSupportedPreviewSizes();
        if (!previewSizes.isEmpty()) {
            mSettings.setPreviewSize(previewSizes.get(0));
        }

        if (DEBUG) {
            try {
                Camera2Helper.debugInfo(mCameraManager, mSettings.getCameraId());
            } catch (Exception e) {
                Log.e(TAG,"", e);
            }
        }
    }

    /**
     * カメラデバイスをシャットダウンします.
     * <p>
     * セッションなども全て閉じます。
     * </p>
     */
    private void shutdown() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        mSurfaces.clear();

        mTakePictureListener = null;
    }

    /**
     * 指定された状態に移行します.
     *
     * @param nextState 移行先の状態
     */
    private void nextState(final State nextState) {
        try {
            if (DEBUG) {
                Log.i(TAG, "nextState " + mState + " -> " + nextState);
            }

            if (mState != null) {
                mState.exit();
            }

            mState = nextState;

            if (mState != null) {
                mState.enter();
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to change a state. nextState=" + nextState, e);
            }
            shutdown();
            mState = null;
            postOnError(new Camera2WrapperException(e, Camera2WrapperException.ERROR_CODE_FAILED_CHANGE_STATE));
        }
    }

    /**
     * カメラの取り付けられた向きを取得します.
     *
     * @return カメラの取り付けられた向き
     */
    public int getSensorOrientation() {
        return Camera2Helper.getSensorOrientation(mCameraManager, mSettings.getCameraId());
    }

    /**
     * 画面の角度を取得します.
     *
     * <p>
     * 返り値には、0, 90, 180, 270 の角度を返却します。
     * </p>
     *
     * @return 画面の角度(0, 90, 180, 270)
     */
    public int getDisplayOrientation() {
        return getOrientation(getDisplayRotation());
    }

    /**
     * 画面の回転を取得します.
     *
     * <p>
     * {@link Settings#setRotation(Rotation)} で指定した回転を設定を返却します。
     * </p>
     *
     * <p>
     * {@link Rotation#FREE} が設定されていた場合には、
     * 画面の回転に合わせて以下のいずれかの値を返却します。
     * <ul>
     *     <li>Surface.ROTATION_0</li>
     *     <li>Surface.ROTATION_90</li>
     *     <li>Surface.ROTATION_180</li>
     *     <li>Surface.ROTATION_270</li>
     * </ul>
     * </p>
     *
     * @return 画面の回転
     */
    public int getDisplayRotation() {
        switch (mSettings.getRotation()) {
            default:
            case FREE:
                return Camera2Helper.getDisplayRotation(mContext);
            case ROTATION_0:
                return Surface.ROTATION_0;
            case ROTATION_90:
                return Surface.ROTATION_90;
            case ROTATION_180:
                return Surface.ROTATION_180;
            case ROTATION_270:
                return Surface.ROTATION_270;
        }
    }

    /**
     * カメラの取り付けられた向きと画面の向きから縦横のスワップが必要か確認します.
     *
     * @return スワップが必要な場合はtrue、それ以外はfalse
     */
    public boolean isSwappedDimensions() {
        int sensorOrientation = getSensorOrientation();
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
                if (DEBUG) {
                    Log.w(TAG, "Display rotation is invalid.");
                }
                break;
        }
        return false;
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

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(final int viewWidth, final int viewHeight) {
        Size previewSize = mSettings.getPreviewSize();
        int rotation = Camera2Helper.getDisplayRotation(mContext);
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * エラーをコールバックに通知します.
     *
     * @param e エラー原因の例外
     */
    private void postOnError(final Camera2WrapperException e) {
        if (mCameraEventListener != null) {
            mCameraEventListener.onError(e);
        }
        nextState(mAbortState);
    }

    private void postOnOpen() {
        if (mCameraEventListener != null) {
            mCameraEventListener.onOpen();
        }
    }

    private void postOnStartPreview() {
        if (mCameraEventListener != null) {
            mCameraEventListener.onStartPreview();
        }
    }

    private void postOnStopPreview() {
        if (mCameraEventListener != null) {
            mCameraEventListener.onStopPreview();
        }
    }

    /**
     * カメラデバイスの状態を処理するクラス.
     */
    private abstract class State {
        /**
         * 状態の名前.
         */
        private String mName;

        /**
         * コンストラクタ.
         * @param name 状態名
         */
        State(final String name) {
            mName = name;
        }

        @NonNull
        @Override
        public String toString() {
            return mName;
        }

        /**
         * 状態に切り替わった時の処理を行います.
         *
         * @throws CameraAccessException カメラデバイスの操作に失敗した場合に発生
         */
        void enter() throws CameraAccessException {
        }

        /**
         * 状態が終了する前の処理を行います.
         *
         * @throws CameraAccessException カメラデバイスの操作に失敗した場合に発生
         */
        void exit() throws CameraAccessException {
        }

        /**
         * カメラデバイスからの画像取得タイミングで呼び出されます.
         *
         * @param result キャプチャー結果
         * @param isCompleted 完了フラグ
         */
        void onCaptureResult(CaptureResult result, boolean isCompleted) {
        }

        /**
         * 状態コードを取得します.
         *
         * @return 状態コード
         */
        abstract int getState();
    }

    /**
     * Surface の初期化を行うクラス.
     */
    private class InitSurfaceState extends State {
        /**
         * コンストラクタ.
         */
        InitSurfaceState() {
            super("InitSurface");
        }

        @Override
        int getState() {
            return STATE_INIT_SURFACE;
        }

        @Override
        public void enter() {
            if (mTextureView.isAvailable()) {
                // TextureView が利用可能の場合は次の状態に遷移
                mSurfaceTexture = mTextureView.getSurfaceTexture();
                nextState(mOpenCameraState);
            } else {
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        }

        /**
         * TextureView の SurfaceTexture の状態が更新通知を受け取るリスナー.
         */
        private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
                mSurfaceTexture = mTextureView.getSurfaceTexture();
                nextState(mOpenCameraState);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
                configureTransform(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            }
        };
    }

    /**
     * カメラデバイスをオープン処理を行うクラス.
     */
    private class OpenCameraState extends State {
        /**
         * コンストラクタ.
         */
        OpenCameraState() {
            super("OpenCamera");
        }

        @Override
        int getState() {
            return STATE_OPEN_CAMERA;
        }

        @Override
        public void enter() throws CameraAccessException {
            if (mCameraDevice == null) {
                if (Camera2WrapperManager.checkCameraPermission(mContext)) {
                    mCameraManager.openCamera(mSettings.getCameraId(), mStateCallback, mBackgroundHandler);
                } else {
                    postOnError(new Camera2WrapperException(Camera2WrapperException.ERROR_CODE_NO_PERMISSION));
                }
            }
        }

        /**
         * カメラデバイスの状態通知を受け取るコールバック.
         */
        private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                mCameraDevice = cameraDevice;
                postOnOpen();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                nextState(mAbortState);
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int error) {
                if (DEBUG) {
                    switch (error) {
                        default:
                            Log.e(TAG, "ERROR_UNKNOWN");
                            break;
                        case ERROR_CAMERA_DEVICE:
                            Log.e(TAG, "ERROR_CAMERA_DEVICE");
                            break;
                        case ERROR_CAMERA_DISABLED:
                            Log.e(TAG, "ERROR_CAMERA_DISABLED");
                            break;
                        case ERROR_CAMERA_IN_USE:
                            Log.e(TAG, "ERROR_CAMERA_IN_USE");
                            break;
                        case ERROR_CAMERA_SERVICE:
                            Log.e(TAG, "ERROR_CAMERA_SERVICE");
                            break;
                        case ERROR_MAX_CAMERAS_IN_USE:
                            Log.e(TAG, "ERROR_MAX_CAMERAS_IN_USE");
                            break;
                    }
                }
                postOnError(new Camera2WrapperException(error));
            }
        };
    }

    /**
     * キャプチャーセッションを作成するクラス.
     */
    private class CreateSessionState extends State {
        private State mNextState;

        CreateSessionState() {
            super("CreateSession");
        }

        void setNextState(State nextState) {
            mNextState = nextState;
        }

        @Override
        int getState() {
            return STATE_CREATE_SESSION;
        }

        @Override
        public void enter() throws CameraAccessException {
            boolean swappedDimensions = isSwappedDimensions();

            int pictureWidth = mSettings.getPictureSize().getWidth();
            int pictureHeight = mSettings.getPictureSize().getHeight();

            int previewWidth;
            int previewHeight;
            if (swappedDimensions) {
                previewWidth = mSettings.getPreviewSize().getHeight();
                previewHeight = mSettings.getPreviewSize().getWidth();
            } else {
                previewWidth = mSettings.getPreviewSize().getWidth();
                previewHeight = mSettings.getPreviewSize().getHeight();
            }

            if (DEBUG) {
                Log.d(TAG, "### sensorOrientation: " + getSensorOrientation());
                switch (Camera2Helper.getDisplayRotation(mContext)) {
                    case Surface.ROTATION_0:
                        Log.d(TAG, "### DisplayRotation: Surface.ROTATION_0");
                        break;
                    case Surface.ROTATION_90:
                        Log.d(TAG, "### DisplayRotation: Surface.ROTATION_90");
                        break;
                    case Surface.ROTATION_180:
                        Log.d(TAG, "### DisplayRotation: Surface.ROTATION_180");
                        break;
                    case Surface.ROTATION_270:
                        Log.d(TAG, "### DisplayRotation: Surface.ROTATION_270");
                        break;
                    default:
                        break;
                }
                Log.d(TAG, "### " + previewWidth + "x" + previewHeight);
            }

            if (mTextureView != null) {
                mTextureView.setAspectRatio(previewWidth, previewHeight);
            }

            List<Surface> outputs = new ArrayList<>(mSurfaces);

            mTargetSurfaces.clear();
            mTargetSurfaces.addAll(mSurfaces);

            if (mSurfaceTexture != null) {
                mSurfaceTexture.setDefaultBufferSize(previewWidth, previewHeight);
                Surface surface = new Surface(mSurfaceTexture);
                mTargetSurfaces.add(surface);
                outputs.add(surface);
            }

            mImageReader = Camera2Helper.createImageReader(pictureWidth, pictureHeight, ImageFormat.JPEG);

            outputs.add(mImageReader.getSurface());

            mCameraDevice.createCaptureSession(outputs, mSessionCallback, mBackgroundHandler);
        }

        /**
         * キャプチャーセッションの設定取得用コールバック.
         */
        private final CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                mCaptureSession = cameraCaptureSession;
                nextState(mNextState);
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                nextState(mAbortState);
                postOnError(new Camera2WrapperException(Camera2WrapperException.ERROR_CODE_FAILED_CREATE_SESSION));
            }
        };
    }

    /**
     * キャプチャーセッションを破棄するクラス.
     */
    private class DestroySessionState extends State {
        DestroySessionState() {
            super("DestroySession");
        }

        @Override
        int getState() {
            return STATE_DESTROY_SESSION;
        }

        @Override
        public void enter() throws CameraAccessException {
            if (mCaptureSession != null) {
                mCaptureSession.stopRepeating();
                mCaptureSession.close();
                mCaptureSession = null;
            }

            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }

            nextState(mOpenCameraState);
        }
    }

    /**
     * プレビューを描画するクラス.
     */
    private class PreviewState extends State {
        /**
         * プレビュー開始フラグ.
         */
        private boolean mStartFlag;

        PreviewState() {
            super("Preview");
        }

        @Override
        int getState() {
            return STATE_PREVIEW;
        }

        @Override
        public void enter() throws CameraAccessException {
            mStartFlag = true;
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            for (Surface surface : mTargetSurfaces) {
                mPreviewRequestBuilder.addTarget(surface);
            }
            chooseStabilizationMode(mPreviewRequestBuilder);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        }

        @Override
        void onCaptureResult(CaptureResult result, boolean isCompleted) {
            // プレビューでは、onCaptureResult が何度も呼び出されるので、ここで弾いています。
            if (mStartFlag) {
                mStartFlag = false;
                postOnStartPreview();
            }
        }
    }

    private class RecordingState extends State {
        private boolean mStartFlag;

        RecordingState() {
            super("Recording");
        }

        @Override
        int getState() {
            return STATE_RECORDING;
        }

        @Override
        public void enter() throws CameraAccessException {
            mStartFlag = true;
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            for (Surface surface : mTargetSurfaces) {
                mPreviewRequestBuilder.addTarget(surface);
            }
            chooseStabilizationMode(mPreviewRequestBuilder);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        }

        @Override
        void onCaptureResult(CaptureResult result, boolean isCompleted) {
            // プレビューでは、onCaptureResult が何度も呼び出されるので、ここで弾いています。
            if (mStartFlag) {
                mStartFlag = false;
                postOnStartPreview();
            }
        }
    }

    /**
     * オートフォーカスを行うクラス.
     */
    private class AutoFocusState extends State {
        /**
         * オートフォーカス開始時間.
         */
        private long mStartTime;

        AutoFocusState() {
            super("AutoFocus");
        }

        @Override
        int getState() {
            return STATE_AUTO_FOCUS;
        }

        @Override
        public void enter() throws CameraAccessException {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            mStartTime = System.currentTimeMillis();
        }

        @Override
        public void onCaptureResult(CaptureResult result, boolean isCompleted) {
            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
            if (DEBUG) {
                Log.d(TAG, "afState: " + Camera2Helper.debugAFState(afState) + " isCompleted: " + isCompleted);
            }
            boolean isAfReady = afState == null
                    || afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                    || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED
                    || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED;
            boolean timeout = (System.currentTimeMillis() - mStartTime) > 5000;
            if (isAfReady || timeout) {
                nextState(mAutoExposureState);
            }
        }
    }

    /**
     * オート露出を行うクラス.
     */
    private class AutoExposureState extends State {
        /**
         * オート露出開始時間.
         */
        private long mStartTime;

        AutoExposureState() {
            super("AutoExposure");
        }

        @Override
        int getState() {
            return STATE_AUTO_EXPOSURE;
        }

        @Override
        public void enter() throws CameraAccessException {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            mStartTime = System.currentTimeMillis();
        }

        @Override
        public void onCaptureResult(CaptureResult result, boolean isCompleted) {
            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
            if (DEBUG) {
                Log.d(TAG, "aeState: " + Camera2Helper.debugAEState(aeState) + " isCompleted: " + isCompleted);
            }
            boolean isAeReady = aeState == null
                    || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                    || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED;
            boolean timeout = (System.currentTimeMillis() - mStartTime) > 5000;
            if (isAeReady || timeout) {
                nextState(mTakePictureState);
            }
        }
    }

    /**
     * 写真撮影を行うクラス.
     */
    private class TakePictureState extends State {
        /**
         * 写真撮影から戻り先のステート.
         */
        private State mReturnState;

        TakePictureState() {
            super("TakePicture");
        }

        /**
         * 戻り先のステートを設定します.
         *
         * @param returnState 戻り先のステート
         */
        void setReturnState(State returnState) {
            mReturnState = returnState;
        }

        @Override
        int getState() {
            return STATE_TAKE_PICTURE;
        }

        @Override
        public void enter() throws CameraAccessException {
            // プレビュー表示中と動画撮影中の場合で設定するテンプレートが異なります
            int template = mReturnState instanceof PreviewState ?
                    CameraDevice.TEMPLATE_STILL_CAPTURE : CameraDevice.TEMPLATE_VIDEO_SNAPSHOT;
            CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(template);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getDisplayOrientation());
            setAutoFlash(captureBuilder);
            chooseStabilizationMode(captureBuilder);
            mImageReader.setOnImageAvailableListener(mTakePictureListener, mBackgroundHandler);

            mCaptureSession.stopRepeating();
            mCaptureSession.capture(captureBuilder.build(), mCaptureCallback, mBackgroundHandler);
        }

        @Override
        public void onCaptureResult(CaptureResult result, boolean isCompleted) {
            if (isCompleted) {
                nextState(mReturnState);
            }
        }

        @Override
        public void exit() throws CameraAccessException {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            mTakePictureListener = null;
        }
    }

    /**
     * カメラデバイスとの接続を打ち切るクラス.
     */
    private class AbortState extends State {
        AbortState() {
            super("Abort");
        }

        @Override
        int getState() {
            return STATE_ABORT;
        }

        @Override
        public void enter() {
            shutdown();
            nextState(null);
        }
    }

    /**
     * カメラデバイスからのキャプチャの状態を受け取るコールバック.
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            onCaptureResult(partialResult, false);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            onCaptureResult(result, true);
        }

        private void onCaptureResult(CaptureResult result, boolean isCompleted) {
            if (mState != null) {
                mState.onCaptureResult(result, isCompleted);
            }
        }
    };

    /**
     * 自動フラッシュの設定を CaptureRequest.Builder に設定します.
     *
     * @param requestBuilder 自動フラッシュ設定を行う CaptureRequest.Builder
     */
    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mSettings.isAutoFlash()) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    /**
     * 手ぶれ補正を設定を CaptureRequest.Builder に設定します.
     *
     * <p>
     * ハードウェアやソフトウェアが対応していない場合には設定を行いません。
     * </p>
     *
     * @param builder 手ぶれ補正を設定を行う CaptureRequest.Builder
     * @throws CameraAccessException カメラの設定の取得に失敗した場合
     */
    private void chooseStabilizationMode(CaptureRequest.Builder builder) throws CameraAccessException {
        CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mSettings.getCameraId());
        int[] availableOpticalStabilization = characteristics.get(
                CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
        if (availableOpticalStabilization != null) {
            for (int mode : availableOpticalStabilization) {
                if (mode == CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON) {
                    builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                            CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
                    builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                            CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
                    return;
                }
            }
        }

        int[] availableVideoStabilization = characteristics.get(
                CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
        if (availableVideoStabilization != null) {
            for (int mode : availableVideoStabilization) {
                if (mode == CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON) {
                    builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                            CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);
                    builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                            CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);
                    return;
                }
            }
        }
    }

    public interface CameraEventListener {
        /**
         * カメラデバイスに接続されたことを通知します.
         */
        void onOpen();

        /**
         * プレビュー開始を通知します.
         */
        void onStartPreview();

        /**
         * プレビュー停止を通知します.
         */
        void onStopPreview();

        /**
         * エラーを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(Camera2WrapperException e);
    }

    /**
     * カメラデバイスの設定.
     *
     * TODO: 必要なパラメータがある場合にはここに設定する
     */
    public class Settings {
        /**
         * カメラID.
         */
        private String mCameraId;

        /**
         * 写真撮影を行うサイズ.
         */
        private Size mPictureSize;

        /**
         * プレビューのサイズ.
         */
        private Size mPreviewSize;

        /**
         * 自動フラッシュ設定.
         */
        private boolean mAutoFlash;

        /**
         * カメラの回転方向.
         */
        private Rotation mRotation = Rotation.FREE;

        /**
         * コンストラクタ.
         * <p>
         * 他でインスタンスが作られないようprivateにしておく。
         * </p>
         */
        private Settings(String cameraId) {
            mCameraId = cameraId;
        }

        /**
         * カメラIDを取得します.
         *
         * @return カメラID
         */
        public String getCameraId() {
            return mCameraId;
        }

        /**
         * 設定されているカメラの向きを取得します.
         *
         * @return カメラの向き
         */
        public int getFacing() {
            try {
                return Camera2Helper.getFacing(mCameraManager, mCameraId);
            } catch (CameraAccessException e) {
                return -1;
            }
        }

        /**
         * カメラの撮影サイズを取得します.
         *
         * @return カメラの撮影サイズ
         */
        public Size getPictureSize() {
            return mPictureSize;
        }

        /**
         * カメラの撮影サイズを設定します.
         *
         * @param pictureSize  カメラの撮影サイズ
         */
        public void setPictureSize(final Size pictureSize) {
            List<Size> sizes = getSupportedPictureSizes();
            for (Size size : sizes) {
                if (size.getWidth() == pictureSize.getWidth() && size.getHeight() == pictureSize.getHeight()) {
                    mPictureSize = pictureSize;
                    return;
                }
            }
            throw new RuntimeException("Not found a match size.");
        }

        /**
         * カメラがサポートしている撮影サイズのリストを取得します.
         *
         * @return カメラがサポートしている撮影サイズのリスト
         */
        @NonNull
        public List<Size> getSupportedPictureSizes() {
            return Camera2Helper.getSupportedPictureSizes(mCameraManager, mCameraId);
        }

        /**
         * カメラのプレビューサイズを取得します.
         *
         * @return カメラのプレビューサイズ
         */
        public Size getPreviewSize() {
            return mPreviewSize;
        }

        /**
         * カメラの撮影サイズを設定します.
         *
         * @param previewSize カメラの撮影サイズ
         */
        public void setPreviewSize(final Size previewSize) {
            List<Size> sizes = getSupportedPreviewSizes();
            for (Size size : sizes) {
                if (size.getWidth() == previewSize.getWidth() && size.getHeight() == previewSize.getHeight()) {
                    mPreviewSize = previewSize;
                    return;
                }
            }
            throw new RuntimeException("Not found a match size.");
        }

        /**
         * カメラがサポートしているプレビューサイズのリストを取得します.
         *
         * @return カメラがサポートしているプレビューサイズのリスト
         */
        @NonNull
        public List<Size> getSupportedPreviewSizes() {
            return Camera2Helper.getSupportedPreviewSizes(mCameraManager, mCameraId);
        }

        /**
         * 自動フラッシュ設定を確認します.
         *
         * @return 自動フラッシュが有効な場合はtrue、それ以外はfalse
         */
        public boolean isAutoFlash() {
            return mAutoFlash;
        }

        /**
         * 自動フラッシュ設定を行います.
         *
         * @param autoFlash 自動フラッシュを有効にする場合はtrue、それ以外はfalse
         */
        public void setAutoFlash(boolean autoFlash) {
            mAutoFlash = autoFlash;
        }

        /**
         * カメラの回転方向を取得します.
         *
         * @return カメラの回転方向
         */
        public Rotation getRotation() {
            return mRotation;
        }

        /**
         * カメラの回転方向を設定します.
         *
         * <p>
         * カメラの映像を指定された方向に回転して撮影を行います。
         * </p>
         *
         * <p>
         * {@link Rotation#FREE} が指定された場合に画面の回転方向で撮影を行います。
         * </p>
         *
         * @param rotation 回転方向
         */
        public void setRotation(Rotation rotation) {
            mRotation = rotation;
        }

        @NonNull
        @Override
        public String toString() {
            return "Settings{" +
                    "mCameraId='" + mCameraId + '\'' +
                    ", mPictureSize=" + mPictureSize +
                    ", mPreviewSize=" + mPreviewSize +
                    '}';
        }
    }
}
