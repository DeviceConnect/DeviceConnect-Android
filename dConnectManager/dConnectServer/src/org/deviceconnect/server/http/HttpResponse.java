/*
 HttpResponse.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Httpレスポンスのデータを保持するクラス.
 * <p>
 * デフォルトでステータスコードはOK(200)に設定されている。
 * </p>
 * 
 * @author NTT DOCOMO, INC.
 */
public final class HttpResponse {

    /** Content-Type. */
    private String mContentType;

    /** body. */
    private byte[] mBody;

    /** ヘッダー群. */
    private Map<String, String> mHeaders;

    /** ステータスコード. */
    private StatusCode mCode;

    /**
     * HTTPレスポンスのステータスコード定数.
     * 
     * @author NTT DOCOMO, INC.
     * 
     */
    public static enum StatusCode {
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
        private StatusCode(final int code) {
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
     * コンストラクタ.
     */
    public HttpResponse() {
        mHeaders = new HashMap<String, String>();
        mCode = StatusCode.OK;
    }

    /**
     * Content-Typeを取得する.
     * 
     * @return Content-Type
     */
    public String getContentType() {
        return mContentType;
    }

    /**
     * Content-Typeを設定する.
     * 
     * @param contentType 設定するContent-Type
     */
    public void setContentType(final String contentType) {
        this.mContentType = contentType;
    }

    /**
     * Bodyを取得する.
     * 
     * @return Bodyのデータ
     */
    public byte[] getBody() {
        return mBody;
    }

    /**
     * Bodyを設定する.
     * 
     * @param body 設定するBodyのデータ
     */
    public void setBody(final byte[] body) {
        this.mBody = body;
    }

    /**
     * レスポンスにヘッダーを追加する.
     * 当メソッドではContent-Typeを追加しない。Content-Typeを設定する場合はsetContentTypeを使うこと。
     * 
     * @param name ヘッダー名
     * @param value 値
     */
    public void addHeader(final String name, final String value) {

        if (name == null || value == null) {
            String argName = name == null ? "name" : "value";
            throw new IllegalArgumentException(argName + " must not be null.");
        }

        // Content-Typeは別に変数としてセットさせるため、ヘッダーには入れさせない
        if ("content-type".equals(name.toLowerCase())) {
            return;
        }

        mHeaders.put(name, value);
    }

    /**
     * ヘッダーを取得する.
     * 
     * @return ヘッダーのマップオブジェクト。
     */
    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    /**
     * ステータスコードを取得する.
     * 
     * @return ステータスコード
     */
    public StatusCode getCode() {
        return mCode;
    }

    /**
     * ステータスコードを設定する.
     * 
     * @param code ステータスコード
     */
    public void setCode(final StatusCode code) {
        this.mCode = code;
    }
}
