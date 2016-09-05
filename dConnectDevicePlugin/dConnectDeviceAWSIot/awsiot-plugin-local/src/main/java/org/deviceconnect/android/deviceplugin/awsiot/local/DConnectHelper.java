/*
 DConnectHelper.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.os.AsyncTask;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.util.HttpUtil;
import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * DeviceConnectヘルパークラス.
 */
public class DConnectHelper {

    /** シングルトンなManagerのインスタンス */
    public static final DConnectHelper INSTANCE = new DConnectHelper();

    /** デバッグタグ */
    private static final String TAG = "DConnectHelper";

    /** 処理完了コールバック */
    public interface FinishCallback {
        /**
         * 処理が完了した時に呼ばれます.
         * @param response レスポンス
         * @param error エラー
         */
        void onFinish(String response, Exception error);
    }

    private Map<String, String> mDefaultHeader = new HashMap<>();
    private AuthInfo mAuthInfo;

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
            return "AuthInfo{" + "mClientId='" + mClientId + '\'' + ", mAccessToken='" + mAccessToken + '\'' + '}';
        }
    }

    private DConnectHelper() {
        mDefaultHeader.put(DConnectMessage.HEADER_GOTAPI_ORIGIN, "http://localhost");
    }

    private String parseMethod(final JSONObject jsonObject) throws JSONException {
        String method = jsonObject.getString("method");
        method = method.replace("org.deviceconnect.action.", "");
        return method.toLowerCase();
    }

    public void sendRequest(final String request, final FinishCallback callback) {
        int requestCode = 0;
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
                    builder.setProfile(jsonObject.getString(key));
                } else if (key.equals("attribute")) {
                    builder.setProfile(jsonObject.getString(key));
                } else if (key.equals("method")) {
                    method = parseMethod(jsonObject);
                } else if (key.equals("requestCode")) {
                    requestCode = jsonObject.getInt(key);
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

            DConnectHelper.INSTANCE.sendRequest(method, builder.toString(), body, callback);
        } catch (JSONException e) {
            if (callback != null) {
                callback.onFinish(null, e);
            }
        }
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

    private String toBody(final Map<String, String> body) {
        String data = "";
        for (String key : body.keySet()) {
            if (data.length() > 0) {
                data += "&";
            }
            data += (key + "=" + body.get(key));
        }
        return data;
    }

    private class HttpTask extends AsyncTask<Void, Void, String> {
        public String mMethod;
        public String mUri;
        public Map<String, String> mHeaders;
        public Map<String, String> mBody;

        public HttpTask(final String method, final String uri, final Map<String, String> headers, final Map<String, String> body) {
            mMethod = method;
            mUri = uri;
            mHeaders = headers;
            mBody = body;
        }

        private String executeRequest() {
            Log.d(TAG, "method=" + mMethod);
            Log.d(TAG, "Uri=" + mUri);
            Log.d(TAG, "Body=" + toBody(mBody));

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
            // TODO scopeを検討
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http");
            builder.setHost("localhost");
            builder.setPort(4035);
            builder.setProfile(AuthorizationProfile.PROFILE_NAME);
            builder.setAttribute(AuthorizationProfile.ATTRIBUTE_ACCESS_TOKEN);
            builder.addParameter(AuthorizationProfile.PARAM_CLIENT_ID, clientId);
            builder.addParameter(AuthorizationProfile.PARAM_SCOPE, "serviceDiscovery,vibration,serviceInformation");
            builder.addParameter(AuthorizationProfile.PARAM_APPLICATION_NAME, "aws");

            byte[] a = HttpUtil.get(builder.toString(), mHeaders);
            if (a == null) {
                return null;
            }

            String response = new String(a);
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

            byte[] a = HttpUtil.get(builder.toString(), mHeaders);
            if (a == null) {
                return null;
            }

            String response = new String(a);
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
                            case AUTHORIZATION:
                            case EXPIRED_ACCESS_TOKEN:
                            case EMPTY_ACCESS_TOKEN:
                            case SCOPE:
                            case NOT_FOUND_CLIENT_ID:
                                response = executeGrant();
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
}
