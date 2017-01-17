/*
 DConnectHttpResponse.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.nanohttpd;

import org.deviceconnect.server.http.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Httpレスポンスのデータを格納するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class DConnectHttpResponse implements HttpResponse {

    private String mContentType;
    private Map<String, String> mHeaders = new HashMap<>();
    private InputStream mInputStream;
    private StatusCode mStatusCode = StatusCode.OK;
    private int mContentLength;

    DConnectHttpResponse() {
    }

    String getContentType() {
        return mContentType;
    }

    Map<String, String> getHeaders() {
        return mHeaders;
    }

    /**
     * コンテンツデータへのInputStreamを返します.
     * <p>
     * NanoHTTPD側で、InputStream#close()が呼び出されるので、注意すること。
     * </p>
     * @return コンテンツデータのストリーム
     */
    InputStream getInputStream() {
        if (mInputStream == null) {
            return new ByteArrayInputStream(new byte[0]);
        }
        return mInputStream;
    }

    int getContentLength() {
        return mContentLength;
    }

    StatusCode getStatusCode() {
        return mStatusCode;
    }

    @Override
    public void setContentType(final String contentType) {
        mContentType = contentType;
    }

    @Override
    public void setContentLength(int contentLength) {
        mContentLength = contentLength;
    }

    @Override
    public void setBody(final byte[] body) {
        mInputStream = new ByteArrayInputStream(body);
        mContentLength = body.length;
    }

    @Override
    public void setBody(final InputStream in) {
        mInputStream = in;
    }

    @Override
    public void addHeader(final String name, final String value) {
        mHeaders.put(name, value);
    }

    @Override
    public void setCode(final StatusCode code) {
        mStatusCode = code;
    }
}
