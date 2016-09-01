package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.content.Context;
import android.util.Log;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotCore;
import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSIotLocalManager extends AWSIotCore {

    private static final boolean DEBUG = true;
    private static final String TAG = "ABC";

    private AWSIotWebClientManager mAWSIotWebClientManager;

    private RemoteDeviceConnectManager mRemoteManager;
    private Context mContext;

    public AWSIotLocalManager(final Context context, final String name, final String id) {
        mContext = context;
        mRemoteManager = new RemoteDeviceConnectManager(name, id);
    }

    public void destroy() {
        if (DEBUG) {
            Log.i(TAG, "AWSIotLocalManager#destroy");
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
                getDeviceShadow();
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
                if (!list.contains(mRemoteManager)) {
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

                if (!topic.equals(mRemoteManager.getRequestTopic())) {
                    if (DEBUG) {
                        Log.e(TAG, "Not found the RemoteDeviceConnectManager. topic=" + topic);
                    }
                    // TODO エラー処理を追加
                    return;
                }

                parseMQTT(mRemoteManager, message);
            }
        });
        mIot.connect(accessKey, secretKey, region);
        mAWSIotWebClientManager = new AWSIotWebClientManager(mContext, this);
    }

    public void publish(final RemoteDeviceConnectManager remote, final String message) {
        mIot.publish(remote.getResponseTopic(), message);
    }

    private void parseMQTT(final RemoteDeviceConnectManager remote, final String message) {
        try {
            JSONObject json = new JSONObject(message);
            JSONObject request = json.optJSONObject(KEY_REQUEST);
            if (request != null) {
                executeDeviceConnectRequest(remote, request.toString());
            }
            JSONObject p2p = json.optJSONObject(KEY_P2P);
            if (p2p != null) {
                mAWSIotWebClientManager.onReceivedSignaling(remote, p2p.toString());
            }
        } catch (JSONException e) {
            if (DEBUG) {
                Log.w(TAG, "", e);
            }
        }
    }

    private void executeDeviceConnectRequest(final RemoteDeviceConnectManager remote, final String request) {
        try {
            JSONObject jsonObject = new JSONObject(request);

            String profile = jsonObject.getString("profile");
            String method = jsonObject.getString("method");
            method = method.replace("org.deviceconnect.action.", "");
            String path = "/gotapi/" + profile;
            if (jsonObject.has("attribute")) {
                path += "/" + jsonObject.getString("attribute");
            }
            String serviceId = null;
            if (jsonObject.has("serviceId")) {
                serviceId = jsonObject.getString("serviceId");
                serviceId = serviceId.replace(remote.getName() + ".", "");
            }
            final int requestCode = jsonObject.getInt("requestCode");

            Map<String, String> param = new HashMap<>();
            param.put("awsflg", "true");
            DConnectHelper.INSTANCE.sendRequest(method, path, serviceId, null, param, new DConnectHelper.FinishCallback<String>() {
                @Override
                public void onFinish(final String str, final Exception error) {
                    publish(remote, createResponse(requestCode, str));
                }
            });
        } catch (JSONException e) {
            publish(remote, "{}");
        }
    }
}
