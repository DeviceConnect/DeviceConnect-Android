/*
 DConnectHelper.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.basic.message.DConnectResponseMessage;
import org.deviceconnect.message.http.event.CloseHandler;
import org.deviceconnect.message.http.event.HttpEventManager;
import org.deviceconnect.message.http.impl.client.HttpDConnectClient;
import org.deviceconnect.message.http.impl.factory.HttpMessageFactory;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.utils.AuthProcesser;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * dConnectヘルパークラス
 */
public class DConnectHelper {

    //region Declaration
    //---------------------------------------------------------------------------------------

    /** シングルトンなManagerのインスタンス */
    public static final DConnectHelper INSTANCE = new DConnectHelper();

    /** デバッグタグ */
    private static final String TAG = "DConnectHelper";
    /** デバッグフラグ */
    private static final boolean DEBUG = true;

    /** DConnectHelperの基底Exception */
    public abstract class DConnectHelperException extends Exception {}
    /** Resultが不正 */
    public class DConnectInvalidResultException extends DConnectHelperException {}
    /** 認証失敗 */
    public class DConnectAuthFailedException extends DConnectHelperException {}

    /** 接続先情報 */
    private HttpHost targetHost = new HttpHost("localhost", 4035, "http");
    /**  */
    private String origin;

    /** イベントハンドラー */
    private EventHandler eventHandler = null;

    /** 処理完了コールバック */
    public interface FinishCallback<Result> {
        /**
         * 処理が完了した時に呼ばれます.
         * @param result 結果
         * @param error エラー
         */
        void onFinish(Result result, Exception error);
    }

    /** イベントハンドラー */
    public interface EventHandler {
        /**
         * イベントが発生した時に呼ばれます.
         * @param event イベント
         */
        void onEvent(JSONObject event);
    }

    /**
     * Serviceの情報
     */
    public static class ServiceInfo {
        public String id;
        public String name;
        public List<String> scopes;
        public ServiceInfo(String id, String name, List<String> scopes) {
            this.id = id;
            this.name = name;
            this.scopes = scopes;
        }
        @Override
        public String toString() {
            return "ServiceInfo{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", scopes='" + scopes.toString() + '\'' + '}';
        }
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
     * イベントハンドラーを設定する
     * @param handler ハンドラー
     */
    public void setEventHandler(EventHandler handler) {
        this.eventHandler = handler;
    }

    /**
     * 接続先情報を設定する
     * @param ssl SSL通信を行う場合true
     * @param host ホスト名
     * @param port ポート番号
     * @param origin Origin
     */
    public void setHostInfo(boolean ssl, String host, int port, String origin) {
        String scheme;
        if (ssl) {
            scheme = "https";
        } else {
            scheme = "http";
        }
        targetHost = new HttpHost(host, port, scheme);
        this.origin = origin;
    }

    /**
     * ServiceDiscoveryを実行
     * @param accessToken AccessToken
     * @param callback コールバック
     */
    public void serviceDiscovery(String accessToken, final FinishCallback<List<ServiceInfo>> callback) {
        if (DEBUG) Log.d(TAG, "serviceDiscovery");
        // 接続情報
        ConnectionParam connectionParam = new ConnectionParam(
                targetHost,
                "GET",
                ServiceDiscoveryProfileConstants.PROFILE_NAME,
                null,
                origin
        );
        // パラメータ
        Map<String, String> params = new HashMap<>();
        params.put(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
        // 接続
        new HttpTask().execute(new TaskParam(connectionParam, params) {
            @Override
            public void callBack(DConnectMessage message) {
                // コールバックがないので処理する意味がない
                if (callback == null) {
                    return;
                }
                // エラーチェック
                int result = message.getInt(DConnectMessage.EXTRA_RESULT);
                if (result == DConnectMessage.RESULT_ERROR) {
                    callback.onFinish(null, new DConnectInvalidResultException());
                    return;
                }
                // サービスリストを取得
                List<Object> services = message.getList(
                        ServiceDiscoveryProfileConstants.PARAM_SERVICES);
                if (services == null) {
                    // サービスがない？
                    callback.onFinish(null, null);
                    return;
                }
                // 詰め直しして返却
                List<ServiceInfo> list = new ArrayList<>();
                for (Object object: services) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> service = (Map<String, Object>) object;
                    @SuppressWarnings("unchecked")
                    ServiceInfo info = new ServiceInfo(
                            service.get(ServiceDiscoveryProfileConstants.PARAM_ID).toString(),
                            service.get(ServiceDiscoveryProfileConstants.PARAM_NAME).toString(),
                            (List<String>) service.get(ServiceDiscoveryProfileConstants.PARAM_SCOPES));
                    list.add(info);
                }
                callback.onFinish(list, null);
            }
        });
    }

