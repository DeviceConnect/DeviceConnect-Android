package org.deviceconnect.android.profile.spec;


public class NumberParameterSpec extends DConnectParameterSpec {

    private final Format mFormat;
    private Double mMaximum;
    private Double mMinimum;
    private boolean mExclusiveMaximum;
    private boolean mExclusiveMinimum;

    private NumberParameterSpec(final Format format) {
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
        boolean isValid = true;
        if (mMaximum != null) {
            isValid &= mExclusiveMaximum ? (mMaximum > value) : (mMaximum >= value);
        }
        if (mMinimum != null) {
            isValid &= mExclusiveMinimum ? (mMinimum < value) : (mMinimum <= value);
        }
        return isValid;
    }

    public static class Builder extends BaseBuilder<Builder> {

        private Format mFormat;
        private Double mMaximum;
        private Double mMinimum;
        private boolean mExclusiveMaximum;
        private boolean mExclusiveMinimum;

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

        public NumberParameterSpec build() {
            if (mFormat == null) {
                mFormat = Format.FLOAT;
            }
            NumberParameterSpec spec = new NumberParameterSpec(mFormat);
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            spec.setMaximum(mMaximum);
            spec.setExclusiveMaximum(mExclusiveMaximum);
            spec.setMinimum(mMinimum);
            spec.setExclusiveMinimum(mExclusiveMinimum);
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
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
