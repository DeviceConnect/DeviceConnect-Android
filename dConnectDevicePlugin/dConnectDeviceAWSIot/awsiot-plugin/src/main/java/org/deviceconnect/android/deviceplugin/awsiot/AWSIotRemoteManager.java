package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotCore;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AWSIotRemoteManager extends AWSIotCore {

    private static final boolean DEBUG = true;
    private static final String TAG = "ABC";

    protected List<RemoteDeviceConnectManager> mManagerList = new ArrayList<>();
    private Context mContext;

    private AWSIotWebServerManager mAWSIotWebServerManager;
    private Map<Integer, Intent> mResponseMap = new HashMap<>();

    public AWSIotRemoteManager(final Context context) {
        mContext = context;
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
                for (RemoteDeviceConnectManager remote : mManagerList) {
                    mIot.subscribe(remote.getResponseTopic());
                    mIot.subscribe(remote.getEventTopic());
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
    public boolean sendRequestToRemoteDeviceConnectManager(final Intent request, final Intent response) {
        if (DEBUG) {
            Log.i(TAG, "AWSIotRemoteManager#sendRequestToRemoteDeviceConnectManager:");
        }

        String serviceId = DConnectProfile.getServiceID(request);
        RemoteDeviceConnectManager remote = findManagerById(serviceId);
        if (remote == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        JSONObject jsonObject = new JSONObject();
        final Bundle extras = request.getExtras();
        final Set<String> keySet = extras.keySet();
        Integer requestCode = null;
        for (final String key: keySet) {
            try {
                // TODO: 処理がループしないようにflgで分岐している。仕様検討。
                if (key.equals("awsflg")) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
                    return true;
                }
                jsonObject.put(key, extras.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
                // TODO: エラー処理
            }
            if (key.endsWith("requestCode")) {
                requestCode = extras.getInt("requestCode");
            }
        }

        try {
            jsonObject.put("method", request.getAction());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (requestCode == null) {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
            return true;
        }

        publish(remote, createRequest(jsonObject.toString()));

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
                onReceivedDeviceConnectResponse(remote, response.toString());
            }
            JSONObject p2p = json.optJSONObject(KEY_P2P);
            if (p2p != null) {
                mAWSIotWebServerManager.onReceivedSignaling(remote, p2p.toString());
            }
        } catch (JSONException e) {
            if (DEBUG) {
                Log.w(TAG, "", e);
            }
        }
    }

    private RemoteDeviceConnectManager findManagerById(final String serviceId) {
        for (RemoteDeviceConnectManager remote : mManagerList) {
            if (remote.getServiceId().equals(serviceId)) {
                return remote;
            }
        }
        return null;
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
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(message);

            // requestCodeをキーに保持してあるレスポンスを取得
            Integer requestCode = jsonObject.getInt("requestCode");
            Intent response = mResponseMap.get(requestCode);
            if (response == null) {
                // TODO: エラー処理
            } else {
                // jsonからintentを作成して送信
                JSONObject responseObj = jsonObject.optJSONObject("response");
                if (responseObj == null) {
                    MessageUtils.setUnknownError(response);
                    sendResponse(response);
                } else {
                    Bundle b = new Bundle();
                    jsonToIntent(remote, responseObj, b);
                    response.putExtras(b);
                    sendResponse(response);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // jsonからintentを作成
    // TODO: ごちゃごちゃしてるので、もっとスマートに
    private void jsonToIntent(RemoteDeviceConnectManager remote, final JSONObject responseObj, final Bundle response) throws JSONException {
        Iterator<?> jsonKeys = responseObj.keys();
        while (jsonKeys.hasNext()) {
            String key = (String) jsonKeys.next();
            if (key.equals("product") || key.equals("version")) {
                continue;
            }

            Object obj = responseObj.get(key);
            if (obj instanceof String) {
                // ServiceDiscoveryで取得するidは、ここで、ローカルのIDにマッピングします。
                // serviceIdの頭に、適当な名前を付加します。
                if (key.equals("id")) {
                    // TODO serviceIdをマッピングし直す
                    String id = remote.getServiceId() + "." + obj;
                    response.putString(key, id);
                } else {
                    response.putString(key, (String) obj);
                }
            } else if (obj instanceof Double) {
                response.putDouble(key, (Double) obj);
            } else if (obj instanceof Integer) {
                response.putInt(key, (Integer) obj);
            } else if (obj instanceof Boolean) {
                response.putBoolean(key, (Boolean) obj);
            } else if (obj instanceof JSONObject) {
                Bundle b = new Bundle();
                jsonToIntent(remote, (JSONObject) obj, b);
                response.putBundle(key, b);
            } else if (obj instanceof JSONArray) {
                List outArray = new ArrayList();
                JSONArray array = (JSONArray) obj;
                for (int i = 0; i < array.length(); i++) {
                    Object ooo = array.get(i);
                    if (ooo instanceof JSONObject) {
                        JSONObject obj2 = (JSONObject) ooo;
                        Bundle b = new Bundle();
                        jsonToIntent(remote, obj2, b);
                        outArray.add(b);
                    } else {
                        outArray.add(ooo);
                    }
                }
                if (outArray.size() > 0) {
                    if (outArray.get(0) instanceof Bundle) {
                        response.putParcelableArray(key, (Parcelable[]) outArray.toArray(new Bundle[outArray.size()]));
                    } else if (outArray.get(0) instanceof String) {
                        response.putStringArray(key, (String[]) outArray.toArray(new String[outArray.size()]));
                    } else {
                        // TODO: その他の処理
                        Log.d(TAG, "**:" + outArray.get(0).toString());
                    }
                }
            } else {
                Log.d(TAG, "*:" + obj.toString());
                // TODO: その他の処理
            }
        }
    }
}
