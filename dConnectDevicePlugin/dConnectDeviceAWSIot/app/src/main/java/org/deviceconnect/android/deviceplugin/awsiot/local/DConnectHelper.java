/*
 DConnectHelper.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.cores.util.AWSIotUtil;
import org.deviceconnect.android.deviceplugin.awsiot.cores.util.HttpUtil;
import org.deviceconnect.android.deviceplugin.awsiot.remote.BuildConfig;
import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DeviceConnectヘルパークラス.
 */
public class DConnectHelper {
    /** デバッグフラグ. */
    private static final boolean DEBUG = BuildConfig.DEBUG;
    /** デバッグタグ. */
    private static final String TAG = "DConnectHelper";

    /** シングルトンなManagerのインスタンス. */
    public static final DConnectHelper INSTANCE = new DConnectHelper();

    public static final String ORIGIN = "http://org.deviceconnect.android.deviceplugin.awsiot";

    private AuthInfo mAuthInfo;

    private Map<String, String> mDefaultHeader = new HashMap<>();
    private List<String> mScopes =  new ArrayList<String>() {
        {
            add("servicediscovery");
            add("serviceinformation");
            add("system");
            add("battery");
            add("connect");
            add("deviceorientation");
            add("filedescriptor");
            add("file");
            add("mediaplayer");
            add("mediastreamrecording");
            add("notification");
            add("phone");
            add("proximity");
            add("settings");
            add("vibration");
            add("light");
            add("remotecontroller");
            add("drivecontroller");
            add("mhealth");
            add("sphero");
            add("dice");
            add("temperature");
            add("camera");
            add("canvas");
            add("health");
            add("touch");
            add("humandetect");
            add("keyevent");
            add("omnidirectionalimage");
            add("tv");
            add("powermeter");
            add("humidity");
            add("illuminance");
            add("videochat");
            add("airconditioner");
            add("atmosphericpressure");
            add("gpio");
        }
    };

    private AWSIotPrefUtil mPrefUtil;
    private AWSIotWebSocketClient mAWSIotWebSocketClient;
    private AWSIotWebSocketClient.OnMessageEventListener mOnMessageEventListener;

    private DConnectHelper() {
        mDefaultHeader.put(DConnectMessage.HEADER_GOTAPI_ORIGIN, ORIGIN);

        mPrefUtil = new AWSIotPrefUtil(AWSIotDeviceApplication.getInstance());
        String accessToken = mPrefUtil.getAuthAccessToken();
        String clientId = mPrefUtil.getAuthClientId();
        if (accessToken != null && clientId != null) {
            mAuthInfo = new AuthInfo(clientId, accessToken);
        }
    }

    public void openWebSocket(final AWSIotWebSocketClient.OnMessageEventListener listener) {
        if (mAWSIotWebSocketClient != null) {
            mAWSIotWebSocketClient.close();
        }

        mOnMessageEventListener = listener;

        mAWSIotWebSocketClient = new AWSIotWebSocketClient(mPrefUtil.getAuthAccessToken());
        mAWSIotWebSocketClient.setOnMessageEventListener(listener);
        mAWSIotWebSocketClient.connect();
    }

    public void closeWebSocket() {
        if (mAWSIotWebSocketClient != null) {
            mAWSIotWebSocketClient.setOnMessageEventListener(null);
            mAWSIotWebSocketClient.close();
            mAWSIotWebSocketClient = null;
        }
    }

