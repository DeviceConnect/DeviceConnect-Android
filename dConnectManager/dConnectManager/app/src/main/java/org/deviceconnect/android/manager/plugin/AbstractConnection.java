package org.deviceconnect.android.manager.plugin;


import android.content.Intent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract class AbstractConnection implements Connection {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private ConnectionListener mConnectionListener;

    private ConnectionState mState = ConnectionState.DISCONNECTED;

    @Override
    public ConnectionState getState() {
        return mState;
    }

    protected void setState(final ConnectionState state) {
        mState = state;
    }

    @Override
    public void setConnectionListener(final ConnectionListener listener) {
        mConnectionListener = listener;
    }

    void notifyOnConnected() {
        final ConnectionListener l = mConnectionListener;
        if (l != null) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    l.onConnected();
                }
            });
        }
    }

    void notifyOnDisconnected() {
        final ConnectionListener l = mConnectionListener;
        if (l != null) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    l.onDisconnected();
                }
            });
        }
    }
}
