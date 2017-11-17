/*
 StringParameterSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


/**
 * String型リクエストパラメータの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class StringParameterSpec extends EnumerableParameterSpec<String, StringDataSpec> {

    /**
     * コンストラクタ.
     * @param format データのフォーマット指定
     */
    StringParameterSpec(final DataFormat format) {
        super(new StringDataSpec(format));
    }

    /**
     * データのフォーマット指定を取得する.
     * @return データのフォーマット指定
     */
    public DataFormat getFormat() {
        return mDataSpec.getFormat();
    }

    /**
     * 文字列の最大長を取得する.
     * @return 文字列の最大長
     */
    public Integer getMaxLength() {
        return mDataSpec.getMaxLength();
    }

    /**
     * 文字列の最大長を設定する.
     * @param maxLength 文字列の最大長
     */
    void setMaxLength(final Integer maxLength) {
        mDataSpec.setMaxLength(maxLength);
    }

    /**
     * 文字列の最小長を取得する.
     * @return 文字列の最小長
     */
    public Integer getMinLength() {
        return mDataSpec.getMinLength();
    }

    /**
     * 文字列の最小長を設定する.
     * @param minLength 文字列の最小長
     */
    void setMinLength(final Integer minLength) {
        mDataSpec.setMinLength(minLength);
    }

    /**
     * 定数一覧を取得する.
     * @return 定数の配列
     * @deprecated getEnum() を使用してください.
     */
    public String[] getEnumList() {
        return mDataSpec.getEnumList();
    }

    /**
     * {@link StringParameterSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder extends EnumerableParameterSpec.Builder<String, Builder> {

        private DataFormat mFormat;
        private Integer mMaxLength;
        private Integer mMinLength;

        /**
         * データのフォーマット指定を設定する.
         * @param format データのフォーマット指定
         * @return ビルダー自身のインスタンス
         */
        public Builder setFormat(final DataFormat format) {
            mFormat = format;
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
         * 定数一覧を取得する.
         * @param enumList 定数の配列
         * @return ビルダー自身のインスタンス
         * @deprecated setEnum(String[]) を使用してください.
         */
        public Builder setEnumList(final String[] enumList) {
            mEnum = enumList;
            return this;
        }

        /**
         * {@link StringParameterSpec}のインスタンスを生成する.
         * @return {@link StringParameterSpec}のインスタンス
         */
        public StringParameterSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.TEXT;
            }
            StringParameterSpec spec = new StringParameterSpec(mFormat);
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            if (mEnum != null) {
                spec.setEnum(mEnum);
            } else {
                spec.setMaxLength(mMaxLength);
                spec.setMinLength(mMinLength);
            }
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }

}
