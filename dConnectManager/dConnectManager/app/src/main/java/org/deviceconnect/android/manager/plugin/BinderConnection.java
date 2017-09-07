/*
 BinderConnection.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import org.deviceconnect.android.IDConnectCallback;
import org.deviceconnect.android.IDConnectPlugin;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * バインダーによる接続.
 *
 * @author NTT DOCOMO, INC.
 */
public class BinderConnection extends AbstractConnection {

    private final ComponentName mPluginName;

    private final IDConnectCallback mCallback;

    private ServiceConnection mServiceConnection;

    private IDConnectPlugin mPlugin;

    private Future<ConnectingResult> mRunningTask;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private Logger mLogger = Logger.getLogger("dconnect.manager");

    public BinderConnection(final Context context,
                            final String pluginId,
                            final ComponentName target,
                            final IDConnectCallback callback) {
        super(context, pluginId);
        mPluginName = target;
        mCallback = callback;
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.BINDER;
    }

    @Override
    public synchronized void connect() throws ConnectingException {
        mLogger.info("BinderConnection.connect: " + mPluginName.getPackageName());
        if (!(ConnectionState.DISCONNECTED == getState() || ConnectionState.SUSPENDED == getState())) {
            return;
        }
        setConnectingState();

        ConnectingResult result;
        try {
            mRunningTask = mExecutor.submit(new ConnectingTask());
            result = mRunningTask.get();
            mRunningTask = null;
        } catch (InterruptedException e) {
            setSuspendedState(ConnectionError.CANCELED);
            throw new ConnectingException("Connection procedure was canceled.");
        } catch (ExecutionException e) {
            setSuspendedState(ConnectionError.INTERNAL_ERROR);
            throw new ConnectingException(e);
        }

        if (result.mError == null) {
            mPlugin = result.mPlugin;
            mServiceConnection = result.mServiceConnection;
            setConnectedState();
        } else {
            setSuspendedState(result.mError);
            throw new ConnectingException("Failed to bind with plugin: " + mPluginName);
        }
    }

    @Override
    public void disconnect() {
        synchronized (this) {
            if (ConnectionState.CONNECTED == getState()) {
                mContext.unbindService(mServiceConnection);
                mServiceConnection = null;
                mPlugin = null;
            }
            setDisconnectedState();
        }
    }

    @Override
    public void send(final Intent message) throws MessagingException {
        mLogger.info("BinderConnection.send: sending: target = " + mPluginName.getPackageName());
        synchronized (this) {
            if (ConnectionState.SUSPENDED == getState()) {
                throw new MessagingException(MessagingException.Reason.CONNECTION_SUSPENDED);
            }
            if (ConnectionState.CONNECTED != getState()) {
                throw new MessagingException(MessagingException.Reason.NOT_CONNECTED);
            }
        }
        try {
            mPlugin.sendMessage(message);
            mLogger.info("BinderConnection.send: sent: target = " + mPluginName.getPackageName());
        } catch (RemoteException e) {
            throw new MessagingException(e, MessagingException.Reason.NOT_CONNECTED);
        }
    }

    private class ConnectingTask implements Callable<ConnectingResult> {

        @Override
        public ConnectingResult call() throws Exception {
            mLogger.info("ConnectingTask.call: " + mPluginName);

            final Object lockObj = new Object();
            final ConnectingResult result = new ConnectingResult();

            Intent intent = new Intent();
            intent.setComponent(mPluginName);
            final ServiceConnection serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(final ComponentName componentName, final IBinder binder) {
                    mLogger.info("onServiceConnected: componentName = " + componentName + ", binder = " + binder);
                    try {
                        IDConnectPlugin plugin = IDConnectPlugin.Stub.asInterface(binder);
                        plugin.registerCallback(mCallback);

                        synchronized (lockObj) {
                            result.mIsComplete = true;
                            result.mPlugin = plugin;
                            result.mServiceConnection = this;
                            lockObj.notifyAll();
                        }

                    } catch (RemoteException e) {
                        e.printStackTrace(); // TODO エラーハンドリング
                    }
                }

                @Override
                public void onServiceDisconnected(final ComponentName componentName) {
                    mLogger.info("onServiceDisconnected: componentName = " + componentName);
                    synchronized (BinderConnection.this) {
                        mServiceConnection = null;
                        mPlugin = null;
                        setSuspendedState(ConnectionError.TERMINATED);
                    }
                }
            };
            boolean canBind = mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            if (!canBind) {
                result.mError = ConnectionError.NOT_PERMITTED;
                return result;
            }

            synchronized (lockObj) {
                lockObj.wait(2000);
            }

            if (!result.mIsComplete) {
                result.mError = ConnectionError.NOT_RESPONDED;
            }
            return result;
        }
    }

    private class ConnectingResult {
        boolean mIsComplete;
        IDConnectPlugin mPlugin;
        ServiceConnection mServiceConnection;
        ConnectionError mError;
    }
}
