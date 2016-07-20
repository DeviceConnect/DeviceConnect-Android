package org.deviceconnect.android.profile.spec;


import org.json.JSONException;
import org.json.JSONObject;

public class NumberRequestParamSpec extends DConnectRequestParamSpec {

    private static final String KEY_FORMAT = "format";
    private static final String KEY_MAXIMUM = "maximum";
    private static final String KEY_MINIMUM = "minimum";
    private static final String KEY_EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
    private static final String KEY_EXCLUSIVE_MINIMUM = "exclusiveMinimum";
    private static final String KEY_ENUM = "enum";

    private final Format mFormat;
    private Double mMaximum;
    private Double mMinimum;
    private boolean mExclusiveMaximum;
    private boolean mExclusiveMinimum;

    private NumberRequestParamSpec(final Format format) {
        super(Type.NUMBER);
        mFormat = format;
    }

    public Format getFormat() {
        return mFormat;
    }

    void setMaximum(final Double maximum) {
        mMaximum = maximum;
    }

    public Double getMaximum() {
        return mMaximum;
    }

    void setMinimum(final Double minimum) {
        mMinimum = minimum;
    }

    public Double getMinimum() {
        return mMinimum;
    }

    void setExclusiveMaximum(final boolean exclusiveMaximum) {
        mExclusiveMaximum = exclusiveMaximum;
    }

    public boolean getExclusiveMaximum() {
        return mExclusiveMaximum;
    }

    void setExclusiveMinimum(final boolean exclusiveMinimum) {
        mExclusiveMinimum = exclusiveMinimum;
    }

    public boolean getExclusiveMinimum() {
        return mExclusiveMinimum;
    }

    @Override
    public boolean validate(final Object obj) {
        if (!super.validate(obj)) {
            return false;
        }
        if (obj == null) {
            return true;
        }
        switch (mFormat) {
            case FLOAT:
                return validateFloat(obj);
            case DOUBLE:
                return validateDouble(obj);
            default:
                throw new IllegalStateException();
        }
    }

    private boolean validateFloat(final Object param) {
        if (param instanceof String) {
            try {
                return validateRange(Float.parseFloat((String) param));
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (param instanceof Float) {
            return validateRange((float) param);
        } else {
            return false;
        }
    }

    private boolean validateDouble(final Object param) {
        if (param instanceof String) {
            try {
                return validateRange(Double.parseDouble((String) param));
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (param instanceof Double) {
            return validateRange((double) param);
        } else {
            return false;
        }
    }

    private boolean validateRange(final double value) {
        if (mMaximum != null) {
            return mExclusiveMaximum ? (mMaximum < value) : (mMaximum <= value);
        }
        if (mMinimum != null) {
            return mExclusiveMinimum ? (mMinimum > value) : (mMinimum >= value);
        }
        return true;
    }

    public static NumberRequestParamSpec fromJson(final JSONObject json) throws JSONException {
        Builder builder = new Builder();
        builder.setRequired(json.getBoolean(KEY_REQUIRED));
        if (json.has(KEY_FORMAT)) {
            Format format = Format.parse(json.optString(KEY_FORMAT));
            if (format == null) {
                throw new IllegalArgumentException("format is invalid: " + json.optString(KEY_FORMAT));
            }
            builder.setFormat(format);
        }
        if (json.has(KEY_MAXIMUM)) {
            builder.setMaximum(json.getLong(KEY_MAXIMUM));
        }
        if (json.has(KEY_MINIMUM)) {
            builder.setMinimum(json.getLong(KEY_MINIMUM));
        }
        if (json.has(KEY_EXCLUSIVE_MAXIMUM)) {
            builder.setExclusiveMaximum(json.getBoolean(KEY_EXCLUSIVE_MAXIMUM));
        }
        if (json.has(KEY_EXCLUSIVE_MINIMUM)) {
            builder.setExclusiveMinimum(json.getBoolean(KEY_EXCLUSIVE_MINIMUM));
        }
        return builder.build();
    }

    public static class Builder {
        private String mName;
        private boolean mIsRequired;
        private Format mFormat;
        private Double mMaximum;
        private Double mMinimum;
        private boolean mExclusiveMaximum;
        private boolean mExclusiveMinimum;

        public void setName(final String name) {
            mName = name;
        }

        public Builder setRequired(final boolean isRequired) {
            mIsRequired = isRequired;
            return this;
        }

        public Builder setFormat(final Format format) {
            mFormat = format;
            return this;
        }

        public Builder setMaximum(final double maximum) {
            mMaximum = maximum;
            return this;
        }

        public Builder setMinimum(final double minimum) {
            mMinimum = minimum;
            return this;
        }

        public Builder setExclusiveMaximum(final boolean exclusiveMaximum) {
            mExclusiveMaximum = exclusiveMaximum;
            return this;
        }

        public Builder setExclusiveMinimum(final boolean exclusiveMinimum) {
            mExclusiveMinimum = exclusiveMinimum;
            return this;
        }

        public NumberRequestParamSpec build() {
            if (mFormat == null) {
                mFormat = Format.FLOAT;
            }
            NumberRequestParamSpec spec = new NumberRequestParamSpec(mFormat);
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            spec.setMaximum(mMaximum);
            spec.setExclusiveMaximum(mExclusiveMaximum);
            spec.setMinimum(mMinimum);
            spec.setExclusiveMinimum(mExclusiveMinimum);
            return spec;
        }
    }

    public enum Format {
        FLOAT("float"),
        DOUBLE("double");

        private final String mName;

        Format(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public static Format parse(final String name) {
            for (Format format : Format.values()) {
                if (format.mName.equalsIgnoreCase(name)) {
                    return format;
                }
            }
            return null;
        }
    }
}
