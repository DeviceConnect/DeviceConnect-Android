package org.deviceconnect.android.profile.spec;


import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.ArrayList;
import java.util.List;

public class DConnectApiSpec {

    private String mName;

    private Method mMethod;

    private String mPath;

    private List<DConnectRequestParamSpec> mRequestParamList = new ArrayList<DConnectRequestParamSpec>();

    public DConnectApiSpec(final String name, final String method, final String path) {
        mName = name;
        mMethod = Method.parse(method);
        mPath = path;
        if (mMethod == null) {
            throw new IllegalArgumentException("method is invalid: " + method);
        }
    }

    public String getName() {
        return mName;
    }

    public Method getMethod() {
        return mMethod;
    }

    public String getPath() {
        return mPath;
    }

    public List<DConnectRequestParamSpec> getRequestParamList() {
        return mRequestParamList;
    }

    public void addRequestParam(final DConnectRequestParamSpec requestParam) {
        mRequestParamList.add(requestParam);
    }

    public enum Method {

        GET("GET"),
        PUT("PUT"),
        POST("POST"),
        DELETE("DELETE");

        private String mName;

        Method(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public static Method parse(final String value) {
            for (Method method : values()) {
                if (method.mName.equalsIgnoreCase(value)) {
                    return method;
                }
            }
            return null;
        }

        public static Method fromAction(final String action) {
            if (IntentDConnectMessage.ACTION_GET.equals(action)) {
                return GET;
            } else if (IntentDConnectMessage.ACTION_PUT.equals(action)) {
                return PUT;
            } else if (IntentDConnectMessage.ACTION_POST.equals(action)) {
                return POST;
            } else if (IntentDConnectMessage.ACTION_DELETE.equals(action)) {
                return DELETE;
            }
            return null;
        }

        public boolean isValid(final String name) {
            return parse(name) != null;
        }
    }
}