    public void sendRequest(final String request, final ConversionCallback conversionCallback, final FinishCallback callback) {
        try {
            String method = null;

            URIBuilder builder = new URIBuilder();
            builder.setScheme("http");
            builder.setHost("localhost");
            builder.setPort(4035);

            Map<String, String> body = new HashMap<>();
            body.put(AWSIotUtil.PARAM_SELF_FLAG, "true");
            if (mAuthInfo != null) {
                body.put(DConnectMessage.EXTRA_ACCESS_TOKEN, mAuthInfo.getAccessToken());
            }

            JSONObject jsonObject = new JSONObject(request);
            Iterator<String> it = jsonObject.keys();
            while (it.hasNext()) {
                String key = it.next();
                if (key.equals("api")) {
                    builder.setApi(jsonObject.getString(key));
                } else if (key.equals("profile")) {
                    builder.setProfile(jsonObject.getString(key));
                } else if (key.equals("interface")) {
                    builder.setInterface(jsonObject.getString(key));
                } else if (key.equals("attribute")) {
                    builder.setAttribute(jsonObject.getString(key));
                } else if (key.equals("action")) {
                    method = parseMethod(jsonObject);
                } else if (key.equals("sessionKey")) {
                    if (conversionCallback != null) {
                        body.put(key, conversionCallback.convertSessionKey(jsonObject.getString(key)));
                    } else {
                        body.put(key, jsonObject.getString(key));
                    }
                } else if (key.equals("uri")) {
                    if (conversionCallback != null) {
                        body.put(key, conversionCallback.convertUri(jsonObject.getString(key)));
                    } else {
                        body.put(key, jsonObject.getString(key));
                    }
                } else if (key.equals("requestCode")) {
                } else if (key.equals("origin")) {
                } else if (key.equals("accessToken")) {
                } else if (key.equals("_type")) {
                } else if (key.equals("_app_type")) {
                } else if (key.equals("receiver")) {
                } else if (key.equals("version")) {
                } else if (key.equals("product")) {
                } else {
                    body.put(key, jsonObject.getString(key));
                }
            }

            sendRequest(method, builder.toString(), body, callback);
        } catch (JSONException e) {
            if (callback != null) {
                callback.onFinish(errorMessage(), e);
            }
        }
    }

    public void sendRequest(final String method, final String uri, final FinishCallback callback) {
        sendRequest(method, uri, new HashMap<String, String>(), callback);
    }

    public void availability(final FinishCallback callback) {
        sendRequest("GET", "http://localhost:4035/gotapi/availability", callback);
    }

    public void serviceDiscovery(final FinishCallback callback) {
        sendRequest("GET", "http://localhost:4035/gotapi/servicediscovery", callback);
    }

    public void serviceDiscoverySelfOnly(final FinishCallback callback) {
        Map<String, String> param = new HashMap<>();
        param.put("_selfOnly", "true");
        sendRequest("GET", "http://localhost:4035/gotapi/servicediscovery", param, callback);
    }

    public void serviceInformation(final String serviceId, final FinishCallback callback) {
        Map<String, String> param = new HashMap<>();
        param.put("serviceId", serviceId);
        sendRequest("GET", "http://localhost:4035/gotapi/serviceinformation", param, callback);
    }

