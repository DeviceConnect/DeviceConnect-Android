/*
 In.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

/**
 * パラメータの格納場所.
 *
 * @author NTT DOCOMO, INC.
 */
public enum In {
    /**
     * クエリーにパラメータが格納されます.
     */
    QUERY("query"),

    /**
     * ヘッダーにパラメータが格納されます.
     */
    HEADER("header"),

    /**
     * パスにパラメータが格納されます.
     */
    PATH("path"),

    /**
     * フォームにパラメータが格納されます.
     */
    FORM("formData"),

    /**
     * ボディにパラメータが格納されます.
     */
    BODY("body");

    private String mName;

    In(final String name) {
        mName = name;
    }

    /**
     * パラメータの格納場所名を取得します.
     *
     * @return パラメータの格納場所名
     */
    public String getName() {
        return mName;
    }

    /**
     * 文字列からパラメータの格納場所を取得します.
     *
     * @param value パラメータの格納場所名
     * @return パラメータの格納場所
     */
    public static In parse(final String value) {
        for (In in : values()) {
            if (in.mName.equalsIgnoreCase(value)) {
                return in;
            }
        }
        return null;
    }
}
