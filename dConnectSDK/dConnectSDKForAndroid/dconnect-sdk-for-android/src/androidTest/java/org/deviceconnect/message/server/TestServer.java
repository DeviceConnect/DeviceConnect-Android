package org.deviceconnect.message.server;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

public class TestServer extends NanoWSD {

    private ServerCallback mServerCallback;
    private WebSocketCallback mWebSocketCallback;

    public TestServer() {
        super(4035);
    }

    public void setServerCallback(final ServerCallback serverCallback) {
        mServerCallback = serverCallback;
    }

    public void setWebSocketCallback(WebSocketCallback webSocketCallback) {
        mWebSocketCallback = webSocketCallback;
    }

    @Override
    public Response serve(final String uri, final Method method, final Map<String, String> headers,
                          final Map<String, String> parms, final Map<String, String> files) {

        if (mServerCallback != null) {
            return mServerCallback.serve(uri, method, headers, parms, files);
        } else {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
        }
    }

    @Override
    protected WebSocket openWebSocket(final IHTTPSession handshake) {
        return new NanoWebSocket(handshake);
    }

    public interface ServerCallback {
        Response serve(final String uri, final Method method, final Map<String, String> headers,
                              final Map<String, String> parms, final Map<String, String> files);
    }

    public interface WebSocketCallback {
        void onOpen(NanoWSD.WebSocket webSocket);
        void onClose(NanoWSD.WebSocket webSocket, WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote);
        void onMessage(NanoWSD.WebSocket webSocket, WebSocketFrame message);
        void onPong(NanoWSD.WebSocket webSocket, WebSocketFrame pong);
        void onException(NanoWSD.WebSocket webSocket, IOException exception);
    }

    private class NanoWebSocket extends NanoWSD.WebSocket {
        NanoWebSocket(final IHTTPSession handshakeRequest) {
            super(handshakeRequest);
        }

        @Override
        protected void onOpen() {
            if (mWebSocketCallback != null) {
                mWebSocketCallback.onOpen(this);
            }
        }

        @Override
        protected void onClose(WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
            if (mWebSocketCallback != null) {
                mWebSocketCallback.onClose(this, code, reason, initiatedByRemote);
            }
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            if (mWebSocketCallback != null) {
                mWebSocketCallback.onMessage(this, message);
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            if (mWebSocketCallback != null) {
                mWebSocketCallback.onPong(this, pong);
            }
        }

        @Override
        protected void onException(IOException exception) {
            if (mWebSocketCallback != null) {
                mWebSocketCallback.onException(this, exception);
            }
        }
    }
}
