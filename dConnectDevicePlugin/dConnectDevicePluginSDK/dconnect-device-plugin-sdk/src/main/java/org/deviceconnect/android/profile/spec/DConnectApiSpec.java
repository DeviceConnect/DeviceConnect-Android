package org.deviceconnect.android.profile.spec;


import android.content.Intent;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DConnectApiSpec implements DConnectApiSpecConstants {

    private static final String NAME = "name";
    private static final String PATH = "path";
    private static final String METHOD = "method";
    private static final String TYPE = "type";
    private static final String REQUEST_PARAMS = "requestParams";

    private String mName;
    private Type mType;
    private Method mMethod;
    private String mPath;
    private DConnectRequestParamSpec[] mRequestParamList;

    private DConnectApiSpec() {}

    private void setName(final String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

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
        String name = apiObj.getString(NAME);
        String path = apiObj.getString(PATH);
        String methodStr = apiObj.getString(METHOD);
        String typeStr = apiObj.getString(TYPE);

        Method method = Method.parse(methodStr);
        if (method == null) {
            throw new JSONException("method is invalid: " + methodStr);
        }
        Type type = Type.parse(typeStr);
        if (type == null) {
            throw new JSONException("type is invalid: " + typeStr);
        }

        List<DConnectRequestParamSpec> paramList = new ArrayList<DConnectRequestParamSpec>();
        if (apiObj.has(REQUEST_PARAMS)) {
            JSONArray requestParams = apiObj.getJSONArray(REQUEST_PARAMS);
            for (int k = 0; k < requestParams.length(); k++) {
                JSONObject paramObj = requestParams.getJSONObject(k);
                DConnectRequestParamSpec paramSpec = DConnectRequestParamSpec.fromJson(paramObj);
                paramList.add(paramSpec);
            }
        }

        return new Builder()
            .setName(name)
            .setType(type)
            .setMethod(method)
            .setPath(path)
            .setRequestParamList(paramList)
            .build();
    }

    public static class Builder {
        private String mName;
        private Type mType;
        private Method mMethod;
        private String mPath;
        private List<DConnectRequestParamSpec> mRequestParams;

        public Builder setName(final String name) {
            mName = name;
            return this;
        }

        public Builder setType(final Type type) {
            mType = type;
            return this;
        }

        public Builder setMethod(final Method method) {
            mMethod = method;
            return this;
        }

        public Builder setPath(final String path) {
            mPath = path;
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
            spec.setName(mName);
            spec.setType(mType);
            spec.setMethod(mMethod);
            spec.setPath(mPath);
            spec.setRequestParamList(
                mRequestParams.toArray(new DConnectRequestParamSpec[mRequestParams.size()]));
            return spec;
        }
    }

}
