/*
 LiteralOrigin.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;

/**
 * An origin which is checked literally.
 * 
 * @author NTT DOCOMO, INC.
 */
class LiteralOrigin implements Origin {

    /**
     * The origin.
     */
    private final String mOrigin;

    /**
     * Constructor.
     * 
     * @param originExp the string expression of an origin
     */
    LiteralOrigin(final String originExp) {
        mOrigin = originExp;
    }

    @Override
    public boolean matches(final Origin origin) {
        if (!(origin instanceof LiteralOrigin)) {
            return false;
        }
        return mOrigin.equals(((LiteralOrigin) origin).mOrigin);
    }

    @Override
    public String toString() {
        return mOrigin;
    }

}
