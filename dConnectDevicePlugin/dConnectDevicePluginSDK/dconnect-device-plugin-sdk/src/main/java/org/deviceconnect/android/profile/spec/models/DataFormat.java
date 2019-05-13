/*
 DataFormat.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

/**
 * データの拡張フォーマット指定.
 *
 * @author NTT DOCOMO, INC.
 */
public enum DataFormat {
    /**
     * 32bit 整数.
     */
    INT32("int32"),

    /**
     * 64bit 整数.
     */
    INT64("int64"),

    /**
     * 32bit 実数.
     */
    FLOAT("float"),

    /**
     * 64bit 実数.
     */
    DOUBLE("double"),

    /**
     * テキスト.
     */
    TEXT("text"),

    /**
     * バイトデータ.
     */
    BYTE("byte"),

    /**
     * バイナリデータ.
     */
    BINARY("binary"),

    /**
     * 日付.
     */
    DATE("date"),

    /**
     * 日付と時刻.
     */
    DATE_TIME("date-time"),

    /**
     * パスワード.
     */
    PASSWORD("password"),

    /**
     * RGB.
     */
    RGB("rgb");

    /**
     * フォーマット名.
     */
    private final String mName;

    /**
     * コンストラクタ.
     * @param name おfーマット名
     */
    DataFormat(final String name) {
        mName = name;
    }

    /**
     * フォーマット名を取得します.
     *
     * @return フォーマット名
     */
    public String getName() {
        return mName;
    }

    /**
     * 文字列からフォーマットを取得します.
     *
     * <p>
     * フォーマットがない場合には、null を返却します。
     * </p>
     *
     * @param name フォーマット名
     * @return フォーマット
     */
    public static DataFormat fromName(final String name) {
        for (DataFormat format : DataFormat.values()) {
            if (format.mName.equalsIgnoreCase(name)) {
                return format;
            }
        }
        return null;
    }
}
