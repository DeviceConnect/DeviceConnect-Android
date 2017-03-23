/*
 CanvasDrawImageObject.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.canvas;

import android.content.Intent;

import org.deviceconnect.profile.CanvasProfileConstants;

/**
 * Canvas Draw Image Object.
 *
 * @author NTT DOCOMO, INC.
 */
public class CanvasDrawImageObject {
    /**
     * Mode.
     */
    public enum Mode {
        /**
         * non-scale mode.
         */
        NON_SCALE_MODE,

        /**
         * scale mode.
         */
        SCALE_MODE,

        /**
         * fill mode.
         */
        FILL_MODE,
    }

    /**
     * Draw Canvas Action.
     */
    public static final String ACTION_DRAW_CANVAS = "org.deviceconnect.android.deviceplugin.host.canvas.DRAW";

    /**
     * Delete Canvas Action.
     */
    public static final String ACTION_DELETE_CANVAS = "org.deviceconnect.android.deviceplugin.host.canvas.DELETE";

    /**
     * datakind.
     */
    private static final String DATAKIND = "drawImage";

    /**
     * Intent Extra key. identifying value of data stored in the intent .
     */
    private static final String EXTRA_DATAKIND = "datakind";

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
    private String mData;

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
        mData = null;
        mMode = null;
        mX = 0.0;
        mY = 0.0;
    }

    /**
     * Constructor.
     *
     * @param data data
     * @param mode mode
     * @param x    x
     * @param y    y
     */
    public CanvasDrawImageObject(final String data, final Mode mode,
                                 final double x, final double y) {
        mData = data;
        mMode = mode;
        mX = x;
        mY = y;
    }

    /**
     * Gets a data of image.
     *
     * @return data
     */
    public String getData() {
        return mData;
    }

    /**
     * Gets a mode of rendering.
     *
     * @return mode
     */
    public Mode getMode() {
        return mMode;
    }

    /**
     * Gets a x position.
     *
     * @return x position
     */
    public double getX() {
        return mX;
    }

    /**
     * Gets a y position.
     *
     * @return y position
     */
    public double getY() {
        return mY;
    }

    /**
     * Sets a CanvasDrawImageObject's data to intent.
     *
     * @param intent intent
     */
    public void setValueToIntent(final Intent intent) {
        intent.putExtra(EXTRA_DATAKIND, DATAKIND);
        intent.putExtra(EXTRA_DATA, mData);
        intent.putExtra(EXTRA_MODE, mMode.ordinal());
        intent.putExtra(EXTRA_X, mX);
        intent.putExtra(EXTRA_Y, mY);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CanvasDrawImageObject o = (CanvasDrawImageObject) obj;
        if (!mData.equals(o.mData)) {
            return false;
        }
        if (!mMode.equals(o.mMode)) {
            return false;
        }
        return true;
    }

    /**
     * Create a CanvasDrawImageObject from intent.
     *
     * @param intent intent
     * @return CanvasDrawImageObject or null on error
     */
    public static CanvasDrawImageObject create(final Intent intent) {
        if (intent == null) {
            return null;
        }

        String kind = intent.getStringExtra(EXTRA_DATAKIND);
        if (!DATAKIND.equals(kind)) {
            return null;
        }

        CanvasDrawImageObject obj = new CanvasDrawImageObject();
        obj.mData = intent.getStringExtra(EXTRA_DATA);
        int modeOrdinal = intent.getIntExtra(EXTRA_MODE, Mode.NON_SCALE_MODE.ordinal());
        if (0 <= modeOrdinal && modeOrdinal < Mode.values().length) {
            obj.mMode = Mode.values()[modeOrdinal];
        } else {
            obj.mMode = Mode.values()[0];
        }
        obj.mX = intent.getDoubleExtra(EXTRA_X, 0.0);
        obj.mY = intent.getDoubleExtra(EXTRA_Y, 0.0);
        return obj;
    }

    /**
     * string mode convert to enum mode.
     *
     * @param mode string mode.
     * @return enum mode.
     */
    public static Mode convertMode(final String mode) {
        if (mode == null || mode.equals("")) {
            return Mode.NON_SCALE_MODE;
        } else if (mode.equals(CanvasProfileConstants.Mode.SCALES.getValue())) {
            return Mode.SCALE_MODE;
        } else if (mode.equals(CanvasProfileConstants.Mode.FILLS.getValue())) {
            return Mode.FILL_MODE;
        }
        return null;
    }
}
