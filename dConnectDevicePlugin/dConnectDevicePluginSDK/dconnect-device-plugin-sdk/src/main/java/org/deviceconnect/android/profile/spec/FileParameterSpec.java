/*
 FileParameterSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


/**
 * File型リクエストパラメータの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class FileParameterSpec extends DConnectParameterSpec<FileDataSpec> {

    /**
     * コンストラクタ.
     */
    FileParameterSpec() {
        super(new FileDataSpec());
    }

    /**
     * {@link FileParameterSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder extends BaseBuilder<Builder> {

        /**
         * {@link FileParameterSpec}のインスタンスを生成する.
         * @return {@link FileParameterSpec}のインスタンス
         */
        public FileParameterSpec build() {
            FileParameterSpec spec = new FileParameterSpec();
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
