/*
 Tag.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

/**
 * Device Connect の API の種類.
 * <p>
 * 拡張: x-type
 * </p>
 */
public enum XType {
    /**
     * ワンショット.
     */
    ONESHOT("one-shot"),

    /**
     * イベント.
     */
    EVENT("event"),

    /**
     * ストリーミング.
     */
    STREAMING("streaming");

    private String mName;

    XType(final String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public static XType parse(final String value) {
        for (XType type : values()) {
            if (type.mName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
