package org.deviceconnect.android.profile.spec;


public abstract class DConnectParameterSpec<T extends DConnectDataSpec> implements DConnectSpecConstants {

    protected final T mDataSpec;
    String mName;
    Boolean mIsRequired;

    protected DConnectParameterSpec(final T dataSpec) {
        mDataSpec = dataSpec;
    }

    public DataType getDataType() {
        return mDataSpec.getDataType();
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
        return mIsRequired != null ? mIsRequired : false;
    }

    public final boolean validate(final Object param) {
        if (param == null) {
            return !isRequired();
        }
        return mDataSpec.validate(param);
    }

    public abstract static class BaseBuilder<T extends BaseBuilder<T>> {

        protected String mName;
        protected Boolean mIsRequired;

        protected abstract T getThis();

        public T setName(final String name) {
            mName = name;
            return getThis();
        }

        public T setRequired(final boolean isRequired) {
            mIsRequired = isRequired;
            return getThis();
        }

    }
}
