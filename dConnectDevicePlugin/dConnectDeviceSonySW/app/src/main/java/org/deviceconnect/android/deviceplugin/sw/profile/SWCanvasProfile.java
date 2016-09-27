/*
 CanvasProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;

import org.deviceconnect.android.deviceplugin.sw.R;
import org.deviceconnect.android.deviceplugin.sw.SWConstants;
import org.deviceconnect.android.deviceplugin.sw.service.SWService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.util.CanvasProfileUtils;
import org.deviceconnect.message.DConnectMessage;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SonySWデバイスプラグインの{@link CanvasProfile}実装.
 *
 * @author NTT DOCOMO, INC.
 */
public class SWCanvasProfile extends CanvasProfile {

    private final ExecutorService mImageService = Executors.newSingleThreadExecutor();

    private final DConnectApi mPostDrawImageApi = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_DRAW_IMAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final byte[] data = getData(request);
            final double x = getX(request);
            final double y = getY(request);
            final String mode = getMode(request);
            String mimeType = getMIMEType(request);
            if (mimeType != null && !mimeType.contains("image")) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "Unsupported mimeType: " + mimeType);
                return true;
            }
            if (data == null) {
                mImageService.execute(new Runnable() {
                    @Override
                    public void run() {
                        String uri = getURI(request);
                        byte[] result = getData(uri);
                        if (result == null) {
                            MessageUtils.setInvalidRequestParameterError(response, "could not get image from uri.");
                            sendResponse(response);
                            return;
                        }
                        drawImage(response, result, x, y, mode);
                        sendResponse(response);
                    }
                });
                return false;
            } else {
                drawImage(response, data, x, y, mode);
                return true;
            }
        }
    };

    /**
     * コンストラクタ.
     */
    public SWCanvasProfile() {
        addApi(mPostDrawImageApi);
    }

    private void drawImage(final Intent response, byte[] data, double x, double y, String mode) {
        DisplaySize size = determineDisplaySize(getContext(), ((SWService) getService()).getHostPackageName());
        boolean result = showDisplay(data, x, y, mode, size);
        if (!result) {
            /* unknown mode-value. */
            MessageUtils.setInvalidRequestParameterError(response);
            return;
        }
        setResult(response, DConnectMessage.RESULT_OK);
    }

    /**
     * SWの画面に画像を表示する.
     *
     * @param data      バイナリデータ
     * @param x         x座標
     * @param y         y座標
     * @param mode      画像描画モード
     * @param size      画面サイズ
     * @return true: success / false: error(unknown mode-value)
     */
    private boolean showDisplay(final byte[] data, final double x, final double y,
                                final String mode, final DisplaySize size) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // 最終的にSWに表示するBitmapの作成(大きさはSWの画面サイズ)
        final int width = size.getWidth();
        final int height = size.getHeight();
        Bitmap viewBitmap = Bitmap.createBitmap(width, height, SWConstants.DEFAULT_BITMAP_CONFIG);

        boolean isDraw = false;
        if (mode == null || mode.equals("")) {
            // 等倍描画モード 
            CanvasProfileUtils.drawImageForNonScalesMode(viewBitmap, bitmap, x, y);
            isDraw = true;
        } else if (mode.equals(Mode.SCALES.getValue())) {
            // スケールモード 
            CanvasProfileUtils.drawImageForScalesMode(viewBitmap, bitmap);
            isDraw = true;
        } else if (mode.equals(Mode.FILLS.getValue())) {
            // フィルモード 
            CanvasProfileUtils.drawImageForFillsMode(viewBitmap, bitmap);
            isDraw = true;
        }

        if (isDraw) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(SWConstants.OUTPUTSTREAM_SIZE);
            viewBitmap.compress(CompressFormat.JPEG, SWConstants.BITMAP_DECODE_QUALITY, outputStream);

            Intent intent = new Intent(Control.Intents.CONTROL_DISPLAY_DATA_INTENT);
            intent.putExtra(Control.Intents.EXTRA_DATA, outputStream.toByteArray());
            sendToHostApp(intent);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 指定されたホストアプリケーションに対応するSWの画面サイズを返す.
     *
     * @param context            コンテキスト
     * @param hostAppPackageName ホストアプリケーション名(SW1orSW2)
     * @return 画面サイズ
     */
    private static DisplaySize determineDisplaySize(final Context context, final String hostAppPackageName) {
        boolean smartWatch2Supported = DeviceInfoHelper.isSmartWatch2ApiAndScreenDetected(context, hostAppPackageName);
        int width;
        int height;
        if (smartWatch2Supported) {
            width = context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_width);
            height = context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_height);
        } else {
            width = context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_width);
            height = context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_height);
        }
        return new DisplaySize(width, height);
    }

    private void sendToHostApp(final Intent request) {
        ((SWService) getService()).sendRequest(request);
    }
}

