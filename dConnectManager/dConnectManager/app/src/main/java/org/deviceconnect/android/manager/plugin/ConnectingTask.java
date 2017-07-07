package org.deviceconnect.android.manager.plugin;


import java.util.concurrent.Callable;

class ConnectingTask implements Callable<Void> {

    private final Object mLock = new Object();

    private final Connection mConnection;

    ConnectingTask(final Connection connection) {
        mConnection = connection;
    }

    @Override
    public Void call() throws Exception {

        return null;
    }

}
