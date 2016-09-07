package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.util.Log;

import org.deviceconnect.message.DConnectMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;

public class AWSIotWebSocketClient extends WebSocketClient {

    private static final boolean DEBUG = true;
    private static final String TAG = "";

    private String mSessionKey;

    public AWSIotWebSocketClient(final String serverURI, final String sessionKey) {
        this(URI.create(serverURI), sessionKey);
    }

    public AWSIotWebSocketClient(final URI serverURI, final String sessionKey) {
        super(serverURI);
        mSessionKey = sessionKey;
    }

    @Override
    public void onOpen(final ServerHandshake handshakedata) {
        try {
            send("{\"" + DConnectMessage.EXTRA_SESSION_KEY + "\":\"" + mSessionKey + "\"}");
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
