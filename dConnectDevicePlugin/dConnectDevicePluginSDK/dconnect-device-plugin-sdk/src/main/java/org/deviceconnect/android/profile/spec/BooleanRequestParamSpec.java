package org.deviceconnect.android.profile.spec;


import org.json.JSONException;
import org.json.JSONObject;

public class BooleanRequestParamSpec extends DConnectRequestParamSpec {

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
    public boolean validate(final Object param) {
        if (!super.validate(param)) {
            return false;
        }
        return param instanceof Boolean;
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
