/*
 WebRTCApplication.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc;

import android.app.Application;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.webrtc.core.Peer;
import org.deviceconnect.android.deviceplugin.webrtc.core.PeerConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Application for WebRTC.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebRTCApplication extends Application {

    /**
     * Tag for debugging.
     */
    private static final String TAG = "WEBRTC";

    /**
     * Map that contains the PeerConfig and Peer.
     */
    private final Map<PeerConfig, Peer> mPeerMap = new HashMap<>();

    /**
     * VideoChatActivity call timestamp.
     */
    private long mCallTimeStamp = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "WebRTCApplication:onCreate");
        }
    }

    @Override
    public void onTerminate() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "WebRTCApplication:onTerminate");
        }
        destroyPeer();
        super.onTerminate();
    }

    /**
     * Gets the peer corresponding to the config.
     * @param config Instance of PeerConfig
     * @param callback Callback to notify the Peer
     */
    public void getPeer(final PeerConfig config, final OnGetPeerCallback callback) {
        if (config == null) {
            throw new NullPointerException("config is null.");
        }

        if (callback == null) {
            throw new NullPointerException("callback is null.");
        }

        final Peer.OnConnectCallback peerCallback = new Peer.OnConnectCallback() {
            @Override
            public void onConnected(final Peer peer) {
                callback.onGetPeer(peer);
            }
            @Override
            public void onError() {
                callback.onGetPeer(null);
            }
        };

        synchronized (mPeerMap) {
            Peer peer = mPeerMap.get(config);
            if (peer != null) {
                if (peer.isConnected()) {
                    callback.onGetPeer(peer);
                } else {
                    peer.connect(peerCallback);
                }
            } else {
                try {
                    peer = new Peer(getApplicationContext(), config);
                    peer.connect(peerCallback);
                    mPeerMap.put(config, peer);
                } catch (Exception e) {
                    callback.onGetPeer(null);
                }
            }
        }
    }

    /**
     * Destroy the all Peer.
     */
    public void destroyPeer() {
        synchronized (mPeerMap) {
            for (Map.Entry<PeerConfig, Peer> entry : mPeerMap.entrySet()) {
                entry.getValue().destroy();
                entry.getValue().removePeerEventListener();
            }
            mPeerMap.clear();
        }
    }

    /**
     * Checks whether device has a video chat.
     * @return true if device has a video chat, false otherwise
     */
    public boolean isConnected() {
        synchronized (mPeerMap) {
            for (Map.Entry<PeerConfig, Peer> entry : mPeerMap.entrySet()) {
                if (entry.getValue().hasConnections()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Destroy the Peer corresponding to the config.
     * @param config config
     */
    public void destroyPeer(final PeerConfig config) {
        synchronized (mPeerMap) {
            Peer peer = mPeerMap.remove(config);
            if (peer != null) {
                peer.destroy();
            }
        }
    }

    /**
     * Gets the list of PeerConfig.
     * @return list
     */
    public List<PeerConfig> getPeerConfig() {
        synchronized (mPeerMap) {
            List<PeerConfig> list = new ArrayList<>();

            // shadow copy
            Set<PeerConfig> configs = mPeerMap.keySet();
            for (PeerConfig config : configs) {
                list.add(config);
            }
            return list;
        }
    }

    /**
     * Gets the peer corresponding to the config.
     * @param config PeerConfig
     * @return instance of Peer
     */
    public Peer getPeer(final PeerConfig config) {
        synchronized (mPeerMap) {
            return mPeerMap.get(config);
        }
    }

    /**
     * This interface is used to implement {@link WebRTCApplication} callbacks.
     */
    public interface OnGetPeerCallback {
        /**
         * Gets the peer.
         * @param peer instance of peer
         */
        void onGetPeer(Peer peer);
    }

    /**
     * Set callTimeStamp.
     * @param timeStamp timestamp.
     */
    public void setCallTimeStamp(final long timeStamp) {
        mCallTimeStamp = timeStamp;
    }

    /**
     * Get callTimeStamp;
     * @return callTimeStamp.
     */
    public long getCallTimeStamp() {
        return mCallTimeStamp;
    }
}
