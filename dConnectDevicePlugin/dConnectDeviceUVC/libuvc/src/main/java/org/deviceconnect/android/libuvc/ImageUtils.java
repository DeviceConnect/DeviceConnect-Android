/*
 ImageUtils.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc;

import android.graphics.Bitmap;

/**
 * NDKとのインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
public final class ImageUtils {

    /**
     * YUY2 のフォーマットを定義します.
     * <p>
     * MEMO: NDK 側でも同じ定義をしているので注意すること。
     * </p>
     */
    private static final int FMT_YUY2 = 0;

    /**
     * NV12 のフォーマットを定義します.
     */
    private static final int FMT_NV12 = 1;

    /**
     * M420 のフォーマットを定義します.
     */
    private static final int FMT_M420 = 2;

    /**
     * I420 のフォーマットを定義します.
     */
    private static final int FMT_I420 = 3;

    private ImageUtils() {
    }

    /**
     * YUY2 のデータをBitmapに格納します.
     *
     * @param bitmap 格納先のBitmap
     * @param yuv YUY2のデータ
     * @param width 横幅
     * @param height 縦幅
     * @return 格納に成功した場合はtrue、それ以外はfalse
     */
    public static boolean decodeYUY2(final Bitmap bitmap, final byte[] yuv, final int width, final int height) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap is null.");
        }

        if (yuv == null) {
            throw new IllegalArgumentException("yuv is null.");
        }

        if (bitmap.getWidth() != width) {
            throw new IllegalArgumentException("width is invalid.");
        }

        if (bitmap.getHeight() != height) {
            throw new IllegalArgumentException("height is invalid.");
        }
        return nativeDecodeYUV(bitmap, yuv, width, height, FMT_YUY2);
    }

    /**
     * NV12 のデータをBitmapに格納します.
     *
     * @param bitmap 格納先のBitmap
     * @param yuv NV12のデータ
     * @param width 横幅
     * @param height 縦幅
     * @return 格納に成功した場合はtrue、それ以外はfalse
     */
    public static boolean decodeNV12(final Bitmap bitmap, final byte[] yuv, final int width, final int height) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap is null.");
        }

        if (yuv == null) {
            throw new IllegalArgumentException("yuv is null.");
        }

        if (bitmap.getWidth() != width) {
            throw new IllegalArgumentException("width is invalid.");
        }

        if (bitmap.getHeight() != height) {
            throw new IllegalArgumentException("height is invalid.");
        }
        return nativeDecodeYUV(bitmap, yuv, width, height, FMT_NV12);
    }

    /**
     * M420 のデータをBitmapに格納します.
     *
     * @param bitmap 格納先のBitmap
     * @param yuv M420のデータ
     * @param width 横幅
     * @param height 縦幅
     * @return 格納に成功した場合はtrue、それ以外はfalse
     */
    public static boolean decodeM420(final Bitmap bitmap, final byte[] yuv, final int width, final int height) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap is null.");
        }

        if (yuv == null) {
            throw new IllegalArgumentException("yuv is null.");
        }

        if (bitmap.getWidth() != width) {
            throw new IllegalArgumentException("width is invalid.");
        }

        if (bitmap.getHeight() != height) {
            throw new IllegalArgumentException("height is invalid.");
        }
        return nativeDecodeYUV(bitmap, yuv, width, height, FMT_M420);
    }

    /**
     * I420 のデータをBitmapに格納します.
     *
     * @param bitmap 格納先のBitmap
     * @param yuv I420のデータ
     * @param width 横幅
     * @param height 縦幅
     * @return 格納に成功した場合はtrue、それ以外はfalse
     */
    public static boolean decodeI420(final Bitmap bitmap, final byte[] yuv, final int width, final int height) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap is null.");
        }

        if (yuv == null) {
            throw new IllegalArgumentException("yuv is null.");
        }

        if (bitmap.getWidth() != width) {
            throw new IllegalArgumentException("width is invalid.");
        }

        if (bitmap.getHeight() != height) {
            throw new IllegalArgumentException("height is invalid.");
        }
        return nativeDecodeYUV(bitmap, yuv, width, height, FMT_I420);
    }

    /**
     * YUV420SemiPlannerのデータをBitmapに格納します.
     *
     * @param bitmap 格納先のBitmap
     * @param yuv420sp YUV420SemiPlannerのデータ
     * @param width 横幅
     * @param height 縦幅
     */
    public static void decodeYUV420SP(Bitmap bitmap, byte[] yuv420sp, int width, int height) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap is null.");
        }

        if (yuv420sp == null) {
            throw new IllegalArgumentException("yuv420sp is null.");
        }

        if (bitmap.getWidth() != width) {
            throw new IllegalArgumentException("width is invalid.");
        }

        if (bitmap.getHeight() != height) {
            throw new IllegalArgumentException("height is invalid.");
        }
        nativeDecodeYUV420SP(bitmap, yuv420sp, width, height);
    }

    public static void copyJpegHuffTable(byte[] src, int srcLength, byte[] dest) {
        if (srcLength + 432 > dest.length) {
            throw new IllegalArgumentException("dest array size is small.");
        }
        nativeCopyJpeg(src, srcLength, dest);
    }

    private static native boolean nativeDecodeYUV(Bitmap bitmap, byte[] yuv422, int width, int height, int type);
    private static native void nativeDecodeYUV420SP(Bitmap bitmap, byte[] yuv420sp, int width, int height);
    private static native void nativeCopyJpeg(byte[] src, int srcLength, byte[] dest);
}
