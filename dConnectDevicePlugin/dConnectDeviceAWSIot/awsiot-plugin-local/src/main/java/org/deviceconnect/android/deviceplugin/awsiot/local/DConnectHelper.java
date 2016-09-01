/*
 DConnectHelper.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpHost;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.utils.URIBuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * DeviceConnectヘルパークラス.
 */
public class DConnectHelper {

    //region Declaration
    //---------------------------------------------------------------------------------------

    /** シングルトンなManagerのインスタンス */
    public static final DConnectHelper INSTANCE = new DConnectHelper();

    /** デバッグタグ */
    private static final String TAG = "DConnectHelper";

    /** 接続先情報 */
    private HttpHost targetHost = new HttpHost("localhost", 4035, "http");
    /**  */
    private String origin;

//    /** イベントハンドラー */
//    private EventHandler eventHandler = null;

    /** 処理完了コールバック */
    public interface FinishCallback<Result> {
        /**
         * 処理が完了した時に呼ばれます.
         * @param result 結果
         * @param error エラー
         */
        void onFinish(Result result, Exception error);
    }

    /**
     * 認証情報
     */
    public static class AuthInfo {
        public String clientId;
        public String accessToken;
        public AuthInfo(String clientId, String accessToken) {
            this.clientId = clientId;
            this.accessToken = accessToken;
        }
        @Override
        public String toString() {
            return "AuthInfo{" + "clientId='" + clientId + '\'' + ", accessToken='" + accessToken + '\'' + '}';
        }
    }

    //endregion
    //---------------------------------------------------------------------------------------
    //region Methods


    /**
     * リクエスト送信
     * @param method Method
     * @param path Path
     * @param serviceId ServiceID
     * @param accessToken AccessToken
     * @param params パラメータ
     * @param callback Callback
     */
    public void sendRequest(String method, String path, String serviceId, String accessToken, Map<String, String> params, final FinishCallback<String> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "sendRequest");
        // 接続情報
        ConnectionParam connectionParam = new ConnectionParam(
                targetHost,
                method,
                path,
                origin
        );
        // パラメータ
        if (params == null) {
            params = new HashMap<>();
        }
        if (accessToken != null) {
            params.put(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
        }
        if (serviceId != null) {
            params.put(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
        }
        // 接続
        new HttpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TaskParam(connectionParam, params) {
            @Override
            public void callBack(String message) {
                if (callback != null) {
                    callback.onFinish(message, null);
                }
            }
        });
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region Tasks

    /**
     * 接続情報
     */
    private class ConnectionParam {
        public HttpHost host;
        public String profileName;
        public String attributeName;
        public String origin;
        public String method;
        public String path;
        public ConnectionParam(HttpHost host, String method, String path, String origin) {
            this.host = host;
            this.method = method;
            this.path = path;
            this.origin = origin;
        }
    }

    /**
     * Task用パラメータ
     */
    private class TaskParam {
        /** 接続情報 */
        public ConnectionParam connection;
        /** その他パラメータ */
        public Map<String, String> params;
        /** コールバック */
        public void callBack(String message) {}

        public TaskParam(ConnectionParam url, Map<String, String > params) {
            this.connection = url;
            this.params = params;
        }
    }

    /**
     * Http用Task
     */
    private class HttpTask extends AsyncTask<TaskParam, Void, String> {

        /** パラメータ */
        TaskParam param = null;

        /**
         * Background処理.
         *
         * @param params Params
         */
        @Override
        protected String doInBackground(TaskParam... params) {

            param = params[0];
//            DConnectMessage message = new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
            String message = null;

            HttpURLConnection con = null;
            InputStream stream = null;

            try {
                // URI作成
                URIBuilder builder = new URIBuilder();
                ConnectionParam connectionParam = param.connection;
                builder.setScheme(connectionParam.host.getSchemeName());
                builder.setHost(connectionParam.host.getHostName());
                builder.setPort(connectionParam.host.getPort());
                if (connectionParam.path == null) {
                    builder.setProfile(connectionParam.profileName);
                    builder.setAttribute(connectionParam.attributeName);
                } else {
                    builder.setPath(connectionParam.path);
                }
                // Query作成
                StringBuilder query = new StringBuilder();
                boolean isQuery = connectionParam.method.equals("GET") ||
                        connectionParam.method.equals("DELETE");
                if (isQuery) {
                    // GET/DELETEはQueryをURIに付加
                    for (String key: param.params.keySet()) {
                        builder.addParameter(key, param.params.get(key));
                    }
                } else {
                    // 他はQueryをBodyに
                    for (String key: param.params.keySet()) {
                        if (query.length() > 0) {
                            query.append("&");
                        }
                        query.append(key);
                        query.append("=");
                        query.append(URLEncoder.encode(param.params.get(key), "UTF-8"));
                    }
                }
                URL url = builder.build().toURL();
                if (BuildConfig.DEBUG) Log.d(TAG, url.toString());

                // 接続
                con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod(connectionParam.method);
                con.setInstanceFollowRedirects(false);

                // Origin
                if (connectionParam.origin != null) {
                    con.setRequestProperty(DConnectMessage.HEADER_GOTAPI_ORIGIN, connectionParam.origin);
                }

                if (!isQuery) {
                    // POSTなどはQueryをBodyに
                    con.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");
                    con.setDoOutput(true);
                    BufferedOutputStream bo = new BufferedOutputStream(con.getOutputStream());
                    DataOutputStream wr = new DataOutputStream(bo);
                    wr.writeBytes (query.toString());
                    wr.flush ();
                    wr.close ();
                    if (BuildConfig.DEBUG) Log.d(TAG, query.toString());
                }

                // 接続
                con.connect();

                // レスポンス取得
                stream = con.getInputStream();
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
                if (con.getResponseCode() == 200) {
//                    message = new BasicDConnectMessage(responseStrBuilder.toString());
                    message = responseStrBuilder.toString();
                } else {
                    Log.e(TAG, "Invalid response code:" + con.getResponseCode());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error on HttpConnection", e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "stream close error", e);
                    }
                }
                if (con != null) {
                    con.disconnect();
                }
            }
//            if (BuildConfig.DEBUG) Log.d(TAG, message.toString());
            return message;
        }

        @Override
        protected void onPostExecute(String message) {
            param.callBack(message);
        }
    }

    //endregion
    //---------------------------------------------------------------------------------------

}
