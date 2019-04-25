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
    /**
     * GET メソッド.
     */
    GET("GET"),

    /**
     * PUT メソッド.
     */
    PUT("PUT"),

    /**
     * POST メソッド.
     */
    POST("POST"),

    /**
     * DELETE メソッド.
     */
    DELETE("DELETE"),

    /**
     * OPTIONS メソッド.
     * <p>
     * Device Connect では使用しないが定義だけしておく。
     * </p>
     */
    OPTIONS("OPTIONS"),

    /**
     * HEAD メソッド.
     * <p>
     * Device Connect では使用しないが定義だけしておく。
     * </p>
     */
    HEAD("HEAD"),

    /**
     * PATCH メソッド.
     * <p>
     * Device Connect では使用しないが定義だけしておく。
     * </p>
     */
    PATCH("PATCH");

    private String mName;

    Method(final String name) {
        mName = name;
    }

    /**
     * HTTPメソッド名を取得します.
     *
     * <p>
     * HTTPメソッドは、全て大文字で返却されます。
     * </p>
     *
     * @return HTTPメソッド名
     */
    public String getName() {
        return mName;
    }

    /**
     * 文字列から HTTP メソッドを取得します.
     *
     * @param value HTTPメソッドの文字列
     * @return HTTPメソッド
     */
    public static Method parse(final String value) {
        for (Method method : values()) {
            if (method.mName.equalsIgnoreCase(value)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Device Connect のアクションからHTTPメソッドを取得します.
     *
     * @param action Device Connect のアクション
     * @return HTTPメソッド
     */
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
}
