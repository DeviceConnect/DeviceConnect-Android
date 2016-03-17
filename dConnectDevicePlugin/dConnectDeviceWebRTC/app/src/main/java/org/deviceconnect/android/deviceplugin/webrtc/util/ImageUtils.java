/*
 ImageUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.util;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

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

    public static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    public static void decodeYUV420SP(Bitmap bitmap, byte[] yuv420sp, int width, int height) {
        nativeDecodeYUV420SP(bitmap, yuv420sp, width, height);
    }

    private static native void nativeDecodeYUV420SP(Bitmap bitmap, byte[] yuv420sp, int width, int height);


    public static void decodeYUV420SP2(Bitmap bitmap, ByteBuffer yuv420sp, int width, int height) {
        nativeDecodeYUV420SP2(bitmap, yuv420sp, width, height);
    }

    private static native void nativeDecodeYUV420SP2(Bitmap bitmap, ByteBuffer yuv420sp, int width, int height);

    public static void decodeYUV420SP3(Bitmap bitmap, ByteBuffer[] yuv420sp, int width, int height, int[] strides) {
        if (strides[0] != width) {
            nativeDecodeYUV420SP4(bitmap, yuv420sp, width, height, strides);
        } else if (strides[1] != width / 2) {
            nativeDecodeYUV420SP4(bitmap, yuv420sp, width, height, strides);
        } else if (strides[2] != width / 2) {
            nativeDecodeYUV420SP4(bitmap, yuv420sp, width, height, strides);
        } else {
            nativeDecodeYUV420SP3(bitmap, yuv420sp, width, height);
        }
    }

    private static native void nativeDecodeYUV420SP3(Bitmap bitmap, ByteBuffer[] yuv420sp, int width, int height);
    private static native void nativeDecodeYUV420SP4(Bitmap bitmap, ByteBuffer[] yuv420sp, int width, int height, int[] strides);
}
