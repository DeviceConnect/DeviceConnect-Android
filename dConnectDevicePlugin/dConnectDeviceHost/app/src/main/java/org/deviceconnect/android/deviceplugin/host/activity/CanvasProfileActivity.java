/*
 HostCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawImageObject;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawUtils;
import org.deviceconnect.android.deviceplugin.host.setting.HostAlertDialogFragment;

/**
 * Canvas Profile Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class CanvasProfileActivity extends Activity implements HostAlertDialogFragment.OnAlertDialogListener {

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
    private StartingDialogFragment mDialog;

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
        ViewTreeObserver viewTreeObserver = mCanvasView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mDialog = new StartingDialogFragment();
                mDialog.show(getFragmentManager(), "dialog");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        refreshImage(mIntent);
                    }
                }).start();
                removeOnGlobalLayoutListener(mCanvasView.getViewTreeObserver(), this);
            }
        });

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
        if (drawObj == null) {
            mDialog.dismiss();
            HostAlertDialogFragment oomDialog = HostAlertDialogFragment.create(DIALOG_TYPE_NOT_FOUND, getString(R.string.host_canvas_error_title),
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
            if (uri.startsWith("content://")) {
                data = CanvasDrawUtils.getContentData(this, uri);
            } else {
                data = CanvasDrawUtils.getData(uri);
            }
        } catch (OutOfMemoryError e) {
            mDialog.dismiss();
            HostAlertDialogFragment oomDialog = HostAlertDialogFragment.create(DIALOG_TYPE_OOM, getString(R.string.host_canvas_error_title),
                                                getString(R.string.host_canvas_error_oom_message), getString(R.string.host_ok));
            oomDialog.show(getFragmentManager(), DIALOG_TYPE_OOM);
            return;
        }
        if (data == null) {
            // failed to load data.
            mDialog.dismiss();
            HostAlertDialogFragment oomDialog = HostAlertDialogFragment.create(DIALOG_TYPE_NOT_FOUND, getString(R.string.host_canvas_error_title),
                    getString(R.string.host_canvas_error_not_found_message), getString(R.string.host_ok));
            oomDialog.show(getFragmentManager(), DIALOG_TYPE_NOT_FOUND);
            return;
        }
        mBitmap = CanvasDrawUtils.getBitmap(data);
        if (mBitmap == null) {
            // failed to load bitmap.
            mDialog.dismiss();
            HostAlertDialogFragment oomDialog = HostAlertDialogFragment.create(DIALOG_TYPE_NOT_FOUND, getString(R.string.host_canvas_error_title),
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
            }

        });
    }
    // Remove ViewTreeObserver.
    private void removeOnGlobalLayoutListener(final ViewTreeObserver observer, final ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (observer == null) {
            return ;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            observer.removeGlobalOnLayoutListener(listener);
        } else {
            observer.removeOnGlobalLayoutListener(listener);
        }
    }

    @Override
    public void onPositiveButton(String tag) {
        if (tag.equals(DIALOG_TYPE_OOM)
                || tag.equals(DIALOG_TYPE_NOT_FOUND)) {
            finish();
        }
    }

    @Override
    public void onNegativeButton(String tag) {

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
}
