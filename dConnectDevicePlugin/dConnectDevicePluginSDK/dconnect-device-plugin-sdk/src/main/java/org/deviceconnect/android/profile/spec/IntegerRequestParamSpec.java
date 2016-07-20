package org.deviceconnect.android.profile.spec;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IntegerRequestParamSpec extends DConnectRequestParamSpec {

    private static final String KEY_FORMAT = "format";
    private static final String KEY_MAXIMUM = "maximum";
    private static final String KEY_MINIMUM = "minimum";
    private static final String KEY_EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
    private static final String KEY_EXCLUSIVE_MINIMUM = "exclusiveMinimum";
    private static final String KEY_ENUM = "enum";

    private final Format mFormat;
    private Long mMaximum;
    private Long mMinimum;
    private boolean mExclusiveMaximum;
    private boolean mExclusiveMinimum;
    private long[] mEnumList;

    private IntegerRequestParamSpec(final Format format) {
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
            if (mMaximum != null) {
                return mExclusiveMaximum ? (mMaximum < value) : (mMaximum <= value);
            }
            if (mMinimum != null) {
                return mExclusiveMinimum ? (mMinimum > value) : (mMinimum >= value);
            }
            return true;
        }
    }

    public static IntegerRequestParamSpec fromJson(final JSONObject json) throws JSONException {
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
        if (json.has(KEY_ENUM)) {
            JSONArray array = json.getJSONArray(KEY_ENUM);
            long[] enumList = new long[array.length()];
            for (int i = 0; i < array.length(); i++) {
                enumList[i] = array.getLong(i);
            }
            builder.setEnumList(enumList);
        }
        return builder.build();
    }

    public static class Builder {
        private String mName;
        private boolean mIsRequired;
        private Format mFormat;
        private Long mMaximum;
        private Long mMinimum;
        private boolean mExclusiveMaximum;
        private boolean mExclusiveMinimum;
        private long[] mEnumList;

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

        public IntegerRequestParamSpec build() {
            if (mFormat == null) {
                mFormat = Format.INT32;
            }
            IntegerRequestParamSpec spec = new IntegerRequestParamSpec(mFormat);
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            spec.setEnumList(mEnumList);
            spec.setMaximum(mMaximum);
            spec.setExclusiveMaximum(mExclusiveMaximum);
            spec.setMinimum(mMinimum);
            spec.setExclusiveMinimum(mExclusiveMinimum);
            return spec;
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
