/*
 CameraOverlay.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;
import org.deviceconnect.android.provider.FileManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * カメラのプレビューをオーバーレイで表示するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("deprecation")
public class CameraOverlay implements Camera.PreviewCallback, Camera.ErrorCallback {
    /**
     * JPEGの圧縮クオリティを定義.
     */
    private static final int JPEG_COMPRESS_QUALITY = 100;

    /** ファイル名に付けるプレフィックス. */
    private static final String FILENAME_PREFIX = "android_camera_";

    /** ファイルの拡張子. */
    private static final String FILE_EXTENSION = ".jpg";

    /** Default Maximum Frame Rate. */
    private static final double DEFAULT_MAX_FPS = 10.0d;

    /** 日付のフォーマット. */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    /** コンテキスト. */
    private final Context mContext;

    /** ウィンドウ管理クラス. */
    private final WindowManager mWinMgr;

    /** 作業用スレッド。 */
    private final HandlerThread mWorkerThread;

    /** ハンドラ. */
    private final Handler mHandler;

    /** Camera ID. */
    private final int mCameraId;

    /** ファイル管理クラス. */
    private FileManager mFileMgr;

    /** プレビュー画面. */
    private Preview mPreview;

    /** 使用するカメラのインスタンス. */
    private Camera mCamera;

    /**
     * カメラの操作をブロックするロックオブジェクト.
     */
    private final Object mCameraLock = new Object();

    /**
     * 画像を送るサーバ.
     */
    private MixedReplaceMediaServer mServer;

    /**
     * プレビューサイズ.
     */
    private HostDeviceRecorder.PictureSize mPreviewSize;

    /**
     * 写真サイズ.
     */
    private HostDeviceRecorder.PictureSize mPictureSize;

    /**
     * 最後のフレームを取得した時間.
     */
    private long mLastFrameTime;

    /**
     * インターバル.
     */
    private long mFrameInterval;

    /**
     * プレビューのFPS.
     */
    private double mMaxFps;

    /**
     * JPEGのクォリティ.
     */
    private int mJpegQuality = JPEG_COMPRESS_QUALITY;

    /**
     * カメラの向き.
     */
    private int mFacingDirection = 1;

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("host.dplugin");

    /**
     * プレビュー表示モード.
     */
    private boolean mPreviewMode;

    /**
     * カメラパラメーター.
     */
    Camera.Parameters mParams;

    /**
     * フラッシュライト使用中フラグ.
     */
    private boolean mUseFlashLight = false;

    /**
     * フラッシュライト状態.
     */
    private boolean mFlashLightState = false;

    /**
     * 画面回転のイベントを受け付けるレシーバー.
     */
    private BroadcastReceiver mOrientReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
                updatePosition(mPreview);
            }
        }
    };

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param cameraId Camera ID.
     */
    public CameraOverlay(final Context context, final int cameraId) {
        mContext = context;
        mWinMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWorkerThread = new HandlerThread(getClass().getSimpleName());
        mWorkerThread.start();
        mHandler = new Handler(mWorkerThread.getLooper());
        mCameraId = cameraId;

        mMaxFps = DEFAULT_MAX_FPS;
        setPreviewFrameRate(mMaxFps);
    }

    @Override
    protected void finalize() throws Throwable {
        mWorkerThread.quit();
        super.finalize();
    }

    /**
     * プレビューモードを設定します.
     *
     * @param flag trueの場合はプレビューモードをON、それ以外はプレビューモードをOFF
     */
    public void setPreviewMode(final boolean flag) {
        mPreviewMode = flag;
    }

    public void setFacingDirection(final int dir) {
        mFacingDirection = dir;
    }

    public HostDeviceRecorder.PictureSize getPictureSize() {
        return mPictureSize;
    }

    public void setPictureSize(final HostDeviceRecorder.PictureSize size) {
        mPictureSize = size;
    }

    public HostDeviceRecorder.PictureSize getPreviewSize() {
        return mPreviewSize;
    }

    public void setPreviewSize(final HostDeviceRecorder.PictureSize size) {
        mPreviewSize = size;
    }

    public void setPreviewFrameRate(final double max) {
        mMaxFps = max;
        mFrameInterval = (long) (1 / max) * 1000L;
    }

    public double getPreviewMaxFrameRate() {
        return mMaxFps;
    }

    public void setJpegQuality(final int jpegQuality) {
        mJpegQuality = jpegQuality;
    }

    /**
     * MixedReplaceMediaServerを設定する.
     *
     * @param server サーバのインスタンス
     */
    public void setServer(final MixedReplaceMediaServer server) {
        mServer = server;
    }

    /**
     * FileManagerを設定する.
     *
     * @param mgr FileManagerのインスタンス
     */
    public void setFileManager(final FileManager mgr) {
        mFileMgr = mgr;
    }

    /**
     * カメラのオーバーレイが表示されているかを確認する.
     *
     * @return 表示されている場合はtrue、それ以外はfalse
     */
    public synchronized boolean isShow() {
        return mPreview != null;
    }

    /**
     * Overlayを表示する.
     * @param callback Overlayの表示結果を通知するコールバック
     */
    public void show(final Callback callback) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    CapabilityUtil.checkCapability(mContext, mHandler, new CapabilityUtil.Callback() {
                        @Override
                        public void onSuccess() {
                            try {
                                showInternal(callback);
                            } catch (IOException e) {
                                if (BuildConfig.DEBUG) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onFail() {
                            callback.onFail();
                        }
                    });
                } else {
                    try {
                        showInternal(callback);
                    } catch (IOException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void showInternal(final Callback callback) throws IOException {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mPreview = new Preview(mContext);

                    Point size = getDisplaySize();
                    int pt = (int) (5 * getScaledDensity());
                    WindowManager.LayoutParams l = new WindowManager.LayoutParams(pt, pt,
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
                    l.x = -size.x / 2;
                    l.y = -size.y / 2;
                    mWinMgr.addView(mPreview, l);

                    if (mCamera == null) {
                        mCamera = Camera.open(mCameraId);
                        if (mCamera == null) {
                            throw new IOException("Failure to open the camera.");
                        }
                    }
                    setCameraParameter(mCamera);
                    mPreview.switchCamera(mCameraId, mCamera);
                    mCamera.setPreviewCallback(CameraOverlay.this);
                    mCamera.setErrorCallback(CameraOverlay.this);

                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
                    mContext.registerReceiver(mOrientReceiver, filter);

                    callback.onSuccess();
                } catch (Throwable t) {
                    if (BuildConfig.DEBUG) {
                        Log.w("Overlay", "", t);
                    }
                    callback.onFail();
                }
            }
        });
    }

    private void setCameraParameter(final Camera camera) {
        if (camera != null) {
            Camera.Parameters params = camera.getParameters();
            params.setPictureSize(mPictureSize.getWidth(), mPictureSize.getHeight());
            params.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            params.setPreviewFrameRate((int) mMaxFps);
            try {
                camera.setParameters(params);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Overlayを非表示にする.
     */
    public void hide() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mCameraLock) {
                        mPreviewMode = false;

                        if (mCamera != null) {
                            mPreview.setCamera(0, null);
                            mCamera.stopPreview();
                            mCamera.setPreviewCallback(null);
                            if (!mFlashLightState) {
                                mCamera.release();
                                mParams = null;
                                mCamera = null;
                            }
                        }

                        if (mPreview != null) {
                            mWinMgr.removeView(mPreview);
                            mPreview = null;
                        }

                        mContext.unregisterReceiver(mOrientReceiver);
                    }
                } catch (Throwable t) {
                    if (BuildConfig.DEBUG) {
                        Log.w("Overlay", "", t);
                    }
                }
            }
        });
    }

    /**
     * 写真撮影を行う.
     * <p>
     * 写真撮影の結果はlistenerに通知される。
     * </p>
     * @param listener 撮影結果を通知するリスナー
     */
    public void takePicture(final OnTakePhotoListener listener) {
        if (isShow()) {
            takePictureInternal(listener);
        } else {
            show(new CameraOverlay.Callback() {
                @Override
                public void onSuccess() {
                    takePictureInternal(listener);
                }
                @Override
                public void onFail() {
                    listener.onFailedTakePhoto("Permission for overlay view is not granted.");
                }
            });
        }
    }

    /**
     * 撮影後の後始末を行います.
     */
    private void cleanup() {
        synchronized (CameraOverlay.this) {
            if (!mPreviewMode) {
                hide();
            } else if (mCamera != null) {
                mCamera.startPreview();
            }
        }
    }

    /**
     * 写真撮影を行う内部メソッド.
     *
     * @param listener 撮影結果を通知するリスナー
     */
    private void takePictureInternal(final OnTakePhotoListener listener) {
        synchronized (mCameraLock) {
            if (mPreview == null || mCamera == null) {
                if (listener != null) {
                    listener.onFailedTakePhoto("Failed to open camera.");
                }
                return;
            }
            mPreview.takePicture(new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(final byte[] data, final Camera camera) {
                    if (data == null) {
                        listener.onFailedTakePhoto("Failed to take picture.");
                        cleanup();
                        return;
                    }
                    try {
                        Bitmap original = BitmapFactory.decodeByteArray(data, 0, data.length);
                        int degrees = Preview.getCameraDisplayOrientation(mContext, mCameraId);
                        Bitmap rotated;
                        if (degrees == 0) {
                            rotated = original;
                        } else {
                            Matrix m = new Matrix();
                            m.setRotate(degrees * mFacingDirection);
                            rotated = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), m, true);
                            original.recycle();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        rotated.compress(CompressFormat.JPEG, mJpegQuality, baos);
                        byte[] jpeg = baos.toByteArray();
                        rotated.recycle();

                        // 常に違うファイル名になるためforceOverwriteはtrue
                        mFileMgr.saveFile(createNewFileName(), jpeg, true, new FileManager.SaveFileCallback() {
                            @Override
                            public void onSuccess(@NonNull final String uri) {
                                String filePath = mFileMgr.getBasePath().getAbsolutePath() + "/" + uri;
                                if (listener != null) {
                                    listener.onTakenPhoto(uri, filePath);
                                }
                                cleanup();
                            }

                            @Override
                            public void onFail(@NonNull final Throwable throwable) {
                                if (listener != null) {
                                    listener.onFailedTakePhoto(throwable.getMessage());
                                }
                                cleanup();
                            }
                        });
                    } catch (OutOfMemoryError e) {
                        listener.onFailedTakePhoto("Too large picture size.");
                        cleanup();
                    }
                }
            });
        }
    }

    /**
     * Displayの密度を取得する.
     *
     * @return 密度
     */
    private float getScaledDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        mWinMgr.getDefaultDisplay().getMetrics(metrics);
        return metrics.scaledDensity;
    }

    /**
     * Displayのサイズを取得する.
     *
     * @return サイズ
     */
    private Point getDisplaySize() {
        Display disp = mWinMgr.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        return size;
    }

    /**
     * Viewの座標を画面の左上に移動する.
     *
     * @param view 座標を移動するView
     */
    private void updatePosition(final View view) {
        if (view == null) {
            return;
        }
        Point size = getDisplaySize();
        final WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
        lp.x = -size.x / 2;
        lp.y = -size.y / 2;
        view.post(new Runnable() {
            @Override
            public void run() {
                mWinMgr.updateViewLayout(view, lp);
            }
        });
    }

    /**
     * 新規のファイル名を作成する.
     *
     * @return ファイル名
     */
    private String createNewFileName() {
        return FILENAME_PREFIX + mSimpleDateFormat.format(new Date()) + FILE_EXTENSION;
    }

    @Override
    public void onError(final int error, final Camera camera) {
        if (BuildConfig.DEBUG) {
            Log.w("Overlay", "onError: " + error);
        }
        hide();
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        synchronized (mCameraLock) {
            final long currentTime = System.currentTimeMillis();
            if (mLastFrameTime != 0) {
                if ((currentTime - mLastFrameTime) < mFrameInterval) {
                    mLastFrameTime = currentTime;
                    return;
                }
            }

            if (mCamera != null && mCamera.equals(camera)) {
                mCamera.setPreviewCallback(null);

                if (mServer != null && mPreview != null) {
                    int format = mPreview.getPreviewFormat();
                    int width = mPreview.getPreviewWidth();
                    int height = mPreview.getPreviewHeight();

                    YuvImage yuvimage = new YuvImage(data, format, width, height, null);
                    Rect rect = new Rect(0, 0, width, height);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (yuvimage.compressToJpeg(rect, mJpegQuality, baos)) {
                        byte[] jdata = baos.toByteArray();

                        int degree = Preview.getCameraDisplayOrientation(mContext, mCameraId);
                        if (degree == 0) {
                            mServer.offerMedia(jdata);
                        } else {
                            try {
                                BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
                                bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                                Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFactoryOptions);
                                if (bmp != null) {
                                    Matrix m = new Matrix();
                                    m.setRotate(degree * mFacingDirection);

                                    Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
                                    if (rotatedBmp != null) {
                                        baos.reset();
                                        if (rotatedBmp.compress(CompressFormat.JPEG, mJpegQuality, baos)) {
                                            mServer.offerMedia(baos.toByteArray());
                                        }
                                        rotatedBmp.recycle();
                                    }
                                    bmp.recycle();
                                }
                            } catch (OutOfMemoryError e) {
                                mServer.stop();
                                return;
                            }
                        }
                    }
                }

                mCamera.setPreviewCallback(this);
            }

            mLastFrameTime = currentTime;
        }
    }

    /**
     * 写真撮影結果を通知するリスナー.
     */
    public interface OnTakePhotoListener {
        /**
         * 写真撮影を行った画像へのURIを通知する.
         *
         * @param uri URI
         * @param filePath file path.
         */
        void onTakenPhoto(String uri, String filePath);

        /**
         * 写真撮影に失敗したことを通知する.
         * @param errorMessage DeviceConnect エラーメッセージ
         */
        void onFailedTakePhoto(String errorMessage);
    }

    /**
     * Overlayの表示結果を通知するコールバック.
     */
    public interface Callback {
        /**
         * 表示できたことを通知します.
         */
        void onSuccess();

        /**
         * 表示できなかったことを通知します.
         */
        void onFail();
    }

    /**
     * フラッシュライトの仕様状態を取得する.
     * @return 使用中はtrue、それ以外はfalse
     */
    public synchronized boolean isUseFlashLight() {
        return mUseFlashLight;
    }

    /**
     * フラッシュライトの状態を取得する.
     * @return 点灯中はtrue、それ以外はfalse
     */
    public synchronized boolean isFlashLightState() {
        return mFlashLightState;
    }

    /**
     * フラッシュライト点灯.
     */
    public synchronized void turnOnFlashLight() {
        if (!isShow() && mCamera == null) {
            mCamera = Camera.open();
            if (mCamera == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SurfaceTexture preview = new SurfaceTexture(0);
                try {
                    mCamera.setPreviewTexture(preview);
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
            mCamera.startPreview();
        }

        if (mCamera != null && !isFlashLightState()) {
            Parameters p = mCamera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(p);
        }
        mFlashLightState = true;
        mUseFlashLight = true;
    }

    /**
     * フラッシュライト消灯.
     */
    public synchronized void turnOffFlashLight() {
        if (mCamera != null && isFlashLightState()) {
            Parameters p = mCamera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(p);

            if (!isShow()) {
                mCamera.stopPreview();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        mCamera.setPreviewTexture(null);
                    } catch (IOException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
                mCamera.release();
                mParams = null;
                mCamera = null;
            }
        }
        mFlashLightState = false;
        mUseFlashLight = false;
    }

}
