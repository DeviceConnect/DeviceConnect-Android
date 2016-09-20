/*
 DConnectProfileSpecJsonParserFactory.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.parser;


/**
 * DConnectProfileSpecJsonParserのインスタンスを生成するファクトリークラス.
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectProfileSpecJsonParserFactory {

    abstract public DConnectProfileSpecJsonParser createParser();

    /**
     * デフォルトのDConnectProfileSpecJsonParserFactoryを取得する.
     * @return DConnectProfileSpecJsonParserFactoryのインスタンス
     */
    public static DConnectProfileSpecJsonParserFactory getDefaultFactory() {
        return new SwaggerJsonParserFactory();
    }

}
