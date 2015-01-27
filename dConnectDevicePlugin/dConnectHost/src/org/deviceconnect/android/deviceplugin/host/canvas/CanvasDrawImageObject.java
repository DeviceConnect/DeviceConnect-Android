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
    
    // TODO: CanvasProfileConstants.Mode に置き換える
    public enum Mode {
        NONSCALE_MODE,
        SCALE_MODE,
        FILL_MODE,
    };
    
    public static final String DATAKIND = "drawImage";
    
    private static final String EXTRA_DATA = "data";
    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_X = "x";
    private static final String EXTRA_Y = "y";
    
    public byte[] data;
    public Mode mode;
    public double x;
    public double y;
    
    public CanvasDrawImageObject() {
        this.data = null;
        this.mode = null;
        this.x = 0.0;
        this.y = 0.0;
    }
    
    public CanvasDrawImageObject(final byte[] data, final Mode mode, final double x, final double y) {
        this.data = data;
        this.mode = mode;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public void getValueFromIntent(final Intent intent) {

        String dataKind = intent.getStringExtra(EXTRA_DATAKIND);
        if (dataKind == null || !dataKind.equals(DATAKIND)) {
            throw new IllegalStateException("datakind is not match.");
        }

        this.data = intent.getByteArrayExtra(EXTRA_DATA);
        int modeOrdinal = intent.getIntExtra(EXTRA_MODE, Mode.NONSCALE_MODE.ordinal());
        if (0 <= modeOrdinal && modeOrdinal < Mode.values().length) {
            this.mode = Mode.values()[modeOrdinal];
        } else {
            this.mode = Mode.values()[0];
        }
        this.x = intent.getDoubleExtra(EXTRA_X, 0.0);
        this.y = intent.getDoubleExtra(EXTRA_Y, 0.0);
    }
    
    @Override
    public void setValueToIntent(final Intent intent) {
        
        intent.putExtra(EXTRA_DATAKIND, DATAKIND);
        
        intent.putExtra(EXTRA_DATA, this.data);
        intent.putExtra(EXTRA_MODE, this.mode.ordinal());
        intent.putExtra(EXTRA_X, this.x);
        intent.putExtra(EXTRA_Y, this.y);
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
    public void draw(Bitmap viewBitmap) {
        
        if (viewBitmap == null) {
            return;
        }
        
        /* draw data is nothing. */
        if (data == null) {
            return;
        }
        
        /* data convert to bitmap. */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        
        /* draw image. */
        if (mode == Mode.NONSCALE_MODE) {
            CanvasProfileUtils.drawImageForNonScalesMode(viewBitmap, bitmap, x, y);
        } else if (mode == Mode.SCALE_MODE) {
            CanvasProfileUtils.drawImageForScalesMode(viewBitmap, bitmap);
        } else if (mode == Mode.FILL_MODE) {
            CanvasProfileUtils.drawImageForFillsMode(viewBitmap, bitmap);
        } else {
            // checking the mode value in HostCanvasProfile.java, here should you do not pass.
        }
    }
}
