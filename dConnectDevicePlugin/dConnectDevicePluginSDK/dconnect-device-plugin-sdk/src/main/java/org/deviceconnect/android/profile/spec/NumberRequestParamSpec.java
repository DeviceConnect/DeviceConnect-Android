package org.deviceconnect.android.profile.spec;


import org.json.JSONException;
import org.json.JSONObject;

public class NumberRequestParamSpec extends DConnectRequestParamSpec {

    private static final String FORMAT = "format";
    private static final String MAX_VALUE = "maxValue";
    private static final String MIN_VALUE = "minValue";
    private static final String EXCLUSIVE_MAX_VALUE = "exclusiveMaxValue";
    private static final String EXCLUSIVE_MIN_VALUE = "exclusiveMinValue";

    private final Format mFormat;
    private Double mMaxValue;
    private Double mMinValue;
    private Double mExclusiveMaxValue;
    private Double mExclusiveMinValue;

    private NumberRequestParamSpec(final Format format) {
        super(Type.NUMBER);
        mFormat = format;
    }

    public Format getFormat() {
        return mFormat;
    }

    void setMaxValue(final Double maxValue) {
        mMaxValue = maxValue;
    }

    public Double getMaxValue() {
        return mMaxValue;
    }

    void setMinValue(final Double minValue) {
        mMinValue = minValue;
    }

    public Double getMinValue() {
        return mMinValue;
    }

    void setExclusiveMaxValue(final Double exclusiveMaxValue) {
        mExclusiveMaxValue = exclusiveMaxValue;
    }

    public Double getExclusiveMaxValue() {
        return mExclusiveMaxValue;
    }

    void setExclusiveMinValue(final Double exclusiveMinValue) {
        mExclusiveMinValue = exclusiveMinValue;
    }

    public Double getExclusiveMinValue() {
        return mExclusiveMinValue;
    }

    @Override
    public boolean validate(final Object param) {
        if (!super.validate(param)) {
            return false;
        }
        switch (mFormat) {
            case FLOAT:
                return validateFloat(param);
            case DOUBLE:
                return validateDouble(param);
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
            return validateRange((Integer) param);
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
        } else if (param instanceof Long) {
            return validateRange((Long) param);
        } else {
            return false;
        }
    }

    private boolean validateRange(final double value) {
        if (mMaxValue != null && mMaxValue < value) {
            return false;
        }
        if (mExclusiveMaxValue != null && mExclusiveMaxValue <= value) {
            return false;
        }
        if (mMinValue != null && mMinValue > value) {
            return false;
        }
        if (mExclusiveMinValue != null && mExclusiveMinValue >= value) {
            return false;
        }
        return true;
    }

    public static NumberRequestParamSpec fromJson(final JSONObject json) throws JSONException {
        Builder builder = new Builder();
        builder.setName(json.getString(NAME));
        builder.setMandatory(json.getBoolean(MANDATORY));
        if (json.has(FORMAT)) {
            Format format = Format.parse(json.getString(FORMAT));
            if (format == null) {
                throw new IllegalArgumentException("format is invalid: " + json.getString(FORMAT));
            }
            builder.setFormat(format);
        }
        if (json.has(MAX_VALUE)) {
            builder.setMaxValue(json.getLong(MAX_VALUE));
        }
        if (json.has(MIN_VALUE)) {
            builder.setMinValue(json.getLong(MIN_VALUE));
        }
        if (json.has(EXCLUSIVE_MAX_VALUE)) {
            builder.setExclusiveMaxValue(json.getLong(EXCLUSIVE_MAX_VALUE));
        }
        if (json.has(EXCLUSIVE_MIN_VALUE)) {
            builder.setExclusiveMinValue(json.getLong(EXCLUSIVE_MIN_VALUE));
        }
        return builder.build();
    }

    public static class Builder {
        private String mName;
        private boolean mIsMandatory;
        private Format mFormat;
        private Double mMaxValue;
        private Double mMinValue;
        private Double mExclusiveMaxValue;
        private Double mExclusiveMinValue;

        public Builder setName(final String name) {
            mName = name;
            return this;
        }

        public Builder setMandatory(final boolean isMandatory) {
            mIsMandatory = isMandatory;
            return this;
        }

        public Builder setFormat(final Format format) {
            mFormat = format;
            return this;
        }

        public Builder setMaxValue(final double maxValue) {
            mMaxValue = maxValue;
            return this;
        }

        public Builder setMinValue(final double minValue) {
            mMinValue = minValue;
            return this;
        }

        public Builder setExclusiveMaxValue(final double exclusiveMaxValue) {
            mExclusiveMaxValue = exclusiveMaxValue;
            return this;
        }

        public Builder setExclusiveMinValue(final double exclusiveMinValue) {
            mExclusiveMinValue = exclusiveMinValue;
            return this;
        }

        public NumberRequestParamSpec build() {
            if (mFormat == null) {
                mFormat = Format.FLOAT;
            }
            NumberRequestParamSpec spec = new NumberRequestParamSpec(mFormat);
            spec.setName(mName);
            spec.setMandatory(mIsMandatory);
            if (mMaxValue != null) {
                spec.setMaxValue(mMaxValue);
            } else {
                spec.setExclusiveMaxValue(mExclusiveMaxValue);
            }
            if (mMinValue != null) {
                spec.setMinValue(mMinValue);
            } else {
                spec.setExclusiveMinValue(mExclusiveMinValue);
            }
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
