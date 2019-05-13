/*
 Header.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.List;

/**
 * レスポンスの一部として送信できるヘッダーのリスト.
 *
 * @author NTT DOCOMO, INC.
 */
public class Header extends AbstractSpec {
    /**
     * ヘッダーの詳細.
     */
    private String mDescription;

    /**
     * ヘッダーのタイプ.
     * <p>
     * Required.
     * </p>
     */
    private DataType mType;

    /**
     * ヘッダーのタイプの拡張フォーマット.
     */
    private DataFormat mFormat;

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
     * デフォルト値.
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
     * 文字列のパターン.
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
     */
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

    public Items getItems() {
        return mItems;
    }

    public void setItems(Items items) {
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
