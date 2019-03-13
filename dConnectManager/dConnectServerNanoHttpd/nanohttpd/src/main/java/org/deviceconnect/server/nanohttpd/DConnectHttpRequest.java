/*
 DConnectHttpRequest.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.nanohttpd;

import org.deviceconnect.server.http.HttpRequest;

import java.util.Map;

/**
 * Httpリクエストのデータを格納するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class DConnectHttpRequest implements HttpRequest {

    private Method mMethod;
    private String mUri;
    private String mQueryString;
    private String mRemoteIpAddress;
    private String mRemoteHostName;
    private Map<String, String> mHeaders;
    private Map<String, String> mQuery;
    private Map<String, String> mFiles;

    void setMethod(final Method method) {
        mMethod = method;
    }

    void setUri(final String uri) {
        mUri = uri;
    }

    void setHeaders(final Map<String, String> headers) {
        mHeaders = headers;
    }

    void setQuery(final Map<String, String> query) {
        mQuery = query;
    }

    void setQueryString(final String queryString) {
        mQueryString = queryString;
    }

    void setRemoteIpAddress(final String ipAddress) {
        mRemoteIpAddress = ipAddress;
    }

    void setRemoteHostName(final String hostName) {
        mRemoteHostName = hostName;
    }

    void setFiles(final Map<String, String> files) {
        mFiles = files;
    }

    @Override
    public Method getMethod() {
        return mMethod;
    }

    @Override
    public String getUri() {
        return mUri;
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return mQuery;
    }

    @Override
    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    @Override
    public Map<String, String> getFiles() {
        return mFiles;
    }

    @Override
    public String getQueryString() {
        return mQueryString;
    }

    @Override
    public String getRemoteIpAddress() {
        return mRemoteIpAddress;
    }

    @Override
    public String getRemoteHostName() {
        return mRemoteHostName;
    }

    @Override
    public String toString() {
        return mMethod + " " + mUri + "\nQuery: " + mQueryString + "\nHeaders: " + mHeaders + "\nFiles: " + mFiles;
    }
}
