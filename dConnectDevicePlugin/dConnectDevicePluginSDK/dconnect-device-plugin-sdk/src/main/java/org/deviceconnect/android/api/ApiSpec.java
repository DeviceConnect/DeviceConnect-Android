package org.deviceconnect.android.api;


import java.util.List;

public class ApiSpec {

    String mName;

    Method mMethod;

    String mPath;

    List<RequestParamSpec> mRequestParamList;

    public String getName() {
        return mName;
    }

    public Method getMethod() {
        return mMethod;
    }

    public String getPath() {
        return mPath;
    }

    public List<RequestParamSpec> getRequestParamList() {
        return mRequestParamList;
    }

    public enum Method {
        GET,
        PUT,
        POST,
        DELETE
    }
}
