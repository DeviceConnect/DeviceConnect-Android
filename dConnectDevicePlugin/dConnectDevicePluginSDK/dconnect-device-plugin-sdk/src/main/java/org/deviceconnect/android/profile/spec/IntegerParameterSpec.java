package org.deviceconnect.android.profile.spec;


public class IntegerParameterSpec extends DConnectParameterSpec<IntegerDataSpec> {

    IntegerParameterSpec(final DataFormat format) {
        super(new IntegerDataSpec(format));
    }

    public DataFormat getFormat() {
        return mDataSpec.getFormat();
    }

    void setExclusiveMaximum(final boolean exclusiveMaximum) {
        mDataSpec.setExclusiveMaximum(exclusiveMaximum);
    }

    void setMaximum(final Long maximum) {
        mDataSpec.setMaximum(maximum);
    }

    public Long getMinimum() {
        return mDataSpec.getMinimum();
    }

    public long[] getEnumList() {
        return mDataSpec.getEnumList();
    }

    void setExclusiveMinimum(final boolean exclusiveMinimum) {
        mDataSpec.setExclusiveMinimum(exclusiveMinimum);
    }

    public boolean isExclusiveMaximum() {
        return mDataSpec.isExclusiveMaximum();
    }

    void setMinimum(final Long minimum) {
        mDataSpec.setMinimum(minimum);
    }

    void setEnumList(final long[] enumList) {
        mDataSpec.setEnumList(enumList);
    }

    public Long getMaximum() {
        return mDataSpec.getMaximum();
    }

    public boolean isExclusiveMinimum() {
        return mDataSpec.isExclusiveMinimum();
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
            case INT32:
                return validateInt32(obj);
            case INT64:
                return validateInt64(obj);
            default:
                throw new IllegalStateException();
        }
    }

    private boolean validateInt32(final Object param) {
        if (param instanceof String) {
            try {
                return validateRange(Integer.parseInt((String) param));
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (param instanceof Integer) {
            return validateRange((Integer) param);
        } else {
            return false;
        }
    }

    private boolean validateInt64(final Object param) {
        if (param instanceof String) {
            try {
                return validateRange(Long.parseLong((String) param));
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (param instanceof Long) {
            return validateRange((Long) param);
        } else {
            return false;
        }
    }

    private boolean validateRange(final long value) {
        if (getEnumList() != null) {
            for (long e : getEnumList()) {
                if (e == value) {
                    return true;
                }
            }
            return false;
        } else {
            boolean isValid = true;
            if (getMaximum() != null) {
                isValid &=  isExclusiveMaximum() ? (getMaximum() > value) : (getMaximum() >= value);
            }
            if (getMinimum() != null) {
                isValid &= isExclusiveMinimum() ? (getMinimum() < value) : (getMinimum() <= value);
            }
            return isValid;
        }
    }

    public static class Builder extends BaseBuilder<Builder> {

        private DataFormat mFormat;
        private Long mMaximum;
        private Long mMinimum;
        private Boolean mExclusiveMaximum;
        private Boolean mExclusiveMinimum;
        private long[] mEnumList;

        public Builder setFormat(final DataFormat format) {
            mFormat = format;
            return this;
        }

        public Builder setMaximum(final Long maximum) {
            mMaximum = maximum;
            return this;
        }

        public Builder setMinimum(final Long minimum) {
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

        public Builder setEnumList(final long[] enumList) {
            mEnumList = enumList;
            return this;
        }

        public IntegerParameterSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.INT32;
            }
            IntegerParameterSpec spec = new IntegerParameterSpec(mFormat);
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            spec.setEnumList(mEnumList);
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
