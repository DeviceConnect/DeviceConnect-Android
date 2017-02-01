package org.deviceconnect.android.uiapp.utils;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;

public final class Utils {

    private Utils() {
    }

    public static Drawable convertToGrayScale(final Drawable drawable) {
        Drawable clone = drawable.getConstantState().newDrawable().mutate();
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0.2f);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        clone.setColorFilter(filter);
        return clone;
    }
}
