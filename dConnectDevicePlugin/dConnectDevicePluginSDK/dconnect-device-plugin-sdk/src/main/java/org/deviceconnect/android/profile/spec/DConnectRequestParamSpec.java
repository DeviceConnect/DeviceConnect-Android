package org.deviceconnect.android.profile.spec;


import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class DConnectRequestParamSpec {

    protected static final String NAME = "name";
    protected static final String MANDATORY = "mandatory";
    protected static final String TYPE = "type";

    final Type mType;
    String mName;
    boolean mIsMandatory;

    protected DConnectRequestParamSpec(final Type type) {
        mType = type;
    }

    public Type getType() {
        return mType;
    }

    void setName(final String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    void setMandatory(final boolean isMandatory) {
        mIsMandatory = isMandatory;
    }

    public boolean isMandatory() {
        return mIsMandatory;
    }

    public boolean validate(final Object param) {
        if (param == null) {
            return !isMandatory();
        }
        return param instanceof Boolean;
    }

    public Bundle toBundle() {
        return null; //TODO
    }

    public enum Type {

        STRING,
        INTEGER,
        NUMBER,
        BOOLEAN;

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
        String type = json.getString(TYPE);
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
            default:
                throw new IllegalArgumentException();
        }
        return spec;
    }

    public static class Enum<T> {

        private String mName;

        private T mValue;

        public void setName(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public void setValue(final T value) {
            mValue = value;
        }

        public T getValue() {
            return mValue;
        }
    }

}
