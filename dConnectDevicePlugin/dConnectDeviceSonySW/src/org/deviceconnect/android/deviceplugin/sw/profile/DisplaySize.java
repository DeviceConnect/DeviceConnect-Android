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
    public final int mWidth;
    /**
     * SW's Display Height.
     */
    public final int mHeight;

    public DisplaySize(int w, int h) {
        mWidth = w;
        mHeight = h;
    }
}
