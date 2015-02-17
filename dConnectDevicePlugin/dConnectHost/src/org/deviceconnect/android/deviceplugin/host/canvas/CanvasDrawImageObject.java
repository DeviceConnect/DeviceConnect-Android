/*
 CanvasDrawImageObject.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.canvas;

import org.deviceconnect.android.profile.util.CanvasProfileUtils;
import org.deviceconnect.profile.CanvasProfileConstants;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Canvas Draw Image Object.
 * 
 * @author NTT DOCOMO, INC.
 */
public class CanvasDrawImageObject implements CanvasDrawObjectInterface {
    
    /**
     * Mode.
     */
    public enum Mode {
        
        /**
         * non-scale mode.
         */
        NONSCALE_MODE,
        /**
         * scale mode.
         */
        SCALE_MODE,
        /**
         * fill mode.
         */
        FILL_MODE,
    };
    
    /**
     * datakind.
     */
    public static final String DATAKIND = "drawImage";
    
    /**
     * intent extra key(data).
     */
    private static final String EXTRA_DATA = "data";
    /**
     * intent extra key(mode).
     */
    private static final String EXTRA_MODE = "mode";
    /**
     * intent extra key(x).
     */
    private static final String EXTRA_X = "x";
    /**
     * intent extra key(y).
     */
    private static final String EXTRA_Y = "y";
    
    /**
     * data.
     */
    private byte[] mData;
    /**
     * mode.
     */
    private Mode mMode;
    /**
     * x.
     */
    private double mX;
    /**
     * y.
     */
    private double mY;
    
    /**
     * Constructor.
     */
    public CanvasDrawImageObject() {
        this.mData = null;
        this.mMode = null;
        this.mX = 0.0;
        this.mY = 0.0;
    }

    /**
     * Constructor.
     * @param data data
     * @param mode mode
     * @param x x
     * @param y y
     */
    public CanvasDrawImageObject(final byte[] data, final Mode mode, final double x, final double y) {
        this.mData = data;
        this.mMode = mode;
        this.mX = x;
        this.mY = y;
    }
    
    @Override
    public void getValueFromIntent(final Intent intent) {

        String dataKind = intent.getStringExtra(EXTRA_DATAKIND);
        if (dataKind == null || !dataKind.equals(DATAKIND)) {
            throw new IllegalStateException("datakind is not match.");
        }

        this.mData = intent.getByteArrayExtra(EXTRA_DATA);
        int modeOrdinal = intent.getIntExtra(EXTRA_MODE, Mode.NONSCALE_MODE.ordinal());
        if (0 <= modeOrdinal && modeOrdinal < Mode.values().length) {
            this.mMode = Mode.values()[modeOrdinal];
        } else {
            this.mMode = Mode.values()[0];
        }
        this.mX = intent.getDoubleExtra(EXTRA_X, 0.0);
        this.mY = intent.getDoubleExtra(EXTRA_Y, 0.0);
    }
    
    @Override
    public void setValueToIntent(final Intent intent) {
        
        intent.putExtra(EXTRA_DATAKIND, DATAKIND);
        
        intent.putExtra(EXTRA_DATA, this.mData);
        intent.putExtra(EXTRA_MODE, this.mMode.ordinal());
        intent.putExtra(EXTRA_X, this.mX);
        intent.putExtra(EXTRA_Y, this.mY);
    }

    /**
     * string mode convert to enum mode.
     * @param mode string mode.
     * @return enum mode.
     */
    public static Mode convertMode(final String mode) {
        if (mode == null || mode.equals("")) {
            return Mode.NONSCALE_MODE;
        } else if (mode.equals(CanvasProfileConstants.Mode.SCALES.getValue())) {
            return Mode.SCALE_MODE;
        } else if (mode .equals(CanvasProfileConstants.Mode.FILLS.getValue())) {
            return Mode.FILL_MODE;
        }
        return null;
    }

    @Override
    public void draw(final Bitmap viewBitmap) {
        
        if (viewBitmap == null) {
            return;
        }
        
        // draw data is nothing.
        if (mData == null) {
            return;
        }
         
        // data convert to bitmap.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(mData, 0, mData.length, options);
        
        // can't decode bitmap.
        if (bitmap == null) {
            return;
        }
        
        // draw image.
        if (mMode == Mode.NONSCALE_MODE) {
            CanvasProfileUtils.drawImageForNonScalesMode(viewBitmap, bitmap, mX, mY);
        } else if (mMode == Mode.SCALE_MODE) {
            CanvasProfileUtils.drawImageForScalesMode(viewBitmap, bitmap);
        } else if (mMode == Mode.FILL_MODE) {
            CanvasProfileUtils.drawImageForFillsMode(viewBitmap, bitmap);
        } else {
            // checking the mode value in HostCanvasProfile.java, here should you do not pass.
            return;
        }
    }
}
