package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotCore;
import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class AWSIotLocalManager extends AWSIotCore {

    private static final boolean DEBUG = true;
    private static final String TAG = "AWS-Local";

    private RemoteDeviceConnectManager mRemoteManager;
    private AWSIotWebClientManager mAWSIotWebClientManager;
    private AWSIotWebServerManager mAWSIotWebServerManager;
    private AWSIotWebSocketClient mAWSIotWebSocketClient;
    private OnEventListener mOnEventListener;
    private Context mContext;

    private String mSessionKey = UUID.randomUUID().toString();

    public AWSIotLocalManager(final Context context, final AWSIotController controller, final RemoteDeviceConnectManager remote) {
        mContext = context;
        mIot = controller;
        mRemoteManager = remote;
    }

    public void disconnect() {
        if (DEBUG) {
            Log.i(TAG, "AWSIotLocalManager#disconnect");
        }

        if (mAWSIotWebSocketClient != null) {
            mAWSIotWebSocketClient.close();
            mAWSIotWebSocketClient = null;
        }

        if (mAWSIotWebClientManager != null) {
            mAWSIotWebClientManager.destroy();
            mAWSIotWebClientManager = null;
        }

        if (mAWSIotWebServerManager != null) {
            mAWSIotWebServerManager.destroy();
            mAWSIotWebServerManager = null;
        }

        if (mIot != null) {
           unsubscribeTopic();
        }
    }

    public void setOnEventListener(final OnEventListener listener) {
        mOnEventListener = listener;
    }

    public void connectAWSIoT() {
        mAWSIotWebClientManager = new AWSIotWebClientManager(mContext, this);
        mAWSIotWebServerManager = new AWSIotWebServerManager(mContext, this);

        mAWSIotWebSocketClient = new AWSIotWebSocketClient("http://localhost:4035/websocket", mSessionKey) {
            @Override
            public void onMessage(final String message) {
                publishEvent(message);
            }
        };
        mAWSIotWebSocketClient.connect();

        subscribeTopic();
    }

    public void publish(final String message) {
        mIot.publish(mRemoteManager.getResponseTopic(), message);
    }

    public void publishEvent(final String message) {
        mIot.publish(mRemoteManager.getEventTopic(), message);
    }

    private void subscribeTopic() {
        mIot.subscribe(mRemoteManager.getRequestTopic(), mMessageCallback);
    }

    private void unsubscribeTopic() {
        mIot.unsubscribe(mRemoteManager.getRequestTopic());
    }

    private void parseMQTT(final String message) {
        try {
            JSONObject json = new JSONObject(message);
            int requestCode = json.optInt("requestCode");
            JSONObject request = json.optJSONObject(KEY_REQUEST);
            if (request != null) {
                onReceivedDeviceConnectRequest(requestCode, request.toString());
            }
            JSONObject p2p = json.optJSONObject(KEY_P2P_REMOTE);
            if (p2p != null) {
                mAWSIotWebClientManager.onReceivedSignaling(p2p.toString());
            }
            JSONObject p2pa = json.optJSONObject(KEY_P2P_LOCAL);
            if (p2pa != null) {
                mAWSIotWebServerManager.onReceivedSignaling(p2pa.toString());
            }
        } catch (JSONException e) {
            if (DEBUG) {
                Log.w(TAG, "", e);
            }
        }
    }

    private void onReceivedDeviceConnectRequest(final int requestCode, final String request) {
        if (DEBUG) {
            Log.i(TAG, "onReceivedDeviceConnectRequest: request=" + request);
        }

        DConnectHelper.INSTANCE.sendRequest(request, new DConnectHelper.ConversionCallback() {
            @Override
            public String convertUri(final String uri) {
                Uri u = Uri.parse(uri);
                if (u.getHost().equals("localhost") || u.getHost().equals("127.0.0.1")) {
                    return mAWSIotWebServerManager.createWebServer(u.getAuthority(), u.getEncodedPath() + "?" + u.getEncodedQuery());
                } else {
                    return uri;
                }
            }
            @Override
            public String convertSessionKey(final String sessionKey) {
                return mSessionKey;
            }
        }, new DConnectHelper.FinishCallback() {
            @Override
            public void onFinish(final String response, final Exception error) {
                if (DEBUG) {
                    Log.d(TAG, "onReceivedDeviceConnectRequest: requestCode=" + requestCode + " response=" + response);
                }
                publish(createResponse(requestCode, response));
            }
        });
    }

    private final AWSIotController.MessageCallback mMessageCallback = new AWSIotController.MessageCallback() {
        @Override
        public void onReceivedMessage(final String topic, final String message, final Exception err) {
            if (DEBUG) {
                Log.d(TAG, "AWSIoTLocalManager#onReceivedMessage");
                Log.d(TAG, "topic=" + topic);
                Log.d(TAG, "message=" + message);
            }

            if (err != null) {
                if (DEBUG) {
                    Log.w(TAG, "", err);
                }
                return;
            }

            if (mOnEventListener != null) {
                mOnEventListener.onReceivedMessage(topic, message);
            }

            if (!topic.equals(mRemoteManager.getRequestTopic())) {
                if (DEBUG) {
                    Log.e(TAG, "Not found the RemoteDeviceConnectManager. topic=" + topic);
                }
                return;
            }

            parseMQTT(message);
        }
    };

    public interface OnEventListener {
        void onConnected();
        void onReconnecting();
        void onDisconnected();
        void onReceivedMessage(String topic, String message);
    }
}
