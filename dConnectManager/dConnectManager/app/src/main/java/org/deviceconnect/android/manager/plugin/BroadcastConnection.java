package org.deviceconnect.android.manager.plugin;

import android.content.Context;
import android.content.Intent;


public class BroadcastConnection extends AbstractConnection {

    private final Context mContext;

    public BroadcastConnection(final Context context, final String pluginId) {
        super(pluginId);
        mContext = context;
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
