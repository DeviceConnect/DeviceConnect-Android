package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotCore;
import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSIotRemoteManager extends AWSIotCore {

    private static final boolean DEBUG = true;
    private static final String TAG = "AWS-Remote";

    protected List<RemoteDeviceConnectManager> mManagerList = new ArrayList<>();
    private Context mContext;

    private AWSIotWebServerManager mAWSIotWebServerManager;
    private Map<Integer, Intent> mResponseMap = new HashMap<>();

    private OnEventListener mOnEventListener;

    public AWSIotRemoteManager(final Context context) {
        mContext = context;
    }

    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    public void disconnect() {
        if (DEBUG) {
            Log.i(TAG, "AWSIotRemoteManager#disconnect");
        }

        if (mAWSIotWebServerManager != null) {
            mAWSIotWebServerManager.destroy();
            mAWSIotWebServerManager = null;
        }

        if (mIot != null) {
            for (RemoteDeviceConnectManager remote : mManagerList) {
                mIot.unsubscribe(remote.getResponseTopic());
                mIot.unsubscribe(remote.getEventTopic());
            }

            mIot.disconnect();
            mIot = null;
        }
    }

    public List<RemoteDeviceConnectManager> getRemoteManagerList() {
        return mManagerList;
    }

    public void connectAWSIoT(final String accessKey, final String secretKey, final Regions region) {
        if (mIot != null) {
            if (DEBUG) {
                Log.w(TAG, "mIoT is already running.");
            }
            return;
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
                        Log.w(TAG, "AWSIoTRemoteManager#onConnected", err);
                    }
                    return;
                }
                getDeviceShadow();

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
                    Log.d(TAG, "AWSIoTRemoteManager#onReceivedShadow");
                    Log.d(TAG, "thingName=" + thingName);
                    Log.d(TAG, "result=" + result);
                }

                if (err != null) {
                    if (DEBUG) {
                        Log.w(TAG, "AWSIoTRemoteManager#onReceivedShadow", err);
                    }
                    return;
                }
                mManagerList = parseDeviceShadow(result);

                // TODO 全部に対してsubscribeして良いのか
                if (mManagerList != null) {
                    for (RemoteDeviceConnectManager remote : mManagerList) {
                        mIot.subscribe(remote.getResponseTopic());
                        mIot.subscribe(remote.getEventTopic());
                    }
                }
            }

            @Override
            public void onReceivedMessage(final String topic, final String message, final Exception err) {
                if (DEBUG) {
                    Log.d(TAG, "AWSIoTRemoteManager#onReceivedMessage");
                    Log.d(TAG, "topic=" + topic);
                    Log.d(TAG, "message=" + message);
                }

                if (err != null) {
                    if (DEBUG) {
                        Log.w(TAG, "ASWIotRemoteManager#onReceivedMessage", err);
                    }
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
        });
        mIot.connect(accessKey, secretKey, region);
        mAWSIotWebServerManager = new AWSIotWebServerManager(mContext, this);
    }

    public boolean sendRequest(final Intent request, final Intent response) {
        RemoteDeviceConnectManager remote = findManagerById(DConnectProfile.getServiceID(request));
        if (remote == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        if (DEBUG) {
            Log.i(TAG, "@@@@@@@@@ AWSIotRemoteManager#sendRequest");
        }

        if (existAWSFlag(request)) {
            MessageUtils.setUnknownError(response);
            return true;
        }

        String message = AWSIotRemoteUtil.intentToJson(request, new AWSIotRemoteUtil.ConversionIntentCallback() {
            @Override
            public String convertServiceId(final String id) {
                return getServiceId(id);
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

    public String createWebServer(final RemoteDeviceConnectManager remote, final String address, final String path) {
        return mAWSIotWebServerManager.createWebServer(remote, address, path);
    }

    public void publish(final RemoteDeviceConnectManager remote, final String message) {
        mIot.publish(remote.getRequestTopic(), message);
    }

    private void sendResponse(final Intent intent) {
        ((DConnectMessageService) mContext).sendResponse(intent);
    }

    private void sendEvent(final Intent event, final String accessToken) {
        ((DConnectMessageService) mContext).sendEvent(event, accessToken);
    }

    private boolean existAWSFlag(final Intent intent) {
        return intent.getExtras().getBoolean("awsflag", false);
    }

    private RemoteDeviceConnectManager findManagerById(final String serviceId) {
        for (TempDevice t : mDeviceList) {
            if (serviceId.equals(t.mId)) {
                return t.mRemote;
            }
        }
        return null;    }

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
            JSONObject p2p = json.optJSONObject(KEY_P2P);
            if (p2p != null) {
                mAWSIotWebServerManager.onReceivedSignaling(remote, p2p.toString());
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
                            return generateServiceId(remote, id);
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
            } else {
                Log.e(TAG, "Not found response. requestCode=" + requestCode);
                for (Integer req : mResponseMap.keySet()) {
                    Log.e(TAG, req + "::: " + mResponseMap.get(req));
                }
            }
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
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
            String serviceId = jsonObject.optString("serviceId");
            String sessionKey = jsonObject.optString("sessionKey");

            // TODO json->intent 変換
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<TempDevice> mDeviceList = new ArrayList<>();

    private class TempDevice {
        RemoteDeviceConnectManager mRemote;
        String mServiceId;
        String mId;
    }

    private String generateServiceId(final RemoteDeviceConnectManager remote, final String serviceId) {
        for (TempDevice t : mDeviceList) {
            if (remote.equals(t.mRemote) && serviceId.equals(t.mServiceId)) {
                return t.mId;
            }
        }

        TempDevice t = new TempDevice();
        t.mRemote = remote;
        t.mServiceId = serviceId;
        t.mId = AWSIotUtil.md5(remote.getServiceId() + serviceId);
        mDeviceList.add(t);
        return t.mId;
    }

    private String getServiceId(final String serviceId) {
        for (TempDevice t : mDeviceList) {
            if (serviceId.equals(t.mId)) {
                return t.mServiceId;
            }
        }
        return null;
    }

    public interface OnEventListener {
        void onConnected();
        void onReconnecting();
        void onDisconnected();
        void onReceivedMessage(String topic, String message);
    }
}
