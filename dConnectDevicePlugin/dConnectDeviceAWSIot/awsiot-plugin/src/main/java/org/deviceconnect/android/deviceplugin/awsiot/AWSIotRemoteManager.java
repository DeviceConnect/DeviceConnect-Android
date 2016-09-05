package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Context;
import android.content.Intent;
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

    public void destroy() {
        if (DEBUG) {
            Log.i(TAG, "AWSIotRemoteManager#destroy");
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

                // TODO 全部に対してsubcribeして良いのか
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
                        Log.w(TAG, "", err);
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
                parseMQTT(remote, message);
            }
        });
        mIot.connect(accessKey, secretKey, region);
        mAWSIotWebServerManager = new AWSIotWebServerManager(mContext, this);
    }

    /**
     * リモート先のDevice Connect ManagerにMQTT経由でリクエストを送信します.
     * @param request リクエスト
     * @param response レスポンス
     * @return 同期フラグ
     */
    public boolean sendRequestToRemoteDeviceConnectManager2(final Intent request, final Intent response) {
        if (DEBUG) {
            Log.i(TAG, "AWSIotRemoteManager#sendRequest:");
        }

        RemoteDeviceConnectManager remote = findManagerById(DConnectProfile.getServiceID(request));
        if (remote == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        DConnectHelper.sendRequest(request, new DConnectHelper.Callback() {
            @Override
            public void onCallback(final byte[] data) {
                if (data == null) {
                    Log.e("ABC", "aaaaa = " + data);
                } else {
                    Log.e("ABC", "aaaaa = " + new String(data));
                }
                MessageUtils.setNotFoundServiceError(response);
                sendResponse(response);
            }
        });

        return false;
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

        int requestCode = generateRequestCode();
        publish(remote, createRequest(requestCode, AWSIotRemoteUtil.intentToJson(request)));
        mResponseMap.put(requestCode, response);
        return false;
    }

    public boolean sendServiceDiscovery(final Intent request, final Intent response) {
        if (DEBUG) {
            Log.i(TAG, "@@@@@@@@@ AWSIotRemoteManager#sendServiceDiscovery");
        }

        int requestCode = generateRequestCode();
        for (RemoteDeviceConnectManager remote : mManagerList) {
            publish(remote, createRequest(requestCode, AWSIotRemoteUtil.intentToJson(request)));
        }
        mResponseMap.put(requestCode, response);
        return false;
    }

    public void createWebServer(final RemoteDeviceConnectManager remote, final String address) {
        mAWSIotWebServerManager.createWebServer(remote, address);
    }

    public void publish(final RemoteDeviceConnectManager remote, final String message) {
        mIot.publish(remote.getRequestTopic(), message);
    }

    private void sendResponse(final Intent intent) {
        ((DConnectMessageService) mContext).sendResponse(intent);
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

    private RemoteDeviceConnectManager findManagerById(final String serviceId) {
        return AWSIotRemoteUtil.getRemoteId(serviceId);
    }

    private RemoteDeviceConnectManager parseTopic(final String topic) {
        for (RemoteDeviceConnectManager remote : mManagerList) {
            if (remote.getResponseTopic().equals(topic)) {
                return remote;
            }
        }
        return null;
    }

    private void onReceivedDeviceConnectResponse(final RemoteDeviceConnectManager remote, final String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            Integer requestCode = jsonObject.getInt("requestCode");
            Intent response = mResponseMap.get(requestCode);
            if (response != null) {
                // jsonからintentを作成して送信
                JSONObject responseObj = jsonObject.optJSONObject("response");
                if (responseObj == null) {
                    MessageUtils.setUnknownError(response);
                    sendResponse(response);
                } else {
                    Bundle b = new Bundle();
                    AWSIotRemoteUtil.jsonToIntent(remote, responseObj, b);
                    response.putExtras(b);
                    sendResponse(response);
                }
            } else {
                Log.e(TAG, "Not found response. requestCode=" + requestCode);
                for (Integer req : mResponseMap.keySet()) {
                    Log.e(TAG, req + "::: " + mResponseMap.get(req));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface OnEventListener {
        void onConnected();
        void onReconnecting();
        void onDisconnected();
        void onReceivedMessage(String topic, String message);
    }
}
