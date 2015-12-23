/*
 SignalingClient.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.WritableCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.WebSocket;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * シグナリングサーバーと通信を行うクライアント.
 *
 * @author NTT DOCOMO, INC.
 */
public final class SignalingClient {
    /**
     * Tag for debugging.
     */
    private static final String TAG = "WEBRTC";

    /**
     * Defined the HTTP timeout.
     */
    private static final int TIMEOUT_HTTP_CONNECTION = 25000;

    /**
     * Defined the WebSocket timeout.
     */
    private static final int TIMEOUT_WEBSOCKET = 30000;

    /**
     * トークン.
     */
    private final String mStrToken = PeerUtil.randomToken(16);

    /**
     * Peeのコンフィグ.
     */
    private PeerConfig mConfig;

    /**
     * Peer Id.
     */
    private String mId;

    /**
     * Credential.
     */
    private String mCredential;

    /**
     * Disconnect flag.
     */
    private boolean mDisconnectFlag;

    /**
     * WebSocket connected to peer server.
     */
    private WebSocket mWebSocket;

    /**
     * Handler.
     */
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * Server options.
     */
    private SignalingOption mOption;

    /**
     * Queue that stores messages that send to the server.
     */
    private List<String> mQueueMessage = new ArrayList<>();

    /**
     * Instance of Runnable that will send message to the server.
     */
    private Runnable mSendingRun;

    /**
     * Callbacks of SignalingClient.
     */
    private OnSignalingCallback mSignalingCallback;

    /**
     * Constructor.
     * @param config config of the peer server
     */
    public SignalingClient(final PeerConfig config) {
        mConfig = config;
        mOption = new SignalingOption();
        mDisconnectFlag = true;
        retrieveId(null);
    }

    /**
     * Disconnect from peer server.
     */
    public void disconnect() {
        if (!mDisconnectFlag) {
            mDisconnectFlag = true;
            stopWebSocket();

            if (mSignalingCallback != null) {
                mSignalingCallback.onDisconnect();
            }
        }
    }