    private void sendRequest(final String method, final String uri, final Map<String, String> body, final FinishCallback callback) {
        new HttpTask(method, uri, mDefaultHeader, body) {
            @Override
            protected void onPostExecute(final String message) {
                if (callback != null) {
                    if (message == null) {
                        callback.onFinish(errorMessage(), null);
                    } else {
                        callback.onFinish(message, null);
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String parseMethod(final JSONObject jsonObject) throws JSONException {
        String method = jsonObject.getString("action");
        method = method.replace("org.deviceconnect.action.", "");
        return method.toLowerCase();
    }

    private String errorMessage() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result", 1);
            jsonObject.put("errorCode", 1);
            jsonObject.put("errorMessage", "");
            return jsonObject.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    private String toBody(final Map<String, String> body) {
        if (body == null) {
            return "";
        }

        String data = "";
        for (String key : body.keySet()) {
            if (data.length() > 0) {
                data += "&";
            }
            data += (key + "=" + body.get(key));
        }
        return data;
    }

    private String toScope() {
        String data = "";
        for (String scope : mScopes) {
            if (data.length() > 0) {
                data += ",";
            }
            data += scope;
        }
        return data;
    }

    private class HttpTask extends AsyncTask<Void, Void, String> {
        public String mMethod;
        public String mUri;
        public Map<String, String> mHeaders;
        public Map<String, String> mBody;

        public HttpTask(final String method, final String uri, final Map<String, String> headers, final Map<String, String> body) {
            mMethod = method.toLowerCase();
            mUri = uri;
            mHeaders = headers;
            mBody = body;
        }

        private String executeRequest() {
            if (DEBUG) {
                Log.d(TAG, "method=" + mMethod);
                Log.d(TAG, "Uri=" + mUri);
                Log.d(TAG, "Body=" + toBody(mBody));
            }

            byte[] resp = null;
            if ("get".equals(mMethod)) {
                resp = HttpUtil.get(mUri + "?" + toBody(mBody), mHeaders);
            } else if ("post".equals(mMethod)) {
                resp = HttpUtil.post(mUri, mHeaders, mBody);
            } else if ("put".equals(mMethod)) {
                resp = HttpUtil.put(mUri, mHeaders, mBody);
            } else if ("delete".equals(mMethod)) {
                resp = HttpUtil.delete(mUri + "?" + toBody(mBody), mHeaders);
            }
            return (resp != null) ? new String(resp) : null;
        }

        private String executeAccessToken(final String clientId) {

            String profile = getProfile();
            if (profile != null) {
                mScopes.add(profile);
            }

            URIBuilder builder = new URIBuilder();
            builder.setScheme("http");
            builder.setHost("localhost");
            builder.setPort(4035);
            builder.setProfile(AuthorizationProfile.PROFILE_NAME);
            builder.setAttribute(AuthorizationProfile.ATTRIBUTE_ACCESS_TOKEN);
            builder.addParameter(AuthorizationProfile.PARAM_CLIENT_ID, clientId);
            builder.addParameter(AuthorizationProfile.PARAM_SCOPE, toScope());
            builder.addParameter(AuthorizationProfile.PARAM_APPLICATION_NAME, "aws");

            byte[] data = HttpUtil.get(builder.toString(), mHeaders);
            if (data == null) {
                return null;
            }

            String response = new String(data);
            try {
                JSONObject jsonObject = new JSONObject(response);
                int result = jsonObject.getInt("result");
                if (result == 0) {
                    String accessToken = jsonObject.getString("accessToken");
                    mAuthInfo = new AuthInfo(clientId, accessToken);
                    mBody.put("accessToken", accessToken);
                    mPrefUtil.setAuthAccessToken(accessToken);
                    mPrefUtil.setAuthClientId(clientId);
                    openWebSocket(mOnMessageEventListener);
                    return executeRequest();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return response;
        }

        private String executeGrant() {
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http");
            builder.setHost("localhost");
            builder.setPort(4035);
            builder.setProfile(AuthorizationProfile.PROFILE_NAME);
            builder.setAttribute(AuthorizationProfile.ATTRIBUTE_GRANT);

            byte[] data = HttpUtil.get(builder.toString(), mHeaders);
            if (data == null) {
                return null;
            }

            String response = new String(data);
            try {
                JSONObject jsonObject = new JSONObject(response);
                int result = jsonObject.getInt("result");
                if (result == 0) {
                    String clientId = jsonObject.getString("clientId");
                    return executeAccessToken(clientId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return response;
        }

        private String getProfile() {
            Uri uri = Uri.parse(mUri);
            List<String> paths = uri.getPathSegments();
            if (paths.size() > 1) {
                return paths.get(1);
            }
            return null;
        }

        @Override
        protected String doInBackground(final Void... params) {
            String response = executeRequest();
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("result");
                    if (result == DConnectMessage.RESULT_ERROR) {
                        DConnectMessage.ErrorCode errorCode = DConnectMessage.ErrorCode.getInstance(jsonObject.getInt("errorCode"));
                        switch (errorCode) {
                            case SCOPE:
                            case AUTHORIZATION:
                            case EXPIRED_ACCESS_TOKEN:
                            case EMPTY_ACCESS_TOKEN:
                            case NOT_FOUND_CLIENT_ID:
                                String resp = executeGrant();
                                if (resp != null) {
                                    response = resp;
                                }
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }
    }

    /**
     * 認証情報.
     */
    public static class AuthInfo {
        public String mClientId;
        public String mAccessToken;

        public AuthInfo(final String clientId, final String accessToken) {
            mClientId = clientId;
            mAccessToken = accessToken;
        }

        public String getAccessToken() {
            return mAccessToken;
        }

        @Override
        public String toString() {
            return "AuthInfo{" + "mClientId=" + mClientId + ", mAccessToken=" + mAccessToken + "}";
        }
    }

    public interface ConversionCallback {
        String convertUri(String uri);
        String convertSessionKey(String sessionKey);
    }

    /** 処理完了コールバック */
    public interface FinishCallback {
        /**
         * 処理が完了した時に呼ばれます.
         * @param response レスポンス
         * @param error エラー
         */
        void onFinish(String response, Exception error);
    }
}
