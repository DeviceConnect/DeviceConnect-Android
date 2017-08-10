/*
 ConnectionType.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


/**
 * プラグイン連携タイプ.
 *
 * @author NTT DOCOMO, INC.
 */
public enum ConnectionType {

    /**
     * Binderによる連携.
     */
    BINDER,

    /**
     * Broadcast Intentによる連携.
     */
    BROADCAST,

    /**
     * オンメモリ上でのオブジェクトによる内部的な連携.
     *
     * マネージャのパッケージにプラグインを同梱する必要がある.
     */
    INTERNAL

}
