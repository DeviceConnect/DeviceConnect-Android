/*
 BroadcastConnection.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;

import android.content.Context;
import android.content.Intent;


/**
 * ブロードキャストによる接続.
 *
 * @author NTT DOCOMO, INC.
 */
public class BroadcastConnection extends AbstractConnection {

    public BroadcastConnection(final Context context, final String pluginId) {
        super(context, pluginId);
        setState(ConnectionState.CONNECTED);
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.BROADCAST;
    }

    @Override
    public void connect() throws ConnectingException {
        // NOP.
    }

    @Override
    public void disconnect() {
        // NOP.
    }

    @Override
    public void send(final Intent message) throws MessagingException {
        mContext.sendBroadcast(message);
    }
}
