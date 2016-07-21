package org.deviceconnect.android.profile.spec;


public abstract class DConnectParameterSpec {

    final Type mType;
    String mName;
    boolean mIsRequired;

    protected DConnectParameterSpec(final Type type) {
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

    public enum Type {

        STRING,
        INTEGER,
        NUMBER,
        BOOLEAN,
        FILE,
        ARRAY;

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

    public abstract static class BaseBuilder<T extends BaseBuilder<T>> {

        protected String mName;
        protected boolean mIsRequired;

        protected abstract T getThis();

        public void setName(final String name) {
            mName = name;
        }

        public T setRequired(final boolean isRequired) {
            mIsRequired = isRequired;
            return getThis();
        }

    }
}
