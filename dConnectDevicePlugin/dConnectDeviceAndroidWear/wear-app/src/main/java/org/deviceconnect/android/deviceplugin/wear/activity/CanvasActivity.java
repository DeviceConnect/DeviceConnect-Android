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
import androidx.wear.widget.BoxInsetLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.Wearable;

import org.deviceconnect.android.deviceplugin.wear.R;
import org.deviceconnect.android.deviceplugin.wear.WearApplication;
import org.deviceconnect.android.deviceplugin.wear.WearConst;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static androidx.wear.widget.BoxInsetLayout.LayoutParams.BOX_ALL;

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
    /**
     * Canvas Layout.
     */
    private FrameLayout mFrameLayout;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock((PowerManager.PARTIAL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP), "DeviceConnect:CanvasWakelockTag");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_canvas);
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        mImageView = (ImageView) findViewById(R.id.canvas_image);
        mImageView.setVisibility(View.INVISIBLE);
        mFrameLayout = findViewById(R.id.canvas_frame);

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
                if (!result.isSuccess()) {
                    finish();
                    return;
                }

                Bitmap bitmap = result.getBitmap();
                if (bitmap == null) {
                    return;
                }

                int x = intent.getIntExtra(WearConst.PARAM_X, 0);
                int y = intent.getIntExtra(WearConst.PARAM_Y, 0);
                int mode = intent.getIntExtra(WearConst.PARAM_MODE, 0);
                setImageBitmap(bitmap, mode, x, y);
            }
        }).execute();
    }

    private void sendResultToHost(final String destinationId, final String requestId, final String resultCode) {
        String data = requestId + "," + resultCode;
        String path = WearConst.WEAR_TO_DEVICE_CANVAS_RESULT;
        ((WearApplication) getApplication()).sendMessage(destinationId, path, data);
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
                BoxInsetLayout.LayoutParams normalLayoutParam = new BoxInsetLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER, BOX_ALL);
                mFrameLayout.setLayoutParams(normalLayoutParam);
                break;
            case WearConst.MODE_SCALES:
                mImageView.setImageBitmap(bitmap);
                mImageView.setScaleType(ImageView.ScaleType.FIT_START);
                mImageView.setTranslationX(x);
                mImageView.setTranslationY(y);
                mImageView.setVisibility(View.VISIBLE);
                BoxInsetLayout.LayoutParams scaleLayoutParam = new BoxInsetLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, Gravity.LEFT | Gravity.TOP);
                mFrameLayout.setLayoutParams(scaleLayoutParam);
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
                BoxInsetLayout.LayoutParams fillLayoutParam = new BoxInsetLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, Gravity.LEFT | Gravity.TOP);
                mFrameLayout.setLayoutParams(fillLayoutParam);
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
        Task<DataClient.GetFdForAssetResponse> getFdForAssetResponseTask =
                Wearable.getDataClient(getApplicationContext()).getFdForAsset(asset);

        InputStream assetInputStream = null;
        try {
            DataClient.GetFdForAssetResponse getFdForAssetResponse = null;
            getFdForAssetResponse = Tasks.await(getFdForAssetResponseTask, TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assetInputStream = getFdForAssetResponse.getInputStream();
        } catch (TimeoutException exception) {
            Log.e("WEAR", "Failed retrieving asset, Timeout failed: " + exception);
        } catch (ExecutionException exception) {
            Log.e("WEAR", "Failed retrieving asset, Task failed: " + exception);
        } catch (InterruptedException exception) {
            Log.e("WEAR", "Failed retrieving asset, interrupt occurred: " + exception);
        }
        return assetInputStream;
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

        public boolean isSuccess() {
            return WearConst.RESULT_SUCCESS.equals(mResultCode);
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
