package org.deviceconnect.android.profile.spec;


public class BooleanParameterSpec extends DConnectParameterSpec {

    BooleanParameterSpec() {
        super(new BooleanDataSpec());
    }

    public static class Builder extends BaseBuilder<Builder> {

        public BooleanParameterSpec build() {
            BooleanParameterSpec spec = new BooleanParameterSpec();
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
