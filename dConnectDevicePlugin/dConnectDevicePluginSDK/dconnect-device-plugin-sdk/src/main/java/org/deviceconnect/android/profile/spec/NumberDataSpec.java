/*
 NumberDataSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


/**
 * Number型データの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class NumberDataSpec extends EnumerableDataSpec<Double> {

    private final DataFormat mFormat;
    private Double mMaximum;
    private Double mMinimum;
    private Boolean mExclusiveMaximum;
    private Boolean mExclusiveMinimum;

    /**
     * コンストラクタ.
     *
     * @param format データのフォーマット指定
     */
    NumberDataSpec(final DataFormat format) {
        super(DataType.NUMBER);
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
     * 最大値を取得する.
     * @return 最大値
     */
    public Double getMaximum() {
        return mMaximum;
    }

    /**
     * 最大値を設定する.
     * @param maximum 最大値
     */
    void setMaximum(final Double maximum) {
        mMaximum = maximum;
    }

    /**
     * 最小値を取得する.
     * @return 最小値
     */
    public Double getMinimum() {
        return mMinimum;
    }

    /**
     * 最小値を設定する.
     * @param minimum 最小値
     */
    void setMinimum(final Double minimum) {
        mMinimum = minimum;
    }

    /**
     * 最大値自体を指定可能かどうかのフラグを取得する.
     * @return 指定できない場合は<code>true</code>. それ以外の場合は<code>false</code>
     */
    public boolean isExclusiveMaximum() {
        return mExclusiveMaximum != null ? mExclusiveMaximum : false;
    }

    /**
     * 最大値自体を指定可能かどうかのフラグを設定する.
     * @param exclusiveMaximum 指定できない場合は<code>true</code>. それ以外の場合は<code>false</code>
     */
    public void setExclusiveMaximum(final Boolean exclusiveMaximum) {
        mExclusiveMaximum = exclusiveMaximum;
    }

    /**
     * 最小値自体を指定可能かどうかのフラグを取得する
     * @return 指定できない場合は<code>true</code>. それ以外の場合は<code>false</code>
     */
    public boolean isExclusiveMinimum() {
        return mExclusiveMinimum != null ? mExclusiveMinimum : false;
    }

    /**
     * 最小値自体を指定可能かどうかのフラグを設定する.
     * @param exclusiveMinimum 指定できない場合は<code>true</code>. それ以外の場合は<code>false</code>
     */
    public void setExclusiveMinimum(final Boolean exclusiveMinimum) {
        mExclusiveMinimum = exclusiveMinimum;
    }

    @Override
    public boolean validate(final Object obj) {
        if (obj == null) {
            return true;
        }
        switch (getFormat()) {
            case FLOAT:
                return validateFloat(obj);
            case DOUBLE:
                return validateDouble(obj);
            default:
                throw new IllegalStateException();
        }
    }

    private boolean validateFloat(final Object param) {
        if (param instanceof String) {
            try {
                return validateRange(Float.parseFloat((String) param));
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (param instanceof Float) {
            return validateRange((float) param);
        } else if (param instanceof Double) {
            return validateRange(((Double) param).doubleValue());
        } else {
            return false;
        }
    }

    private boolean validateDouble(final Object param) {
        if (param instanceof String) {
            try {
                return validateRange(Double.parseDouble((String) param));
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (param instanceof Double) {
            return validateRange((double) param);
        } else if (param instanceof Float) {
            return validateRange(((Float) param).doubleValue());
        } else {
            return false;
        }
    }

    private boolean validateRange(final double value) {
        if (getEnum() != null) {
            for (Double e : getEnum()) {
                if (e != null && e == value) {
                    return true;
                }
            }
            return false;
        } else {
            boolean isValid = true;
            if (getMaximum() != null) {
                isValid &= isExclusiveMaximum() ? (getMaximum() > value) : (getMaximum() >= value);
            }
            if (getMinimum() != null) {
                isValid &= isExclusiveMinimum() ? (getMinimum() < value) : (getMinimum() <= value);
            }
            return isValid;
        }
    }

    /**
     * {@link NumberDataSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder extends EnumerableDataSpec.Builder<Double, Builder> {

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
         * {@link NumberDataSpec}のインスタンスを生成する.
         * @return {@link NumberDataSpec}のインスタンス
         */
        public NumberDataSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.FLOAT;
            }
            NumberDataSpec spec = new NumberDataSpec(mFormat);
            if (mEnumList != null) {
                spec.setEnum(mEnumList);
            } else {
                spec.setMaximum(mMaximum);
                spec.setExclusiveMaximum(mExclusiveMaximum);
                spec.setMinimum(mMinimum);
                spec.setExclusiveMinimum(mExclusiveMinimum);
            }
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }

}
