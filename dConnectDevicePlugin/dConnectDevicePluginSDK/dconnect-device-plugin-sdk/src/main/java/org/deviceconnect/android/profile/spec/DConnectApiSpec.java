package org.deviceconnect.android.profile.spec;


import android.content.Intent;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DConnectApiSpec implements DConnectApiSpecConstants {

    private static final String KEY_X_TYPE = "x-type";
    private static final String KEY_PARAMETERS = "parameters";

    private Type mType;
    private Method mMethod;
    private String mPath;
    private String mProfileName;
    private String mInterfaceName;
    private String mAttributeName;
    private DConnectRequestParamSpec[] mRequestParamList;

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

    void setRequestParamList(final DConnectRequestParamSpec[] paramList) {
        mRequestParamList = paramList;
    }

    public DConnectRequestParamSpec[] getRequestParamList() {
        return mRequestParamList;
    }

    public boolean validate(final Intent request) {
        Bundle extras = request.getExtras();
        for (DConnectRequestParamSpec paramSpec : getRequestParamList()) {
            Object paramValue = extras.get(paramSpec.getName());
            if (!paramSpec.validate(paramValue)) {
                return false;
            }
        }
        return true;
    }

    public static DConnectApiSpec fromJson(final JSONObject apiObj) throws JSONException {
        Type type = Type.parse(apiObj.getString(KEY_X_TYPE));
        JSONArray parameters = apiObj.getJSONArray(KEY_PARAMETERS);

        List<DConnectRequestParamSpec> paramSpecList = new ArrayList<DConnectRequestParamSpec>();
        for (int i = 0; i < parameters.length(); i++) {
            JSONObject parameter = parameters.getJSONObject(i);
            paramSpecList.add(DConnectRequestParamSpec.fromJson(parameter));
        }

        return new Builder()
            .setType(type)
            .setRequestParamList(paramSpecList)
            .build();
    }

    public static class Builder {
        private Type mType;
        private List<DConnectRequestParamSpec> mRequestParams;

        public Builder setType(final Type type) {
            mType = type;
            return this;
        }

        public Builder setRequestParamList(final List<DConnectRequestParamSpec> requestParamList) {
            mRequestParams = requestParamList;
            return this;
        }

        public DConnectApiSpec build() {
            if (mRequestParams == null) {
                mRequestParams = new ArrayList<DConnectRequestParamSpec>();
            }
            DConnectApiSpec spec = new DConnectApiSpec();
            spec.setType(mType);
            spec.setRequestParamList(
                mRequestParams.toArray(new DConnectRequestParamSpec[mRequestParams.size()]));
            return spec;
        }
    }

}
