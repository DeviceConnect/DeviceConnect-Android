package org.deviceconnect.android.deviceplugin.theta.utils;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v4.util.LruCache;

public final class BitmapUtils {

    /**
     * Cache size of thumbnail.
     *
     * 100 thumbnails will be cached.
     *
     * Unit: byte.
     */
    private static final int THUMBNAIL_CACHE_SIZE = (5 * 1024 * 1024) * 100;
    private static LruCache<String, Bitmap> mThumbnailCache = new LruCache<String, Bitmap>(THUMBNAIL_CACHE_SIZE) {
        @Override
        protected int sizeOf(final String key, final Bitmap value) {
            return value.getByteCount() / 1024;
        }
    };

    public static synchronized Bitmap getBitmapCache(final String key) {
        return mThumbnailCache.get(key);
    }

    public static synchronized void putBitmapCache(final String key, final Bitmap value) {
        mThumbnailCache.put(key, value);
    }
    public static Bitmap resize(final Bitmap b, final int newWidth, final int newHeight) {
        if (b == null) {
            return null;
        }

        int oldWidth = b.getWidth();
        int oldHeight = b.getHeight();
        if (oldWidth == newWidth && oldHeight == newHeight) {
            return b;
        }
        float scaleWidth = ((float) newWidth) / oldWidth;
        float scaleHeight = ((float) newHeight) / oldHeight;
        float scaleFactor = Math.min(scaleWidth, scaleHeight);
        Matrix scale = new Matrix();
        scale.postScale(scaleFactor, scaleFactor);

        Bitmap result = Bitmap.createBitmap(b, 0, 0, oldWidth, oldHeight, scale, false);
        b.recycle();
        return result;
    }

    private BitmapUtils() {
    }
}
