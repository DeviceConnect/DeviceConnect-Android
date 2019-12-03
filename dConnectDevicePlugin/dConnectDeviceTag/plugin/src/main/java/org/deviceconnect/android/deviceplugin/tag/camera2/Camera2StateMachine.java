/*
 Camera2StateMachine.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.camera2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.tag.BuildConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Camera2 API でカメラデバイスを制御するためのクラス.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2StateMachine {
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
    public static final int STATE_NOT_OPEN = 0;

    /**
     * カメラデバイスの画像を表示する Surface を初期化している状態.
     */
    public static final int STATE_INIT_SURFACE = 1;

    /**
     * カメラデバイスと接続している状態.
     */
    public static final int STATE_OPEN_CAMERA = 2;

    /**
     * カメラデバイスとのキャプチャセッションを作成している状態.
     */
    public static final int STATE_CREATE_SESSION = 3;

    /**
     * カメラデバイスとのキャプチャセッションを破棄している状態.
     */
    public static final int STATE_DESTROY_SESSION = 4;

    /**
     * カメラデバイスのプレビューを開始している状態.
     */
    public static final int STATE_PREVIEW = 5;

    /**
     * カメラデバイスのオートフォーカスを行なっている状態.
     */
    public static final int STATE_AUTO_FOCUS = 6;

    /**
     * カメラデバイスのオート露出を行なっている状態.
     */
    public static final int STATE_AUTO_EXPOSURE = 7;

    /**
     * カメラデバイスの写真撮影を行なっている状態.
     */
    public static final int STATE_TAKE_PICTURE = 8;

    /**
     * カメラデバイスと切断している状態.
     */
    public static final int STATE_ABORT = 9;

    /**
     * パーミッションに許可が無い場合のエラーコード.
     */
    public static final int ERROR_CODE_NO_PERMISSION = 1;

    /**
     * セッションの作成に失敗した場合のエラーコード.
     */
    public static final int ERROR_CODE_FAILED_CREATE_SESSION = 2;

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
     * 撮影結果を読み込むクラス.
     */
    private ImageReader mPreviewImageReader;

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
     * SurfaceTexture をもつ Surface.
     */
    private Surface mSurface;

    /**
     * ハンドラー.
     */
    private Handler mHandler;

    /**
     * 撮影結果を通知するリスナー.
     */
    private ImageReader.OnImageAvailableListener mTakePictureListener;

    /**
     * プレビュー用のリスナー.
     */
    private ImageReader.OnImageAvailableListener mPreviewListener;

    /**
     * カメラデバイスへの設定.
     */
    private Settings mSettings = new Settings();

    /**
     * カメラデバイスの状態.
     */
    private State mState;

    /**
     * エラー通知用コールバック.
     */
    private ErrorCallback mErrorCallback;

    private final State mInitSurfaceState = new InitSurfaceState();
    private final State mOpenCameraState = new OpenCameraState();
    private final State mCreateSessionState = new CreateSessionState();
    private final State mDestroySessionState = new DestroySessionState();
    private final State mPreviewState = new PreviewState();
    private final State mAutoFocusState = new AutoFocusState();
    private final State mAutoExposureState = new AutoExposureState();
    private final State mTakePictureState = new TakePictureState();
    private final State mAbortState = new AbortState();

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public Camera2StateMachine(@NonNull Context context) {
        mContext = context;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (mCameraManager == null) {
            throw new RuntimeException("Not supported a window Manager.");
        }
        initSettings();
    }

    /**
     * カメラのパーミッショに許可が下りているか確認します.
     * <p>
     * 端末の SDK レベルが 23 未満の場合には、パーミッションが不要なので常にtrueを返却します。
     * </p>
     * @param context コンテキスト
     * @return カメラのパーミッションに許可が下りている場合はtrue、それ以外はfalse
     */
    public static boolean checkCameraPermission(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = context.checkSelfPermission(Manifest.permission.CAMERA);
            return (permissionCheck == PackageManager.PERMISSION_GRANTED);
        } else {
            return true;
        }
    }

    /**
     * カメラのパーミッションを要求します.
     *
     * @param activity リクエストレスポンスを受け取るActivity
     * @param requestCode リクエストコード
     */
    public static void requestCameraPermission(final Activity activity, final int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.CAMERA
            }, requestCode);
        }
    }

    /**
     * エラーコールバックを設定します.
     *
     * @param errorCallback エラーコールバック
     */
    public void setErrorCallback(ErrorCallback errorCallback) {
        mErrorCallback = errorCallback;
    }

    /**
     * カメラのプレビュー表示を行う{@link AutoFitTextureView} を設定します.
     * <p>
     * {@link #setSurfaceTexture(SurfaceTexture)} が同時に設定されていた場合にも、
     * この関数で設定した{@link AutoFitTextureView} に優先してカメラのプレビューを描画します。
     * </p>
     * @param textureView カメラのプレビューを表示するView
     */
    public void setTextureView(AutoFitTextureView textureView) {
        mTextureView = textureView;
    }

    /**
     * カメラのプレビュー表示を行う {@link SurfaceTexture} を設定します.
     *
     * @param surfaceTexture カメラのプレビュー描画を行うSurfaceTexture
     */
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
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
        return startPreview(null);
    }

    /**
     * カメラデバイスのプレビューを開始します.
     * <p>
     * {@link #setTextureView(AutoFitTextureView)} もしくは、{@link #setTextureView(AutoFitTextureView)}
     * のどちらに設定してから呼び出してください。
     * </p>
     *
     * @return プレビューの開始に成功した場合はtrue、それ以外はfalse
     */
    public boolean startPreview(final ImageReader.OnImageAvailableListener previewListener) {
        if (mState == null) {
            mPreviewListener = previewListener;
            ((OpenCameraState) mOpenCameraState).mOpenCallback = () -> nextState(mCreateSessionState);
            if (mTextureView != null) {
                nextState(mInitSurfaceState);
                return true;
            } else if (mSurfaceTexture != null) {
                nextState(mOpenCameraState);
                return true;
            }
        } else if (mState == mOpenCameraState) {
            mPreviewListener = previewListener;
            nextState(mCreateSessionState);
            return true;
        }
        return false;
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
    private void initSettings() {
        try {
            String cameraId = Camera2Helper.getCameraId(mCameraManager, CameraCharacteristics.LENS_FACING_BACK);
            if (cameraId == null) {
                cameraId = Camera2Helper.getCameraId(mCameraManager, CameraCharacteristics.LENS_FACING_FRONT);
            }
            mSettings.setCameraId(cameraId);
        } catch (Exception e) {
            throw new RuntimeException("Not support.");
        }

        List<Size> pictureSizes = mSettings.getSupportedPictureSizes();
        if (!pictureSizes.isEmpty()) {
            mSettings.setPictureSize(pictureSizes.get(0));
        }

        List<Size> previewSizes = mSettings.getSupportedPreviewSizes();
        if (!previewSizes.isEmpty()) {
            mSettings.setPreviewSize(previewSizes.get(previewSizes.size() - 2));
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
    }

    /**
     * 指定された状態に移行します.
     *
     * @param nextState 移行先の状態
     */
    private void nextState(final State nextState) {
        try {
            if (mState != null) {
                if (DEBUG) {
                    Log.d(TAG, "    Exit: " + mState);
                }
                mState.exit();
            }

            mState = nextState;

            if (mState != null) {
                if (DEBUG) {
                    Log.d(TAG, "    Enter: " + mState);
                }
                mState.enter();
            }
        } catch (CameraAccessException e) {
            shutdown();
        }
    }

    /**
     * カメラの取り付けられた向きを取得します.
     *
     * @return カメラの取り付けられた向き
     */
    private int getSensorOrientation() {
        return Camera2Helper.getSensorOrientation(mCameraManager, mSettings.getCameraId());
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
     * @param errorCode エラーコード
     */
    private void postError(final int errorCode) {
        if (mErrorCallback != null) {
            mErrorCallback.onError(errorCode);
        }
        nextState(mAbortState);
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
         * カメラデバイスを接続したことを通知するコールバック.
         */
        private OpenCallback mOpenCallback;

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
                if (checkCameraPermission(mContext)) {
                    mCameraManager.openCamera(mSettings.getCameraId(), mStateCallback, mHandler);
                } else {
                    postError(ERROR_CODE_NO_PERMISSION);
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
                if (mOpenCallback != null) {
                    mOpenCallback.onOpen();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                nextState(mAbortState);
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int error) {
                postError(error);
            }
        };
    }


    /**
     * キャプチャーセッションを作成するクラス.
     */
    private class CreateSessionState extends State {
        CreateSessionState() {
            super("CreateSession");
        }

        @Override
        int getState() {
            return STATE_CREATE_SESSION;
        }

        @Override
        public void enter() throws CameraAccessException {
            boolean swappedDimensions = false;
            int sensorOrientation = getSensorOrientation();
            switch (Camera2Helper.getDisplayRotation(mContext)) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (sensorOrientation == 90 || sensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (sensorOrientation == 0 || sensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    if (DEBUG) {
                        Log.w(TAG, "Display rotation is invalid.");
                    }
                    break;
            }

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

            if (mTextureView != null) {
                mTextureView.setAspectRatio(previewWidth, previewHeight);
            }

            mSurfaceTexture.setDefaultBufferSize(previewWidth, previewHeight);
            mSurface = new Surface(mSurfaceTexture);

            mPreviewImageReader = Camera2Helper.createImageReader(previewWidth, previewHeight, ImageFormat.JPEG);
            mPreviewImageReader.setOnImageAvailableListener(mPreviewListener, mHandler);

            mImageReader = Camera2Helper.createImageReader(pictureWidth, pictureHeight, ImageFormat.YUV_420_888);

            ArrayList<Surface> outputs = new ArrayList<>();
            outputs.add(mSurface);
            outputs.add(mPreviewImageReader.getSurface());
            outputs.add(mImageReader.getSurface());

            mCameraDevice.createCaptureSession(outputs, mSessionCallback, mHandler);
        }

        /**
         * キャプチャーセッションの設定取得用コールバック.
         */
        private final CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                mCaptureSession = cameraCaptureSession;
                nextState(mPreviewState);
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                postError(ERROR_CODE_FAILED_CREATE_SESSION);
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
        public void enter() {
            if (mCaptureSession != null) {
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
        PreviewState() {
            super("Preview");
        }

        @Override
        int getState() {
            return STATE_PREVIEW;
        }

        @Override
        public void enter() throws CameraAccessException {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mSurface);
            mPreviewRequestBuilder.addTarget(mPreviewImageReader.getSurface());
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mHandler);
        }
    }

    /**
     * オートフォーカスを行うクラス.
     */
    private class AutoFocusState extends State {
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
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mHandler);
        }

        @Override
        public void onCaptureResult(CaptureResult result, boolean isCompleted) {
            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
            boolean isAfReady = afState == null
                    || afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                    || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED;
            if (isAfReady) {
                nextState(mAutoExposureState);
            }
        }
    }

    /**
     * オート露出を行うクラス.
     */
    private class AutoExposureState extends State {
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
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mHandler);
        }

        @Override
        public void onCaptureResult(CaptureResult result, boolean isCompleted) {
            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
            boolean isAeReady = aeState == null
                    || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                    || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED;
            if (isAeReady) {
                nextState(mTakePictureState);
            }
        }
    }

    /**
     * 写真撮影を行うクラス.
     */
    private class TakePictureState extends State {
        TakePictureState() {
            super("TakePicture");
        }

        @Override
        int getState() {
            return STATE_TAKE_PICTURE;
        }

        @Override
        public void enter() throws CameraAccessException {
            CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(Camera2Helper.getDisplayRotation(mContext)));
            mImageReader.setOnImageAvailableListener(mTakePictureListener, mHandler);

            mCaptureSession.stopRepeating();
            mCaptureSession.capture(captureBuilder.build(), mCaptureCallback, mHandler);
        }

        @Override
        public void onCaptureResult(CaptureResult result, boolean isCompleted) {
            if (isCompleted) {
                nextState(mPreviewState);
            }
        }

        @Override
        public void exit() throws CameraAccessException {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mHandler);
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

    public interface OpenCallback {
        void onOpen();
    }

    /**
     * エラー通知用のコールバック.
     */
    public interface ErrorCallback {
        /**
         * エラーを通知します.
         *
         * @param errorCode エラーコード
         */
        void onError(int errorCode);
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
         * コンストラクタ.
         * <p>
         * 他でインスタンスが作られないようprivateにしておく。
         * </p>
         */
        private Settings() {}

        /**
         * カメラIDを取得します.
         *
         * @return カメラID
         */
        public String getCameraId() {
            return mCameraId;
        }

        /**
         * カメラIDを設定します.
         *
         * @param cameraId カメラID
         */
        public void setCameraId(final String cameraId) {
            mCameraId = cameraId;
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
         * カメラがサポートしているプレビューサイズから指定のサイズに近い値を返却します.
         *
         * @param width 横幅
         * @param height 縦幅
         * @return 近い値
         */
        public Size getApproximatePreviewSize(final int width, final int height) {
            List<Size> sizes = getSupportedPreviewSizes();
            if (sizes.isEmpty()) {
                return null;
            }

            Size currentSize = sizes.get(0);
            int value = Integer.MAX_VALUE;
            for (Size size : sizes) {
                int dw = width - size.getWidth();
                int dh = height - size.getHeight();
                int dist = dw * dw + dh * dh;
                if (dist < value) {
                    currentSize = size;
                    value = dist;
                }
            }
            return currentSize;
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
