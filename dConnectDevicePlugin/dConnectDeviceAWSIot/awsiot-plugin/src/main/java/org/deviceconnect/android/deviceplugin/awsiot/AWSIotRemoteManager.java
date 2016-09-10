package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotCore;
import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.p2p.WebClient;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSIotRemoteManager extends AWSIotCore {

    private static final boolean DEBUG = true;
    private static final String TAG = "AWS-Remote";

    private List<RemoteDeviceConnectManager> mManagerList = new ArrayList<>();
    private Context mContext;

    private AWSIotWebServerManager mAWSIotWebServerManager;
    private AWSIotWebClientManager mAWSIotWebClientManager;
    private AWSIotDeviceManager mAWSIotDeviceManager;
    private Map<Integer, Intent> mResponseMap = new HashMap<>();

    private OnEventListener mOnEventListener;

    public AWSIotRemoteManager(final Context context, final AWSIotController controller) {
        mContext = context;
        mIot = controller;
        mAWSIotDeviceManager = new AWSIotDeviceManager();
    }

    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    public void connect() {
        if (mAWSIotWebServerManager == null) {
            mAWSIotWebServerManager = new AWSIotWebServerManager(mContext, this);
        }
        if (mAWSIotWebClientManager == null) {
            mAWSIotWebClientManager = new AWSIotWebClientManager(mContext, this);
        }

        if (mIot.isConnected()) {
            mIot.connectMQTT(new AWSIotController.ConnectCallback() {
                @Override
                public void onConnected(final Exception err) {
                    mIot.getShadow(KEY_DCONNECT_SHADOW_NAME, new AWSIotController.GetShadowCallback() {
                        @Override
                        public void onReceivedShadow(final String thingName, final String result, final Exception err) {
                            if (err != null) {
                                mManagerList = null;
                            } else {
                                mManagerList = parseDeviceShadow(mContext, result);
                            }
                            subscribeTopic();
                        }
                    });
                }
            });
        }
    }

    public void disconnect() {
        if (DEBUG) {
            Log.i(TAG, "AWSIotRemoteManager#disconnect");
        }

        if (mAWSIotWebServerManager != null) {
            mAWSIotWebServerManager.destroy();
            mAWSIotWebServerManager = null;
        }

        if (mAWSIotWebClientManager != null) {
            mAWSIotWebClientManager.destroy();
            mAWSIotWebClientManager = null;
        }

        if (mIot != null) {
            unsubscribeTopic();
        }
    }

    public boolean sendRequest(final Intent request, final Intent response) {
        if (!mIot.isConnected()) {
            MessageUtils.setUnknownError(response);
            return true;
        }

        RemoteDeviceConnectManager remote = mAWSIotDeviceManager.findManagerById(DConnectProfile.getServiceID(request));
        if (remote == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        if (DEBUG) {
            Log.i(TAG, "@@@@@@@@@ AWSIotRemoteManager#sendRequest");
        }

        String action = request.getAction();
        if (IntentDConnectMessage.ACTION_PUT.equals(action)) {
            EventManager.INSTANCE.addEvent(request);
        } else if (IntentDConnectMessage.ACTION_DELETE.equals(action)) {
            EventManager.INSTANCE.removeEvent(request);
        }

        String message = AWSIotRemoteUtil.intentToJson(request, new AWSIotRemoteUtil.ConversionIntentCallback() {
            @Override
            public String convertServiceId(final String id) {
                return mAWSIotDeviceManager.getServiceId(id);
            }

            @Override
            public String convertUri(final String uri) {
                // TODO 他のスキーマがある場合には追加
                if (uri.startsWith("content://")) {
                    return "http://localhost" + WebClient.PATH_CONTENT_PROVIDER + "?" + uri;
                } else {
                    return uri;
                }
            }
        });

        int requestCode = generateRequestCode();
        publish(remote, createRequest(requestCode, message));
        mResponseMap.put(requestCode, response);
        return false;
    }

    public boolean sendServiceDiscovery(final Intent request, final Intent response) {
        if (DEBUG) {
            Log.i(TAG, "@@@@@@@@@ AWSIotRemoteManager#sendServiceDiscovery");
        }

        if (!mIot.isConnected()) {
            MessageUtils.setUnknownError(response);
            return true;
        }

        if (existAWSFlag(request)) {
            MessageUtils.setUnknownError(response);
            return true;
        }

        String message = AWSIotRemoteUtil.intentToJson(request, null);

        // TODO 複数に送った場合には、まーじする必要が有る
        int requestCode = generateRequestCode();
        for (RemoteDeviceConnectManager remote : mManagerList) {
            publish(remote, createRequest(requestCode, message));
        }
        mResponseMap.put(requestCode, response);
        return false;
    }

    public void publish(final RemoteDeviceConnectManager remote, final String message) {
        mIot.publish(remote.getRequestTopic(), message);
    }

    private String createWebServer(final RemoteDeviceConnectManager remote, final String address, final String path) {
        return mAWSIotWebServerManager.createWebServer(remote, address, path);
    }

    private void subscribeTopic() {
        for (RemoteDeviceConnectManager remote : mManagerList) {
            mIot.subscribe(remote.getResponseTopic(), mMessageCallback);
            mIot.subscribe(remote.getEventTopic(), mMessageCallback);
        }
    }

    private void unsubscribeTopic() {
        for (RemoteDeviceConnectManager remote : mManagerList) {
            mIot.unsubscribe(remote.getResponseTopic());
            mIot.unsubscribe(remote.getEventTopic());
        }
    }

    private void sendResponse(final Intent intent) {
        ((DConnectMessageService) mContext).sendResponse(intent);
    }

    private void sendEvent(final Intent event, final String accessToken) {
        ((DConnectMessageService) mContext).sendEvent(event, accessToken);
    }

    private boolean existAWSFlag(final Intent intent) {
        Object o = intent.getExtras().get(AWSIotCore.PARAM_SELF_FLAG);
        if (o instanceof String) {
            return "true".equals(o);
        } else if (o instanceof Boolean) {
            return (Boolean) o;
        } else {
            return false;
        }
    }

    private RemoteDeviceConnectManager parseTopic(final String topic) {
        for (RemoteDeviceConnectManager remote : mManagerList) {
            if (remote.getResponseTopic().equals(topic)) {
                return remote;
            }
            if (remote.getEventTopic().equals(topic)) {
                return remote;
            }
        }
        return null;
    }

    private void parseMQTT(final RemoteDeviceConnectManager remote, final String message) {
        try {
            JSONObject json = new JSONObject(message);
            JSONObject response = json.optJSONObject(KEY_RESPONSE);
            if (response != null) {
                onReceivedDeviceConnectResponse(remote, message);
            }
            JSONObject p2p = json.optJSONObject(KEY_P2P_REMOTE);
            if (p2p != null) {
                mAWSIotWebServerManager.onReceivedSignaling(remote, p2p.toString());
            }
            JSONObject p2pa = json.optJSONObject(KEY_P2P_LOCAL);
            if (p2pa != null) {
                mAWSIotWebClientManager.onReceivedSignaling(remote, p2pa.toString());
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, "", e);
            }
        }
    }

    private void onReceivedDeviceConnectResponse(final RemoteDeviceConnectManager remote, final String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            Integer requestCode = jsonObject.getInt("requestCode");
            Intent response = mResponseMap.get(requestCode);
            if (response != null) {
                JSONObject responseObj = jsonObject.optJSONObject("response");
                if (responseObj == null) {
                    MessageUtils.setUnknownError(response);
                } else {
                    Bundle b = new Bundle();
                    AWSIotRemoteUtil.jsonToIntent(responseObj, b, new AWSIotRemoteUtil.ConversionJsonCallback() {
                        @Override
                        public String convertServiceId(final String id) {
                            return mAWSIotDeviceManager.generateServiceId(remote, id);
                        }

                        @Override
                        public String convertName(final String name) {
                            return remote.getName() + " " + name;
                        }

                        @Override
                        public String convertUri(final String uri) {
                            Uri u = Uri.parse(uri);
                            return createWebServer(remote, u.getAuthority(), u.getPath() + "?" + u.getEncodedQuery());
                        }
                    });
                    response.putExtras(b);
                }
                sendResponse(response);
            }
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "onReceivedDeviceConnectResponse", e);
            }
        }
    }

    private void onReceivedDeviceConnectEvent(final RemoteDeviceConnectManager remote, final String message) {
        if (DEBUG) {
            Log.d(TAG, "onReceivedDeviceConnectEvent: " + remote);
            Log.d(TAG, "message=" + message);
        }

        try {
            JSONObject jsonObject = new JSONObject(message);

            String profile = jsonObject.optString("profile");
            String inter = jsonObject.optString("interface");
            String attribute = jsonObject.optString("attribute");
            String serviceId = mAWSIotDeviceManager.generateServiceId(remote, jsonObject.optString("serviceId"));

            List<Event> events = EventManager.INSTANCE.getEventList(serviceId, profile, inter, attribute);
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);

                String sessionKey = intent.getStringExtra("sessionKey");

                // TODO json->intent 変換をちゃんと検討すること。
                Bundle b = new Bundle();
                AWSIotRemoteUtil.jsonToIntent(jsonObject, b, new AWSIotRemoteUtil.ConversionJsonCallback() {
                    @Override
                    public String convertServiceId(final String id) {
                        return mAWSIotDeviceManager.generateServiceId(remote, id);
                    }

                    @Override
                    public String convertName(final String name) {
                        return remote.getName() + " " + name;
                    }

                    @Override
                    public String convertUri(final String uri) {
                        return uri;
                    }
                });
                intent.putExtras(b);
                intent.putExtra("sessionKey", sessionKey);

                sendEvent(intent, event.getAccessToken());
            }
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "onReceivedDeviceConnectEvent", e);
            }
        }
    }

    private final AWSIotController.MessageCallback mMessageCallback = new AWSIotController.MessageCallback() {
        @Override
        public void onReceivedMessage(final String topic, final String message, final Exception err) {
            if (err != null) {
                Log.e(TAG, "", err);
                return;
            }

            if (mOnEventListener != null) {
                mOnEventListener.onReceivedMessage(topic, message);
            }

            RemoteDeviceConnectManager remote = parseTopic(topic);
            if (remote == null) {
                if (DEBUG) {
                    Log.e(TAG, "Not found the RemoteDeviceConnectManager. topic=" + topic);
                }
                return;
            }

            if (topic.endsWith(RemoteDeviceConnectManager.EVENT)) {
                onReceivedDeviceConnectEvent(remote, message);
            } else if (topic.endsWith(RemoteDeviceConnectManager.RESPONSE)) {
                parseMQTT(remote, message);
            } else {
                if (DEBUG) {
                    Log.w(TAG, "Unknown topic. topic=" + topic);
                }
            }
        }
    };

    public interface OnEventListener {
        void onConnected(Exception err);
        void onReconnecting();
        void onDisconnected();
        void onReceivedMessage(String topic, String message);
    }
}
