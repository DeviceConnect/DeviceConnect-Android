package org.deviceconnect.android.profile.spec;


import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Device Connect APIの仕様を保持するクラス.
 * @author NTT DOCOMO, INC.
 */
public class DConnectApiSpec implements DConnectSpecConstants {

    private Type mType;
    private Method mMethod;
    private String mPath;
    private String mProfileName;
    private String mInterfaceName;
    private String mAttributeName;
    private DConnectParameterSpec[] mRequestParamList;

    private DConnectApiSpec() {}

    void setType(final Type type) {
        mType = type;
    }

    public Type getType() {
        return mType;
    }

    void setMethod(final Method method) {
        mMethod = method;
    }

    public Method getMethod() {
        return mMethod;
    }

    void setPath(final String path) {
        mPath = path;

        String[] array = path.split("/");
        mProfileName = array[2];
        if (array.length == 4) {
            mAttributeName = array[3];
        } else if (array.length == 5) {
            mInterfaceName = array[3];
            mAttributeName = array[4];
        }
    }

    public String getProfileName() {
        return mProfileName;
    }

    public String getInterfaceName() {
        return mInterfaceName;
    }

    public String getAttributeName() {
        return mAttributeName;
    }

    public String getPath() {
        return mPath;
    }

    void setRequestParamList(final DConnectParameterSpec[] paramList) {
        mRequestParamList = paramList;
    }

    public DConnectParameterSpec[] getRequestParamList() {
        return mRequestParamList;
    }

    public boolean validate(final Intent request) {
        Bundle extras = request.getExtras();
        for (DConnectParameterSpec paramSpec : getRequestParamList()) {
            Object paramValue = extras.get(paramSpec.getName());
            if (!paramSpec.validate(paramValue)) {
                return false;
            }
        }
        return true;
    }

    public static class Builder {
        private Type mType;
        private Method mMethod;
        private List<DConnectParameterSpec> mRequestParams;

        public Builder setType(final Type type) {
            mType = type;
            return this;
        }

        public Builder setMethod(final Method method) {
            mMethod = method;
            return this;
        }

        public Builder setRequestParamList(final List<DConnectParameterSpec> requestParamList) {
            mRequestParams = requestParamList;
            return this;
        }

        public DConnectApiSpec build() {
            if (mRequestParams == null) {
                mRequestParams = new ArrayList<DConnectParameterSpec>();
            }
            DConnectApiSpec spec = new DConnectApiSpec();
            spec.setType(mType);
            spec.setMethod(mMethod);
            spec.setRequestParamList(
                mRequestParams.toArray(new DConnectParameterSpec[mRequestParams.size()]));
            return spec;
        }
    }

}
