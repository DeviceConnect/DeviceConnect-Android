/*
 HttpRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.http;

import java.util.Map;

/**
 * Httpリクエストのデータを保持するクラス.
 * 
 * @author NTT DOCOMO, INC.
 */
public interface HttpRequest {

    /**
     * Httpリクエストのメソッドを定義する.
     *
     * @author NTT DOCOMO, INC.
     */
    enum Method {
        /**
         * GETメソッドを定義する.
         */
        GET("GET"),

        /**
         * POSTメソッドを定義する.
         */
        POST("POST"),

        /**
         * PUTメソッドを定義する.
         */
        PUT("PUT"),

        /**
         * DELETEメソッドを定義する.
         */
        DELETE("DELETE");

        private String mValue;

        Method(final String value) {
            mValue = value;
        }

        public static Method valueFrom(final String value) {
            for (Method m : values()) {
                if (m.mValue.equalsIgnoreCase(value)) {
                    return m;
                }
            }
            return null;
        }
    }

    /**
     * HTTPメソッドを取得する.
     *
     * @return HTTPメソッド名
     */
    Method getMethod();

    /**
     * リクエストURIを取得する.
     * <p>
     *     以下のようなURIを取得できます。
     *     <ul><li>/gotapi/serviceDiscovery</li></ul>
     * </p>
     * @return uri リクエストURI
     */
    String getUri();

    /**
     * ヘッダーを取得する.
     *
     * @return ヘッダー一覧
     */
    Map<String, String> getHeaders();

    /**
     * マルチパートで送られてきたファイルを取得する.
     *
     * @return ファイル一覧
     */
    Map<String, String> getFiles();

    /**
     * パラメータ引数一覧を取得する.
     * @return パラメータ一覧
     */
    Map<String, String> getQueryParameters();

    /**
     * パラメータの文字列を取得する.
     * @return パラメータ
     */
    String getQueryString();

    /**
     * 接続先の IP アドレスを取得する.
     * @return 接続先の IP アドレス
     */
    String getRemoteIpAddress();

    /**
     * 接続先の Host 名を取得する.
     * @return 接続先の Host 名
     */
    String getRemoteHostName();
}
