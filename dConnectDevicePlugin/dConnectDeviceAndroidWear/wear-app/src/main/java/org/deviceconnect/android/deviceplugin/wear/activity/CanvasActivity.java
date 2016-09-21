/*
DataLayerListenerService.java
Copyright (c) 2015NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;

import org.deviceconnect.android.deviceplugin.wear.R;
import org.deviceconnect.android.deviceplugin.wear.WearApplication;
import org.deviceconnect.android.deviceplugin.wear.WearConst;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Canvas.
 */
public class CanvasActivity extends Activity {
    /**
     * Defines the timeout.
     */
    private static final int TIMEOUT_MS = 10000;

    /**
     * ImageView.
     */
    private ImageView mImageView;

    /**
     * Wakelock.
     */
    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP), "CanvasWakelockTag");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_canvas);
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        mImageView = (ImageView) findViewById(R.id.canvas_image);
        mImageView.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        if (intent != null) {
            refreshImage(intent);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
    }
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            refreshImage(intent);
        }
    }

    /**
     * Refresh image.
     * @param intent Intent
     */
    private void refreshImage(final Intent intent) {
        String action = intent.getAction();
        if (WearConst.ACTION_DELETE_CANVAS.equals(action)) {
            finish();
            return;
        }

        (new AsyncTask<Void, LoadingResult, LoadingResult>() {
            @Override
            protected LoadingResult doInBackground(Void... params) {
                Asset asset = intent.getParcelableExtra(WearConst.PARAM_BITMAP);
                return loadBitmapFromAsset(asset);
            }
            @Override
            protected void onPostExecute(LoadingResult result) {
                String sourceId = intent.getStringExtra(WearConst.PARAM_SOURCE_ID);
                String requestId = intent.getStringExtra(WearConst.PARAM_REQUEST_ID);
                sendResultToHost(sourceId, requestId, result.getResultCode());

                Bitmap bitmap = result.getBitmap();
                if (bitmap == null) {
                    return;
                }

                Log.d("AAA", "********* Bitmap: size = " + bitmap.getByteCount());

                int x = intent.getIntExtra(WearConst.PARAM_X, 0);
                int y = intent.getIntExtra(WearConst.PARAM_Y, 0);
                int mode = intent.getIntExtra(WearConst.PARAM_MODE, 0);
                setImageBitmap(bitmap, mode, x, y);
            }
        }).execute();
    }

    private void sendResultToHost(final String destinationId, final String requestId,
                                  final String resultCode) {
        Intent result = new Intent(WearConst.PARAM_DC_WEAR_CANVAS_ACT_TO_SVC);
        result.putExtra(WearConst.PARAM_DESTINATION_ID, destinationId);
        result.putExtra(WearConst.PARAM_REQUEST_ID, requestId);
        result.putExtra(WearConst.PARAM_RESULT, resultCode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(result);
    }

    /**
     * Sets a bitmap to ImageView.
     * @param bitmap bitmap
     * @param mode mode
     * @param x x
     * @param y y
     */
    private synchronized void setImageBitmap(final Bitmap bitmap, final int mode, final int x, final int y) {
        switch (mode) {
            default:
            case WearConst.MODE_NORMAL:
                Matrix matrix = new Matrix();
                matrix.postTranslate((float) x, (float) y);
                mImageView.setImageBitmap(bitmap);
                mImageView.setScaleType(ImageView.ScaleType.MATRIX);
                mImageView.setImageMatrix(matrix);
                mImageView.setVisibility(View.VISIBLE);
                break;
            case WearConst.MODE_SCALES:
                mImageView.setImageBitmap(bitmap);
                mImageView.setScaleType(ImageView.ScaleType.FIT_START);
                mImageView.setTranslationX(x);
                mImageView.setTranslationY(y);
                mImageView.setVisibility(View.VISIBLE);
                break;
            case WearConst.MODE_FILLS:
                BitmapDrawable bd = new BitmapDrawable(getResources(), bitmap);
                bd.setTileModeX(Shader.TileMode.REPEAT);
                bd.setTileModeY(Shader.TileMode.REPEAT);
                mImageView.setImageDrawable(bd);
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                mImageView.setTranslationX(x);
                mImageView.setTranslationY(y);
                mImageView.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Load a bitmap from Asset.
     * @param asset asset
     * @return result of loading
     */
    private LoadingResult loadBitmapFromAsset(final Asset asset) {
        InputStream in = getInputStream(asset);
        if (in == null) {
            return LoadingResult.errorOnConnection();
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (bitmap == null) {
                return LoadingResult.errorNotSupportedFormat();
            }
            return LoadingResult.success(bitmap);
        } catch (OutOfMemoryError e) {
            return LoadingResult.errorTooLargeBitmap();
        }
    }

    /**
     * Get an input stream from Asset.
     * @param asset input stream of asset
     * @return input stream, null on error
     */
    private InputStream getInputStream(final Asset asset) {
        if (asset == null) {
            return null;
        }
        GoogleApiClient client = getClient();
        ConnectionResult result =
            client.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        return Wearable.DataApi.getFdForAsset(client, asset).await().getInputStream();
    }

    /**
     * Gets a GoogleApiClient.
     * @return instance of GoogleApiClient
     */
    private GoogleApiClient getClient() {
        return ((WearApplication) getApplication()).getGoogleApiClient();
    }

    private static class LoadingResult {
        private Bitmap mBitmap;
        private String mResultCode;

        private LoadingResult() {
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public String getResultCode() {
            return mResultCode;
        }

        static LoadingResult success(final Bitmap bitmap) {
            if (bitmap == null) {
                throw new IllegalArgumentException();
            }
            LoadingResult result = new LoadingResult();
            result.mResultCode = WearConst.RESULT_SUCCESS;
            result.mBitmap = bitmap;
            return result;
        }

        static LoadingResult errorTooLargeBitmap() {
            LoadingResult result = new LoadingResult();
            result.mResultCode = WearConst.RESULT_ERROR_TOO_LARGE_BITMAP;
            return result;
        }

        static LoadingResult errorOnConnection() {
            LoadingResult result = new LoadingResult();
            result.mResultCode = WearConst.RESULT_ERROR_CONNECTION_FAILURE;
            return result;
        }

        static LoadingResult errorNotSupportedFormat() {
            LoadingResult result = new LoadingResult();
            result.mResultCode = WearConst.RESULT_ERROR_NOT_SUPPORTED_FORMAT;
            return result;
        }
    }
}
