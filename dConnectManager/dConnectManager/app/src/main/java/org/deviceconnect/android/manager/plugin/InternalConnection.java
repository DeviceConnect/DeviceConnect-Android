package org.deviceconnect.android.manager.plugin;


import android.content.ComponentName;
import android.content.Context;

import org.deviceconnect.android.IDConnectCallback;

/**
 * マネージャに同梱されたプラグインとの接続.
 *
 * @author NTT DOCOMO, INC.
 */
public class InternalConnection extends BinderConnection {

    public InternalConnection(final Context context,
                              final String pluginId,
                              final ComponentName target,
                              final IDConnectCallback callback) {
        super(context, pluginId, target, callback);
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.INTERNAL;
    }
}
