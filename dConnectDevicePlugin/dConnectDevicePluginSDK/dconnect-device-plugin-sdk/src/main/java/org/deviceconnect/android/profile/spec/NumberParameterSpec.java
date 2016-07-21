package org.deviceconnect.android.profile.spec;


public class NumberParameterSpec extends DConnectParameterSpec<NumberDataSpec> {

    NumberParameterSpec(final DataFormat format) {
        super(new NumberDataSpec(format));
    }

    public DataFormat getFormat() {
        return mDataSpec.getFormat();
    }

     void setMaximum(final Double maximum) {
        mDataSpec.setMaximum(maximum);
    }

    public Double getMinimum() {
        return mDataSpec.getMinimum();
    }

    void setMinimum(final Double minimum) {
        mDataSpec.setMinimum(minimum);
    }

    public boolean isExclusiveMaximum() {
        return mDataSpec.isExclusiveMaximum();
    }

    public boolean isExclusiveMinimum() {
        return mDataSpec.isExclusiveMinimum();
    }

    void setExclusiveMinimum(final boolean exclusiveMinimum) {
        mDataSpec.setExclusiveMinimum(exclusiveMinimum);
    }

    public Double getMaximum() {
        return mDataSpec.getMaximum();
    }

    void setExclusiveMaximum(final boolean exclusiveMaximum) {
        mDataSpec.setExclusiveMaximum(exclusiveMaximum);
    }

    @Override
    public boolean validate(final Object obj) {
        if (!super.validate(obj)) {
            return false;
        }
        if (obj == null) {
            return true;
        }
        switch (getFormat()) {
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
        if (getMaximum() != null) {
            isValid &= isExclusiveMaximum() ? (getMaximum() > value) : (getMaximum() >= value);
        }
        if (getMinimum() != null) {
            isValid &= isExclusiveMinimum() ? (getMinimum() < value) : (getMinimum() <= value);
        }
        return isValid;
    }

    public static class Builder extends BaseBuilder<Builder> {

        private DataFormat mFormat;
        private Double mMaximum;
        private Double mMinimum;
        private Boolean mExclusiveMaximum;
        private Boolean mExclusiveMinimum;

        public Builder setFormat(final DataFormat format) {
            mFormat = format;
            return this;
        }

        public Builder setMaximum(final Double maximum) {
            mMaximum = maximum;
            return this;
        }

        public Builder setMinimum(final Double minimum) {
            mMinimum = minimum;
            return this;
        }

        public Builder setExclusiveMaximum(final Boolean exclusiveMaximum) {
            mExclusiveMaximum = exclusiveMaximum;
            return this;
        }

        public Builder setExclusiveMinimum(final Boolean exclusiveMinimum) {
            mExclusiveMinimum = exclusiveMinimum;
            return this;
        }

        public NumberParameterSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.FLOAT;
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

}
