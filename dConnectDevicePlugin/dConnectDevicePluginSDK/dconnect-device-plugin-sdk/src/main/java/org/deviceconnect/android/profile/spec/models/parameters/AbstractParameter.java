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
import org.deviceconnect.android.profile.spec.models.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Body 以外のパラメータ.
 *
 * @author NTT DOCOMO, INC.
 */
public class AbstractParameter extends Parameter {
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
     * 配列のアイテムのタイプ.
     * <p>
     * {@link #mType} が {@link DataType#ARRAY} の場合は Required.
     * </p>
     */
    private Schema mItems;

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
     * ユニーク.
     */
    private Boolean mUniqueItems;

    /**
     * 列挙型.
     */
    private List<Object> mEnum;

    /**
     *
     */
    private Number mMultipleOf;

    /**
     * パラメータのタイプを取得します.
     *
     * @return パラメータのタイプ
     */
    public DataType getType() {
        return mType;
    }

    /**
     * パラメータのタイプを設定します.
     *
     * @param type パラメータのタイプ
     */
    public void setType(DataType type) {
        mType = type;
    }

    /**
     * パラメータの拡張フォーマットを取得します.
     *
     * @return パラメータの拡張フォーマット
     */
    public DataFormat getFormat() {
        return mFormat;
    }

    /**
     * パラメータの拡張フォーマットを設定します.
     *
     * @param format パラメータの拡張フォーマット
     */
    public void setFormat(DataFormat format) {
        mFormat = format;
    }

    /**
     * 空値許可を確認します.
     *
     * @return 空値を許可する場合はtrue、それ以外はfalse
     */
    public Boolean isAllowEmptyValue() {
        return mAllowEmptyValue != null ? mAllowEmptyValue : false;
    }

    /**
     * 空値許可を設定します.
     *
     * @param allowEmptyValue 空値を許可する場合はtrue、それ以外はfalse
     */
    public void setAllowEmptyValue(Boolean allowEmptyValue) {
        mAllowEmptyValue = allowEmptyValue;
    }

    /**
     * 配列のアイテムのタイプを取得します.
     *
     * @return 配列のフォーマット
     */
    public Schema getItems() {
        return mItems;
    }

    /**
     * 配列のアイテムのタイプを設定します.
     *
     * @param items 配列のアイテムのタイプ
     */
    public void setItems(Schema items) {
        mItems = items;
    }

    /**
     * 配列のフォーマットを取得します.
     *
     * @return 配列のフォーマット
     */
    public String getCollectionFormat() {
        return mCollectionFormat;
    }

    /**
     * 配列のフォーマットを設定します.
     *
     * @param collectionFormat 配列のフォーマット
     */
    public void setCollectionFormat(String collectionFormat) {
        mCollectionFormat = collectionFormat;
    }

    /**
     * パラメータのデフォルト値を取得します.
     *
     * @return パラメータのデフォルト値
     */
    public Object getDefault() {
        return mDefault;
    }

    /**
     * パラメータのデフォルト値を設定します.
     *
     * @param aDefault パラメータのデフォルト値
     */
    public void setDefault(Object aDefault) {
        mDefault = aDefault;
    }

    /**
     * パラメータが指定できる最大値を取得します.
     *
     * @return パラメータの最大値
     */
    public Number getMaximum() {
        return mMaximum;
    }

    /**
     * パラメータが指定できる最大値を設定します.
     *
     * @param maximum パラメータの最大値
     */
    public void setMaximum(Number maximum) {
        mMaximum = maximum;
    }

    /**
     * 最大値を含めるか確認します.
     *
     * @return 最大値を含めない場合はtrue、それ以外はfalse
     */
    public Boolean isExclusiveMaximum() {
        return mExclusiveMaximum != null ? mExclusiveMaximum : false;
    }

    /**
     * 最大値を含めるかを設定します.
     *
     * @param exclusiveMaximum 最大値を含めない場合はtrue、それ以外はfalse
     */
    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        mExclusiveMaximum = exclusiveMaximum;
    }

