/*
 CameraOverlay.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import org.deviceconnect.android.activity.IntentHandlerActivity;
import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.provider.FileManager;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * カメラのプレビューをオーバーレイで表示するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("deprecation")
public class CameraOverlay implements Camera.PreviewCallback, Camera.ErrorCallback {
    /**
     * オーバーレイ削除用アクションを定義.
     */
    public static final String DELETE_PREVIEW_ACTION = "org.deviceconnect.android.deviceplugin.host.DELETE_PREVIEW";

    /**
     * NotificationIDを定義.
     */
    private static final int NOTIFICATION_ID = 1010;

    /**
     * 写真を取るまでの待機時間を定義.
     */
    private static final int PERIOD_TAKE_PHOTO_WAIT = 1500;

    /**
     * JPEGの圧縮クオリティを定義.
     */
    private static final int JPEG_COMPRESS_QUALITY = 100;

    /** ファイル名に付けるプレフィックス. */
    private static final String FILENAME_PREFIX = "android_camera_";

    /** ファイルの拡張子. */
    private static final String FILE_EXTENSION = ".png";

    /** 日付のフォーマット. */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    /** コンテキスト. */
    private Context mContext;

    /** ウィンドウ管理クラス. */
    private WindowManager mWinMgr;

    /** 作業用スレッド。 */
    private HandlerThread mWorkerThread;

    /** ハンドラ. */
    private Handler mHandler;

    /** ファイル管理クラス. */
    private FileManager mFileMgr;

    /** プレビュー画面. */
    private Preview mPreview;

    /** 使用するカメラのインスタンス. */
    private Camera mCamera;

    private final Object mCameraLock = new Object();

    /** 画像を送るサーバ. */
    private MixedReplaceMediaServer mServer;

    /**
     * 終了フラグ.
     * <p/>
     * 撮影が終わった後にOverlayを終了するかチェックする
     */
    private boolean mFinishFlag;

    /**
     * 画面回転のイベントを受け付けるレシーバー.
     */
    private BroadcastReceiver mOrientReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (mPreview == null) {
                return;
            }
            String action = intent.getAction();
            if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                updatePosition(mPreview);
            }
        }
    };

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public CameraOverlay(final Context context) {
        mContext = context;
        mWinMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWorkerThread = new HandlerThread(getClass().getSimpleName());
        mWorkerThread.start();
        mHandler = new Handler(mWorkerThread.getLooper());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mWorkerThread.quit();
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
    public void show(@NonNull final Callback callback) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        final ResultReceiver cameraCapabilityCallback = new ResultReceiver(mHandler) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                try {
                                    if (resultCode == Activity.RESULT_OK) {
                                        showInternal(callback);
                                    } else {
                                        callback.onFail();
                                    }
                                } catch (Throwable throwable) {
                                    callback.onFail();
                                }
                            }
                        };
                        final ResultReceiver overlayDrawingCapabilityCallback = new ResultReceiver(mHandler) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                try {
                                    if (resultCode == Activity.RESULT_OK) {
                                        checkCameraCapability(cameraCapabilityCallback);
                                    } else {
                                        callback.onFail();
                                    }
                                } catch (Throwable throwable) {
                                    callback.onFail();
                                }
                            }
                        };

                        checkOverlayDrawingCapability(overlayDrawingCapabilityCallback);
                    } else {
                        showInternal(callback);
                    }
                } catch (final Throwable throwable) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            throw throwable;
                        }
                    });
                }
            }
        });
    }

    private synchronized void showInternal(@NonNull final Callback callback) {
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

                    mCamera = Camera.open();
                    mPreview.switchCamera(mCamera);
                    mCamera.setPreviewCallback(CameraOverlay.this);
                    mCamera.setErrorCallback(CameraOverlay.this);

                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
                    mContext.registerReceiver(mOrientReceiver, filter);

                    sendNotification();

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

    /**
     * オーバーレイ表示のパーミッションを確認します.
     * @param resultReceiver 確認を受けるレシーバ
     */
    @TargetApi(23)
    private void checkOverlayDrawingCapability(@NonNull final ResultReceiver resultReceiver) {
        if (Settings.canDrawOverlays(mContext)) {
            resultReceiver.send(Activity.RESULT_OK, null);
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + mContext.getPackageName()));
            IntentHandlerActivity.startActivityForResult(mContext, intent, new ResultReceiver(mHandler) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (Settings.canDrawOverlays(mContext)) {
                        resultReceiver.send(Activity.RESULT_OK, null);
                    } else {
                        resultReceiver.send(Activity.RESULT_CANCELED, null);
                    }
                }
            });
        }
    }

    /**
     * カメラのパーミッションを確認します.
     * @param resultReceiver 確認を受けるレシーバ
     */
    private void checkCameraCapability(@NonNull final ResultReceiver resultReceiver) {
        PermissionUtility.requestPermissions(mContext, new Handler(), new String[]{Manifest.permission.CAMERA},
                new PermissionUtility.PermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        resultReceiver.send(Activity.RESULT_OK, null);
                    }
                    @Override
                    public void onFail(@NonNull String deniedPermission) {
                        resultReceiver.send(Activity.RESULT_CANCELED, null);
                    }
                });
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
                        mFinishFlag = false;

                        hideNotification();

                        if (mCamera != null) {
                            mPreview.setCamera(null);
                            mCamera.stopPreview();
                            mCamera.setPreviewCallback(null);
                            mCamera.release();
                            mCamera = null;
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
     * Notificationを削除する.
     */
    private void hideNotification() {
        NotificationManager manager = (NotificationManager) mContext
                .getSystemService(Service.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    /**
     * Notificationを送信する.
     */
    private void sendNotification() {
        PendingIntent contentIntent = createPendingIntent();
        Notification notification = createNotification(contentIntent);
        notification.flags = Notification.FLAG_NO_CLEAR;
        NotificationManager manager = (NotificationManager) mContext
                .getSystemService(Service.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
        manager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Notificationを作成する.
     * @param pendingIntent Notificationがクリックされたときに起動するIntent
     * @return Notification
     */
    private Notification createNotification(final PendingIntent pendingIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext.getApplicationContext());
        builder.setContentIntent(pendingIntent);
        builder.setTicker(mContext.getString(R.string.overlay_preview_ticker));
        builder.setSmallIcon(R.drawable.dconnect_icon);
        builder.setContentTitle(mContext.getString(R.string.overlay_preview_content_title));
        builder.setContentText(mContext.getString(R.string.overlay_preview_content_message));
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(false);
        return builder.build();
    }

    /**
     * PendingIntentを作成する.
     * @return PendingIntent
     */
    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(mContext, HostDeviceService.class);
        intent.setAction(DELETE_PREVIEW_ACTION);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 写真撮影後にOverlayを非表示にするフラグを設定する.
     *
     * @param flag trueの場合は撮影後に非表示にする
     */
    public synchronized void setFinishFlag(final boolean flag) {
        mFinishFlag = flag;
    }

    /**
     * 写真撮影を行う.
     * <p>
     * 写真撮影の結果はlistenerに通知される。
     * </p>
     * @param listener 撮影結果を通知するリスナー
     */
    public void takePicture(final OnTakePhotoListener listener) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                takePictureInternal(listener);
            }
        }, PERIOD_TAKE_PHOTO_WAIT);
    }

    /**
     * 撮影後の後始末を行います.
     */
    private void common() {
        synchronized (CameraOverlay.this) {
            if (mFinishFlag) {
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
                    listener.onFailedTakePhoto();
                }
                return;
            }
            mPreview.takePicture(new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(final byte[] data, final Camera camera) {
                    if (data == null) {
                        listener.onFailedTakePhoto();
                        common();
                        return;
                    }

                    mFileMgr.saveFile(createNewFileName(), data, new FileManager.SaveFileCallback() {
                        @Override
                        public void onSuccess(@NonNull final String uri) {
                            String filePath = mFileMgr.getBasePath().getAbsolutePath() + "/" + uri;
                            if (listener != null) {
                                listener.onTakenPhoto(uri, filePath);
                            }
                            common();
                        }

                        @Override
                        public void onFail(@NonNull final Throwable throwable) {
                            if (listener != null) {
                                listener.onFailedTakePhoto();
                            }
                            common();
                        }
                    });
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
            if (mCamera != null && mCamera.equals(camera)) {
                mCamera.setPreviewCallback(null);

                if (mServer != null) {
                    int format = mPreview.getPreviewFormat();
                    int width = mPreview.getPreviewWidth();
                    int height = mPreview.getPreviewHeight();

                    YuvImage yuvimage = new YuvImage(data, format, width, height, null);
                    Rect rect = new Rect(0, 0, width, height);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (yuvimage.compressToJpeg(rect, JPEG_COMPRESS_QUALITY, baos)) {
                        byte[] jdata = baos.toByteArray();

                        int degree = mPreview.getCameraDisplayOrientation(mContext);
                        if (degree == 0) {
                            mServer.offerMedia(jdata);
                        } else {
                            BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
                            bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFactoryOptions);
                            if (bmp != null) {
                                Matrix m = new Matrix();
                                m.setRotate(degree);

                                Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
                                if (rotatedBmp != null) {
                                    baos.reset();
                                    if (rotatedBmp.compress(CompressFormat.JPEG, JPEG_COMPRESS_QUALITY, baos)) {
                                        mServer.offerMedia(baos.toByteArray());
                                    }
                                    rotatedBmp.recycle();
                                }
                                bmp.recycle();
                            }
                        }
                    }
                }

                mCamera.setPreviewCallback(this);
            }
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
         */
        void onFailedTakePhoto();
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
}
