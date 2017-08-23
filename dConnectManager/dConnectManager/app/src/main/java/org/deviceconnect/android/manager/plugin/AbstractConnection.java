/*
 AbstractConnection.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

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

    /** コンテキスト. */
    protected final Context mContext;

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
     * @param context コンテキスト
     * @param pluginId 接続先のプラグインID
     */
    protected AbstractConnection(final Context context, final String pluginId) {
        if (context == null) {
            throw new IllegalArgumentException("context is null.");
        }
        if (pluginId == null) {
            throw new IllegalArgumentException("pluginId is null.");
        }
        mContext = context;
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
                            sendLocalBroadcast(state);
                            l.onConnectionStateChanged(mPluginId, state);
                        }
                    });
                }
            }
        }
    }

    private void sendLocalBroadcast(final ConnectionState state) {
        Intent notification = new Intent(ACTION_CONNECTION_STATE_CHANGED);
        notification.putExtra(EXTRA_PLUGIN_ID, mPluginId);
        notification.putExtra(EXTRA_CONNECTION_STATE, state);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(notification);
    }
}
