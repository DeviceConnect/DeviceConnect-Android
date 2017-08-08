/*
 ConnectionState.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


/**
 * プラグインとの接続状態.
 *
 * @author NTT DOCOMO, INC.
 */
public enum ConnectionState {

    /**
     * 接続処理中.
     */
    CONNECTING,

    /**
     * 接続確立済み.
     */
    CONNECTED,

    /**
     * 接続切断済み.
     */
    DISCONNECTED,

    /**
     * 連携停止中.
     *
     * 接続確立失敗により、接続処理が中断されている状態.
     */
    SUSPENDED

}
