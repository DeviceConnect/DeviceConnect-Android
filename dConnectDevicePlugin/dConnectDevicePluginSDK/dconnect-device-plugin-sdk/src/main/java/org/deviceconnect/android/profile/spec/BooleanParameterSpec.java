package org.deviceconnect.android.profile.spec;


public class BooleanParameterSpec extends DConnectParameterSpec {

    private final String TRUE = "true";
    private final String FALSE = "false";

    BooleanParameterSpec() {
        super(new BooleanDataSpec());
    }

    @Override
    public boolean validate(final Object obj) {
        if (!super.validate(obj)) {
            return false;
        }
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            String strParam = (String) obj;
            return TRUE.equalsIgnoreCase(strParam) || FALSE.equalsIgnoreCase(strParam);
        } else if (obj instanceof Boolean) {
            return true;
        }
        return false;
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
