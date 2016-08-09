/*
 FileDataSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;

/**
 * File型データの仕様.
 *
 * @author NTT DOCOMO, INC.
 */
public class FileDataSpec extends DConnectDataSpec {

    /**
     * コンストラクタ.
     */
    FileDataSpec() {
        super(DataType.FILE);
    }

    @Override
    public boolean validate(final Object param) {
        return true;
    }

    /**
     * {@link FileDataSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder {

        /**
         * {@link FileDataSpec}のインスタンスを生成する.
         * @return {@link FileDataSpec}のインスタンス
         */
        public FileDataSpec build() {
            return new FileDataSpec();
        }

    }

}
