package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.List;

public class Header extends AbstractSpec {

    private String mDescription;
    private DataType mType;
    private DataFormat mFormat;
    private Schema mItems;
    private String mCollectionFormat;
    private Object mDefault;
    private Number mMaximum;
    private Boolean mExclusiveMaximum;
    private Number mMinimum;
    private Boolean mExclusiveMinimum;
    private Integer mMaxLength;
    private Integer mMinLength;
    private String mPattern;
    private Integer mMaxItems;
    private Integer mMinItems;
    private Boolean mUniqueItems;
    private List<Object> mEnum;
    private Number mMultipleOf;

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public DataType getType() {
        return mType;
    }

    public void setType(DataType type) {
        mType = type;
    }

    public DataFormat getFormat() {
        return mFormat;
    }

    public void setFormat(DataFormat format) {
        mFormat = format;
    }

    public Object getDefault() {
        return mDefault;
    }

    public void setDefault(Object aDefault) {
        mDefault = aDefault;
    }

    public Schema getItems() {
        return mItems;
    }

    public void setItems(Schema items) {
        mItems = items;
    }

    public String getCollectionFormat() {
        return mCollectionFormat;
    }

    public void setCollectionFormat(String collectionFormat) {
        mCollectionFormat = collectionFormat;
    }

    public Number getMaximum() {
        return mMaximum;
    }

    public void setMaximum(Number maximum) {
        mMaximum = maximum;
    }

    public Boolean getExclusiveMaximum() {
        return mExclusiveMaximum;
    }

    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        mExclusiveMaximum = exclusiveMaximum;
    }

    public Number getMinimum() {
        return mMinimum;
    }

    public void setMinimum(Number minimum) {
        mMinimum = minimum;
    }

    public Boolean getExclusiveMinimum() {
        return mExclusiveMinimum;
    }

    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        mExclusiveMinimum = exclusiveMinimum;
    }

    public Integer getMaxLength() {
        return mMaxLength;
    }

    public void setMaxLength(Integer maxLength) {
        mMaxLength = maxLength;
    }

    public Integer getMinLength() {
        return mMinLength;
    }

    public void setMinLength(Integer minLength) {
        mMinLength = minLength;
    }

    public String getPattern() {
        return mPattern;
    }

    public void setPattern(String pattern) {
        mPattern = pattern;
    }

    public Integer getMaxItems() {
        return mMaxItems;
    }

    public void setMaxItems(Integer maxItems) {
        mMaxItems = maxItems;
    }

    public Integer getMinItems() {
        return mMinItems;
    }

    public void setMinItems(Integer minItems) {
        mMinItems = minItems;
    }

    public Boolean getUniqueItems() {
        return mUniqueItems;
    }

    public void setUniqueItems(Boolean uniqueItems) {
        mUniqueItems = uniqueItems;
    }

    public List<Object> getEnum() {
        return mEnum;
    }

    public void setEnum(List<Object> anEnum) {
        mEnum = anEnum;
    }

    public Number getMultipleOf() {
        return mMultipleOf;
    }

    public void setMultipleOf(Number multipleOf) {
        mMultipleOf = multipleOf;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mDescription != null) {
            bundle.putString("description", mDescription);
        }

        if (mType != null) {
            bundle.putString("type", mType.getName());
        }

        if (mFormat != null) {
            bundle.putString("format", mFormat.getName());
        }

        if (mItems != null) {
            bundle.putParcelable("items", mItems.toBundle());
        }

        if (mCollectionFormat != null) {
            bundle.putString("collectionFormat", mCollectionFormat);
        }

        if (mDefault != null) {
            if (mDefault instanceof Byte) {
                bundle.putByte("default", ((Number) mDefault).byteValue());
            } else if (mDefault instanceof Short) {
                bundle.putShort("default", ((Number) mDefault).shortValue());
            } else if (mDefault instanceof Integer) {
                bundle.putInt("default", ((Number) mDefault).intValue());
            } else if (mDefault instanceof Long) {
                bundle.putLong("default", ((Number) mDefault).longValue());
            } else if (mDefault instanceof Float) {
                bundle.putFloat("default", ((Number) mDefault).floatValue());
            } else if (mDefault instanceof Double) {
                bundle.putDouble("default", ((Number) mDefault).doubleValue());
            } else if (mDefault instanceof String) {
                bundle.putString("default", (String) mDefault);
            } else if (mDefault instanceof Boolean) {
                bundle.putBoolean("default", (Boolean) mDefault);
            } else if (mDefault instanceof int[]) {
                bundle.putIntArray("default", (int[]) mDefault);
            } else if (mDefault instanceof long[]) {
                bundle.putLongArray("default", (long[]) mDefault);
            } else if (mDefault instanceof double[]) {
                bundle.putDoubleArray("default", (double[]) mDefault);
            } else if (mDefault instanceof float[]) {
                bundle.putFloatArray("default", (float[]) mDefault);
            } else if (mDefault instanceof String[]) {
                bundle.putStringArray("default", (String[]) mDefault);
            } else if (mDefault instanceof boolean[]) {
                bundle.putBooleanArray("default", (boolean[]) mDefault);
            }
        }

        if (mMaximum != null) {
            copyNumber(bundle, "maximum", mMaximum);
        }

        if (mExclusiveMaximum != null) {
            bundle.putBoolean("exclusiveMaximum", mExclusiveMaximum);
        }

        if (mMinimum != null) {
            copyNumber(bundle, "minimum", mMinimum);
        }

        if (mExclusiveMinimum != null) {
            bundle.putBoolean("exclusiveMinimum", mExclusiveMinimum);
        }

        if (mMaxLength != null) {
            bundle.putInt("maxLength", mMaxLength);
        }

        if (mMinLength != null) {
            bundle.putInt("minLength", mMinLength);
        }

        if (mPattern != null) {
            bundle.putString("pattern", mPattern);
        }

        if (mMaxItems != null) {
            bundle.putInt("maxItems", mMaxItems);
        }

        if (mMinItems != null) {
            bundle.putInt("minItems", mMinItems);
        }

        if (mUniqueItems != null) {
            bundle.putBoolean("uniqueItems", mUniqueItems);
        }

        if (mEnum != null) {
            copyEnum(bundle, mType, mFormat, mEnum);
        }

        if (mMultipleOf != null) {
            copyNumber(bundle, "multipleOf", mMultipleOf);
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
