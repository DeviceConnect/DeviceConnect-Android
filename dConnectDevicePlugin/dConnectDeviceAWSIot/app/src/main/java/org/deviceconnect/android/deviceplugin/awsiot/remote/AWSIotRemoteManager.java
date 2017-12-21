/*
 AWSIotRemoteManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.remote;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.AWSIotDeviceManager;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.RDCMListManager;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.cores.p2p.WebClient;
import org.deviceconnect.android.deviceplugin.awsiot.cores.util.AWSIotUtil;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AWSIotRemoteManager {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "AWS-Remote";

    private Context mContext;

    private AWSIotWebServerManager mAWSIotWebServerManager;
    private AWSIotWebClientManager mAWSIotWebClientManager;
    private AWSIotDeviceManager mAWSIotDeviceManager;
    private AWSIotRequestManager mRequestManager;

    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    private AWSIotController mIot;

    private RDCMListManager mRDCMListManager;

    public AWSIotRemoteManager(final Context context, final AWSIotController controller) {
        mContext = context;
        mIot = controller;
        mAWSIotDeviceManager = new AWSIotDeviceManager();
        AWSIotDeviceApplication app = (AWSIotDeviceApplication) mContext.getApplicationContext();
        mRDCMListManager = app.getRDCMListManager();
        mRDCMListManager.setOnEventListener(mUpdateListener);
    }

    public AWSIotController getAWSIotController() {
        return mIot;
    }

    public void connect() {
        if (DEBUG) {
            Log.i(TAG, "AWSIotRemoteManager#connect");
        }

        if (mAWSIotWebServerManager == null) {
            mAWSIotWebServerManager = new AWSIotWebServerManager(mContext, this);
        }

        if (mAWSIotWebClientManager == null) {
            mAWSIotWebClientManager = new AWSIotWebClientManager(mContext, this);
        }

        if (mRequestManager == null) {
            mRequestManager = new AWSIotRequestManager();
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

        if (mRequestManager != null) {
            mRequestManager.destroy();
            mRequestManager = null;
        }
    }

    public boolean sendRequest(final Intent request, final Intent response) {
        if (!mIot.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "Not connected to the AWS IoT.");
            return true;
        }

        RemoteDeviceConnectManager remote = mAWSIotDeviceManager.findManagerById(DConnectProfile.getServiceID(request));
        if (remote == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        if (DEBUG) {
            Log.i(TAG, "@@@@@@@@@ AWSIotRemoteManager#sendRequest: " + DConnectProfile.getProfile(request));
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

        int requestCode = AWSIotUtil.generateRequestCode();
        if (!publish(remote, AWSIotUtil.createRequest(requestCode, message))) {
            MessageUtils.setIllegalDeviceStateError(response, "Not publish to the mqtt.");
            return true;
        }

        AWSIotRequest aws = new AWSIotRequest() {
            @Override
            public void onReceivedMessage(final RemoteDeviceConnectManager remote, final JSONObject responseObj) throws JSONException {
                if (!mRequestManager.pop(mRequestCode)) {
                    return;
                }

                if (responseObj == null) {
                    MessageUtils.setUnknownError(mResponse);
                } else {
                    Bundle b = new Bundle();
                    AWSIotRemoteUtil.jsonToIntent(responseObj, b, new AWSIotRemoteUtil.ConversionJsonCallback() {
                        @Override
                        public String convertServiceId(final String id) {
                            return id;
                        }

                        @Override
                        public String convertName(final String name) {
                            return name;
                        }

                        @Override
                        public String convertUri(final String uri) {
                            Uri u = Uri.parse(uri);
                            String path = u.getPath() + "?" + u.getEncodedQuery();
                            if (u.getEncodedQuery() == null) {
                                path = u.getPath();
                            }
                            return createWebServer(remote, u.getAuthority(), path);
                        }
                    });
                    mResponse.putExtras(b);
                }
                sendResponse(mResponse);
            }
        };
        aws.mRequest = request;
        aws.mResponse = response;
        aws.mRequestCode = requestCode;
        aws.mRequestCount = 1;
        mRequestManager.put(requestCode, aws);

        return false;
    }

    public boolean sendServiceDiscovery(final Intent request, final Intent response) {
        if (DEBUG) {
            Log.i(TAG, "@@@@@@@@@ AWSIotRemoteManager#sendServiceDiscovery");
        }

        if (!mIot.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "Not connected to the AWS IoT.");
            return true;
        }

        if (existAWSFlag(request)) {
            MessageUtils.setUnknownError(response);
            return true;
        }

        List<RemoteDeviceConnectManager> managers = mRDCMListManager.getRDCMList();
        if (managers == null) {
            MessageUtils.setUnknownError(response, "There is no managers.");
            return true;
        }

        String message = AWSIotRemoteUtil.intentToJson(request, null);

        int count = 0;
        int requestCode = AWSIotUtil.generateRequestCode();
        for (RemoteDeviceConnectManager remote : managers) {
            if (isOnlineManager(remote)) {
                if (publish(remote, AWSIotUtil.createRequest(requestCode, message))) {
                    count++;
                }
            }
        }

        if (count == 0) {
            MessageUtils.setUnknownError(response, "There is no managers.");
            return true;
        }

        AWSIotRequest aws = new AWSIotRequest() {
            @Override
            public void onReceivedMessage(final RemoteDeviceConnectManager remote, final JSONObject responseObj) throws JSONException {
                if (responseObj != null && responseObj.has(ServiceDiscoveryProfile.PARAM_SERVICES)) {
                    JSONArray array = responseObj.getJSONArray("services");
                    for (int i = 0; i < array.length(); i++) {
                        Bundle service = new Bundle();
                        JSONObject obj = array.getJSONObject(i);
                        AWSIotRemoteUtil.jsonToIntent(obj, service, new AWSIotRemoteUtil.ConversionJsonCallback() {
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
                                String path = u.getPath() + "?" + u.getEncodedQuery();
                                if (u.getEncodedQuery() == null) {
                                    path = u.getPath();
                                }
                                return createWebServer(remote, u.getAuthority(), path);
                            }
                        });
                        mServices.add(service);
                    }
                }

                if (!mRequestManager.pop(mRequestCode)) {
                    return;
                }

                send();
            }

            @Override
            public void onTimeout() {
                send();
            }

            private void send() {
                DConnectProfile.setResult(mResponse, DConnectMessage.RESULT_OK);
                ServiceDiscoveryProfile.setServices(mResponse, mServices);
                sendResponse(mResponse);
            }
        };
        aws.mRequest = request;
        aws.mResponse = response;
        aws.mRequestCode = requestCode;
        aws.mRequestCount = count;
        mRequestManager.put(requestCode, aws, 6);

        return false;
    }

    public boolean publish(final RemoteDeviceConnectManager remote, final String message) {
        return mIot.publish(remote.getRequestTopic(), message);
    }

    private String createWebServer(final RemoteDeviceConnectManager remote, final String address, final String path) {
        return mAWSIotWebServerManager.createWebServer(remote, address, path);
    }

    private void sendResponse(final Intent intent) {
        ((DConnectMessageService) mContext).sendResponse(intent);
    }

    private void sendEvent(final Intent event, final String accessToken) {
        ((DConnectMessageService) mContext).sendEvent(event, accessToken);
    }

    private boolean existAWSFlag(final Intent intent) {
        Object o = intent.getExtras().get(AWSIotUtil.PARAM_SELF_FLAG);
        if (o instanceof String) {
            return "true".equals(o);
        } else if (o instanceof Boolean) {
            return (Boolean) o;
        } else {
            return false;
        }
    }

    private RemoteDeviceConnectManager parseTopic(final String topic) {
        List<RemoteDeviceConnectManager> managers = mRDCMListManager.getRDCMList();
        if (managers != null) {
            for (RemoteDeviceConnectManager remote : managers) {
                if (remote.getResponseTopic().equals(topic)) {
                    return remote;
                }
                if (remote.getEventTopic().equals(topic)) {
                    return remote;
                }
            }
        }
        return null;
    }

    private void parseMQTT(final RemoteDeviceConnectManager remote, final String message) {
        try {
            JSONObject json = new JSONObject(message);
            JSONObject response = json.optJSONObject(AWSIotUtil.KEY_RESPONSE);
            if (response != null) {
                onReceivedDeviceConnectResponse(remote, message);
            }
            JSONObject p2p = json.optJSONObject(AWSIotUtil.KEY_P2P_REMOTE);
            if (p2p != null) {
                mAWSIotWebServerManager.onReceivedSignaling(remote, p2p.toString());
            }
            JSONObject p2pa = json.optJSONObject(AWSIotUtil.KEY_P2P_LOCAL);
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
            AWSIotRequest request = mRequestManager.get(jsonObject.getInt("requestCode"));
            if (request == null) {
                return;
            }
            request.onReceivedMessage(remote, jsonObject.optJSONObject("response"));
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

                String accessToken = intent.getStringExtra("accessToken");

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
                if (accessToken != null) {
                    intent.putExtra("accessToken", accessToken);
                }

                sendEvent(intent, event.getAccessToken());
            }
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "onReceivedDeviceConnectEvent", e);
            }
        }
    }

    private RDCMListManager.OnEventListener mUpdateListener = new RDCMListManager.OnEventListener() {
        @Override
        public void onRDCMListUpdateSubscribe(final RemoteDeviceConnectManager manager) {
            if (isOnlineManager(manager)) {
                mIot.subscribe(manager.getResponseTopic(), mMessageCallback);
                mIot.subscribe(manager.getEventTopic(), mMessageCallback);
            } else {
                mIot.unsubscribe(manager.getResponseTopic());
                mIot.unsubscribe(manager.getEventTopic());
            }
        }
    };

    private boolean isOnlineManager(final RemoteDeviceConnectManager manager) {
        long checkTime = System.currentTimeMillis() - 600000;
        return (manager.isSubscribe() && manager.isOnline() && (manager.getTimeStamp() - checkTime > 0));
    }

    private final AWSIotController.MessageCallback mMessageCallback = new AWSIotController.MessageCallback() {
        @Override
        public void onReceivedMessage(final String topic, final String message, final Exception err) {
            if (err != null) {
                Log.e(TAG, "", err);
                return;
            }

            final RemoteDeviceConnectManager remote = parseTopic(topic);
            if (remote == null) {
                if (DEBUG) {
                    Log.e(TAG, "Not found the RemoteDeviceConnectManager. topic=" + topic);
                }
                return;
            }

            if (DEBUG) {
                Log.i(TAG, "onReceivedMessage: " + topic + " " + message);
            }

            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
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
        }
    };

    private class AWSIotRequestManager {
        private final Map<Integer, AWSIotRequest> mMap = new HashMap<>();
        private ScheduledExecutorService mExecutorService = Executors.newScheduledThreadPool(8);

        public void destroy() {
            mExecutorService.shutdown();
        }

        public void put(final int requestCode, final AWSIotRequest request, final int timeout, final TimeUnit timeUnit) {
            mMap.put(requestCode, request);
            request.mFuture = mExecutorService.schedule(request, timeout, timeUnit);
        }

        public void put(final int requestCode, final AWSIotRequest request, final int timeout) {
           put(requestCode, request, timeout, TimeUnit.SECONDS);
        }

        public void put(final int requestCode, final AWSIotRequest request) {
            put(requestCode, request, 30, TimeUnit.SECONDS);
        }

        public AWSIotRequest get(int key) {
            return mMap.get(key);
        }

        public void remove(int key) {
            mMap.remove(key);
        }

        public boolean pop(int key) {
            AWSIotRequest request = mMap.get(key);
            if (request == null) {
                return false;
            }

            request.mRequestCount--;
            if (request.mRequestCount > 0) {
                return false;
            }
            mMap.remove(key);
            request.cancel();
            return true;
        }
    }

    private class AWSIotRequest implements Runnable {
        protected int mRequestCode;
        protected int mRequestCount;
        protected Intent mRequest;
        protected Intent mResponse;
        protected ScheduledFuture mFuture;
        protected List<Bundle> mServices = new ArrayList<>();

        @Override
        public void run() {
            if (DEBUG) {
                Log.w(TAG, "timeout " + mRequestCode + " " + DConnectProfile.getProfile(mRequest));
            }
            mRequestManager.remove(mRequestCode);
            onTimeout();
        }

        public void cancel() {
            mFuture.cancel(true);
        }

        public void onReceivedMessage(RemoteDeviceConnectManager remote, JSONObject jsonObject) throws JSONException {
            // do nothing.
        }

        public void onTimeout() {
            // do nothing.
        }
    }
}
