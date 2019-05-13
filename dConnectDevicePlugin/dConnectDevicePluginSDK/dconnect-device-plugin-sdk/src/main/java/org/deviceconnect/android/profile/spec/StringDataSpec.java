/*
 StringDataSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


import java.util.regex.Pattern;


/**
 * String型データの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class StringDataSpec extends EnumerableDataSpec<String> {

    private static final Pattern RGB_PATTERN = Pattern.compile("[0-9a-fA-F]{6}");

    private final DataFormat mFormat;
    private Integer mMaxLength;
    private Integer mMinLength;

    /**
     * コンストラクタ.
     *
     * @param format データのフォーマット指定
     */
    StringDataSpec(final DataFormat format) {
        super(DataType.STRING);
        mFormat = format;
    }

    /**
     * データのフォーマット指定を取得する.
     * @return データのフォーマット指定
     */
    public DataFormat getFormat() {
        return mFormat;
    }

    /**
     * 文字列の最大長を取得する.
     * @return 文字列の最大長
     */
    public Integer getMaxLength() {
        return mMaxLength;
    }

    /**
     * 文字列の最大長を設定する.
     * @param maxLength 文字列の最大長
     */
    void setMaxLength(final Integer maxLength) {
        mMaxLength = maxLength;
    }

    /**
     * 文字列の最小長を取得する.
     * @return 文字列の最小長
     */
    public Integer getMinLength() {
        return mMinLength;
    }

    /**
     * 文字列の最小長を設定する.
     * @param minLength 文字列の最小長
     */
    void setMinLength(final Integer minLength) {
        mMinLength = minLength;
    }

    /**
     * 定数一覧を取得する.
     * @return 定数の配列
     * @deprecated getEnum() を使用してください.
     */
    public String[] getEnumList() {
        return getEnum();
    }

    @Override
    public boolean validate(final Object obj) {
        if (obj == null) {
            return true;
        }
        if (!(obj instanceof String)) {
            return false;
        }
        String param = (String) obj;

        String[] enumList = getEnum();
        if (enumList != null) {
            for (String enumValue : enumList) {
                if (param.equals(enumValue)) {
                    return true;
                }
            }
            return false;
        }

        switch (getFormat()) {
            case TEXT:
                return validateLength(param);
            case BYTE:
            case BINARY:
                return true; // TODO バイナリのサイズ確認(現状、プラグインにはURL形式で通知される)
            case DATE:
                return true; // TODO RFC3339形式であることの確認
            case DATE_TIME:
                return true; // TODO RFC3339形式であることの確認
            case RGB:
                return RGB_PATTERN.matcher(param).matches();
            default:
                throw new IllegalStateException();
        }
    }

    private boolean validateLength(final String param) {
        if (getMaxLength() != null && param.length() > getMaxLength()) {
            return false;
        }
        if (getMinLength() != null && param.length() < getMinLength()) {
            return false;
        }
        return true;
    }

    /**
     * {@link StringDataSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder extends EnumerableDataSpec.Builder<String, Builder> {

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
         * 文字列の最大長を設定する.
         * @param maxLength 文字列の最大長
         * @return ビルダー自身のインスタンス
         */
        public Builder setMaxLength(final int maxLength) {
            mMaxLength = maxLength;
            return this;
        }

        /**
         * 文字列の最小長を設定する.
         * @param minLength 文字列の最小長
         * @return ビルダー自身のインスタンス
         */
        public Builder setMinLength(final int minLength) {
            mMinLength = minLength;
            return this;
        }

        /**
         * 定数一覧を取得する.
         * @param enumList 定数の配列
         * @return ビルダー自身のインスタンス
         * @deprecated setEnum() を使用してください.
         */
        public Builder setEnumList(final String[] enumList) {
            mEnumList = enumList;
            return this;
        }

        /**
         * {@link StringDataSpec}のインスタンスを生成する.
         * @return {@link StringDataSpec}のインスタンス
         */
        public StringDataSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.TEXT;
            }
            StringDataSpec spec = new StringDataSpec(mFormat);
            if (mEnumList != null) {
                spec.setEnum(mEnumList);
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
