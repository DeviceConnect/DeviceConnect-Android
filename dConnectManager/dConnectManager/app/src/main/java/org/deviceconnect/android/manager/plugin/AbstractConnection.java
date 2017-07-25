/*
 AbstractConnection.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * プラグインとの通信機能を実装する基底クラス.
 *
 * @author NTT DOCOMO, INC.
 */
abstract class AbstractConnection implements Connection {

    /** 接続先のプラグインID. */
    private final String mPluginId;

    /** 接続状態変更を通知するスレッド. */
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /** 接続状態変更リスナーのリスト. */
    private final List<ConnectionStateListener> mConnectionStateListeners = new ArrayList<>();

    /** 接続状態. */
    private ConnectionState mState = ConnectionState.DISCONNECTED;

    /**
     * コンストラクタ.
     * @param pluginId 接続先のプラグインID
     */
    protected AbstractConnection(final String pluginId) {
        if (pluginId == null) {
            throw new IllegalArgumentException();
        }
        mPluginId = pluginId;
    }

    @Override
    public String getPluginId() {
        return mPluginId;
    }

    @Override
    public ConnectionState getState() {
        return mState;
    }

    /**
     * 接続状態を設定する.
     * @param state 遷移先の接続状態
     */
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

    /**
     * 接続状態変更を通知する.
     * @param state 遷移先の接続状態
     */
    private void notifyStateChange(final ConnectionState state) {
        synchronized (mConnectionStateListeners) {
            if (mConnectionStateListeners.size() > 0) {
                for (final ConnectionStateListener l : mConnectionStateListeners) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            l.onConnectionStateChanged(mPluginId, state);
                        }
                    });
                }
            }
        }
    }
}
