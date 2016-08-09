/*
 ArrayDataSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


/**
 * Array型データの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class ArrayDataSpec extends DConnectDataSpec {

    private final DConnectDataSpec mItemsSpec;
    private Integer mMaxLength;
    private Integer mMinLength;

    ArrayDataSpec(final DConnectDataSpec itemsSpec) {
        super(DataType.ARRAY);
        mItemsSpec = itemsSpec;
    }

    /**
     * 配列に格納できるデータの仕様を取得する.
     * @return 配列に格納できるデータの仕様
     */
    public DConnectDataSpec getItemsSpec() {
        return mItemsSpec;
    }

    /**
     * 配列の最大長を取得する.
     * @return 配列の最大長
     */
    public Integer getMaxLength() {
        return mMaxLength;
    }

    /**
     * 配列の最大長を設定する.
     * @param maxLength 配列の最大長
     */
    void setMaxLength(final Integer maxLength) {
        mMaxLength = maxLength;
    }

    /**
     * 配列の最小長を取得する.
     * @return 配列の最小長
     */
    public Integer getMinLength() {
        return mMinLength;
    }

    /**
     * 配列の最小長を設定する.
     * @param minLength 配列の最小長
     */
    void setMinLength(final Integer minLength) {
        mMinLength = minLength;
    }

    @Override
    public boolean validate(final Object obj) {
        if (obj == null) {
            return true;
        }
        String arrayParam = obj.toString();
        if (arrayParam.equals("")) { // TODO allowEmptyValueに対応
            return true;
        }
        String[] items = arrayParam.split(","); // TODO csv以外の形式に対応
        for (String item : items) {
            if (!mItemsSpec.validate(item)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@link ArrayDataSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder {

        private DConnectDataSpec mItemsSpec;
        private Integer mMaxLength;
        private Integer mMinLength;

        /**
         * 配列に格納できるデータの仕様を設定する.
         *
         * @param itemsSpec 配列に格納できるデータの仕様
         * @return ビルダー自身のインスタンス
         */
        public Builder setItemsSpec(final DConnectDataSpec itemsSpec) {
            mItemsSpec = itemsSpec;
            return this;
        }

        /**
         * 配列の最大長を設定する.
         * @param maxLength 配列の最大長
         * @return ビルダー自身のインスタンス
         */
        public Builder setMaxLength(final Integer maxLength) {
            mMaxLength = maxLength;
            return this;
        }

        /**
         * 配列の最小長を設定する.
         * @param minLength 配列の最小長
         * @return ビルダー自身のインスタンス
         */
        public Builder setMinLength(final Integer minLength) {
            mMinLength = minLength;
            return this;
        }

        /**
         * {@link ArrayDataSpec}のインスタンスを生成する.
         * @return {@link ArrayDataSpec}のインスタンス
         */
        public ArrayDataSpec build() {
            ArrayDataSpec spec = new ArrayDataSpec(mItemsSpec);
            spec.setMaxLength(mMaxLength);
            spec.setMinLength(mMinLength);
            return spec;
        }
    }
}
