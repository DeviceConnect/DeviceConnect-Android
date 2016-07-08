package org.deviceconnect.android.profile.spec;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IntegerRequestParamSpec extends DConnectRequestParamSpec {

    private static final String FORMAT = "format";
    private static final String MAX_VALUE = "maxValue";
    private static final String MIN_VALUE = "minValue";
    private static final String EXCLUSIVE_MAX_VALUE = "exclusiveMaxValue";
    private static final String EXCLUSIVE_MIN_VALUE = "exclusiveMinValue";
    private static final String ENUM = "enum";
    private static final String VALUE = "value";

    private final Format mFormat;
    private Long mMaxValue;
    private Long mMinValue;
    private Long mExclusiveMaxValue;
    private Long mExclusiveMinValue;
    private Enum<Long>[] mEnumList;

    private IntegerRequestParamSpec(final Format format) {
        super(Type.INTEGER);
        mFormat = format;
    }

    public Format getFormat() {
        return mFormat;
    }

    void setMaxValue(final Long maxValue) {
        mMaxValue = maxValue;
    }

    public Long getMaxValue() {
        return mMaxValue;
    }

    void setMinValue(final Long minValue) {
        mMinValue = minValue;
    }

    public Long getMinValue() {
        return mMinValue;
    }

    void setExclusiveMaxValue(final Long exclusiveMaxValue) {
        mExclusiveMaxValue = exclusiveMaxValue;
    }

    public Long getExclusiveMaxValue() {
        return mExclusiveMaxValue;
    }

    void setExclusiveMinValue(final Long exclusiveMinValue) {
        mExclusiveMinValue = exclusiveMinValue;
    }

    public Long getExclusiveMinValue() {
        return mExclusiveMinValue;
    }

    void setEnumList(final Enum<Long>[] enumList) {
        mEnumList = enumList;
    }

    public Enum<Long>[] getEnumList() {
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
            for (Enum<Long> e : mEnumList) {
                if (e.getValue() == value) {
                    return true;
                }
            }
            return false;
        } else {
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
    }

    public static IntegerRequestParamSpec fromJson(final JSONObject json) throws JSONException {
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
        if (json.has(ENUM)) {
            List<Enum<Long>> enumList = new ArrayList<Enum<Long>>();
            JSONArray array = json.getJSONArray(ENUM);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Enum<Long> enumSpec = new Enum<Long>();
                enumSpec.setName(obj.getString(NAME));
                enumSpec.setValue(obj.getLong(VALUE));
                enumList.add(enumSpec);
            }
            builder.setEnumList(enumList);
        }
        return builder.build();
    }

    public static class Builder {
        private String mName;
        private boolean mIsMandatory;
        private Format mFormat;
        private Long mMaxValue;
        private Long mMinValue;
        private Long mExclusiveMaxValue;
        private Long mExclusiveMinValue;
        private List<Enum<Long>> mEnumList;

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

        public Builder setMaxValue(final long maxValue) {
            mMaxValue = maxValue;
            return this;
        }

        public Builder setMinValue(final long minValue) {
            mMinValue = minValue;
            return this;
        }

        public Builder setExclusiveMaxValue(final long exclusiveMaxValue) {
            mExclusiveMaxValue = exclusiveMaxValue;
            return this;
        }

        public Builder setExclusiveMinValue(final long exclusiveMinValue) {
            mExclusiveMinValue = exclusiveMinValue;
            return this;
        }

        public Builder setEnumList(final List<Enum<Long>> enumList) {
            mEnumList = enumList;
            return this;
        }

        public IntegerRequestParamSpec build() {
            if (mFormat == null) {
                mFormat = Format.INT32;
            }
            IntegerRequestParamSpec spec = new IntegerRequestParamSpec(mFormat);
            spec.setName(mName);
            spec.setMandatory(mIsMandatory);
            if (mEnumList != null) {
                spec.setEnumList(mEnumList.toArray(new Enum[mEnumList.size()]));
            } else {
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
            }
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
