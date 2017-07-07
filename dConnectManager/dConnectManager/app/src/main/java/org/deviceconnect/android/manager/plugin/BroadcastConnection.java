package org.deviceconnect.android.manager.plugin;

import android.content.Context;
import android.content.Intent;


class BroadcastConnection extends AbstractConnection {

    private final Context mContext;

    BroadcastConnection(final Context context) {
        mContext = context;
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.BROADCAST;
    }

    @Override
    public void connect() throws ConnectingException {
        notifyOnConnected();
    }

    @Override
    public void disconnect() {
        notifyOnDisconnected();
    }

    @Override
    public void send(final Intent message) throws MessagingException {
        mContext.sendBroadcast(message);
    }
}
