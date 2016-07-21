package org.deviceconnect.android.profile.spec;


public class IntegerParameterSpec extends DConnectParameterSpec {

    private final Format mFormat;
    private Long mMaximum;
    private Long mMinimum;
    private boolean mExclusiveMaximum;
    private boolean mExclusiveMinimum;
    private long[] mEnumList;

    private IntegerParameterSpec(final Format format) {
        super(Type.INTEGER);
        mFormat = format;
    }

    public Format getFormat() {
        return mFormat;
    }

    void setMaximum(final Long maximum) {
        mMaximum = maximum;
    }

    public Long getMaximum() {
        return mMaximum;
    }

    void setMinimum(final Long minimum) {
        mMinimum = minimum;
    }

    public Long getMinimum() {
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

    void setEnumList(final long[] enumList) {
        mEnumList = enumList;
    }

    public long[] getEnumList() {
        return mEnumList;
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
        if (mEnumList != null) {
            for (long e : mEnumList) {
                if (e == value) {
                    return true;
                }
            }
            return false;
        } else {
            boolean isValid = true;
            if (mMaximum != null) {
                isValid &=  mExclusiveMaximum ? (mMaximum > value) : (mMaximum >= value);
            }
            if (mMinimum != null) {
                isValid &= mExclusiveMinimum ? (mMinimum < value) : (mMinimum <= value);
            }
            return isValid;
        }
    }

    public static class Builder extends BaseBuilder<Builder> {

        private Format mFormat;
        private Long mMaximum;
        private Long mMinimum;
        private boolean mExclusiveMaximum;
        private boolean mExclusiveMinimum;
        private long[] mEnumList;

        public Builder setFormat(final Format format) {
            mFormat = format;
            return this;
        }

        public Builder setMaximum(final long maximum) {
            mMaximum = maximum;
            return this;
        }

        public Builder setMinimum(final long minimum) {
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

        public Builder setEnumList(final long[] enumList) {
            mEnumList = enumList;
            return this;
        }

        public IntegerParameterSpec build() {
            if (mFormat == null) {
                mFormat = Format.INT32;
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

    public enum Format {
        INT32("int32"),
        INT64("int64");

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