    /**
     * パラメータが指定できる最小値を取得します.
     *
     * @return パラメータが指定できる最小値
     */
    public Number getMinimum() {
        return mMinimum;
    }

    /**
     * パラメータが指定できる最小値を設定します.
     *
     * @param minimum パラメータが指定できる最小値
     */
    public void setMinimum(Number minimum) {
        mMinimum = minimum;
    }

    /**
     * 最小値を含めるか確認します.
     *
     * @return 最小値を含めない場合はtrue、それ以外はfalse
     */
    public Boolean isExclusiveMinimum() {
        return mExclusiveMinimum != null ? mExclusiveMinimum : false;
    }

    /**
     * 最小値を含めるかを設定します.
     *
     * @param exclusiveMinimum 最小値を含めない場合はtrue、それ以外はfalse
     */
    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        mExclusiveMinimum = exclusiveMinimum;
    }

    /**
     * 文字列の最大サイズを取得します.
     *
     * @return 文字列の最大サイズ
     */
    public Integer getMaxLength() {
        return mMaxLength;
    }

    /**
     * 文字列の最大サイズを設定します.
     *
     * @param maxLength 文字列の最大サイズ
     */
    public void setMaxLength(Integer maxLength) {
        mMaxLength = maxLength;
    }

    /**
     * 文字列の最小サイズを設定します.
     *
     * @return 文字列の最小サイズ
     */
    public Integer getMinLength() {
        return mMinLength;
    }

    /**
     * 文字列の最小サイズを設定します.
     *
     * @param minLength 文字列の最小サイズ
     */
    public void setMinLength(Integer minLength) {
        mMinLength = minLength;
    }

    /**
     * 文字列のパターンを設定します.
     *
     * <p>
     * 正規表現でパターンを設定することができます。
     * </p>
     *
     * @return 文字列のパターン
     */
    public String getPattern() {
        return mPattern;
    }

    /**
     * 文字列のパターンを設定します.
     *
     * @param pattern 文字列のパターン
     */
    public void setPattern(String pattern) {
        mPattern = pattern;
    }

    /**
     * 配列の最大サイズを取得します.
     *
     * @return 配列の最大サイズ
     */
    public Integer getMaxItems() {
        return mMaxItems;
    }

    /**
     * 配列の最大サイズを設定します.
     *
     * @param maxItems 配列の最大サイズ
     */
    public void setMaxItems(Integer maxItems) {
        mMaxItems = maxItems;
    }

    /**
     * 配列の最小サイズを取得します.
     *
     * @return 配列の最小サイズ
     */
    public Integer getMinItems() {
        return mMinItems;
    }

    /**
     * 配列の最小サイズを設定します.
     *
     * @param minItems 配列の最小サイズ
     */
    public void setMinItems(Integer minItems) {
        mMinItems = minItems;
    }

    public Boolean isUniqueItems() {
        return mUniqueItems != null ? mUniqueItems : false;
    }

    public void setUniqueItems(Boolean uniqueItems) {
        mUniqueItems = uniqueItems;
    }

    /**
     * パラメータに指定できる値の列挙型を取得します.
     *
     * @return パラメータに指定できる値の列挙型
     */
    public List<Object> getEnum() {
        return mEnum;
    }

    /**
     * パラメータに指定できる値の列挙型を設定します.
     *
     * @param anEnum パラメータに指定できる値の列挙型
     */
    public void setEnum(List<Object> anEnum) {
        mEnum = anEnum;
    }

    /**
     * パラメータに指定できる値の列挙型を追加します.
     *
     * @param anEnum パラメータに指定できる値の列挙型
     */
    public void addEnum(Object anEnum) {
        if (mEnum == null) {
            mEnum = new ArrayList<>();
        }
        mEnum.add(anEnum);
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
