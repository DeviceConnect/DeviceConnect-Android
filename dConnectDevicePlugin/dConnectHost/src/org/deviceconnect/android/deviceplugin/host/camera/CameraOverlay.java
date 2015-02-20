package org.deviceconnect.android.deviceplugin.host.camera;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.provider.FileManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * カメラのプレビューをオーバーレイで表示するクラス.
 * @author NTT DOCOMO, INC.
 */
public class CameraOverlay implements Camera.PreviewCallback {
    /**
     * 写真を取るまでの待機時間を定義.
     */
    private static final int PERIOD_TAKE_PHOTO_WAIT = 200;

    /**
     * JPEGの圧縮クオリティを定義.
     */
    private static final int JPEG_COMPRESS_QUALITY = 100;

    /** ファイル名に付けるプレフィックス. */
    private static final String FILENAME_PREFIX = "android_camera_";

    /** ファイルの拡張子. */
    private static final String FILE_EXTENSION = ".png";

    /** 日付のフォーマット. */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
            "yyyyMMdd_kkmmss", Locale.JAPAN);

    /** コンテキスト. */
    private Context mContext;

    /** ウィンドウ管理クラス. */
    private WindowManager mWinMgr;

    /** ハンドラ. */
    private Handler mHandler;

    /** ファイル管理クラス. */
    private FileManager mFileMgr;

    /** プレビュー画面. */
    private Preview mPreview;

    /** 表示用のテキスト. */
    private TextView mTextView;

    /** 使用するカメラのインスタンス. */
    private Camera mCamera;

    /** 画像を送るサーバ. */
    private MixedReplaceMediaServer mServer;

    /**
     * 終了フラグ.
     * <p>
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
                updatePosition(mTextView);
            }
        }
    };

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public CameraOverlay(final Context context) {
        mContext = context;
        mWinMgr = (WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE);
        mHandler = new Handler();
    }

    /**
     * MixedReplaceMediaServerを設定する.
     * @param server サーバのインスタンス
     */
    public void setServer(final MixedReplaceMediaServer server) {
        mServer = server;
    }

    /**
     * FileManagerを設定する.
     * @param mgr FileManagerのインスタンス
     */
    public void setFileManager(final FileManager mgr) {
        mFileMgr = mgr;
    }

    /**
     * カメラのオーバーレイが表示されているかを確認する.
     * @return 表示されている場合はtrue、それ以外はfalse
     */
    public synchronized boolean isShow() {
        return mPreview != null && mTextView != null;
    }

    /**
     * Overlayを表示する.
     */
    public synchronized void show() {
        mPreview = new Preview(mContext);

        Point size = getDisplaySize();
        int pt = (int) (4 * getScaledDensity());
        WindowManager.LayoutParams l = new WindowManager.LayoutParams(
                pt, pt,
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
        mCamera.setPreviewCallback(this);

        mTextView = new TextView(mContext);
        mTextView.setText(R.string.overlay_preview);
        mTextView.setTextColor(Color.RED);
        mTextView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        mTextView.setClickable(true);
        mTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                hide();
            }
        });

        WindowManager.LayoutParams l2 = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        l2.x = -size.x / 2;
        l2.y = -size.y / 2;
        mWinMgr.addView(mTextView, l2);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        mContext.registerReceiver(mOrientReceiver, filter);
    }

    /**
     * Overlayを非表示にする.
     */
    public synchronized void hide() {
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
        if (mTextView != null) {
            mWinMgr.removeView(mTextView);
            mTextView = null;
        }
        mContext.unregisterReceiver(mOrientReceiver);
        mFinishFlag = false;
    }

    /**
     * 写真撮影後にOverlayを非表示にするフラグを設定する.
     * @param flag trueの場合は撮影後に非表示にする
     */
    public synchronized void setFinishFlag(final boolean flag) {
        mFinishFlag = flag;
    }

    /**
     * 写真撮影を行う.
     * <p>
     * 写真撮影の結果はlistenerに通知される。
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
     * 写真撮影を行う内部メソッド.
     * @param listener 撮影結果を通知するリスナー
     */
    private synchronized void takePictureInternal(final OnTakePhotoListener listener) {
        if (mPreview == null || mCamera == null) {
            if (listener != null) {
                listener.onFailedTakePhoto();
            }
            return;
        }
        mPreview.takePicture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, final Camera camera) {
                String fileName = createNewFileName();
                String pictureUri = null;
                try {
                    pictureUri = mFileMgr.saveFile(fileName, data);
                    if (listener != null) {
                        listener.onTakenPhoto(pictureUri);
                    }
                } catch (IOException e) {
                    if (listener != null) {
                        listener.onFailedTakePhoto();
                    }
                }

                synchronized (CameraOverlay.this) {
                    if (mFinishFlag) {
                        hide();
                    } else if (mCamera != null) {
                        mCamera.startPreview();
                    }
                }
            }
        });
    }
    /**
     * Displayの密度を取得する.
     * @return 密度
     */
    private float getScaledDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        mWinMgr.getDefaultDisplay().getMetrics(metrics);
        return metrics.scaledDensity;
    }

    /**
     * Displayのサイズを取得する.
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
     * @param view 座標を移動するView
     */
    private void updatePosition(final View view) {
        if (view == null) {
            return;
        }
        Point size = getDisplaySize();
        WindowManager.LayoutParams lp = 
                (WindowManager.LayoutParams) view.getLayoutParams();
        lp.x = -size.x / 2;
        lp.y = -size.y / 2;
        mWinMgr.updateViewLayout(view, lp);
    }

    /**
     * 新規のファイル名を作成する.
     * @return ファイル名
     */
    private String createNewFileName() {
        return FILENAME_PREFIX + mSimpleDateFormat.format(new Date()) + FILE_EXTENSION;
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        camera.setPreviewCallback(null);

        if (mServer != null) {
            int format = camera.getParameters().getPreviewFormat();
            int width = camera.getParameters().getPreviewSize().width;
            int height = camera.getParameters().getPreviewSize().height;

            YuvImage yuvimage = new YuvImage(data, format, width, height, null);
            Rect rect = new Rect(0, 0, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (yuvimage.compressToJpeg(rect, JPEG_COMPRESS_QUALITY, baos)) {
                byte[] jdata = baos.toByteArray();
                
                int degree = mPreview.getCameraDisplayOrientation(mContext);
                if (degree == 0) {
                    mServer.offerMedia(jdata);
                } else {
                    BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
                    bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);

                    Matrix m = new Matrix();
                    m.setRotate(degree);

                    Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, 
                            bmp.getWidth(), bmp.getHeight(), m, true);

                    baos.reset();
                    if (rotatedBmp.compress(CompressFormat.JPEG,
                            JPEG_COMPRESS_QUALITY, baos)) {
                        mServer.offerMedia(baos.toByteArray());
                    }
                }
            }
        }

        camera.setPreviewCallback(this);
    }

    /**
     * 写真撮影結果を通知するリスナー.
     */
    public interface OnTakePhotoListener {
        /**
         * 写真撮影を行った画像へのURIを通知する.
         * @param uri URI
         */
        void onTakenPhoto(String uri);

        /**
         * 写真撮影に失敗したことを通知する.
         */
        void onFailedTakePhoto();
    }
}
