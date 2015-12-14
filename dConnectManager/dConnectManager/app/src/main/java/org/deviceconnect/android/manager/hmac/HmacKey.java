/*
 HmacKey.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.hmac;

/**
 * HMAC Key.
 * @author NTT DOCOMO, INC.
 */
class HmacKey {

    /**
     * An origin of application.
     */
    private final String mOrigin;

    /**
     * A string expression of HMAC key.
     */
    private final String mKey;

    /**
     * Constructor.
     * 
     * @param origin An origin of application
     * @param key A string expression of HMAC key
     */
    HmacKey(final String origin, final String key) {
        if (origin == null) {
            throw new IllegalArgumentException("origin is null.");
        }
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }
        mOrigin = origin;
        mKey = key;
    }

    /**
     * Gets the origin of application.
     * @return The origin of application
     */
    String getOrigin() {
        return mOrigin;
    }

    /**
     * Get the string expression of HMAC key.
     * @return The string expression of HMAC key
     */
    String getKey() {
        return mKey;
    }

}
