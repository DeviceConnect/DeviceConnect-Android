package org.deviceconnect.android.uiapp.data;

import java.util.ArrayList;
import java.util.List;

public class DCApi {

    public enum Method {
        GET("GET"),
        PUT("PUT"),
        POST("POST"),
        DELETE("DELETE");

        private String mValue;

        Method(final String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }
        public static Method get(final String method) {
            for (Method m : values()) {
                if (m.mValue.equalsIgnoreCase(method)) {
                    return m;
                }
            }
            return null;
        }
    }

    private String mProfile;
    private Method mMethod;
    private String mPath;
    private List<DCParam> mDCParams = new ArrayList<>();
    private String mXType;

    public Method getMethod() {
        return mMethod;
    }

    public void setMethod(Method method) {
        mMethod = method;
    }

    public String getProfile() {
        return mProfile;
    }

    public void setProfile(String profile) {
        mProfile = profile;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public void addParameter(final DCParam param) {
        mDCParams.add(param);
    }

    public List<DCParam> getParameters() {
        return mDCParams;
    }

    public String getXType() {
        return mXType;
    }

    public void setXType(String XType) {
        mXType = XType;
    }
}
