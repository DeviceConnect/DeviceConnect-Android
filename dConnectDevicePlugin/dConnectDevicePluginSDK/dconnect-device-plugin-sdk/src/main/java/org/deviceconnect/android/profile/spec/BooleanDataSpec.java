/*
 BooleanDataSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


/**
 * Boolean型データの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class BooleanDataSpec extends EnumerableDataSpec<Boolean> {

    private final String TRUE = "true";
    private final String FALSE = "false";

    /**
     * コンストラクタ.
     */
    BooleanDataSpec() {
        super(DataType.BOOLEAN);
    }

    @Override
    public boolean validate(final Object obj) {
        if (obj == null) {
            return true;
        }

        Boolean[] enumList = getEnum();
        if (enumList != null) {
            for (Boolean enumValue : enumList) {
                if (enumValue != null && enumValue.equals(obj)) {
                    return true;
                }
            }
            return false;
        }

        if (obj instanceof String) {
            String strParam = (String) obj;
            return TRUE.equalsIgnoreCase(strParam) || FALSE.equalsIgnoreCase(strParam);
        } else if (obj instanceof Boolean) {
            return true;
        }
        return false;
    }

    /**
     * {@link BooleanDataSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder extends EnumerableDataSpec.Builder<Boolean, Builder> {

        /**
         * {@link BooleanDataSpec}のインスタンスを生成する.
         * @return {@link BooleanDataSpec}のインスタンス
         */
        public BooleanDataSpec build() {
            BooleanDataSpec spec = new BooleanDataSpec();
            spec.setEnum(mEnumList);
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
