/*
 ImageUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.util;

import android.graphics.Bitmap;

/**
 * Utility for image transformation.
 *
 * @author NTT DOCOMO, INC.
 */
public final class ImageUtils {
    static {
        System.loadLibrary("ImageUtils");
    }

    private ImageUtils() {
    }

    /**
     * Resize the bitmap.
     * <p>
     * If bitmap is null, return null.
     * </p>
     * <p>
     * Notice: If the bitmap is resized , the original bitmap is recycled.
     * </p>
     * @param bitmap bitmap
     * @return Resized the bitmap
     */
    public static Bitmap resize(final Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int w = roundUp(width, 16);
        int h = roundUp(height, 16);
        if (w != width || h != height) {
            Bitmap b = Bitmap.createScaledBitmap(bitmap, w, h, false);
            // the original bitmap is recycled
            bitmap.recycle();
            return b;
        }
        return bitmap;
    }

    /**
     * Create the yuv buffer by Bitmap.
     * @param bitmap bitmap
     * @return yuv buffer
     */
    public static byte[] createBuffer(final Bitmap bitmap) {
        return createBuffer(bitmap.getWidth(), bitmap.getHeight());
    }

    /**
     * Create the yuv buffer.
     * @param width width
     * @param height height
     * @return yuv buffer
     */
    public static byte[] createBuffer(final int width, final int height) {
        int frameSize = frameSize(width, height);
        byte[] yuv = new byte[frameSize];
        return yuv;
    }

    /**
     * Converts the ARGB to YV12.
     * <p>
     *     Notice: This format assumes
     *     <ul>
     *      <li>an even width</li>
     *      <li>an even height</li>
     *      <li>a horizontal stride multiple of 16 pixels.</li>
     *     </ul>
     * </p>
     * @param outData output
     * @param argb RGB
     * @param width width
     * @param height height
     */
    public static void argbToYV12(final byte[] outData, final int[] argb, final int width, final int height) {
        nativeEncodeYV12(outData, argb, width, height);
    }

    /**
     * Converts the bitmap to YV12.
     * <p>
     *     Notice: This format assumes
     *     <ul>
     *      <li>an even width</li>
     *      <li>an even height</li>
     *      <li>a horizontal stride multiple of 16 pixels.</li>
     *     </ul>
     * </p>
     * @param bitmap bitmap
     * @param outData output
     */
    public static void bitmapToYV12(final Bitmap bitmap, final byte[] outData) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        int[] argb = new int[width * height];
        bitmap.getPixels(argb, 0, width, 0, 0, width, height);
        nativeEncodeYV12(outData, argb, width, height);
    }

    /**
     * Converts the ARGB to yuv420sp.
     * @param yuv420sp Array for storing output data
     * @param argb Array for argb
     * @param width width
     * @param height height
     */
    private static native void nativeEncodeYV12(final byte[] yuv420sp, final int[] argb, final int width, final int height);

    /**
     * Calculates the frame size from width and height.
     * @param width width
     * @param height height
     * @return frame size
     */
    private static int frameSize(int width, int height) {
        int yStride = roundUp(width, 16);
        int uvStride = roundUp(yStride / 2, 16);
        int ySize = yStride * height;
        int uvSize = uvStride * height / 2;
        return ySize + uvSize * 2;
    }

    /**
     * Rounded up number.
     * @param x number
     * @param alignment alignment
     * @return number
     */
    private static int roundUp(int x, int alignment) {
        return (int) Math.ceil(x / (double) alignment) * alignment;
    }
}
