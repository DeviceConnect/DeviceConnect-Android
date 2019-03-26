/*
 DefaultConnectionFactory.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.plugin;

import android.content.Context;

import org.deviceconnect.android.IDConnectCallback;

/**
 * プラグインとコネクションを作成するファクトリークラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DefaultConnectionFactory  implements ConnectionFactory {

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * プラグインからのレスポンスを返すコールバック.
     */
    private IDConnectCallback mCallback;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param callback プラグインからのレスポンスを返すコールバック.
     */
    public DefaultConnectionFactory(final Context context, final IDConnectCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    public Connection createConnectionForPlugin(final DevicePlugin plugin) {
        switch (plugin.getConnectionType()) {
            case INTERNAL:
                return new InternalConnection(mContext, plugin.getPluginId(), plugin.getComponentName(), mCallback);
            case BINDER:
                return new BinderConnection(mContext, plugin.getPluginId(), plugin.getComponentName(), mCallback);
            case BROADCAST:
                return new BroadcastConnection(mContext, plugin.getPluginId());
            case DIRECT:
                return new DirectConnection(mContext, plugin.getPluginId(), plugin.getComponentName(), mCallback);
            default:
                return null;
        }
    }
}
