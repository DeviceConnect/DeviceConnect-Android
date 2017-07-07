package org.deviceconnect.android.manager.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import org.deviceconnect.android.IDConnectPlugin;

class BinderConnection extends AbstractConnection {

    private final Context mContext;

    private final ComponentName mPluginName;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder binder) {
            mPlugin = IDConnectPlugin.Stub.asInterface(binder);
            notifyOnConnected();
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            notifyOnDisconnected();
        }
    };

    private IDConnectPlugin mPlugin;

    BinderConnection(final Context context, final ComponentName target) {
        mContext = context;
        mPluginName = target;
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.BINDER;
    }

    @Override
    public void connect() throws ConnectingException {
        Intent intent = new Intent();
        intent.setComponent(mPluginName);

        try {
            boolean result = mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            if (!result) {
                throw new ConnectingException("Failed to bind to the plugin: " + mPluginName);
            }
        } catch (SecurityException e) {
            throw new ConnectingException(e);
        }
    }

    @Override
    public void disconnect() {
        mContext.unbindService(mServiceConnection);
    }

    @Override
    public void send(final Intent message) throws MessagingException {
        try {
            mPlugin.sendRequest(message);
        } catch (RemoteException e) {
            throw new MessagingException(e);
        }
    }
}
