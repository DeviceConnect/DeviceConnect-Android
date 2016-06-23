package org.deviceconnect.android.profile.spec;


import org.json.JSONException;
import org.json.JSONObject;

public class BooleanRequestParamSpec extends DConnectRequestParamSpec {

    private final String TRUE = "true";
    private final String FALSE = "false";

    private BooleanRequestParamSpec() {
        super(Type.BOOLEAN);
    }

    public static BooleanRequestParamSpec fromJson(final JSONObject json) throws JSONException {
        Builder builder = new Builder();
        builder.setName(json.getString(NAME));
        builder.setMandatory(json.getBoolean(MANDATORY));
        return builder.build();
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

    public static class Builder {

        private String mName;
        private boolean mIsMandatory;

        public Builder setName(final String name) {
            mName = name;
            return this;
        }

        public Builder setMandatory(final boolean isMandatory) {
            mIsMandatory = isMandatory;
            return this;
        }

        public BooleanRequestParamSpec build() {
            BooleanRequestParamSpec spec = new BooleanRequestParamSpec();
            spec.setName(mName);
            spec.setMandatory(mIsMandatory);
            return spec;
        }

    }
}
