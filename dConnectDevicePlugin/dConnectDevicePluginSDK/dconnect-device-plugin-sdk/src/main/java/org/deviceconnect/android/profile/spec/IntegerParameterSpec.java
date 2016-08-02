/*
 IntegerParameterSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


/**
 * Integer型リクエストパラメータの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class IntegerParameterSpec extends DConnectParameterSpec<IntegerDataSpec> {

    /**
     * コンストラクタ.
     * @param format データのフォーマット指定
     */
    IntegerParameterSpec(final DataFormat format) {
        super(new IntegerDataSpec(format));
    }

    /**
     * データのフォーマット指定を取得する.
     * @return データのフォーマット指定
     */
    public DataFormat getFormat() {
        return mDataSpec.getFormat();
    }

    /**
     * 最大値を取得する.
     * @return 最大値
     */
    public Long getMaximum() {
        return mDataSpec.getMaximum();
    }

    /**
     * 最大値を設定する.
     * @param maximum 最大値
     */
    void setMaximum(final Long maximum) {
        mDataSpec.setMaximum(maximum);
    }

    /**
     * 最小値を取得する.
     * @return 最小値
     */
    public Long getMinimum() {
        return mDataSpec.getMinimum();
    }

    /**
     * 最小値を設定する.
     * @param minimum 最小値
     */
    void setMinimum(final Long minimum) {
        mDataSpec.setMinimum(minimum);
    }

    /**
     * 最大値自体を指定可能かどうかのフラグを取得する.
     * @return 指定できない場合は<code>true</code>. それ以外の場合は<code>false</code>
     */
    public boolean isExclusiveMaximum() {
        return mDataSpec.isExclusiveMaximum();
    }

    /**
     * 最大値自体を指定可能かどうかのフラグを設定する.
     * @param exclusiveMaximum 指定できない場合は<code>true</code>. それ以外の場合は<code>false</code>
     */
    void setExclusiveMaximum(final boolean exclusiveMaximum) {
        mDataSpec.setExclusiveMaximum(exclusiveMaximum);
    }

    /**
     * 最小値自体を指定可能かどうかのフラグを取得する
     * @return 指定できない場合は<code>true</code>. それ以外の場合は<code>false</code>
     */
    public boolean isExclusiveMinimum() {
        return mDataSpec.isExclusiveMinimum();
    }

    /**
     * 最小値自体を指定可能かどうかのフラグを設定する.
     * @param exclusiveMinimum 指定できない場合は<code>true</code>. それ以外の場合は<code>false</code>
     */
    void setExclusiveMinimum(final boolean exclusiveMinimum) {
        mDataSpec.setExclusiveMinimum(exclusiveMinimum);
    }

    /**
     * 定数一覧を取得する.
     * @return 定数の配列
     */
    public long[] getEnumList() {
        return mDataSpec.getEnumList();
    }

    /**
     * 定数一覧を設定する.
     * @param enumList 定数の配列
     */
    void setEnumList(final long[] enumList) {
        mDataSpec.setEnumList(enumList);
    }

    /**
     * {@link IntegerParameterSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder extends BaseBuilder<Builder> {

        private DataFormat mFormat;
        private Long mMaximum;
        private Long mMinimum;
        private Boolean mExclusiveMaximum;
        private Boolean mExclusiveMinimum;
        private long[] mEnumList;

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
         * 最大値を設定する.
         * @param maximum 最大値
         * @return ビルダー自身のインスタンス
         */
        public Builder setMaximum(final Long maximum) {
            mMaximum = maximum;
            return this;
        }

        /**
         * 最小値を設定する.
         * @param minimum 最小値
         * @return ビルダー自身のインスタンス
         */
        public Builder setMinimum(final Long minimum) {
            mMinimum = minimum;
            return this;
        }

        /**
         * 最大値自体を指定可能かどうかのフラグを設定する.
         * @param exclusiveMaximum 指定できない場合は<code>true</code>. それ以外の場合は<code>false</code>
         * @return ビルダー自身のインスタンス
         */
        public Builder setExclusiveMaximum(final Boolean exclusiveMaximum) {
            mExclusiveMaximum = exclusiveMaximum;
            return this;
        }

        /**
         * 最小値自体を指定可能かどうかのフラグを設定する.
         * @param exclusiveMinimum 指定できない場合は<code>true</code>. それ以外の場合は<code>false</code>
         * @return ビルダー自身のインスタンス
         */
        public Builder setExclusiveMinimum(final Boolean exclusiveMinimum) {
            mExclusiveMinimum = exclusiveMinimum;
            return this;
        }

        /**
         * 定数一覧を取得する.
         * @param enumList 定数の配列
         * @return ビルダー自身のインスタンス
         */
        public Builder setEnumList(final long[] enumList) {
            mEnumList = enumList;
            return this;
        }

        /**
         * {@link IntegerParameterSpec}のインスタンスを生成する.
         * @return {@link IntegerParameterSpec}のインスタンス
         */
        public IntegerParameterSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.INT32;
            }
            IntegerParameterSpec spec = new IntegerParameterSpec(mFormat);
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            spec.setEnumList(mEnumList);
            spec.setMaximum(mMaximum);
            spec.setExclusiveMaximum(mExclusiveMaximum);
            spec.setMinimum(mMinimum);
            spec.setExclusiveMinimum(mExclusiveMinimum);
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }

}
