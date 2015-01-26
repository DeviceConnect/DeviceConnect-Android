/*
 DisplaySize.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw.profile;

/**
 * SWの画面サイズ.
 */
class DisplaySize {
    /**
     * SW's Display Width.
     */
    private final int mWidth;
    /**
     * SW's Display Height.
     */
    private final int mHeight;

    /**
     * Constructor.
     * @param w Width
     * @param h height
     */
    public DisplaySize(final int w, final int h) {
        mWidth = w;
        mHeight = h;
    }
    /**
     * Get Width.
     * @return width
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Get Height.
     * @return height
     */
    public int getHeight() {
        return mHeight;
    }
}
