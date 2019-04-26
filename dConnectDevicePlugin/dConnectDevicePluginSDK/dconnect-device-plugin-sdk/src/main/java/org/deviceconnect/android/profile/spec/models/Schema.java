/*
 Schema.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * パラメータ、レスポンスなどに使用されるタイプを定義するスキーマ.
 *
 * @author NTT DOCOMO, INC.
 */
public class Schema extends AbstractSpec implements Property {
    /**
     * 外部文書.
     */
    private ExternalDocs mExternalDocs;

    /**
     * 仕様内の他の定義を参照できるようにするためのオブジェクト.
     */
    private String mReference;

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
     * パラメータのデフォルト値.
     */
    private Object mDefault;

    /**
     * タイトル.
     */
    private String mTitle;

    /**
     * 詳細.
     */
    private String mDescription;

    /**
     * 数値の最小値.
     */
    private Number mMinimum;

    /**
     * 数値の最大値.
     */
    private Number mMaximum;

    /**
     * 数値の倍数.
     */
    private Number mMultipleOf;

    /**
     * 数値の最小値を判定に含まない.
     */
    private Boolean mExclusiveMinimum;

    /**
     * 数値の最大値を判定に含まない.
     */
    private Boolean mExclusiveMaximum;

    /**
     * 文字列の最小の長さ.
     */
    private Integer mMinLength;

    /**
     * 文字列の最大の長さ.
     */
    private Integer mMaxLength;

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
     * 空の値の許可.
     * <p>
     * trueの場合は、空の値を設定することができます。
     * </p>
     */
    private Boolean mAllowEmptyValue;

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
     * 配列のアイテムのタイプ.
     * <p>
     * {@link #mType} が {@link DataType#ARRAY} の場合は Required.
     * </p>
     */
    private Items mItems;

    /**
     * プロパティのマップ.
     */
    private Map<String, Schema> mProperties;

    /**
     * properties の最大個数.
     */
    private Integer mMaxProperties;

    /**
     * properties の最小個数.
     */
    private Integer mMinProperties;

    /**
     * properties の要素で必須になるプロパティのリスト.
     */
    private List<String> mRequired;

    /**
     * allOf のリスト.
     */
    private List<Schema> mAllOf;

    /**
     * 追加されたプロパティ.
     */
    private Schema mAdditionalProperties;

    /**
     * 外部文書を取得します.
     *
     * @return 外部文書
     */
    public ExternalDocs getExternalDocs() {
        return mExternalDocs;
    }

    /**
     * 外部文書を設定します.
     *
     * @param externalDocs 外部文書
     */
    public void setExternalDocs(ExternalDocs externalDocs) {
        mExternalDocs = externalDocs;
    }

    /**
     * リファレンスを取得します.
     *
     * @return リファレンス
     */
    public String getReference() {
        return mReference;
    }

    /**
     * リファレンスを設定します.
     *
     * @param reference リファレンス
     */
    public void setReference(String reference) {
        mReference = reference;
    }

    /**
     * タイトルを取得します.
     *
     * @return タイトル
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * タイトルを設定します.
     *
     * @param title タイトル
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * 詳細を取得します.
     *
     * @return 詳細
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * 詳細を設定します.
     *
     * @param description 詳細
     */
    public void setDescription(String description) {
        mDescription = description;
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
    public Number getMaximum() {
        return mMaximum;
    }

    @Override
    public void setMaximum(Number maximum) {
        mMaximum = maximum;
    }

    @Override
    public Boolean isExclusiveMinimum() {
        return mExclusiveMaximum != null ? mExclusiveMaximum : false;
    }

    @Override
    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        mExclusiveMinimum = exclusiveMinimum;
    }

    @Override
    public Boolean isExclusiveMaximum() {
        return mExclusiveMinimum != null ? mExclusiveMinimum : false;
    }

    @Override
    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        mExclusiveMaximum = exclusiveMaximum;
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
    public Integer getMaxLength() {
        return mMaxLength;
    }

    @Override
    public void setMaxLength(Integer maxLength) {
        mMaxLength = maxLength;
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
    public Number getMultipleOf() {
        return mMultipleOf;
    }

    @Override
    public void setMultipleOf(Number multipleOf) {
        mMultipleOf = multipleOf;
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
    public Object getDefault() {
        return mDefault;
    }

    @Override
    public void setDefault(Object aDefault) {
        mDefault = aDefault;
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
    public Boolean isAllowEmptyValue() {
        return mAllowEmptyValue != null ? mAllowEmptyValue : false;
    }

    @Override
    public void setAllowEmptyValue(Boolean allowEmptyValue) {
        mAllowEmptyValue = allowEmptyValue;
    }

    @Override
    public String getCollectionFormat() {
        return mCollectionFormat != null ? mCollectionFormat : "csv";
    }

    @Override
    public void setCollectionFormat(String collectionFormat) {
        mCollectionFormat = collectionFormat;
    }

    /**
     * allOf のリストを取得します.
     *
     * @return allOf のリスト
     */
    public List<Schema> getAllOf() {
        return mAllOf;
    }

    /**
     * allOf のリストを設定します.
     *
     * @param allOf allOf のリスト
     */
    public void setAllOf(List<Schema> allOf) {
        mAllOf = allOf;
    }

    /**
     * プロパティの最大個数を取得します.
     *
     * @return プロパティの最大個数
     */
    public Integer getMaxProperties() {
        return mMaxProperties;
    }

    /**
     * プロパティの最大個数を設定します.
     *
     * @param maxProperties プロパティの最大個数
     */
    public void setMaxProperties(Integer maxProperties) {
        mMaxProperties = maxProperties;
    }

    /**
     * プロパティの最小個数を取得します.
     *
     * @return プロパティの最小個数
     */
    public Integer getMinProperties() {
        return mMinProperties;
    }

    /**
     * プロパティの最小個数を設定します.
     *
     * @param minProperties プロパティの最小個数を
     */
    public void setMinProperties(Integer minProperties) {
        mMinProperties = minProperties;
    }

    /**
     * 必須プロパティのリストを取得します.
     *
     * @return 必須プロパティのリスト
     */
    public List<String> getRequired() {
        return mRequired;
    }

    /**
     * 必須プロパティのリストを設定します.
     *
     * @param required 必須プロパティのリスト
     */
    public void setRequired(List<String> required) {
        mRequired = required;
    }

    /**
     * プロパティのマップを取得します.
     *
     * @return プロパティのマップ
     */
    public Map<String, Schema> getProperties() {
        return mProperties;
    }

    /**
     * プロパティのマップを設定します.
     *
     * @param properties プロパティのマップ
     */
    public void setProperties(Map<String, Schema> properties) {
        mProperties = properties;
    }

    /**
     * 追加のプロパティを取得します.
     *
     * @return 追加のプロパティ
     */
    public Schema getAdditionalProperties() {
        return mAdditionalProperties;
    }

    /**
     * 追加のプロパティを設定します.
     *
     * @param additionalProperties 追加のプロパティ
     */
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
