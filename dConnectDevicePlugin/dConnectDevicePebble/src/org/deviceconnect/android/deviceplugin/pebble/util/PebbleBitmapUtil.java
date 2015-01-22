/*
 PebbleBitmapUtil.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.util;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * 画像を変換するためのユーティリティクラス.
 * @author NTT DOCOMO, INC.
 */
public final class PebbleBitmapUtil {
    /**
     * Paint.
     */
    private static final Paint MY_PAINT = new Paint();

    /**
     * コンストラクタ. ユーティリティクラスなので、private.
     */
    private PebbleBitmapUtil() {
    }

    /**
     * スケールモードで画像をviewBitmapに描画する.
     * @param viewBitmap SWに表示するBitmap
     * @param bitmap 描画する画像のバイナリ
     */
    public static void drawImageForScalesMode(final Bitmap viewBitmap, Bitmap bitmap) {
        
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
    public static void drawImageForNonScalesMode(final Bitmap viewBitmap, final Bitmap bitmap, final double x, final double y) {
        
        // 描画開始地点
        float startGridX = (float)x;
        float startGridY = (float)y;
        
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
    public static void drawImageForFillsMode(final Bitmap viewBitmap, final Bitmap bitmap) {
        
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
     * 指定されたデータを2値化して、PebbleのGBitmap構造に変換する.
     * 
     * @param bitmap 変換するBitmap
     * @return GBitmapのデータ
     */
    public static byte[] convertImageThresholding(final Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        final boolean byThreshold = false;
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        Random random = new Random();
        final int randomNumberMax = 255 ;

        PbiImageStream stream = new PbiImageStream(width, height);
        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                final int threshold = 128;
                final int rgbColors = 3;
                int bitmapColor = pixels[(xx + yy * width)];
                int rr = Color.red(bitmapColor);
                int gg = Color.green(bitmapColor);
                int bb = Color.blue(bitmapColor);
                int x, y;
                y = (rr + gg + bb) / rgbColors;
                if (byThreshold) {
                    if (y < threshold) {
                        x = 0;
                    } else {
                        x = 1;
                    }
                } else {
                    x = 0 ;//誤差拡散法:iOS 側との互換性を有する
                    if( y > 150 ) {
                        x = 1 ;
                    }
                    else if( y > 110 ) {
                        if( y > random.nextInt(randomNumberMax) ) {
                            x = 1 ;
                        }
                    }
                }
                stream.setPixel(xx, yy, x);
            }
        }
        return stream.getStream();
    }
}
