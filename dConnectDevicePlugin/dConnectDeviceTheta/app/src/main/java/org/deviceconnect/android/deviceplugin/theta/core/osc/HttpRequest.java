package org.deviceconnect.android.deviceplugin.theta.core.osc;


class HttpRequest {

    private final Method mMethod;

    private final String mUri;

    private String mBody = "";

    HttpRequest(final Method method, final String uri) {
        mMethod = method;
        mUri = uri;
    }
    HttpRequest(final Method method, final String host, final String path) {
        mMethod = method;
        mUri = "http://" + host + path;
    }

    public Method getMethod() {
        return mMethod;
    }

    public String getUri() {
        return mUri;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(final String body) {
        mBody = body;
    }

    public enum Method {

        GET,

        POST

    }


}
