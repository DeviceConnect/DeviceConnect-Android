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

import org.deviceconnect.android.deviceplugin.awsiot.util.HttpUtil;
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
    private static final boolean DEBUG = true;
    /** デバッグタグ. */
    private static final String TAG = "DConnectHelper";

    /** シングルトンなManagerのインスタンス. */
    public static final DConnectHelper INSTANCE = new DConnectHelper();

    private Map<String, String> mDefaultHeader = new HashMap<>();
    private AuthInfo mAuthInfo;
    private String mSessionKey;


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

    private DConnectHelper() {
        mDefaultHeader.put(DConnectMessage.HEADER_GOTAPI_ORIGIN, "http://localhost");
    }

    public void setSessionKey(final String sessionKey) {
        mSessionKey = sessionKey;
    }

    public void sendRequest(final String request, final FinishCallback callback) {
        try {
            String method = null;

            URIBuilder builder = new URIBuilder();
            builder.setScheme("http");
            builder.setHost("localhost");
            builder.setPort(4035);

            Map<String, String> body = new HashMap<>();

            // TODO
            body.put("awsflg", "true");

            // TODO
            if (mAuthInfo != null) {
                body.put(DConnectMessage.EXTRA_ACCESS_TOKEN, mAuthInfo.getAccessToken());
            }

            JSONObject jsonObject = new JSONObject(request);
            Iterator<String> it = jsonObject.keys();
            while (it.hasNext()) {
                String key = it.next();
                if (key.equals("profile")) {
                    builder.setProfile(jsonObject.getString(key));
                } else if (key.equals("interface")) {
                    builder.setInterface(jsonObject.getString(key));
                } else if (key.equals("attribute")) {
                    builder.setAttribute(jsonObject.getString(key));
                } else if (key.equals("method")) {
                    method = parseMethod(jsonObject);
                } else if (key.equals("sessionKey")) {
                    body.put(key, mSessionKey);
                } else if (key.equals("requestCode")) {
                } else if (key.equals("origin")) {
                } else if (key.equals("accessToken")) {
                } else if (key.equals("_type")) {
                } else if (key.equals("_app_type")) {
                } else if (key.equals("receiver")) {
                } else if (key.equals("version")) {
                } else if (key.equals("api")) {
                } else if (key.equals("product")) {
                } else {
                    body.put(key, jsonObject.getString(key));
                }
            }

            sendRequest(method, builder.toString(), body, callback);
        } catch (JSONException e) {
            if (callback != null) {
                callback.onFinish(null, e);
            }
        }
    }

    public void sendRequest(final String method, final String uri, final FinishCallback callback) {
        sendRequest(method, uri, null, callback);
    }

    private void sendRequest(final String method, final String uri, final Map<String, String> body, final FinishCallback callback) {
        new HttpTask(method, uri, mDefaultHeader, body) {
            @Override
            protected void onPostExecute(final String message) {
                if (callback != null) {
                    callback.onFinish(message, null);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String parseMethod(final JSONObject jsonObject) throws JSONException {
        String method = jsonObject.getString("method");
        method = method.replace("org.deviceconnect.action.", "");
        return method.toLowerCase();
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

            // TODO scopeを検討
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
     * 認証情報
     */
    public static class AuthInfo {
        public String mClientId;
        public String mAccessToken;

        public AuthInfo(final String clientId, final String accessToken) {
            mClientId = clientId;
            mAccessToken = accessToken;
        }

        public String getClientId() {
            return mClientId;
        }

        public String getAccessToken() {
            return mAccessToken;
        }

        @Override
        public String toString() {
            return "AuthInfo{" + "mClientId=" + mClientId + ", mAccessToken=" + mAccessToken + "}";
        }
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
