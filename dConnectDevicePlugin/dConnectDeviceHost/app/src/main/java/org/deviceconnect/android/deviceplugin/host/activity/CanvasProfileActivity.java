/*
 HostCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawImageObject;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawUtils;

/**
 * Canvas Profile Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class CanvasProfileActivity extends Activity  {

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

        mCanvasView = (ImageView) findViewById(R.id.canvasProfileView);

        Button btn = (Button) findViewById(R.id.buttonClose);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });

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
    }

    @Override
    protected void onPause() {
        mForegroundFlag = false;

        dismissDownloadDialog();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
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
        oomDialog.show(getFragmentManager(), DIALOG_TYPE_NOT_FOUND);
    }

    /**
     * メモリ不足エラーダイアログを表示します.
     */
    private void openOutOfMemory() {
        AlertDialogFragment oomDialog = AlertDialogFragment.create(DIALOG_TYPE_OOM,
                getString(R.string.host_canvas_error_title),
                getString(R.string.host_canvas_error_oom_message),
                getString(R.string.host_ok));
        oomDialog.show(getFragmentManager(), DIALOG_TYPE_OOM);
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

        AsyncTask<Void, ResourceResult, ResourceResult> task = new AsyncTask<Void, ResourceResult, ResourceResult>() {
            @Override
            protected void onPreExecute() {
                showDownloadDialog();
            }

            @Override
            protected ResourceResult doInBackground(final Void... params) {

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
            protected void onPostExecute(final ResourceResult result) {
                mDrawImageObject = drawImageObject;

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
            }
        };
        task.execute();
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
     * Show a dialog of dwnload image.
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

    /**
     * エラーダイアログ.
     */
    public static class AlertDialogFragment extends DialogFragment {
        /**
         * タグのキーを定義します.
         */
        private static final String KEY_TAG = "tag";

        /**
         * タイトルのキーを定義します.
         */
        private static final String KEY_TITLE = "title";

        /**
         * メッセージのキーを定義します.
         */
        private static final String KEY_MESSAGE = "message";

        /**
         * Positiveボタンのキーを定義します.
         */
        private static final String KEY_POSITIVE = "yes";

        /**
         * Negativeボタンのキーを定義します.
         */
        private static final String KEY_NEGATIVE = "no";

        /**
         * ボタン無しでAlertDialogを作成します.
         * @param tag タグ
         * @param title タイトル
         * @param message メッセージ
         * @return AlertDialogFragmentのインスタンス
         */
        public static AlertDialogFragment create(final String tag, final String title, final String message) {
            return create(tag, title, message, null, null);
        }

        /**
         * PositiveボタンのみでAlertDialogを作成します.
         * @param tag タグ
         * @param title タイトル
         * @param message メッセージ
         * @param positive positiveボタン名
         * @return AlertDialogFragmentのインスタンス
         */
        public static AlertDialogFragment create(final String tag, final String title, final String message, final String positive) {
            return create(tag, title, message, positive, null);
        }

        /**
         * ボタン有りでAlertDialogを作成します.
         * @param tag タグ
         * @param title タイトル
         * @param message メッセージ
         * @param positive positiveボタン名
         * @param negative negativeボタン名
         * @return AlertDialogFragmentのインスタンス
         */
        public static AlertDialogFragment create(final String tag, final String title, final String message,
                                                 final String positive, final String negative) {
            Bundle args = new Bundle();
            args.putString(KEY_TAG, tag);
            args.putString(KEY_TITLE, title);
            args.putString(KEY_MESSAGE, message);
            if (positive != null) {
                args.putString(KEY_POSITIVE, positive);
            }
            if (negative != null) {
                args.putString(KEY_NEGATIVE, negative);
            }

            AlertDialogFragment dialog = new AlertDialogFragment();
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getArguments().getString(KEY_TITLE));
            builder.setMessage(getArguments().getString(KEY_MESSAGE));
            if (getArguments().getString(KEY_POSITIVE) != null) {
                builder.setPositiveButton(getArguments().getString(KEY_POSITIVE),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                Activity activity = getActivity();
                                if (activity != null) {
                                    activity.finish();
                                }
                            }
                        });
            }
            if (getArguments().getString(KEY_NEGATIVE) != null) {
                builder.setNegativeButton(getArguments().getString(KEY_NEGATIVE), null);
            }
            return builder.create();
        }

        @Override
        public void onCancel(final DialogInterface dialog) {

        }
    }
}
