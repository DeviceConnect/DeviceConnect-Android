/*
 AbstractParameter.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models.parameters;

import android.os.Bundle;

import org.deviceconnect.android.profile.spec.models.DataFormat;
import org.deviceconnect.android.profile.spec.models.DataType;
import org.deviceconnect.android.profile.spec.models.Items;
import org.deviceconnect.android.profile.spec.models.Property;

import java.util.List;

/**
 * Body 以外のパラメータ.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class AbstractParameter extends Parameter implements Property {
    /**
     * パラメータのタイプ.
     *
     * <p>
     * Required.
     * </p>
     */
    private DataType mType;

    /**
     * パラメータのタイプの拡張フォーマット.
     */
    private DataFormat mFormat;

    /**
     * 空の値の許可.
     * <p>
     * trueの場合は、空の値を設定することができます。
     * </p>
     */
    private Boolean mAllowEmptyValue;

    /**
     * 配列の要素の宣言.
     * <p>
     * {@link #mType} が {@link DataType#ARRAY} の場合は Required.
     * </p>
     */
    private Items mItems;

    /**
     * 配列のフォーマット.
     * <p>
     * {@link #mType} が {@link DataType#ARRAY} の場合に使用される配列のフォーマット.
     * csv, ssv, tsv, pipes, multi が定義されます。
     * </p>
     * <p>
     * 省略された場合は csv になります。
     * </p>
     */
    private String mCollectionFormat;

    /**
     * パラメータのデフォルト値.
     */
    private Object mDefault;

    /**
     * 値の最大値.
     */
    private Number mMaximum;

    /**
     * 最大値を含む、含まない.
     */
    private Boolean mExclusiveMaximum;

    /**
     * 値の最小値.
     */
    private Number mMinimum;

    /**
     * 最小値を含む、含まない.
     */
    private Boolean mExclusiveMinimum;

    /**
     * 文字列の最大の長さ.
     */
    private Integer mMaxLength;

    /**
     * 文字列の最小の長さ.
     */
    private Integer mMinLength;

    /**
     * 文字列の正規表現.
     */
    private String mPattern;

    /**
     * 配列の最大のサイズ.
     */
    private Integer mMaxItems;

    /**
     * 配列の最小のサイズ.
     */
    private Integer mMinItems;

    /**
     * 配列の要素ユニーク宣言.
     */
    private Boolean mUniqueItems;

    /**
     * 使用できる値を列挙したリスト.
     */
    private List<Object> mEnum;

    /**
     * 倍数宣言.
     *
     * <p>
     * パラメータの値が、この値の倍数になる必要があります。
     * </p>
     */
    private Number mMultipleOf;

    @Override
    public DataType getType() {
        return mType;
    }

    @Override
    public void setType(DataType type) {
        mType = type;
    }

    @Override
    public DataFormat getFormat() {
        return mFormat;
    }

    @Override
    public void setFormat(DataFormat format) {
        mFormat = format;
    }

    @Override
    public Boolean isAllowEmptyValue() {
        return mAllowEmptyValue != null ? mAllowEmptyValue : false;
    }

    @Override
    public void setAllowEmptyValue(Boolean allowEmptyValue) {
        mAllowEmptyValue = allowEmptyValue;
    }

    @Override
    public Items getItems() {
        return mItems;
    }

    @Override
    public void setItems(Items items) {
        mItems = items;
    }

    @Override
    public String getCollectionFormat() {
        return mCollectionFormat != null ? mCollectionFormat : "csv";
    }

    @Override
    public void setCollectionFormat(String collectionFormat) {
        mCollectionFormat = collectionFormat;
    }

    @Override
    public Object getDefault() {
        return mDefault;
    }

    @Override
    public void setDefault(Object aDefault) {
        mDefault = aDefault;
    }

    @Override
    public Number getMaximum() {
        return mMaximum;
    }

    @Override
    public void setMaximum(Number maximum) {
        mMaximum = maximum;
    }

    @Override
    public Boolean isExclusiveMaximum() {
        return mExclusiveMaximum != null ? mExclusiveMaximum : false;
    }

    @Override
    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        mExclusiveMaximum = exclusiveMaximum;
    }

    @Override
    public Number getMinimum() {
        return mMinimum;
    }

    @Override
    public void setMinimum(Number minimum) {
        mMinimum = minimum;
    }

    @Override
    public Boolean isExclusiveMinimum() {
        return mExclusiveMinimum != null ? mExclusiveMinimum : false;
    }

    @Override
    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        mExclusiveMinimum = exclusiveMinimum;
    }

    @Override
    public Integer getMaxLength() {
        return mMaxLength;
    }

    @Override
    public void setMaxLength(Integer maxLength) {
        mMaxLength = maxLength;
    }

    @Override
    public Integer getMinLength() {
        return mMinLength;
    }

    @Override
    public void setMinLength(Integer minLength) {
        mMinLength = minLength;
    }

    @Override
    public String getPattern() {
        return mPattern;
    }

    @Override
    public void setPattern(String pattern) {
        mPattern = pattern;
    }

    @Override
    public Integer getMaxItems() {
        return mMaxItems;
    }

    @Override
    public void setMaxItems(Integer maxItems) {
        mMaxItems = maxItems;
    }

    @Override
    public Integer getMinItems() {
        return mMinItems;
    }

    @Override
    public void setMinItems(Integer minItems) {
        mMinItems = minItems;
    }

    @Override
    public Boolean isUniqueItems() {
        return mUniqueItems != null ? mUniqueItems : false;
    }

    @Override
    public void setUniqueItems(Boolean uniqueItems) {
        mUniqueItems = uniqueItems;
    }

    @Override
    public List<Object> getEnum() {
        return mEnum;
    }

    @Override
    public void setEnum(List<Object> anEnum) {
        mEnum = anEnum;
    }

    @Override
    public Number getMultipleOf() {
        return mMultipleOf;
    }

    @Override
    public void setMultipleOf(Number multipleOf) {
        mMultipleOf = multipleOf;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        copyParameter(bundle);

        if (mType != null) {
            bundle.putString("type", mType.getName());
        }

        if (mFormat != null) {
            bundle.putString("format", mFormat.getName());
        }

        if (mAllowEmptyValue != null) {
            bundle.putBoolean("allowEmptyValue", mAllowEmptyValue);
        }

        if (mItems != null) {
            bundle.putParcelable("items", mItems.toBundle());
        }

        if (mCollectionFormat != null) {
            bundle.putString("collectionFormat", mCollectionFormat);
        }

        if (mDefault != null && mType != null) {
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
