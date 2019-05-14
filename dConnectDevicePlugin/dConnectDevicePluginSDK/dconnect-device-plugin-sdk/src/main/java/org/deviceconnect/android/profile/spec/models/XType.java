/*
 Tag.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

/**
 * Operation の API タイプ.
 *
 * <p>
 * Device Connect で拡張した定義。<br>
 * x-type
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public enum XType {
    /**
     * ワンショット.
     *
     * <p>
     * リクエストに対するレスポンスを返却するワンショット機能を提供する API タイプ。
     * </p>
     */
    ONESHOT("one-shot"),

    /**
     * イベント.
     *
     * <p>
     * Device Connect に接続した WebSocket に対してイベントの送信の開始・停止を提供する API タイプ。
     * </p>
     */
    EVENT("event"),

    /**
     * ストリーミング.
     *
     * <p>
     * レスポンスに映像などのストリーミングデータへの URL を返却する API タイプ。
     * </p>
     */
    STREAMING("streaming");

    private String mName;

    XType(final String name) {
        mName = name;
    }

    /**
     * API のタイプ名を取得します.
     *
     * @return API のタイプ名
     */
    public String getName() {
        return mName;
    }

    /**
     * 文字列から API タイプを取得します.
     *
     * <p>
     * 文字列に対応する API タイプが存在しない場合は null を返却します。
     * </p>
     *
     * @param value 文字列
     * @return API タイプ
     */
    public static XType parse(final String value) {
        for (XType type : values()) {
            if (type.mName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
