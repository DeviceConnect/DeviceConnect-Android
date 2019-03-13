/*
 HttpResponse.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.http;

import java.io.InputStream;
import java.util.Map;

/**
 * Httpレスポンスのデータを保持するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HttpResponse {

    /**
     * HTTPレスポンスのステータスコード定数.
     * 
     * @author NTT DOCOMO, INC.
     */
    enum StatusCode {
        /** Continue. */
        CONTINUE(100),
        /** Switching Protocols. */
        SWITCHING_PROTOCOLS(101),
        /** OK. */
        OK(200),
        /** Created. */
        CREATED(201),
        /** Accepted. */
        ACCEPTED(202),
        /** Non Authoritative Information. */
        NON_AUTHORITATIVE_INFOMATIN(203),
        /** No Content. */
        NO_CONTENT(204),
        /** Reset Content. */
        RESET_CONTENT(205),
        /** Partial Content. */
        PARTIAL_CONTENT(206),
        /** Multiple Choices. */
        MULTIPLE_CHOICES(300),
        /** Moved Permanentry. */
        MOVED_PERMANENTLY(301),
        /** Moved Temporarily. */
        MOVED_TEMPORARILY(302),
        /** See Other. */
        SEE_OTHER(303),
        /** Not Modified. */
        NOT_MODIFIED(304),
        /** Use Proxy. */
        USE_PROXY(305),
        /** Bad Request. */
        BAD_REQUEST(400),
        /** Unauthorized. */
        UNAUTHORIZED(401),
        /** Payment Required. */
        PAYMENT_REQUIRED(402),
        /** Forbidden. */
        FORBIDDEN(403),
        /** Not Found. */
        NOT_FOUND(404),
        /** Method Not Allowed. */
        METHOD_NOT_ALLOWED(405),
        /** Not Acceptable. */
        NOT_ACCEPTABLE(406),
        /** Proxy Authentication Required. */
        PROXY_AUTHENTICATION_REQUIRED(407),
        /** Request Time Out. */
        REQUEST_TIME_OUT(408),
        /** Confict. */
        CONFICT(409),
        /** GONE. */
        GONE(410),
        /** Length Required. */
        LENGTH_REQUIRED(411),
        /** Precondition Failed. */
        PRECONDITION_FAILED(412),
        /** Request Entry Too Large. */
        REQUEST_ENTRY_TOO_LARGE(413),
        /** Request URI Too Large. */
        REQUEST_URI_TOO_LARGE(414),
        /** Unsupported Media Type. */
        UNSUPPORTED_MEDIA_TYPE(415),
        /** Request Range Not Satisfiable. */
        REQUEST_RANGE_NOT_SATISFIABLE(416),
        /** Expectation Failed. */
        EXPECTATION_FAILED(417),
        /** Internal Server Error. */
        INTERNAL_SERVER_ERROR(500),
        /** Not Implemented. */
        NOT_IMPLEMENTED(501),
        /** Bad Gateway. */
        BAD_GATEWAY(502),
        /** Service Unavailable. */
        SERVICE_UNAVAILABLE(503),
        /** Gateway Time Out. */
        GATEWAY_TIME_OUT(504),
        /** HTTP Version Not Supported. */
        HTTP_VERSION_NOT_SUPPORTED(505);

        /** ステータスコード. */
        private final int mStatusCode;

        /**
         * コンストラクタ.
         * 
         * @param code ステータスコード値
         */
        StatusCode(final int code) {
            mStatusCode = code;
        }

        /**
         * ステータスコード値を取得する.
         * 
         * @return ステータスコード
         */
        public int getCode() {
            return mStatusCode;
        }
    }

    /**
     * Content-Typeを設定する.
     * 
     * @param contentType 設定するContent-Type
     */
    void setContentType(final String contentType);

    /**
     * Content-Typeを取得する.
     * @return 設定するContent-Type
     */
    String getContentType();

    /**
     * Content-Lengthを設定する.
     * @param contentLength コンテンツのサイズ
     */
    void setContentLength(final int contentLength);

    /**
     * Content-Lengthの値を取得する.
     * @return Content-Lengthの値
     */
    int getContentLength();

    /**
     * Bodyにデータを設定する.
     * 
     * @param body 設定するBodyのデータ
     */
    void setBody(final byte[] body);

    /**
     * Bodyにデータを設定する.
     * @param in 設定するデータのストリーム
     */
    void setBody(final InputStream in);

    /**
     * レスポンスにヘッダーを追加する.
     *
     * <p>
     * 当メソッドではContent-Typeを追加しない。
     * Content-Typeを設定する場合はsetContentTypeを使うこと。
     * </p>
     *
     * @param name ヘッダー名
     * @param value 値
     */
    void addHeader(final String name, final String value);

    /**
     * ヘッダー一覧を取得する.
     * @return ヘッダー一覧
     */
    Map<String, String> getHeaders();

    /**
     * ステータスコードを設定する.
     * 
     * @param code ステータスコード
     */
    void setCode(final StatusCode code);

    /**
     * ステータスコードを取得する.
     * @return ステータスコード
     */
    StatusCode getStatusCode();

    byte[] getBody();
}
