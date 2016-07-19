package org.deviceconnect.android.profile.spec;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class DConnectRequestParamSpec {

    protected static final String KEY_NAME = "name";
    protected static final String KEY_REQUIRED = "required";
    protected static final String KEY_TYPE = "type";

    final Type mType;
    String mName;
    boolean mIsRequired;

    protected DConnectRequestParamSpec(final Type type) {
        mType = type;
    }

    public Type getType() {
        return mType;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    void setRequired(final boolean isRequired) {
        mIsRequired = isRequired;
    }

    public boolean isRequired() {
        return mIsRequired;
    }

    public boolean validate(final Object param) {
        if (param == null) {
            return !isRequired();
        }
        return true;
    }

    public Bundle toBundle() {
        return null; //TODO
    }

    public enum Type {

        STRING,
        INTEGER,
        NUMBER,
        BOOLEAN,
        FILE;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static Type fromName(final String name) {
            for (Type type : Type.values()) {
                if (type.getName().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    public static DConnectRequestParamSpec fromJson(final JSONObject json) throws JSONException {
        String type = json.getString(KEY_TYPE);
        DConnectRequestParamSpec.Type paramType = DConnectRequestParamSpec.Type.fromName(type);
        if (paramType == null) {
            return null;
        }
        DConnectRequestParamSpec spec;
        switch (paramType) {
            case BOOLEAN:
                spec = BooleanRequestParamSpec.fromJson(json);
                break;
            case STRING:
                spec = StringRequestParamSpec.fromJson(json);
                break;
            case INTEGER:
                spec = IntegerRequestParamSpec.fromJson(json);
                break;
            case NUMBER:
                spec = NumberRequestParamSpec.fromJson(json);
                break;
            case FILE:
                spec = NumberRequestParamSpec.fromJson(json);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return spec;
    }

}
