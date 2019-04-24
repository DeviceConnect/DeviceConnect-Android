/*
 Method.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * APIのメソッド名.
 *
 * @author NTT DOCOMO, INC.
 */
public enum Method {

    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS"),
    HEAD("HEAD"),
    PATCH("PATCH");

    private String mName;

    Method(final String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public static Method parse(final String value) {
        for (Method method : values()) {
            if (method.mName.equalsIgnoreCase(value)) {
                return method;
            }
        }
        return null;
    }

    public static Method fromAction(final String action) {
        if (IntentDConnectMessage.ACTION_GET.equals(action)) {
            return GET;
        } else if (IntentDConnectMessage.ACTION_PUT.equals(action)) {
            return PUT;
        } else if (IntentDConnectMessage.ACTION_POST.equals(action)) {
            return POST;
        } else if (IntentDConnectMessage.ACTION_DELETE.equals(action)) {
            return DELETE;
        }
        return null;
    }

    public boolean isValid(final String name) {
        return parse(name) != null;
    }
}
