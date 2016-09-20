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
public class BooleanDataSpec extends DConnectDataSpec {

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
    public static class Builder {

        /**
         * {@link BooleanDataSpec}のインスタンスを生成する.
         * @return {@link BooleanDataSpec}のインスタンス
         */
        public BooleanDataSpec build() {
            return new BooleanDataSpec();
        }

    }
}
