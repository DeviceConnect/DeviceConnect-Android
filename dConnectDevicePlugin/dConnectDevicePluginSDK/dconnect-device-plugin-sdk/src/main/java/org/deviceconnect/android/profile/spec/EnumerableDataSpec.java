/*
 EnumerableDataSpec.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


/**
 * 列挙型で定義可能なデータの使用定義.
 *
 * @param <T> データの型
 * @author NTT DOCOMO, INC.
 */
abstract class EnumerableDataSpec<T> extends DConnectDataSpec {

    private T[] mEnumList;

    EnumerableDataSpec(final DataType type) {
        super(type);
    }

    /**
     * 定数一覧を取得する.
     * @return 定数の配列
     */
    public T[] getEnum() {
        return mEnumList;
    }

    /**
     * 定数一覧を設定する.
     * @param enumList 定数の配列
     */
    void setEnum(final T[] enumList) {
        mEnumList = enumList;
    }

    abstract static class Builder<TYPE, BUILDER extends Builder> {
        TYPE[] mEnumList;

        abstract BUILDER getThis();

        public BUILDER setEnum(final TYPE[] enumList) {
            mEnumList = enumList;
            return getThis();
        }
    }
}
