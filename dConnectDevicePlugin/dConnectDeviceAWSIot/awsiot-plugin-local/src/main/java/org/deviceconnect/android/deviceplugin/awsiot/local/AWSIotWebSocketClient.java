package org.deviceconnect.android.deviceplugin.awsiot.local;

import org.deviceconnect.message.DConnectMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;

public class AWSIotWebSocketClient extends WebSocketClient {

    private String mSessionKey;

    public AWSIotWebSocketClient(final String serverURI, final String sessionKey) {
        this(URI.create(serverURI), sessionKey);
    }

    public AWSIotWebSocketClient(final URI serverURI, final String sessionKey) {
        super(serverURI);
        mSessionKey = sessionKey;
    }

    public String getSessionKey() {
        return mSessionKey;
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
    }

    @Override
    public synchronized void onError(final Exception ex) {
    }
}
