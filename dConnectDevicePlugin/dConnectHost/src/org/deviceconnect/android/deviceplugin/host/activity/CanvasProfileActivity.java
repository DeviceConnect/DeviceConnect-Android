/*
 HostCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.activity;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawImageObject;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * Canvas Profile Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class CanvasProfileActivity extends Activity {

    /**
     * Defined a parameter name.
     */
    private static final String PARAM_INTENT = "param_intent";

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
        ViewTreeObserver viewTreeObserver = mCanvasView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                refreshImage(mIntent);
            }
        });

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
     * @param intent argument
     */
    private void setDrawingArgument(final Intent intent) {
        if (intent != null) {
            mIntent = intent;
        }
    }

    /**
     * Refresh image.
     * @param intent Intent
     */
    private synchronized void refreshImage(final Intent intent) {
        CanvasDrawImageObject drawObj = CanvasDrawImageObject.create(intent);
        if (drawObj == null) {
            finish();
            return;
        }

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        String uri = drawObj.getUri();
        mBitmap = CanvasDrawUtils.getBitmap(this, uri);
        if (mBitmap == null) {
            // failed to load bitmap.
            return;
        }

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
    }
}
