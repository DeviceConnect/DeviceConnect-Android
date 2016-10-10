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
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.HashMap;
import java.util.Map;

public class AWSIotWebSocketClient extends WebSocketClient {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "AWSIoT-Local";

    private String mAccessToken;

    private OnMessageEventListener mOnMessageEventListener;

    private static final Map<String, String> DEFAULT_HEADERS = new HashMap<String, String>() {{
        put("Origin", DConnectHelper.ORIGIN);
    }};

    public AWSIotWebSocketClient(final String accessToken) {
        this("http://localhost:4035/gotapi/websocket", accessToken);
    }

    public AWSIotWebSocketClient(final String serverURI, final String accessToken) {
        this(URI.create(serverURI), accessToken);
    }

    public AWSIotWebSocketClient(final URI serverURI, final String sessionKey) {
        super(serverURI, new Draft_17(), DEFAULT_HEADERS, 0);
        mAccessToken = sessionKey;
    }

    public void setOnMessageEventListener(OnMessageEventListener onMessageEventListener) {
        mOnMessageEventListener = onMessageEventListener;
    }

    @Override
    public void onOpen(final ServerHandshake handshakedata) {
        if (DEBUG) {
            Log.i(TAG, "Open the WebSocket. accessToken=" + mAccessToken);
        }

        try {
            send("{\"" + DConnectMessage.EXTRA_ACCESS_TOKEN + "\":\"" + mAccessToken + "\"}");
        } catch (NotYetConnectedException e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }
    }

    @Override
    public void onMessage(final String message) {
        if (message.contains("result")) {
            try {
                JSONObject json = new JSONObject(message);
                int result = json.getInt("result");
                if (result == DConnectMessage.RESULT_ERROR) {
                    // TODO WebSocketが開けなかった時の処理を検討
                    if (DEBUG) {
                        Log.w(TAG, "Failed to open the WebSocket. message" + message);
                    }
                } else {
                    if (DEBUG) {
                        Log.i(TAG, "Open the WebSocket.");
                    }
                }
            } catch (JSONException e) {
                if (DEBUG) {
                    Log.e(TAG, "Failed to parse message. " + message, e);
                }
            }
        } else {
            if (mOnMessageEventListener != null) {
                mOnMessageEventListener.onMessage(message);
            }
        }
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        if (DEBUG) {
            Log.i(TAG, "AWSIotWebSocketClient#onClose: " + code + " " + reason);
        }

        // TODO 再接続処理
    }

    @Override
    public synchronized void onError(final Exception ex) {
        if (DEBUG) {
            Log.e(TAG, "", ex);
        }
    }

    public interface OnMessageEventListener {
        void onMessage(String message);
    }
}
