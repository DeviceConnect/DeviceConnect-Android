package org.deviceconnect.android.profile.spec;


public class IntegerDataSpec extends DConnectDataSpec {

    private final DataFormat mFormat;
    private Long mMaximum;
    private Long mMinimum;
    private Boolean mExclusiveMaximum;
    private Boolean mExclusiveMinimum;
    private long[] mEnumList;

    IntegerDataSpec(final DataFormat format) {
        super(DataType.INTEGER);
        mFormat = format;
    }

    public DataFormat getFormat() {
        return mFormat;
    }

    public Long getMaximum() {
        return mMaximum;
    }

    public void setMaximum(final Long maximum) {
        mMaximum = maximum;
    }

    public Long getMinimum() {
        return mMinimum;
    }

    public void setMinimum(final Long minimum) {
        mMinimum = minimum;
    }

    public boolean isExclusiveMaximum() {
        return mExclusiveMaximum != null ? mExclusiveMaximum : false;
    }

    public void setExclusiveMaximum(final Boolean exclusiveMaximum) {
        mExclusiveMaximum = exclusiveMaximum;
    }

    public boolean isExclusiveMinimum() {
        return mExclusiveMinimum != null ? mExclusiveMinimum : false;
    }

    public void setExclusiveMinimum(final Boolean exclusiveMinimum) {
        mExclusiveMinimum = exclusiveMinimum;
    }

    public long[] getEnumList() {
        return mEnumList;
    }

    public void setEnumList(final long[] enumList) {
        mEnumList = enumList;
    }

    @Override
    public boolean validate(final Object obj) {
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

    public static class Builder {

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

        public IntegerDataSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.INT32;
            }
            IntegerDataSpec spec = new IntegerDataSpec(mFormat);
            spec.setEnumList(mEnumList);
            spec.setMaximum(mMaximum);
            spec.setExclusiveMaximum(mExclusiveMaximum);
            spec.setMinimum(mMinimum);
            spec.setExclusiveMinimum(mExclusiveMinimum);
            return spec;
        }
    }

}
