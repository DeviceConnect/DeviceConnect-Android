package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Schema extends AbstractSpec {
    private ExternalDocs mExternalDocs;
    private String mReference;
    private DataType mType;
    private DataFormat mFormat;
    private Object mDefault;
    private String mTitle;
    private String mDescription;
    private Number mMinimum;
    private Number mMaximum;
    private Number mMultipleOf;
    private Boolean mExclusiveMinimum;
    private Boolean mExclusiveMaximum;
    private Integer mMinLength;
    private Integer mMaxLength;
    private String mPattern;
    private Integer mMaxItems;
    private Integer mMinItems;
    private Boolean mUniqueItems;
    private Integer mMaxProperties;
    private Integer mMinProperties;
    private List<String> mRequired;
    private List<Object> mEnum;

    private Schema mItems;
    private List<Schema> mAllOf;
    private Map<String, Schema> mProperties;
    private Schema mAdditionalProperties;

    public ExternalDocs getExternalDocs() {
        return mExternalDocs;
    }

    public void setExternalDocs(ExternalDocs externalDocs) {
        mExternalDocs = externalDocs;
    }

    public String getReference() {
        return mReference;
    }

    public void setReference(String reference) {
        mReference = reference;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public Number getMinimum() {
        return mMinimum;
    }

    public void setMinimum(Number minimum) {
        mMinimum = minimum;
    }

    public Number getMaximum() {
        return mMaximum;
    }

    public void setMaximum(Number maximum) {
        mMaximum = maximum;
    }

    public Number getMultipleOf() {
        return mMultipleOf;
    }

    public void setMultipleOf(Number multipleOf) {
        mMultipleOf = multipleOf;
    }

    public Boolean isExclusiveMinimum() {
        return mExclusiveMaximum != null ? mExclusiveMaximum : false;
    }

    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        mExclusiveMinimum = exclusiveMinimum;
    }

    public Boolean isExclusiveMaximum() {
        return mExclusiveMinimum != null ? mExclusiveMinimum : false;
    }

    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        mExclusiveMaximum = exclusiveMaximum;
    }

    public Integer getMinLength() {
        return mMinLength;
    }

    public void setMinLength(Integer minLength) {
        mMinLength = minLength;
    }

    public Integer getMaxLength() {
        return mMaxLength;
    }

    public void setMaxLength(Integer maxLength) {
        mMaxLength = maxLength;
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

    public Integer getMaxProperties() {
        return mMaxProperties;
    }

    public void setMaxProperties(Integer maxProperties) {
        mMaxProperties = maxProperties;
    }

    public Integer getMinProperties() {
        return mMinProperties;
    }

    public void setMinProperties(Integer minProperties) {
        mMinProperties = minProperties;
    }

    public List<String> getRequired() {
        return mRequired;
    }

    public void setRequired(List<String> required) {
        mRequired = required;
    }

    public List<Object> getEnum() {
        return mEnum;
    }

    public void setEnum(List<Object> anEnum) {
        mEnum = anEnum;
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

    public List<Schema> getAllOf() {
        return mAllOf;
    }

    public void setAllOf(List<Schema> allOf) {
        mAllOf = allOf;
    }

    public Map<String, Schema> getProperties() {
        return mProperties;
    }

    public void setProperties(Map<String, Schema> properties) {
        mProperties = properties;
    }

    public Schema getAdditionalProperties() {
        return mAdditionalProperties;
    }

    public void setAdditionalProperties(Schema additionalProperties) {
        mAdditionalProperties = additionalProperties;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mReference != null) {
            bundle.putString("$ref", mReference);
        }

        if (mType != null) {
            bundle.putString("type", mType.getName());
        }

        if (mFormat != null) {
            bundle.putString("format", mFormat.getName());
        }

        if (mTitle != null) {
            bundle.putString("title", mTitle);
        }

        if (mDescription != null) {
            bundle.putString("description", mDescription);
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

        if (mMaxProperties != null) {
            bundle.putInt("maxProperties", mMaxProperties);
        }

        if (mMinProperties != null) {
            bundle.putInt("minProperties", mMinProperties);
        }

        if (mRequired != null) {
            bundle.putStringArray("required", mRequired.toArray(new String[0]));
        }

        if (mEnum != null) {
            copyEnum(bundle, mType, mFormat, mEnum);
        }

        if (mItems != null) {
            bundle.putParcelable("items", mItems.toBundle());
        }

        if (mAllOf != null && !mAllOf.isEmpty()) {
            List<Bundle> allOf = new ArrayList<>();
            for (Schema schema : mAllOf) {
                allOf.add(schema.toBundle());
            }
            bundle.putParcelableArray("allOf", allOf.toArray(new Bundle[0]));
        }

        if (mProperties != null && !mProperties.isEmpty()) {
            Bundle properties = new Bundle();
            for (Map.Entry<String, Schema> entry : mProperties.entrySet()) {
                properties.putParcelable(entry.getKey(), entry.getValue().toBundle());
            }
            bundle.putParcelable("properties", properties);
        }

        if (mAdditionalProperties != null) {
            bundle.putParcelable("additionalProperties", mAdditionalProperties.toBundle());
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
