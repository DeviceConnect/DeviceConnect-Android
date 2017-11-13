/*
 BooleanParameterSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


/**
 * Boolean型リクエストパラメータの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class BooleanParameterSpec extends EnumerableParameterSpec<Boolean, BooleanDataSpec> {

    /**
     * コンストラクタ.
     */
    BooleanParameterSpec() {
        super(new BooleanDataSpec());
    }

    /**
     * {@link BooleanParameterSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder extends EnumerableParameterSpec.Builder<Boolean, Builder> {

        /**
         * {@link BooleanParameterSpec}のインスタンスを生成する.
         * @return {@link BooleanParameterSpec}のインスタンス
         */
        public BooleanParameterSpec build() {
            BooleanParameterSpec spec = new BooleanParameterSpec();
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            spec.setEnum(mEnum);
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
