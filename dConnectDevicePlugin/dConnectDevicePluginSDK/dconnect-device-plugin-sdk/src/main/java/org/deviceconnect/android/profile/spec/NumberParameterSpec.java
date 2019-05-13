/*
 NumberParameterSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


/**
 * Number型リクエストパラメータの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class NumberParameterSpec extends EnumerableParameterSpec<Double, NumberDataSpec> {

    /**
     * コンストラクタ.
     *
     * @param format データのフォーマット指定
     */
    NumberParameterSpec(final DataFormat format) {
        super(new NumberDataSpec(format));
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
    public Double getMaximum() {
        return mDataSpec.getMaximum();
    }

    /**
     * 最大値を設定する.
     * @param maximum 最大値
     */
    void setMaximum(final Double maximum) {
        mDataSpec.setMaximum(maximum);
    }

    /**
     * 最小値を取得する.
     * @return 最小値
     */
    public Double getMinimum() {
        return mDataSpec.getMinimum();
    }

    /**
     * 最小値を設定する.
     * @param minimum 最小値
     */
    void setMinimum(final Double minimum) {
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
     * {@link NumberParameterSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder extends EnumerableParameterSpec.Builder<Double, Builder> {

        private DataFormat mFormat;
        private Double mMaximum;
        private Double mMinimum;
        private Boolean mExclusiveMaximum;
        private Boolean mExclusiveMinimum;

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
        public Builder setMaximum(final Double maximum) {
            mMaximum = maximum;
            return this;
        }

        /**
         * 最小値を設定する.
         * @param minimum 最小値
         * @return ビルダー自身のインスタンス
         */
        public Builder setMinimum(final Double minimum) {
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
         * {@link NumberParameterSpec}のインスタンスを生成する.
         * @return {@link NumberParameterSpec}のインスタンス
         */
        public NumberParameterSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.FLOAT;
            }
            NumberParameterSpec spec = new NumberParameterSpec(mFormat);
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            if (mEnum != null) {
                spec.setEnum(mEnum);
            } else {
                spec.setMaximum(mMaximum);
                spec.setExclusiveMaximum(mExclusiveMaximum != null ? mExclusiveMaximum : false);
                spec.setMinimum(mMinimum);
                spec.setExclusiveMinimum(mExclusiveMinimum != null ? mExclusiveMinimum : false);
            }
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }

}
