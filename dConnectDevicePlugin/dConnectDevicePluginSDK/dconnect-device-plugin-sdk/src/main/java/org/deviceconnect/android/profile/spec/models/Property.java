/*
 Property.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import java.util.List;

/**
 * パラメータに使用されるタイプを定義.
 *
 * @author NTT DOCOMO, INC.
 */
public interface Property {
    /**
     * パラメータのタイプを取得します.
     *
     * <p>
     * 必須パラメータになります。
     * </p>
     *
     * @return パラメータのタイプ
     */
    DataType getType();

    /**
     * パラメータのタイプを設定します.
     *
     * <p>
     * 必須パラメータになります。
     * </p>
     *
     * @param type パラメータのタイプ
     */
    void setType(DataType type);

    /**
     * パラメータの拡張フォーマットを取得します.
     *
     * @return パラメータの拡張フォーマット
     */
    DataFormat getFormat();

    /**
     * パラメータの拡張フォーマットを設定します.
     *
     * @param format パラメータの拡張フォーマット
     */
    void setFormat(DataFormat format);

    /**
     * パラメータのデフォルト値を取得します.
     *
     * @return パラメータのデフォルト値
     */
    Object getDefault();

    /**
     * パラメータのデフォルト値を設定します.
     *
     * @param aDefault パラメータのデフォルト値
     */
    void setDefault(Object aDefault);

    /**
     * パラメータが指定できる最大値を取得します.
     *
     * @return パラメータの最大値
     */
    Number getMaximum();

    /**
     * パラメータが指定できる最大値を設定します.
     *
     * @param maximum パラメータの最大値
     */
    void setMaximum(Number maximum);

    /**
     * 最大値を含めるか確認します.
     *
     * @return 最大値を含めない場合はtrue、それ以外はfalse
     */
    Boolean isExclusiveMaximum();

    /**
     * 最大値を含めるかを設定します.
     *
     * <p>
     * null が指定された場合には、false として扱います。
     * </p>
     *
     * @param exclusiveMaximum 最大値を含めない場合はtrue、それ以外はfalse
     */
    void setExclusiveMaximum(Boolean exclusiveMaximum);

    /**
     * パラメータが指定できる最小値を取得します.
     *
     * @return パラメータが指定できる最小値
     */
    Number getMinimum();

    /**
     * パラメータが指定できる最小値を設定します.
     *
     * @param minimum パラメータが指定できる最小値
     */
    void setMinimum(Number minimum);

    /**
     * 最小値を含めるか確認します.
     *
     * @return 最小値を含めない場合はtrue、それ以外はfalse
     */
    Boolean isExclusiveMinimum();

    /**
     * 最小値を含めるかを設定します.
     *
     * <p>
     * null が指定された場合には、false として扱います。
     * </p>
     *
     * @param exclusiveMinimum 最小値を含めない場合はtrue、それ以外はfalse
     */
    void setExclusiveMinimum(Boolean exclusiveMinimum);

    /**
     * 文字列の最大サイズを取得します.
     *
     * @return 文字列の最大サイズ
     */
    Integer getMaxLength();

    /**
     * 文字列の最大サイズを設定します.
     *
     * @param maxLength 文字列の最大サイズ
     */
    void setMaxLength(Integer maxLength);

    /**
     * 文字列の最小サイズを設定します.
     *
     * @return 文字列の最小サイズ
     */
    Integer getMinLength();

    /**
     * 文字列の最小サイズを設定します.
     *
     * @param minLength 文字列の最小サイズ
     */
    void setMinLength(Integer minLength);

    /**
     * 文字列のパターンを設定します.
     *
     * <p>
     * 正規表現でパターンを設定することができます。
     * </p>
     *
     * @return 文字列のパターン
     */
    String getPattern();

    /**
     * 文字列のパターンを設定します.
     *
     * @param pattern 文字列のパターン
     */
    void setPattern(String pattern);


    /**
     * 空値許可を確認します.
     *
     * @return 空値を許可する場合はtrue、それ以外はfalse
     */
    Boolean isAllowEmptyValue();

    /**
     * 空値許可を設定します.
     *
     * <p>
     * null が指定された場合には、false として扱います。
     * </p>
     *
     * @param allowEmptyValue 空値を許可する場合はtrue、それ以外はfalse
     */
    void setAllowEmptyValue(Boolean allowEmptyValue);

    /**
     * 配列のアイテムのタイプを取得します.
     *
     * @return 配列のフォーマット
     */
    Items getItems();

    /**
     * 配列のアイテムのタイプを設定します.
     *
     * @param items 配列のアイテムのタイプ
     */
    void setItems(Items items);

    /**
     * 配列のフォーマットを取得します.
     *
     * <p>
     * {@link #getType()} が {@link DataType#ARRAY} の場合に使用される配列のフォーマット.
     * csv, ssv, tsv, pipes, multi が設定可能です。
     * </p>
     *
     * <p>
     * 省略された場合は csv になります。
     * </p>
     *
     * @return 配列のフォーマット
     */
    String getCollectionFormat();

    /**
     * 配列のフォーマットを設定します.
     *
     * @param collectionFormat 配列のフォーマット
     */
    void setCollectionFormat(String collectionFormat);

    /**
     * 配列の最大サイズを取得します.
     *
     * @return 配列の最大サイズ
     */
    Integer getMaxItems();

    /**
     * 配列の最大サイズを設定します.
     *
     * @param maxItems 配列の最大サイズ
     */
    void setMaxItems(Integer maxItems);

    /**
     * 配列の最小サイズを取得します.
     *
     * @return 配列の最小サイズ
     */
    Integer getMinItems();

    /**
     * 配列の最小サイズを設定します.
     *
     * @param minItems 配列の最小サイズ
     */
    void setMinItems(Integer minItems);

    /**
     * 配列の全ての値がユニーク宣言を取得します.
     *
     * <p>
     * uniqueItems が true の場合には、配列の中身が全てユニークになっている必要があります。
     * </p>
     *
     * @return 配列の全ての値がユニークなる場合はtrue、それ以外は false
     */
    Boolean isUniqueItems();

    /**
     * 配列の全ての値がユニーク宣言を設定します.
     *
     * @param uniqueItems 配列の全ての値がユニークなる場合はtrue、それ以外は false
     */
    void setUniqueItems(Boolean uniqueItems);

    /**
     * パラメータに指定できる値の列挙型を取得します.
     *
     * @return パラメータに指定できる値の列挙型
     */
    List<Object> getEnum();

    /**
     * パラメータに指定できる値の列挙型を設定します.
     *
     * @param anEnum パラメータに指定できる値の列挙型
     */
    void setEnum(List<Object> anEnum);

    /**
     * 数値の倍数宣言を取得します.
     *
     * <p>
     * multipleOfが設定されている場合は、指定された値で割り切れるようになる必要があります。
     * </p>
     *
     * @return multipleOf の値
     */
    Number getMultipleOf();

    /**
     * 数値パラメータが multipleOf の倍数宣言を設定します.
     *
     * @param multipleOf 値
     */
    void setMultipleOf(Number multipleOf);
}
