package org.deviceconnect.android.profile.spec;


public class NumberDataSpec extends DConnectDataSpec {

    private final DataFormat mFormat;
    private Double mMaximum;
    private Double mMinimum;
    private Boolean mExclusiveMaximum;
    private Boolean mExclusiveMinimum;

    NumberDataSpec(final DataFormat format) {
        super(DataType.NUMBER);
        mFormat = format;
    }

    public DataFormat getFormat() {
        return mFormat;
    }

    public Double getMaximum() {
        return mMaximum;
    }

    void setMaximum(final Double maximum) {
        mMaximum = maximum;
    }

    public Double getMinimum() {
        return mMinimum;
    }

    void setMinimum(final Double minimum) {
        mMinimum = minimum;
    }

    public boolean isExclusiveMaximum() {
        return mExclusiveMaximum != null ? mExclusiveMaximum : false;
    }

    void setExclusiveMaximum(final Boolean exclusiveMaximum) {
        mExclusiveMaximum = exclusiveMaximum;
    }

    public boolean isExclusiveMinimum() {
        return mExclusiveMinimum != null ? mExclusiveMinimum : false;
    }

    void setExclusiveMinimum(final Boolean exclusiveMinimum) {
        mExclusiveMinimum = exclusiveMinimum;
    }

    @Override
    public boolean validate(final Object obj) {
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

    public static class Builder {

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

        public NumberDataSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.FLOAT;
            }
            NumberDataSpec spec = new NumberDataSpec(mFormat);
            spec.setMaximum(mMaximum);
            spec.setExclusiveMaximum(mExclusiveMaximum);
            spec.setMinimum(mMinimum);
            spec.setExclusiveMinimum(mExclusiveMinimum);
            return spec;
        }
    }

}
