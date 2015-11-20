package org.deviceconnect.android.deviceplugin.theta.utils;


import android.graphics.Bitmap;
import android.graphics.Matrix;

public final class BitmapUtils {


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
