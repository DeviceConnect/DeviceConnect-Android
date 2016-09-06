package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.content.Context;
import android.util.Log;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotCore;
import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class AWSIotLocalManager extends AWSIotCore {

    private static final boolean DEBUG = true;
    private static final String TAG = "AWS-Local";

    private RemoteDeviceConnectManager mRemoteManager;
    private AWSIotWebClientManager mAWSIotWebClientManager;
    private AWSIotWebSocketClient mAWSIotWebSocketClient;
    private OnEventListener mOnEventListener;
    private Context mContext;

    private String mSessionKey = UUID.randomUUID().toString();

    public AWSIotLocalManager(final Context context, final String name, final String id) {
        mContext = context;
        mRemoteManager = new RemoteDeviceConnectManager(name, id);

        mAWSIotWebSocketClient = new AWSIotWebSocketClient("http://localhost:4035/websocket", mSessionKey) {
            @Override
            public void onMessage(final String message) {
                publishEvent(message);
            }
        };
        mAWSIotWebSocketClient.connect();

        DConnectHelper.INSTANCE.setSessionKey(mSessionKey);
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

        if (mIot != null) {
            if (mRemoteManager != null) {
                mIot.unsubscribe(mRemoteManager.getRequestTopic());
            }
            mIot.disconnect();
            mIot = null;
        }
    }

    public void setOnEventListener(final OnEventListener listener) {
        mOnEventListener = listener;
    }

    public void connectAWSIoT(final String accessKey, final String secretKey, final Regions region) {
        if (mIot != null) {
            if (DEBUG) {
                Log.w(TAG, "mIoT is already running.");
            }
            return;
        }

        if (accessKey == null || secretKey == null || region == null) {
            throw new RuntimeException("");
        }

        mIot = new AWSIotController();
        mIot.setEventListener(new AWSIotController.EventListener() {
            @Override
            public void onConnected(final Exception err) {
                if (DEBUG) {
                    Log.d(TAG, "AWSIoTLocalManager#onConnected");
                }
                if (err != null) {
                    if (DEBUG) {
                        Log.e(TAG, "AWSIotLocalManager#onConnected", err);
                    }
                    return;
                }
                mIot.subscribe(mRemoteManager.getRequestTopic());

                if (mOnEventListener != null) {
                    mOnEventListener.onConnected();
                }
            }

            @Override
            public void onReconnecting() {
                if (mOnEventListener != null) {
                    mOnEventListener.onReconnecting();
                }
            }

            @Override
            public void onDisconnected() {
                if (mOnEventListener != null) {
                    mOnEventListener.onDisconnected();
                }
            }

            @Override
            public void onReceivedShadow(final String thingName, final String result, final Exception err) {
                if (DEBUG) {
                    Log.d(TAG, "AWSIoTLocalManager#onReceivedShadow");
                    Log.d(TAG, "thingName=" + thingName);
                    Log.d(TAG, "result=" + result);
                }

                if (err != null) {
                    if (DEBUG) {
                        Log.e(TAG, "AWSIotLocalManager#onReceivedShadow", err);
                    }
                    return;
                }

                List<RemoteDeviceConnectManager> list = parseDeviceShadow(result);
                if (list != null && !list.contains(mRemoteManager)) {
                    updateDeviceShadow(mRemoteManager);
                }
            }

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
                    // TODO エラー処理を追加
                    return;
                }

                if (mOnEventListener != null) {
                    mOnEventListener.onReceivedMessage(topic, message);
                }

                if (!topic.equals(mRemoteManager.getRequestTopic())) {
                    if (DEBUG) {
                        Log.e(TAG, "Not found the RemoteDeviceConnectManager. topic=" + topic);
                    }
                    // TODO エラー処理を追加
                    return;
                }

                parseMQTT(message);
            }
        });
        mIot.connect(accessKey, secretKey, region);
        mAWSIotWebClientManager = new AWSIotWebClientManager(mContext, this);
    }

    public void publish(final String message) {
        mIot.publish(mRemoteManager.getResponseTopic(), message);
    }

    public void publishEvent(final String message) {
        mIot.publish(mRemoteManager.getEventTopic(), message);
    }

    private void parseMQTT(final String message) {
        try {
            JSONObject json = new JSONObject(message);
            int requestCode = json.optInt("requestCode");
            JSONObject request = json.optJSONObject(KEY_REQUEST);
            if (request != null) {
                executeDeviceConnectRequest(requestCode, request.toString());
            }
            JSONObject p2p = json.optJSONObject(KEY_P2P);
            if (p2p != null) {
                mAWSIotWebClientManager.onReceivedSignaling(p2p.toString());
            }
        } catch (JSONException e) {
            if (DEBUG) {
                Log.w(TAG, "", e);
            }
        }
    }

    private void executeDeviceConnectRequest(final int requestCode, final String request) {
        if (DEBUG) {
            Log.i(TAG, "executeDeviceConnectRequest: request=" + request);
        }

        DConnectHelper.INSTANCE.sendRequest(request, new DConnectHelper.FinishCallback() {
            @Override
            public void onFinish(final String response, final Exception error) {
                if (DEBUG) {
                    Log.d(TAG, "executeDeviceConnectRequest: requestCode=" + requestCode + " response=" + response);
                }
                publish(createResponse(requestCode, response));
            }
        });
    }

    public interface OnEventListener {
        void onConnected();
        void onReconnecting();
        void onDisconnected();
        void onReceivedMessage(String topic, String message);
    }
}
