/*
 CanvasProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw.profile;

import java.io.ByteArrayOutputStream;

import org.deviceconnect.android.deviceplugin.sw.R;
import org.deviceconnect.android.deviceplugin.sw.SWConstants;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.message.DConnectMessage;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;

/**
 * SonySWデバイスプラグインの{@link CanvasProfile}実装.
 * @author NTT DOCOMO, INC.
 */
public class SWCanvasProfile extends CanvasProfile {

    /**
     * コンストラクタ.
     */
    public SWCanvasProfile() {
        
    }

    @Override
    protected boolean onPostDrawImage(final Intent request, final Intent response, final String serviceId, 
            final String mimeType, final byte[] data, final double x, final double y, final String mode) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No device is found: " + serviceId);
            return true;
        }
        if (data == null || serviceId == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }
        DisplaySize size = determineDisplaySize(getContext(), SWUtil.toHostAppPackageName(device.getName()));

        showDisplay(data, x, y, mode, size, serviceId, response);

        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    /**
     * SWの画面に画像を表示する.
     * 
     * @param data バイナリデータ
     * @param x x座標
     * @param y y座標
     * @param mode 画像描画モード
     * @param size 画面サイズ
     * @param serviceId サービスID
     * @param response レスポンス
     */
    private void showDisplay(final byte[] data, final double x, final double y,
            final String mode, final DisplaySize size, final String serviceId,
            final Intent response) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // 最終的にSWに表示するBitmapの作成(大きさはSWの画面サイズ)
        final int width = size.getWidth();
        final int height = size.getHeight();
        Bitmap viewBitmap = Bitmap.createBitmap(width, height, SWConstants.DEFAULT_BITMAP_CONFIG);

        if (mode == null || mode.equals("")) {
            // 等倍描画モード 
            drawImageForNonScalesMode(viewBitmap, bitmap, x, y);
        } else if (mode.equals(Mode.SCALES.getValue())) {
            // スケールモード 
            drawImageForScalesMode(viewBitmap, bitmap);
        } else if (mode.equals(Mode.FILLS.getValue())) {
            // フィルモード 
        	drawImageForFillsMode(viewBitmap, bitmap);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(SWConstants.OUTPUTSTREAM_SIZE);
        viewBitmap.compress(CompressFormat.JPEG, SWConstants.BITMAP_DECODE_QUALITY, outputStream);
        
        Intent intent = new Intent(Control.Intents.CONTROL_DISPLAY_DATA_INTENT);
        intent.putExtra(Control.Intents.EXTRA_DATA, outputStream.toByteArray());
        sendToHostApp(intent, serviceId);
    }
    
    /**
     * スケールモードで画像をviewBitmapに描画する.
     * @param viewBitmap SWに表示するBitmap
     * @param bitmap 描画する画像のバイナリ
     */
    private void drawImageForScalesMode(final Bitmap viewBitmap, final Bitmap bitmap) {
        
        // 描画開始地点
        float startGridX = 0;
        float startGridY = 0;
        
        // 画像サイズ取得
        float getSizeW = bitmap.getWidth();
        float getSizeH = bitmap.getHeight();

        // 拡大率:縦横で長い方が画面ピッタリになるように
        float scale;
        final int width = viewBitmap.getWidth();
        final int height = viewBitmap.getHeight();
        if (getSizeW > getSizeH) {
            scale = width / getSizeW;
        } else {
            scale = height / getSizeH;
        }
        // 目標の大きさ
        int targetW = (int) Math.ceil(scale * getSizeW);
        int targetH = (int) Math.ceil(scale * getSizeH);
        
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetW, targetH, false);
        
        // 画像描写開始位置の修正
        if (getSizeW > getSizeH) {
            startGridY = (height / 2 - targetH / 2);
        } else {
            startGridX = (width / 2 - targetW / 2);
        }
        
        //canvasに表示用Bitmapをセット
        Canvas canvas = new Canvas(viewBitmap);
        
        //リサイズした画像をセンタリングしてcanvasにセット
        canvas.drawBitmap(resizedBitmap, startGridX, startGridY, null);
    }
    
    /**
     * 等倍描画モードで画像をviewBitmapに描画する.
     * @param viewBitmap SWに表示するBitmap
     * @param bitmap 描画する画像のバイナリ
     * @param x x座標
     * @param y y座標
     */
    private void drawImageForNonScalesMode(final Bitmap viewBitmap,
                            final Bitmap bitmap, final double x, final double y) {
        
        // 描画開始地点
        float startGridX = (float) x;
        float startGridY = (float) y;
        
        //canvasに表示用Bitmapをセット
        Canvas canvas = new Canvas(viewBitmap);
        
        //リサイズした画像をセンタリングしてcanvasにセット
        canvas.drawBitmap(bitmap, startGridX, startGridY, null);
    }
    
    /**
     * フィルモードで画像をviewBitmapに描画する.
     * @param viewBitmap SWに表示するBitmap
     * @param bitmap 描画する画像のバイナリ
     */
    private void drawImageForFillsMode(final Bitmap viewBitmap, final Bitmap bitmap) {
        
        // 画像サイズ取得
        float getSizeW = bitmap.getWidth();
        float getSizeH = bitmap.getHeight();
        
        //canvasに表示用Bitmapをセット
        Canvas canvas = new Canvas(viewBitmap);
        
        // タイル状に敷き詰めて描画する
        final int width = viewBitmap.getWidth();
        final int height = viewBitmap.getHeight();
        for (int drawY = 0; drawY <= height; drawY += getSizeH) {
            for (int drawX = 0; drawX <= width; drawX += getSizeW) {
                canvas.drawBitmap(bitmap, drawX, drawY, null);
            }
        }
    }

    /**
     * 指定されたホストアプリケーションに対応するSWの画面サイズを返す.
     * 
     * @param context コンテキスト
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

    /**
     * ホストアプリケーションに対してインテントを送信する.
     * 
     * @param intent インテント
     * @param serviceId サービスID
     */
    protected void sendToHostApp(final Intent intent, final String serviceId) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        String deviceName = device.getName();
        intent.putExtra(Control.Intents.EXTRA_AEA_PACKAGE_NAME, getContext().getPackageName());
        intent.setPackage(SWUtil.toHostAppPackageName(deviceName));
        getContext().sendBroadcast(intent, Registration.HOSTAPP_PERMISSION);
    }
}

