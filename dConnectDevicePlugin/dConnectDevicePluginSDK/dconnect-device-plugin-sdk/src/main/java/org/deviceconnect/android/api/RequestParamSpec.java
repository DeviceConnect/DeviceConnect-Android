package org.deviceconnect.android.api;


public abstract class RequestParamSpec {

    String mName;

    Type mType;

    boolean mIsMandatory;

    public RequestParamSpec() {

    }

    public String getName() {
        return mName;
    }

    public Type getType() {
        return mType;
    }

    public boolean isMandatory() {
        return mIsMandatory;
    }


    public enum Type {

        STRING,
        INTEGER,
        NUMBER,
        BOOLEAN;

        public String getName() {
            return this.name().toLowerCase();
        }

    }

}