    /**
     * 認証
     * @param appName アプリ名
     * @param clientId ClientID
     * @param scopes スコープ
     * @param callback 完了コールバック
     */
    public void auth(String appName, String clientId, String[] scopes, final FinishCallback<AuthInfo> callback) {
        if (DEBUG) Log.d(TAG, "auth");
        boolean isSSL = "https".equals(targetHost.getSchemeName());
        String host = targetHost.getHostName();
        int port = targetHost.getPort();

        final Handler handler = new Handler();
        final AuthProcesser.AuthorizationHandler authHandler = new AuthProcesser.AuthorizationHandler() {
            @Override
            public void onAuthorized(final String clientId, final String accessToken) {
                if (callback != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFinish(new AuthInfo(clientId, accessToken), null);
                        }
                    });
                }
            }
            @Override
            public void onAuthFailed(DConnectMessage.ErrorCode error) {
                if (callback != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFinish(null, new DConnectAuthFailedException());
                        }
                    });
                }
            }
        };

        if (clientId == null) {
            AuthProcesser.asyncAuthorize(host, port, isSSL, origin, appName, scopes, authHandler);
        } else {
            AuthProcesser.asyncRefreshToken(host, port, isSSL, clientId, origin, appName, scopes, authHandler);
        }
    }

    /**
     * イベント登録
     * @param profile Profile
     * @param attribute Attribute
     * @param serviceId ServiceID
     * @param accessToken AccessToken
     * @param clientId ClientID
     * @param callback Callback
     */
    public void registerEvent(String profile, String attribute, String serviceId, String accessToken, String clientId, final FinishCallback<Void> callback) {
        if (DEBUG) Log.d(TAG, "registerEvent:" + profile + ":" + attribute);
        // 接続情報
        ConnectionParam connectionParam = new ConnectionParam(
                targetHost,
                "PUT",
                profile,
                attribute,
                origin
        );
        // パラメータ
        Map<String, String> params = new HashMap<>();
        params.put(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
        params.put(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
        params.put(DConnectMessage.EXTRA_SESSION_KEY, clientId);
        // 接続
        new EventTask().execute(new TaskParam(connectionParam, params) {
            @Override
            public void callBack(DConnectMessage message) {
                // コールバックがないので処理する意味がない
                if (callback == null) {
                    return;
                }
                // エラーチェック
                int result = message.getInt(DConnectMessage.EXTRA_RESULT);
                if (result == DConnectMessage.RESULT_ERROR) {
                    callback.onFinish(null, new DConnectInvalidResultException());
                    return;
                }
                callback.onFinish(null, null);
            }
        });
    }

    /**
     * メッセージ送信
     * @param serviceId ServiceID
     * @param accessToken AccessToken
     * @param channelId ChannelID
     * @param text Text
     */
    public void sendMessage(String serviceId, String accessToken, String channelId, String text, final FinishCallback<Void> callback) {
        if (DEBUG) Log.d(TAG, "sendMessage:" + channelId + ":" + text);
        // 接続情報
        ConnectionParam connectionParam = new ConnectionParam(
                targetHost,
                "POST",
                "messageHook",
                "message",
                origin
        );
        // パラメータ
        Map<String, String> params = new HashMap<>();
        params.put(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
        params.put(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
        params.put("text", text);
        params.put("channelId", channelId);
        // 接続
        new HttpTask().execute(new TaskParam(connectionParam, params) {
            @Override
            public void callBack(DConnectMessage message) {
                // コールバックがないので処理する意味がない
                if (callback == null) {
                    return;
                }
                // エラーチェック
                int result = message.getInt(DConnectMessage.EXTRA_RESULT);
                if (result == DConnectMessage.RESULT_ERROR) {
                    callback.onFinish(null, new DConnectInvalidResultException());
                    return;
                }
                callback.onFinish(null, null);
            }
        });
    }

    /**
     * リクエスト送信
     * @param method Method
     * @param path Path
     * @param serviceId ServiceID
     * @param accessToken AccessToken
     * @param params パラメータ
     * @param callback Callback
     */
    public void sendRequest(String method, String path, String serviceId, String accessToken, Map<String, String> params, final FinishCallback<Map<String, Object>> callback) {
        if (DEBUG) Log.d(TAG, "sendRequest");
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
        params.put(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
        params.put(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
        // 接続
        new HttpTask().execute(new TaskParam(connectionParam, params) {
            @Override
            public void callBack(DConnectMessage message) {
                // コールバックがないので処理する意味がない
                if (callback == null) {
                    return;
                }
                // エラーチェック
                int result = message.getInt(DConnectMessage.EXTRA_RESULT);
                if (result == DConnectMessage.RESULT_ERROR) {
                    callback.onFinish(message, new DConnectInvalidResultException());
                    return;
                }
                callback.onFinish(message, null);
            }
        });
    }

    /**
     * WebSocketを開く
     * @param sessionKey SessionKey
     * @return 成功時true
     */
    public boolean openWebsocket(String sessionKey) {
        boolean isSSL = "https".equals(targetHost.getSchemeName());
        String host = targetHost.getHostName();
        int port = targetHost.getPort();

        if (sessionKey == null) {
            sessionKey = "dummy_key";
        }
        return HttpEventManager.INSTANCE.connect(host, port, isSSL, sessionKey, new CloseHandler() {
            @Override
            public void onClosed() {
                Log.d(TAG, "ws closed");
                // TODO: 再接続処理
            }
        });
    }

    /**
     * WebSocketを閉じる
     */
    public void closeWebsocket() {
        HttpEventManager.INSTANCE.disconnect();
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
        public ConnectionParam(HttpHost host, String method, String profileName, String attributeName, String origin) {
            this.host = host;
            this.method = method;
            this.profileName = profileName;
            this.attributeName = attributeName;
            this.origin = origin;
        }
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
        public void callBack(DConnectMessage message) {}

        public TaskParam(ConnectionParam url, Map<String, String > params) {
            this.connection = url;
            this.params = params;
        }
    }

    /**
     * Http用Task
     */
    private class HttpTask extends AsyncTask<TaskParam, Void, DConnectMessage> {

        /** パラメータ */
        TaskParam param = null;

        /**
         * Background処理.
         *
         * @param params Params
         */
        @Override
        protected DConnectMessage doInBackground(TaskParam... params) {

            param = params[0];
            DConnectMessage message = new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
            try {
                // パラメータ
                URIBuilder builder = new URIBuilder();
                ConnectionParam connectionParam = param.connection;
                if (connectionParam.path == null) {
                    builder.setProfile(connectionParam.profileName);
                    builder.setAttribute(connectionParam.attributeName);
                } else {
                    builder.setPath(connectionParam.path);
                }
                for (String key: param.params.keySet()) {
                    builder.addParameter(key, param.params.get(key));
                }
                // 接続
                HttpUriRequest request;
                switch (connectionParam.method) {
                    case "POST":
                        request = new HttpPost(builder.build());
                        break;
                    case "PUT":
                        request = new HttpPut(builder.build());
                        break;
                    case "DELETE":
                        request = new HttpDelete(builder.build());
                        break;
                    default:
                        request = new HttpGet(builder.build());
                }
                if (DEBUG) Log.d(TAG, request.getURI().toString());
                if (connectionParam.origin != null) {
                    request.addHeader(DConnectMessage.HEADER_GOTAPI_ORIGIN, connectionParam.origin);
                }
                HttpClient client = new HttpDConnectClient();
                HttpResponse response = client.execute(connectionParam.host, request);
                if (response.getStatusLine().getStatusCode() == 200) {
                    message = (new HttpMessageFactory()).newDConnectMessage(response);
                } else {
                    Log.e(TAG, response.getStatusLine().toString());
                }
            } catch (URISyntaxException | IOException e) {
                Log.e(TAG, "error", e);
            }
            if (DEBUG) Log.d(TAG, message.toString());
            return message;
        }

        @Override
        protected void onPostExecute(DConnectMessage message) {
            param.callBack(message);
        }
    }

    /**
     * EventTask
     */
    private class EventTask extends AsyncTask<TaskParam, Void, DConnectMessage> {

        /** パラメータ */
        TaskParam param = null;

        /**
         * Background処理.
         *
         * @param params Params
         */
        @Override
        protected DConnectMessage doInBackground(TaskParam... params) {

            param = params[0];
            DConnectMessage message = new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
            // パラメータ
            URIBuilder builder = new URIBuilder();
            ConnectionParam connectionParam = param.connection;
            builder.setScheme(connectionParam.host.getSchemeName());
            builder.setHost(connectionParam.host.getHostName());
            builder.setPort(connectionParam.host.getPort());
            builder.setProfile(connectionParam.profileName);
            builder.setAttribute(connectionParam.attributeName);
            for (String key: param.params.keySet()) {
                builder.addParameter(key, param.params.get(key));
            }
            if (connectionParam.origin != null) {
                HttpEventManager.INSTANCE.setOrigin(connectionParam.origin);
            }
            // 接続
            try {
                HttpResponse response;
                if (connectionParam.method.equals("PUT")) {
                    // イベント登録
                    response = HttpEventManager.INSTANCE.registerEvent(builder, new org.deviceconnect.message.event.EventHandler() {
                        @Override
                        public void onEvent(final JSONObject event) {
                            if (event != null && eventHandler != null) {
                                eventHandler.onEvent(event);
                            }
                        }
                    });
                } else {
                    // イベント解除
                    response = HttpEventManager.INSTANCE.unregisterEvent(builder);
                }
                message = (new HttpMessageFactory()).newDConnectMessage(response);
            } catch (IOException e) {
                Log.e(TAG, "error", e);
            }
            return message;
        }

        @Override
        protected void onPostExecute(DConnectMessage message) {
            if (DEBUG) Log.d(TAG, message.toString());
            param.callBack(message);
        }
    }

    //endregion
    //---------------------------------------------------------------------------------------

}
