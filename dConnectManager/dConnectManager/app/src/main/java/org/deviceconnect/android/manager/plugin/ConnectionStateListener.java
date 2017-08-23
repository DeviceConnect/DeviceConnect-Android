/*
 ConnectionStateListener.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


/**
 * プラグインとの接続状態の変更通知を受信するリスナー.
 *
 * @author NTT DOCOMO, INC.
 */
public interface ConnectionStateListener {

    /**
     * プラグインとの接続状態の変更通知を受信する.
     *
     * @param pluginId プラグインID
     * @param state 変更後の接続状態
     */
    void onConnectionStateChanged(String pluginId, ConnectionState state);

}
