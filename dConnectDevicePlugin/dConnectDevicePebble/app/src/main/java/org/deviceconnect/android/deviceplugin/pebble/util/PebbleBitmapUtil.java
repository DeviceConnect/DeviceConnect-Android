/*
 PebbleBitmapUtil.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.util;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * 画像を変換するためのユーティリティクラス.
 * @author NTT DOCOMO, INC.
 */
public final class PebbleBitmapUtil {

    /**
     * コンストラクタ. ユーティリティクラスなので、private.
     */
    private PebbleBitmapUtil() {
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
        final int randomNumberMax = 255;

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
                    x = 0; //誤差拡散法:iOS 側との互換性を有する
                    if (y > 150) {
                        x = 1;
                    } else if (y > 110) {
                        if (y > random.nextInt(randomNumberMax)) {
                            x = 1;
                        }
                    }
                }
                stream.setPixel(xx, yy, x);
            }
        }
        return stream.getStream();
    }
}
