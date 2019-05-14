/*
 DataType.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

/**
 * データの種類.
 *
 * @author NTT DOCOMO, INC.
 */
public enum DataType {
    /**
     * 配列.
     */
    ARRAY("array"),

    /**
     * ブール値.
     */
    BOOLEAN("boolean"),

    /**
     * 整数.
     */
    INTEGER("integer"),

    /**
     * 実数.
     */
    NUMBER("number"),

    /**
     * 文字列.
     */
    STRING("string"),

    /**
     * ファイル.
     */
    FILE("file");

    /**
     * データ名.
     */
    private final String mName;

    /**
     * コンストラクタ.
     * @param name データ名
     */
    DataType(final String name) {
        mName = name;
    }

    /**
     * データ名を取得します.
     *
     * @return データ名
     */
    public String getName() {
        return mName;
    }

    /**
     * 文字列からデータの種類を取得します.
     *
     * @param name データ名
     * @return データの種類
     */
    public static DataType fromName(final String name) {
        for (DataType type : DataType.values()) {
            if (type.mName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
