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
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawImageObject;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawUtils;

import java.io.File;

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
    /** Download start dialog. */
    private StartingDialogFragment mDialog;
    /** Download flag. */
    private boolean downloading = false;
    /**
     * Implementation of BroadcastReceiver.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (CanvasDrawImageObject.ACTION_DRAW_CANVAS.equals(action)) {
                setDrawingArgument(intent);
                mDialog = new StartingDialogFragment();
                mDialog.show(getFragmentManager(), "dialog");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        refreshImage(intent);
                    }
                }).start();
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

        Intent intent = mIntent;
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(CanvasDrawImageObject.ACTION_DRAW_CANVAS);
        filter.addAction(CanvasDrawImageObject.ACTION_DELETE_CANVAS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
        mDialog = new StartingDialogFragment();
        mDialog.show(getFragmentManager(), "dialog");
        new Thread(new Runnable() {
            @Override
            public void run() {
                refreshImage(mIntent);
            }
        }).start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
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
     * Refresh image.
     *
     * @param intent Intent
     */
    private synchronized void refreshImage(final Intent intent) {
        final CanvasDrawImageObject drawObj = CanvasDrawImageObject.create(intent);
        if (downloading) {
            return;
        }
        downloading = true;
        if (drawObj == null) {
            mDialog.dismiss();
            AlertDialogFragment oomDialog = AlertDialogFragment.create(DIALOG_TYPE_NOT_FOUND, getString(R.string.host_canvas_error_title),
                    getString(R.string.host_canvas_error_not_found_message), getString(R.string.host_ok));
            oomDialog.show(getFragmentManager(), DIALOG_TYPE_NOT_FOUND);
            return;
        }

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        String uri = drawObj.getData();
        byte[] data;
        try {
            File cache = getCacheDir();
            if (uri.startsWith(cache.getAbsolutePath())) {
                data = CanvasDrawUtils.getCacheData(uri);
            } else {
                data = CanvasDrawUtils.getData(uri);
            }
        } catch (OutOfMemoryError e) {
            mDialog.dismiss();
            AlertDialogFragment oomDialog = AlertDialogFragment.create(DIALOG_TYPE_OOM, getString(R.string.host_canvas_error_title),
                                                getString(R.string.host_canvas_error_oom_message), getString(R.string.host_ok));
            oomDialog.show(getFragmentManager(), DIALOG_TYPE_OOM);
            return;
        }
        if (data == null) {
            // failed to load data.
            mDialog.dismiss();
            AlertDialogFragment oomDialog = AlertDialogFragment.create(DIALOG_TYPE_NOT_FOUND, getString(R.string.host_canvas_error_title),
                    getString(R.string.host_canvas_error_not_found_message), getString(R.string.host_ok));
            oomDialog.show(getFragmentManager(), DIALOG_TYPE_NOT_FOUND);
            return;
        }
        mBitmap = CanvasDrawUtils.getBitmap(data);
        if (mBitmap == null) {
            // failed to load bitmap.
            mDialog.dismiss();
            AlertDialogFragment oomDialog = AlertDialogFragment.create(DIALOG_TYPE_NOT_FOUND, getString(R.string.host_canvas_error_title),
                    getString(R.string.host_canvas_error_not_found_message), getString(R.string.host_ok));
            oomDialog.show(getFragmentManager(), DIALOG_TYPE_NOT_FOUND);
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (drawObj.getMode()) {
                    default:
                    case NONSCALE_MODE:
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
                mDialog.dismiss();
                downloading = false;
            }

        });
    }

    /**
     * Show a dialog of dwnload image.
     */
    public static class StartingDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            String title = getString(R.string.host_canvas_download_title);
            String msg = getString(R.string.host_canvas_download_message);
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(title);
            progressDialog.setMessage(msg);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            setCancelable(false);
            return progressDialog;
        }

        @Override
        public void onPause() {
            dismiss();
            super.onPause();
        }
    }

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
                builder.setNegativeButton(getArguments().getString(KEY_NEGATIVE),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                            }
                        });
            }
            return builder.create();
        }

        @Override
        public void onCancel(final DialogInterface dialog) {

        }

    }

}
