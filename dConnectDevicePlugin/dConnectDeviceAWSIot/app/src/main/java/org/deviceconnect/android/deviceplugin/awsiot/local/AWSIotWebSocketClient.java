/*
 AWSIotWebSocketClient.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.remote.BuildConfig;
import org.deviceconnect.message.DConnectMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.HashMap;
import java.util.Map;

public class AWSIotWebSocketClient extends WebSocketClient {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "";

    private String mAccessToken;

    private static Map<String, String> DEFAULT_HEADERS = new HashMap<String, String>() {{
        put("Origin", "http://org.deviceconnect.android.deviceplugin.awsiot");
    }};


    public AWSIotWebSocketClient(final String serverURI, final String accessToken) {
        this(URI.create(serverURI), accessToken);
    }

    public AWSIotWebSocketClient(final URI serverURI, final String sessionKey) {
        super(serverURI, new Draft_17(), DEFAULT_HEADERS, 0);
        mAccessToken = sessionKey;
    }

    @Override
    public void onOpen(final ServerHandshake handshakedata) {
        try {
            send("{\"" + DConnectMessage.EXTRA_ACCESS_TOKEN + "\":\"" + mAccessToken + "\"}");
        } catch (NotYetConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(final String message) {
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        if (DEBUG) {
            Log.i(TAG, "AWSIotWebSocketClient#onClose");
        }

        // TODO 再接続処理
    }

    @Override
    public synchronized void onError(final Exception ex) {
        if (DEBUG) {
            Log.e(TAG, "", ex);
        }
    }
}
