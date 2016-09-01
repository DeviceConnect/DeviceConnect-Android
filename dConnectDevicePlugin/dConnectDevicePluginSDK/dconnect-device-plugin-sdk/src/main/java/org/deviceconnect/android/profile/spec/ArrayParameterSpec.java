/*
 ArrayParameterSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;

/**
 * Array型リクエストパラメータの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class ArrayParameterSpec extends DConnectParameterSpec<ArrayDataSpec> {

    /**
     * コンストラクタ.
     *
     * @param itemSpec 配列の要素のデータ仕様
     */
    ArrayParameterSpec(final DConnectDataSpec itemSpec) {
        super(new ArrayDataSpec(itemSpec));
    }

    /**
     * 配列に格納できるデータの仕様を取得する.
     * @return 配列に格納できるデータの仕様
     */
    public DConnectDataSpec getItemSpec() {
        return mDataSpec.getItemsSpec();
    }

    /**
     * 配列の最大長を取得する.
     * @return 配列の最大長
     */
    public Integer getMaxLength() {
        return mDataSpec.getMaxLength();
    }

    /**
     * 配列の最大長を設定する.
     * @param maxLength 配列の最大長
     */
    void setMaxLength(final Integer maxLength) {
        mDataSpec.setMaxLength(maxLength);
    }

    /**
     * 配列の最小長を取得する.
     * @return 配列の最小長
     */
    public Integer getMinLength() {
        return mDataSpec.getMinLength();
    }

    /**
     * 配列の最小長を設定する.
     * @param minLength 配列の最小長
     */
    void setMinLength(final Integer minLength) {
        mDataSpec.setMinLength(minLength);
    }

    /**
     * {@link ArrayParameterSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder extends BaseBuilder<Builder> {

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
         * {@link ArrayParameterSpec}のインスタンスを生成する.
         * @return {@link ArrayParameterSpec}のインスタンス
         */
        public ArrayParameterSpec build() {
            ArrayParameterSpec spec = new ArrayParameterSpec(mItemsSpec);
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            spec.setMaxLength(mMaxLength);
            spec.setMinLength(mMinLength);
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
