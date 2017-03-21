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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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
            add("airconditioner");
            add("atmosphericpressure");
            add("battery");
            add("camera");
            add("canvas");
            add("connection");
            add("deviceorientation");
            add("drivecontroller");
            add("ecg");
            add("echonetLite");
            add("file");
            add("filedescriptor");
            add("geolocation");
            add("gpio");
            add("health");
            add("humandetection");
            add("humidity");
            add("illuminance");
            add("keyevent");
            add("light");
            add("mediaplayer");
            add("mediastreamrecording");
            add("messagehook");
            add("notification");
            add("omnidirectionalimage");
            add("phone");
            add("poseestimation");
            add("power");
            add("powermeter");
            add("proximity");
            add("remotecontroller");
            add("servicediscovery");
            add("serviceinformation");
            add("setting");
            add("sphero");
            add("stressestimation");
            add("system");
            add("temperature");
            add("touch");
            add("tv");
            add("vibration");
            add("videochat");
            add("walkstate");
        }
    };

    private AWSIotPrefUtil mPrefUtil;

    private boolean mSSL;
    private String mHost = "localhost";
    private int mPort = 4035;
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

    /**
     * SSLフラグを取得する.
     * @return SSLが有効の場合はtrue、それ以外はfalse
     */
    public boolean isSSL() {
        return mSSL;
    }

    /**
     * SSLフラグを設定する.
     * @param SSL SSLが有効の場合はtrue、それ以外はfalse
     */
    public void setSSL(final boolean SSL) {
        mSSL = SSL;
    }

    /**
     * Host名を取得する.
     * @return ホスト名
     */
    public String getHost() {
        return mHost;
    }

    /**
     * Host名を設定する.
     * @param host ホスト名
     */
    public void setHost(final String host) {
        mHost = host;
    }

    /**
     * ポート番号を取得する.
     * @return ポート番号
     */
    public int getPort() {
        return mPort;
    }

    /**
     * ポート番号を設定する.
     * @param port ポート番号
     */
    public void setPort(int port) {
        mPort = port;
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
    public void getSystem(final FinishCallback callback) {
        Map<String, String> param = new HashMap<>();
        if (mAuthInfo != null) {
            String accessToken = mAuthInfo.getAccessToken();
            param.put("accessToken", accessToken);
        }
        sendRequest("GET", "http://localhost:4035/gotapi/system", param, callback);
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
                    mPrefUtil.setAuthAccessToken(accessToken);
                    mPrefUtil.setAuthClientId(clientId);
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

    private class URIBuilder {

        /**
         * スキーム.
         */
        private String mScheme = isSSL() ? "https" : "http";

        /**
         * ホスト.
         */
        private String mHost = DConnectHelper.this.getHost();

        /**
         * ポート番号.
         */
        private int mPort = DConnectHelper.this.getPort();

        /**
         * パス.
         */
        private String mPath;

        /**
         * パラメータ.
         */
        private Map<String, String> mParameters = new HashMap<>();

        /**
         * API.
         */
        private String mApi = DConnectMessage.DEFAULT_API;

        /**
         * プロファイル.
         */
        private String mProfile;

        /**
         * インターフェース.
         */
        private String mInterface;

        /**
         * アトリビュート.
         */
        private String mAttribute;

        /**
         * コンストラクタ.
         */
        URIBuilder() {
        }

        /**
         * URIから {@link URIBuilder} クラスを生成する.
         *
         * @param uri URI
         * @throws URISyntaxException URIフォーマットが不正な場合
         */
        URIBuilder(final String uri) throws URISyntaxException {
            this(new URI(uri));
        }

        /**
         * URIから {@link URIBuilder} クラスを生成する.
         *
         * @param uri URI
         */
        URIBuilder(final URI uri) {
            mScheme = uri.getScheme();
            mHost = uri.getHost();
            mPort = uri.getPort();
            mPath = uri.getPath();

            String query = uri.getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] splitted = param.split("=");
                    if (splitted.length == 2) {
                        addParameter(splitted[0], splitted[1]);
                    } else {
                        addParameter(splitted[0], "");
                    }
                }
            }
        }

        @Override
        public synchronized String toString() {
            return toString(true);
        }

        /**
         * スキームを取得する.
         *
         * @return スキーム
         */
        public synchronized String getScheme() {
            return mScheme;
        }

        /**
         * スキームを設定する.
         *
         * @param scheme スキーム
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setScheme(final String scheme) {
            mScheme = scheme;
            return this;
        }

        /**
         * ホスト名を取得する.
         *
         * @return ホスト名
         */
        public synchronized String getHost() {
            return mHost;
        }

        /**
         * ホスト名を設定する.
         *
         * @param host ホスト名
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setHost(final String host) {
            if (host == null) {
                throw new NullPointerException("host is null.");
            }
            if (host.isEmpty()) {
                throw new IllegalArgumentException("host is empty.");
            }
            mHost = host;
            return this;
        }

        /**
         * ポート番号を取得する. ポート番号が指定されていない場合は-1を返す
         *
         * @return ポート番号
         */
        public synchronized int getPort() {
            return mPort;
        }

        /**
         * ポート番号を設定する.
         *
         * @param port ポート番号
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setPort(final int port) {
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("port is invalid. port=" + port);
            }
            mPort = port;
            return this;
        }

        /**
         * パスを取得する.
         *
         * @return パス
         */
        public synchronized String getPath() {
            return mPath;
        }

        /**
         * APIのパスを文字列で設定する.
         * このパラメータが設定されている場合はビルド時に api、profile、interface、attribute は無視される。
         *
         * @param path パス
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setPath(final String path) {
            mPath = path;
            return this;
        }

        /**
         * APIを取得する.
         *
         * @return API
         */
        public synchronized String getApi() {
            return mApi;
        }

        /**
         * APIを取得する.
         * パスが設定されている場合には、このパラメータは無視される。
         *
         * @param api API
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setApi(final String api) {
            mApi = api;
            return this;
        }

        /**
         * プロファイルを取得する.
         *
         * @return プロファイル
         */
        public synchronized String getProfile() {
            return mProfile;
        }

        /**
         * プロファイルを設定する.
         * <p>
         * パスが設定されている場合には、このパラメータは無視される。
         *
         * @param profile プロファイル
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setProfile(final String profile) {
            mProfile = profile;
            return this;
        }

        /**
         * インターフェースを取得する.
         *
         * @return インターフェース
         */
        public synchronized String getInterface() {
            return mInterface;
        }

        /**
         * インターフェースを設定する.
         * <p>
         * パスが設定されている場合には、このパラメータは無視される。
         *
         * @param inter インターフェース
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setInterface(final String inter) {
            mInterface = inter;
            return this;
        }

        /**
         * アトリビュートを取得する.
         *
         * @return アトリビュート
         */
        public synchronized String getAttribute() {
            return mAttribute;
        }

        /**
         * アトリビュートを設定する.
         * <p>
         * {@link #setPath}でパスが設定されている場合には、このパラメータは無視される。
         *
         * @param attribute アトリビュート
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setAttribute(final String attribute) {
            mAttribute = attribute;
            return this;
        }

        /**
         * サービスIDを設定する.
         * @param serviceId サービスID
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder setServiceId(final String serviceId) {
            addParameter(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
            return this;
        }

        /**
         * サービスIDを取得する.
         * <p>
         * 設定されていない場合にはnullを返却する。
         * </p>
         * @return サービスID
         */
        public synchronized String getServiceId() {
            return getParameter(DConnectMessage.EXTRA_SERVICE_ID);
        }

        /**
         * 指定したクエリパラメータを取得する.
         * <p>
         * 指定されたクエリパラメータが存在しない場合にはnullを返却する。
         * </p>
         * @param name クエリパラメータ名
         * @return クエリパラメータ
         */
        public synchronized String getParameter(final String name) {
            return mParameters.get(name);
        }

        /**
         * キーバリューでクエリパラメータを追加する.
         *
         * @param key  キー
         * @param value バリュー
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder addParameter(final String key, final String value) {
            if (key == null) {
                throw new NullPointerException("key is null.");
            }
            if (value == null) {
                throw new NullPointerException("value is null.");
            }
            mParameters.put(key, value);
            return this;
        }

        /**
         * 指定されたクエリパラメータを削除する.
         * @param key クエリパラメータ名
         * @return {@link URIBuilder} インスタンス
         */
        public synchronized URIBuilder removeParameter(final String key) {
            if (key == null) {
                throw new NullPointerException("key is null.");
            }
            mParameters.remove(key);
            return this;
        }

        /**
         * {@link Uri} オブジェクトを取得する.
         *
         * @return {@link Uri} オブジェクト
         */
        public Uri build() {
            return Uri.parse(toString(true));
        }

        /**
         * URIを文字列にして取得する.
         *
         * @param ascii ASCII変換の有無
         * @return URIを表す文字列
         */
        private synchronized String toString(final boolean ascii) {
            StringBuilder builder = new StringBuilder();

            if (mScheme != null) {
                builder.append(mScheme);
                builder.append("://");
            }
            if (mHost != null) {
                builder.append(mHost);
            }
            if (mPort > 0) {
                builder.append(":");
                builder.append(mPort);
            }
            if (mPath != null) {
                builder.append(mPath);
            } else {
                if (mApi != null) {
                    builder.append("/");
                    builder.append(mApi);
                }
                if (mProfile != null) {
                    builder.append("/");
                    builder.append(mProfile);
                }
                if (mInterface != null) {
                    builder.append("/");
                    builder.append(mInterface);
                }
                if (mAttribute != null) {
                    builder.append("/");
                    builder.append(mAttribute);
                }
            }

            if (mParameters != null && mParameters.size() > 0) {
                if (ascii) {
                    builder.append("?");
                    builder.append(concatenateStringWithEncode(mParameters, "UTF-8"));
                } else {
                    builder.append("?");
                    builder.append(concatenateString(mParameters));
                }
            }

            return builder.toString();
        }

        private String concatenateString(final Map<String, String> map) {
            String string = "";
            for (Map.Entry<String, String> e : map.entrySet()) {
                if (string.length() > 0) {
                    string += "&";
                }
                string += e.getKey() + "=" + e.getValue();
            }
            return string;
        }

        private String concatenateStringWithEncode(final Map<String, String> map, final String charset) {
            try {
                String string = "";
                for (Map.Entry<String, String> e : map.entrySet()) {
                    if (string.length() > 0) {
                        string += "&";
                    }
                    string += e.getKey() + "=" + URLEncoder.encode(e.getValue(), charset);
                }
                return string;
            } catch (UnsupportedEncodingException e) {
                return "";
            }
        }
    }
}
