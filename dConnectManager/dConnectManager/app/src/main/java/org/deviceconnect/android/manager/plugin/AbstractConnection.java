package org.deviceconnect.android.manager.plugin;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract class AbstractConnection implements Connection {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final List<ConnectionStateListener> mConnectionStateListeners = new ArrayList<>();

    private ConnectionState mState = ConnectionState.DISCONNECTED;

    @Override
    public ConnectionState getState() {
        return mState;
    }

    void setState(final ConnectionState state) {
        mState = state;
        notifyStateChange(state);
    }

    @Override
    public void addConnectionStateListener(final ConnectionStateListener listener) {
        synchronized (mConnectionStateListeners) {
            for (ConnectionStateListener cache : mConnectionStateListeners) {
                if (cache == listener) {
                    return;
                }
            }
            mConnectionStateListeners.add(listener);
        }
    }

    @Override
    public void removeConnectionStateListener(ConnectionStateListener listener) {
        synchronized (mConnectionStateListeners) {
            for (Iterator<ConnectionStateListener> it = mConnectionStateListeners.iterator(); it.hasNext(); ) {
                ConnectionStateListener cache = it.next();
                if (cache == listener) {
                    it.remove();
                    return;
                }
            }
        }
    }

    private void notifyStateChange(final ConnectionState state) {
        synchronized (mConnectionStateListeners) {
            if (mConnectionStateListeners.size() > 0) {
                for (final ConnectionStateListener l : mConnectionStateListeners) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            l.onConnectionStateChanged(state);
                        }
                    });
                }
            }
        }
    }
}
