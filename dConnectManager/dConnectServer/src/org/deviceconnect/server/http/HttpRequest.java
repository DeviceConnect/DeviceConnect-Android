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
public final class HttpRequest {

    /** HTTPメソッド GET. */
    public static final String HTTP_METHOD_GET = "GET";

    /** HTTPメソッド POST. */
    public static final String HTTP_METHOD_POST = "POST";

    /** HTTPメソッド PUT. */
    public static final String HTTP_METHOD_PUT = "PUT";

    /** HTTPメソッド DELETE. */
    public static final String HTTP_METHOD_DELETE = "DELETE";

    /** HTTPメソッド. */
    private String mMethod;

    /** リクエストURI. */
    private String mUri;

    /** ヘッダー群. */
    private Map<String, String> mHeaders;

    /** HTTPリクエストのBodyデータ. */
    private byte[] mBody;

    /**
     * HTTPメソッドを取得する.
     * 
     * @return HTTPメソッド名
     */
    public String getMethod() {
        return mMethod;
    }

    /**
     * HTTPメソッド名を設定する.
     * 
     * @param method HTTPメソッド名
     */
    public void setMethod(final String method) {
        this.mMethod = method;
    }

    /**
     * リクエストURIを取得する.
     * 
     * @return uri リクエストURI
     */
    public String getUri() {
        return mUri;
    }

    /**
     * リクエストURIを設定する.
     * 
     * @param uri 設定するURI
     */
    public void setUri(final String uri) {
        this.mUri = uri;
    }

    /**
     * ヘッダーを取得する.
     * 
     * @return ヘッダー
     */
    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    /**
     * ヘッダーを設定する.
     * 
     * @param headers 設定するヘッダーのデータ
     */
    public void setHeaders(final Map<String, String> headers) {
        this.mHeaders = headers;
    }

    /**
     * HTTPリクエストのBodyデータを取得する.
     * 
     * @return HTTPリクエストのBodyデータ
     */
    public byte[] getBody() {
        return mBody;
    }

    /**
     * HTTPリクエストのBodyデータを設定する.
     * 
     * @param body HTTPリクエストのBodyデータ
     */
    public void setBody(final byte[] body) {
        this.mBody = body;
    }

}