    /**
     * Destroys this instance.
     */
    public void destroy() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@ SignalingClient::destroy");
        }
        disconnect();
    }

    /**
     * Returns true if disconnected to the server.
     * @return true if disconnected to the server, false otherwise
     */
    public boolean isDisconnectFlag() {
        return mDisconnectFlag;
    }

    /**
     * Returns true if connected to the server.
     * @return true if connected to the server, false otherwise
     */
    public boolean isOpen() {
        if (mWebSocket != null) {
            return mWebSocket.isOpen();
        }
        return false;
    }

    /**
     * Gets my id.
     * @return id
     */
    public String getId() {
        return mId;
    }

    /**
     * Sets a callback.
     * @param callback callback of SignalingClient
     */
    public void setOnSignalingCallback(OnSignalingCallback callback) {
        mSignalingCallback = callback;
    }

    /**
     * Retrieves the list of peer that can be connected.
     * @param callback Callback to return the list of peer
     */
    public void listAllPeers(final OnAllPeersCallback callback) {
        Uri uri = Uri.parse(createDiscoveryUrl());
        AsyncHttpRequest req = new AsyncHttpRequest(uri, "GET");
        addConfig(req);

        AsyncHttpClient client = AsyncHttpClient.getDefaultInstance();
        client.executeJSONArray(req, new AsyncHttpClient.JSONArrayCallback() {
            @Override
            public void onCompleted(final Exception e, final AsyncHttpResponse source, final JSONArray result) {
                if (e != null) {
                    if (callback != null) {
                        callback.onErrorCallback();
                    }
                } else {
                    int statusCode = source.code();
                    if (statusCode == 200) {
                        if (callback != null) {
                            callback.onCallback(result);
                        }
                    } else {
                        if (callback != null) {
                            callback.onErrorCallback();
                        }
                    }
                }
            }
        });
    }

    /**
     * Retrieves my id from peer server.
     * @param id old id
     */
    private void retrieveId(final String id) {
        Uri uri = Uri.parse(createRetrievedUrl(id));
        AsyncHttpRequest req = new AsyncHttpRequest(uri, "GET");
        req.setHeader("Content-Length", String.valueOf(0));
        req.setTimeout(TIMEOUT_HTTP_CONNECTION);
        addConfig(req);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@ retrieveId");
            Log.d(TAG, "@@@ uri=" + req.toString());
        }

        AsyncHttpClient client = AsyncHttpClient.getDefaultInstance();
        client.executeString(req, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(final Exception e, final AsyncHttpResponse source, final String result) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "@@@ retrieveId response " + source);
                    Log.d(TAG, "@@@ result: " + result);
                }

                if (e != null) {
                    if (mSignalingCallback != null) {
                        mSignalingCallback.onError("Failed to connect a server.");
                    }
                    return;
                }

                if (result != null && result.length() > 0 && source != null) {
                    int resultCode = source.code();
                    source.close();

                    if (resultCode == 200) {
                        try {
                            JSONObject jsonResult = new JSONObject(result);
                            mId = jsonResult.getString("id");
                            mCredential = jsonResult.getString("credential");
                        } catch (Exception ex) {
                            mId = result;
                        }

                        if (mId != null) {
                            startXhrStream();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startWebSocket();
                                }
                            }, 1000L);

                            if (mHandler != null && mSendingRun != null) {
                                mHandler.post(mSendingRun);
                            }
                        }
                    } else {
                        if (mSignalingCallback != null) {
                            mSignalingCallback.onError("Failed to connect a server.");
                        }
                    }
                } else {
                    if (source != null) {
                        source.close();
                    }

                    if (mSignalingCallback != null) {
                        mSignalingCallback.onError("Retrieved PeerId is failed.");
                    }
                }
            }
        });
    }

    /**
     * Starts XHR stream.
     */
    private void startXhrStream() {
        Uri uri = Uri.parse(createSkyWayServer());
        AsyncHttpRequest req = new AsyncHttpRequest(uri, "POST");
        req.setHeader("Accept-Encoding", "gzip, deflate");
        req.setHeader("Connection", "keep-alive");
        req.setHeader("Accept", "*/*");
        req.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        req.addHeader("_index", "1");
        req.addHeader("_streamIndex", String.valueOf(0));
        req.setTimeout(TIMEOUT_HTTP_CONNECTION);
        addConfig(req);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@ startXhrStream");
            Log.d(TAG, "@@@ request=" + req);
        }

        AsyncHttpClient client = AsyncHttpClient.getDefaultInstance();
        client.executeString(req, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(final Exception e, final AsyncHttpResponse source, final String result) {

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "@@@ startXhrStream response.");
                    Log.d(TAG, "@@@ result=" + result);
                    Log.d(TAG, "@@@ message=" + source.message());
                    Log.d(TAG, "@@@ source=" + source);
                    if (e != null) {
                        Log.e(TAG, "@@@ ", e);
                    }
                }

                String type = null;
                if (result != null) {
                    int msgIndex = result.indexOf("{");
                    if (msgIndex != -1) {
                        try {
                            String error = result.substring(msgIndex);
                            JSONObject ex = new JSONObject(error);
                            type = ex.getString("type");
                        } catch (Exception e2) {
                            // do nothing
                        }
                    }
                    if ("OPEN".equalsIgnoreCase(type)) {
                        mDisconnectFlag = false;
                        if (mSignalingCallback != null) {
                            mSignalingCallback.onOpen(mId);
                        }
                    }
                }
            }
        });
    }

    /**
     * Starts a WebSocket.
     */
    private void startWebSocket() {
        Uri uri = Uri.parse(createWSString());
        AsyncHttpRequest req = new AsyncHttpRequest(uri, "GET");
        req.setHeader("User-Agent", PeerUtil.USER_AGENT);
        req.setTimeout(TIMEOUT_WEBSOCKET);
        addConfig(req);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@ startWebSocket: " + uri.toString());
        }

        AsyncHttpClient client = AsyncHttpClient.getDefaultInstance();
        client.websocket(req, PeerUtil.SCHEME_HTTP, new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(final Exception ex, final WebSocket webSocket) {
                if (ex != null) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "", ex);
                    }

                    mDisconnectFlag = true;
                    if (mSignalingCallback != null) {
                        mSignalingCallback.onError(ex.toString());
                    }
                } else {
                    mWebSocket = webSocket;
                    mSendingRun = new Runnable() {
                        @Override
                        public void run() {
                            sendMessage();
                        }
                    };

                    webSocket.setPongCallback(new WebSocket.PongCallback() {
                        @Override
                        public void onPongReceived(String s) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "@@@ PongCallback");
                            }
                        }
                    });
                    webSocket.setWriteableCallback(new WritableCallback() {
                        @Override
                        public void onWriteable() {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "@@@ WriteableCallback");
                            }
                        }
                    });
                    webSocket.setDataCallback(new DataCallback() {
                        @Override
                        public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "@@@ DataCallback");
                            }
                            bb.recycle();
                        }
                    });
                    webSocket.setClosedCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception ex) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "@@@ ClosedCallback");
                                Log.d(TAG, "@@@ ex=" + ex);
                            }

                            if (mSignalingCallback != null) {
                                mSignalingCallback.onClose();
                            }
                        }
                    });
                    webSocket.setEndCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception ex) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "@@@ EndCallback");
                                Log.d(TAG, "@@@ ex=" + ex);
                            }
                        }
                    });
                    webSocket.setStringCallback(new com.koushikdutta.async.http.WebSocket.StringCallback() {
                        @Override
                        public void onStringAvailable(final String s) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "@@@ StringCallback");
                                Log.d(TAG, "@@@ string=" + s);
                            }

                            try {
                                handleMessage(new JSONObject(s));
                            } catch (Exception e) {
                                if (BuildConfig.DEBUG) {
                                    Log.w(TAG, "@@@ json error.", e);
                                }
                            }
                        }
                    });

                    if (webSocket.isOpen()) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "@@@ WebSocket is open.");
                        }
                        mDisconnectFlag = false;
                        if (mSignalingCallback != null) {
                            mSignalingCallback.onOpen(mId);
                        }
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "@@@ WebSocket cannot open.");
                        }
                        mDisconnectFlag = true;
                    }
                }
            }
        });
    }

    /**
     * Stops a WebSocket.
     */
    private void stopWebSocket() {
        if (mWebSocket != null) {
            mWebSocket.close();
            mWebSocket = null;
        }
    }

    /**
     * Added a config to the request.
     * @param req request
     */
    private void addConfig(final AsyncHttpRequest req) {
        if (mConfig.getDomain() != null && mConfig.getDomain().length() > 0) {
            req.addHeader("Origin", mConfig.getDomain());
        }
    }

    /**
     * Handles a message.
     * @param json message
     */
    private void handleMessage(final JSONObject json) {
        String type = PeerUtil.getJSONString(json, "type", "");
        String src = PeerUtil.getJSONString(json, "src", "");
        JSONObject payload = json.optJSONObject("payload");
        String connectionId = PeerUtil.getJSONString(payload, "connectionId", null);;
        String payloadType = PeerUtil.getJSONString(payload, "type", "");

        if (type.equalsIgnoreCase("open")) {
            if (mDisconnectFlag) {
                mDisconnectFlag = false;
                if (mSignalingCallback != null) {
                    mSignalingCallback.onOpen(mId);
                }
            }
        } else if (type.equalsIgnoreCase("close")) {
            if (!mDisconnectFlag) {
                mDisconnectFlag = true;
                if (mSignalingCallback != null) {
                    mSignalingCallback.onClose();
                }
            }
        } else if (type.equalsIgnoreCase("error")) {
            String msg = PeerUtil.getJSONString(payload, "msg", "");
            if (mSignalingCallback != null) {
                mSignalingCallback.onError(msg);
            }
        } else if (type.equalsIgnoreCase("id-taken")) {
            mDisconnectFlag = true;
            String msg = String.format("ID `%s` is taken.", mId);
            if (mSignalingCallback != null) {
                mSignalingCallback.onError(msg);
            }
        } else if (type.equalsIgnoreCase("invalid-key")) {
            mDisconnectFlag = true;
            String msg = String.format("API KEY `%s` is invalid.", mConfig.getApiKey());
            if (mSignalingCallback != null) {
                mSignalingCallback.onError(msg);
            }
        } else if (type.equalsIgnoreCase("ping")) {
            queueMessage("{\"type\":\"PONG\"}");
        } else if (type.equalsIgnoreCase("leave")) {
        } else if (type.equalsIgnoreCase("expire")) {
        } else if (type.equalsIgnoreCase("offer")) {
            if (payloadType.equalsIgnoreCase("media")) {
            } else if (payloadType.equalsIgnoreCase("data")) {
            }
            if (mSignalingCallback != null) {
                mSignalingCallback.onOffer(json);
            }
        } else if (type.equalsIgnoreCase("answer")) {
            if (mSignalingCallback != null) {
                mSignalingCallback.onAnswer(json);
            }
        } else if (type.equalsIgnoreCase("candidate")) {
            if (mSignalingCallback != null) {
                mSignalingCallback.onCandidate(json);
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "@@@ Unknown type." + type);
            }
        }
    }

    /**
     * Gets a url that retrieve id.
     * @param id old id
     * @return url
     */
    private String createRetrievedUrl(final String id) {
        StringBuilder build = new StringBuilder();
        build.append(mOption.mScheme);
        build.append("://");
        build.append(mOption.mHost);
        build.append(":");
        build.append(mOption.mPort);
        build.append("/" + mConfig.getApiKey());
        build.append("/id?");
        String strTSValue = PeerUtil.getTSValue();
        build.append(strTSValue);
        if (id != null && id.length() > 0) {
            build.append("&id=" + id);
        }
        return build.toString();
    }

    /**
     * Gets a url that retrieve list of id.
     * @return url
     */
    private String createDiscoveryUrl() {
        StringBuilder build = new StringBuilder();
        build.append(mOption.mScheme);
        build.append("://");
        build.append(mOption.mHost);
        build.append(":");
        build.append(mOption.mPort);
        build.append("/active/list");
        build.append("/" + mConfig.getApiKey());
        return build.toString();
    }

    /**
     * Gets a url of WebSocket.
     * @return url
     */
    private String createWSString() {
        StringBuilder build = new StringBuilder();
        build.append(mOption.mScheme);
        build.append("://");
        build.append(mOption.mHost);
        build.append("/peerjs");
        build.append("?key=" + mConfig.getApiKey());
        build.append("&id=" + mId);
        build.append("&token=" + mStrToken);
        return build.toString();
    }

    /**
     * Gets a url of SkyWay server.
     * @return url
     */
    private String createSkyWayServer() {
        StringBuilder build = new StringBuilder();
        build.append(mOption.mScheme);
        build.append("://");
        build.append(mOption.mHost);
        build.append(":");
        build.append(mOption.mPort);
        build.append("/" + mConfig.getApiKey());
        build.append("/" + mId);
        build.append("/" + mStrToken);
        build.append("/id");
        build.append("?i=0");
        return build.toString();
    }

    /**
     * Sends a message to the server.
     */
    private void sendMessage() {
        if (mWebSocket != null && mWebSocket.isOpen() && !mQueueMessage.isEmpty()) {
            boolean continues = false;
            String msg = null;

            synchronized (mQueueMessage) {
                if (!mQueueMessage.isEmpty()) {
                    msg = mQueueMessage.remove(0);
                }
            }

            if (msg != null) {
                mWebSocket.send(msg);
                synchronized (mQueueMessage) {
                    continues = !mQueueMessage.isEmpty();
                }
            }

            if (continues && mSendingRun != null) {
                mHandler.postDelayed(mSendingRun, 100L);
            }
        }
    }

    /**
     * Inserts the message into the queue.
     * @param message message
     */
    void queueMessage(final String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "@@@  " + message);
        }

        boolean firstTime = false;
        synchronized (mQueueMessage) {
            if (mQueueMessage.isEmpty()) {
                firstTime = true;
            }
            mQueueMessage.add(message);
        }

        if (firstTime && mSendingRun != null) {
            mHandler.postDelayed(mSendingRun, 100L);
        }
    }

    /**
     * SignalingOption.
     *
     * @author NTT DOCOMO, INC.
     */
    public class SignalingOption {
        String mScheme = PeerUtil.SCHEME_HTTPS;
        String mHost = PeerUtil.SKYWAY_HOST;
        int mPort = PeerUtil.SKYWAY_PORT;
    }

    /**
     * This interface is used to implement {@link SignalingClient} callbacks.
     */
    public interface OnAllPeersCallback {
        void onCallback(JSONArray result);
        void onErrorCallback();
    }

    /**
     * This interface is used to implement {@link SignalingClient} callbacks.
     */
    public interface OnSignalingCallback {
        void onOpen(String peerId);
        void onClose();
        void onOffer(JSONObject jsonMsg);
        void onAnswer(JSONObject json);
        void onCandidate(JSONObject json);
        void onDisconnect();
        void onError(String message);
    }
}
