/*
 IntegerDataSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


import java.util.ArrayList;
import java.util.List;

/**
 * Integer型データの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class IntegerDataSpec extends EnumerableDataSpec<Long> {

    private final DataFormat mFormat;
    private Long mMaximum;
    private Long mMinimum;
    private Boolean mExclusiveMaximum;
    private Boolean mExclusiveMinimum;

    /**
     * コンストラクタ.
     *
     * @param format データのフォーマット指定
     */
    IntegerDataSpec(final DataFormat format) {
        super(DataType.INTEGER);
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
    public Long getMaximum() {
        return mMaximum;
    }

    /**
     * 最大値を設定する.
     * @param maximum 最大値
     */
    void setMaximum(final Long maximum) {
        mMaximum = maximum;
    }

    /**
     * 最小値を取得する.
     * @return 最小値
     */
    public Long getMinimum() {
        return mMinimum;
    }

    /**
     * 最小値を設定する.
     * @param minimum 最小値
     */
    void setMinimum(final Long minimum) {
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
    void setExclusiveMaximum(final Boolean exclusiveMaximum) {
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
    void setExclusiveMinimum(final Boolean exclusiveMinimum) {
        mExclusiveMinimum = exclusiveMinimum;
    }

    /**
     * 定数一覧を取得する.
     * @return 定数の配列
     * @deprecated getEnum() を使用してください.
     */
    public long[] getEnumList() {
        Long[] cache = getEnum();
        List<Long> nonNullList = new ArrayList<>();
        for (Long value : cache) {
            if (value != null) {
                nonNullList.add(value);
            }
        }
        long[] result = new long[nonNullList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = nonNullList.get(i);
        }
        return result;
    }

    @Override
    public boolean validate(final Object obj) {
        if (obj == null) {
            return true;
        }
        switch (getFormat()) {
            case INT32:
                return validateInt32(obj);
            case INT64:
                return validateInt64(obj);
            default:
                throw new IllegalStateException();
        }
    }

    private boolean validateInt32(final Object param) {
        if (param instanceof String) {
            try {
                return validateRange(Integer.parseInt((String) param));
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (param instanceof Integer) {
            return validateRange((Integer) param);
        } else if (param instanceof Long) {
            return validateRange((Long) param);
        } else {
            return false;
        }
    }

    private boolean validateInt64(final Object param) {
        if (param instanceof String) {
            try {
                return validateRange(Long.parseLong((String) param));
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (param instanceof Long) {
            return validateRange((Long) param);
        } else if (param instanceof Integer) {
            return validateRange(((Integer) param).longValue());
        } else {
            return false;
        }
    }

    private boolean validateRange(final long value) {
        if (getEnum() != null) {
            for (Long e : getEnum()) {
                if (e != null && e == value) {
                    return true;
                }
            }
            return false;
        } else {
            boolean isValid = true;
            if (getMaximum() != null) {
                isValid &=  isExclusiveMaximum() ? (getMaximum() > value) : (getMaximum() >= value);
            }
            if (getMinimum() != null) {
                isValid &= isExclusiveMinimum() ? (getMinimum() < value) : (getMinimum() <= value);
            }
            return isValid;
        }
    }

    /**
     * {@link IntegerDataSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder extends EnumerableDataSpec.Builder<Long, Builder> {

        private DataFormat mFormat;
        private Long mMaximum;
        private Long mMinimum;
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
         * @deprecated setEnum(Long[]) を使用してください.
         */
        public Builder setEnumList(final long[] enumList) {
            Long[] result = null;
            if (enumList != null) {
                result = new Long[enumList.length];
                for (int i = 0; i < enumList.length; i++) {
                    result[i] = enumList[i];
                }
            }
            mEnumList = result;
            return this;
        }

        /**
         * {@link IntegerDataSpec}のインスタンスを生成する.
         * @return {@link IntegerDataSpec}のインスタンス
         */
        public IntegerDataSpec build() {
            if (mFormat == null) {
                mFormat = DataFormat.INT32;
            }
            IntegerDataSpec spec = new IntegerDataSpec(mFormat);
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
