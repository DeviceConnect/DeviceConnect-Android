/*
 CanvasProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Canvas Profile Utility.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class CanvasProfileUtils {

    /**
     * Private Contructor.
     */
    private CanvasProfileUtils() {
    }
    /**
     * Draw the image to viewBitmap at the same scale drawing mode.
     * @param viewBitmap Bitmap to be displayed on the device.
     * @param bitmap Bitmap image.
     * @param x x
     * @param y y
     */
    public static void drawImageForNonScalesMode(final Bitmap viewBitmap,
             final Bitmap bitmap, final double x, final double y) {
        
        float startGridX = (float) x;
        float startGridY = (float) y;
        
        Canvas canvas = new Canvas(viewBitmap);
        
        canvas.drawBitmap(bitmap, startGridX, startGridY, null);
    }
    
    /**
     * draw the image to viewBitmap in scale mode.
     * @param viewBitmap Bitmap to be displayed on the device.
     * @param bitmap Bitmap image.
     */
    public static void drawImageForScalesMode(final Bitmap viewBitmap, final Bitmap bitmap) {
        
        float startGridX = 0;
        float startGridY = 0;
        
        float getSizeW = bitmap.getWidth();
        float getSizeH = bitmap.getHeight();

        float scale;
        final int width = viewBitmap.getWidth();
        final int height = viewBitmap.getHeight();
        if ((getSizeW / width) > (getSizeH / height)) {
            scale = width / getSizeW;
        } else {
            scale = height / getSizeH;
        }

        int targetW = (int) Math.ceil(scale * getSizeW);
        int targetH = (int) Math.ceil(scale * getSizeH);
        
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetW, targetH, false);
        
        if ((getSizeW / width) > (getSizeH / height)) {
            startGridY = (height / 2 - targetH / 2);
        } else {
            startGridX = (width / 2 - targetW / 2);
        }
        
        Canvas canvas = new Canvas(viewBitmap);
        canvas.drawBitmap(resizedBitmap, startGridX, startGridY, null);
    }
    
    /**
     * draw the image to viewBitmap in fill mode.
     * @param viewBitmap Bitmap to be displayed on the device.
     * @param bitmap Bitmap image.
     */
    public static void drawImageForFillsMode(final Bitmap viewBitmap, final Bitmap bitmap) {
        
        float getSizeW = bitmap.getWidth();
        float getSizeH = bitmap.getHeight();
        
        Canvas canvas = new Canvas(viewBitmap);
        
        final int width = viewBitmap.getWidth();
        final int height = viewBitmap.getHeight();
        for (int drawY = 0; drawY <= height; drawY += getSizeH) {
            for (int drawX = 0; drawX <= width; drawX += getSizeW) {
                canvas.drawBitmap(bitmap, drawX, drawY, null);
            }
        }
    }
}
