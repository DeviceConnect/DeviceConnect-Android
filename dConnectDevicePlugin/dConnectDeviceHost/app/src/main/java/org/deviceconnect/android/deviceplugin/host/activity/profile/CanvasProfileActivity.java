/*
 HostCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.activity.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.AlertDialogFragment;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawImageObject;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawUtils;
import org.deviceconnect.android.libmedia.streaming.util.QueueThread;

/**
 * Canvas Profile Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class CanvasProfileActivity extends AppCompatActivity{

    /**
     * Defined a parameter name.
     */
    private static final String PARAM_INTENT = "param_intent";

    /**
     *  Defined a dialog type:{@value}.
     */
    private static final String DIALOG_TYPE_OOM = "TYPE_OOM";

    /**
     *  Defined a dialog type:{@value}.
     */
    private static final String DIALOG_TYPE_NOT_FOUND = "TYPE_NOT_FOUND";

    /**
     * 画像リソース取得結果.
     */
    private enum ResourceResult {
        /**
         * リソースの取得に成功.
         */
        Success,

        /**
         * リソースの取得時にOut Of Memoryが発生.
         */
        OutOfMemory,

        /**
         * リソースの取得に失敗.
         */
        NotFoundResource
    }

    /**
     * Canvas view object.
     */
    private ImageView mCanvasView;

    /**
     * Argument that draw in canvas.
     */
    private Intent mIntent;

    /**
     * Bitmap that was sent from web application.
     */
    private Bitmap mBitmap;

    /**
     * Download start dialog.
     */
    private StartingDialogFragment mDialog;

    /**
     * Download flag.
     */
    private boolean mDownloadFlag = false;

    /**
     * フォアグラウンドフラグ.
     */
    private boolean mForegroundFlag = false;

    /**
     * 表示用オブジェクト.
     */
    private CanvasDrawImageObject mDrawImageObject;

    /**
     * Implementation of BroadcastReceiver.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (CanvasDrawImageObject.ACTION_DRAW_CANVAS.equals(action)) {
                setDrawingArgument(intent);
                refreshImage(intent);
            } else if (CanvasDrawImageObject.ACTION_DELETE_CANVAS.equals(action)) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_canvas_profile);

        mCanvasView = findViewById(R.id.canvasProfileView);

        Button btn =  findViewById(R.id.buttonClose);
        btn.setOnClickListener((v) -> finish());

        Intent intent = null;
        if (savedInstanceState != null) {
            intent = (Intent) savedInstanceState.get(PARAM_INTENT);
        }
        if (intent == null) {
            intent = getIntent();
        }
        setDrawingArgument(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mForegroundFlag = true;

        IntentFilter filter = new IntentFilter();
        filter.addAction(CanvasDrawImageObject.ACTION_DRAW_CANVAS);
        filter.addAction(CanvasDrawImageObject.ACTION_DELETE_CANVAS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        refreshImage(mIntent);

        if (mDownloadThread != null) {
            mDownloadThread.terminate();
        }
        mDownloadThread = new DownloadThread();
        mDownloadThread.setName("Canvas-Download-Thread");
        mDownloadThread.start();
    }

    @Override
    protected void onPause() {
        mForegroundFlag = false;

        if (mDownloadThread != null) {
            mDownloadThread.terminate();
            mDownloadThread = null;
        }

        dismissDownloadDialog();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mIntent != null) {
            outState.putParcelable(PARAM_INTENT, mIntent);
        }
    }

    /**
     * Set a argument that draw in canvas.
     *
     * @param intent argument
     */
    private void setDrawingArgument(final Intent intent) {
        if (intent != null) {
            mIntent = intent;
        }
    }

    /**
     * リソースが見つからない場合のエラーダイアログを表示します.
     */
    private void openNotFoundDrawImage() {
        AlertDialogFragment oomDialog = AlertDialogFragment.create(DIALOG_TYPE_NOT_FOUND,
                getString(R.string.host_canvas_error_title),
                getString(R.string.host_canvas_error_not_found_message),
                getString(R.string.host_ok));
        oomDialog.show(getSupportFragmentManager(), DIALOG_TYPE_NOT_FOUND);
    }

    /**
     * メモリ不足エラーダイアログを表示します.
     */
    private void openOutOfMemory() {
        AlertDialogFragment oomDialog = AlertDialogFragment.create(DIALOG_TYPE_OOM,
                getString(R.string.host_canvas_error_title),
                getString(R.string.host_canvas_error_oom_message),
                getString(R.string.host_ok));
        oomDialog.show(getSupportFragmentManager(), DIALOG_TYPE_OOM);
    }

    /**
     * ダウンロードダイアログを表示します.
     */
    private synchronized void showDownloadDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mDialog = new StartingDialogFragment();
        mDialog.show(getFragmentManager(), "dialog");
    }

    /**
     * ダウンロードダイアログを非表示にします.
     */
    private synchronized void dismissDownloadDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    /**
     * 画面に表示する画像を更新します.
     * @param intent 更新する画像データが入ったintent
     */
    private void refreshImage(final Intent intent) {
        final CanvasDrawImageObject drawImageObject = CanvasDrawImageObject.create(intent);
        if (drawImageObject == null) {
            openNotFoundDrawImage();
            return;
        }

        if (mDownloadFlag) {
            return;
        }
        mDownloadFlag = true;

        runOnUiThread(this::showDownloadDialog);

        mDownloadThread.add(new Runnable() {
            private ResourceResult download() {
                if (mBitmap != null) {
                    // 同じデータへのURIならば、すでに画像を読み込んでいるの表示する
                    if (mDrawImageObject != null && drawImageObject.getData().equals(mDrawImageObject.getData())) {
                        return ResourceResult.Success;
                    } else {
                        if (mBitmap != null && !mBitmap.isRecycled()) {
                            mBitmap.recycle();
                            mBitmap = null;
                        }
                    }
                }

                String uri = drawImageObject.getData();
                byte[] data;
                try {
                    if (uri.startsWith("http")) {
                        data = CanvasDrawUtils.getData(uri);
                    } else if (uri.startsWith("content")) {
                        data = CanvasDrawUtils.getContentData(CanvasProfileActivity.this, uri);
                    } else {
                        data = CanvasDrawUtils.getCacheData(uri);
                    }
                    mBitmap = CanvasDrawUtils.getBitmap(data);
                    if (mBitmap == null) {
                        return ResourceResult.NotFoundResource;
                    }
                } catch (OutOfMemoryError e) {
                    return ResourceResult.OutOfMemory;
                } catch (Exception e) {
                    return ResourceResult.NotFoundResource;
                }
                return ResourceResult.Success;
            }

            @Override
            public void run() {
                ResourceResult result = download();

                mDrawImageObject = drawImageObject;

                runOnUiThread(() -> {
                    dismissDownloadDialog();
                    if (mForegroundFlag) {
                        switch (result) {
                            case Success:
                                showDrawObject(mDrawImageObject);
                                break;
                            case OutOfMemory:
                                openOutOfMemory();
                                break;
                            case NotFoundResource:
                                openNotFoundDrawImage();
                                break;
                        }
                    }
                    mDownloadFlag = false;
                });
            }
        });
    }

    /**
     * 画面を更新します.
     * @param drawObj 更新する画像データ
     */
    private void showDrawObject(final CanvasDrawImageObject drawObj) {
        switch (drawObj.getMode()) {
            default:
            case NON_SCALE_MODE:
                Matrix matrix = new Matrix();
                matrix.postTranslate((float) drawObj.getX(), (float) drawObj.getY());
                mCanvasView.setImageBitmap(mBitmap);
                mCanvasView.setScaleType(ScaleType.MATRIX);
                mCanvasView.setImageMatrix(matrix);
                break;
            case SCALE_MODE:
                mCanvasView.setImageBitmap(mBitmap);
                mCanvasView.setScaleType(ScaleType.FIT_CENTER);
                mCanvasView.setTranslationX((int) drawObj.getX());
                mCanvasView.setTranslationY((int) drawObj.getY());
                break;
            case FILL_MODE:
                BitmapDrawable bd = new BitmapDrawable(getResources(), mBitmap);
                bd.setTileModeX(Shader.TileMode.REPEAT);
                bd.setTileModeY(Shader.TileMode.REPEAT);
                mCanvasView.setImageDrawable(bd);
                mCanvasView.setScaleType(ScaleType.FIT_XY);
                mCanvasView.setTranslationX((int) drawObj.getX());
                mCanvasView.setTranslationY((int) drawObj.getY());
                break;
        }
    }

    /**
     * Show a dialog of download image.
     */
    public static class StartingDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            String title = getString(R.string.host_canvas_download_title);
            String msg = getString(R.string.host_canvas_download_message);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View v = inflater.inflate(R.layout.dialog_progress, null);
            TextView titleView = v.findViewById(R.id.title);
            TextView messageView = v.findViewById(R.id.message);
            titleView.setText(title);
            messageView.setText(msg);
            builder.setView(v);

            return builder.create();
        }

        @Override
        public void onPause() {
            dismiss();
            super.onPause();
        }
    }

    private DownloadThread mDownloadThread;

    private static class DownloadThread extends QueueThread<Runnable> {
        private void terminate() {
            interrupt();

            try {
                join(300);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    get().run();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}
