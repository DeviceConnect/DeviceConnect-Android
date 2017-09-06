/*
 DConnectHelper.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.deviceconnect.android.app.simplebot.BuildConfig;
import org.deviceconnect.android.app.simplebot.R;
import org.deviceconnect.android.app.simplebot.data.SettingData;
import org.deviceconnect.message.DConnectEventMessage;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.message.DConnectSDKFactory;
import org.deviceconnect.message.entity.MultipartEntity;
import org.deviceconnect.message.entity.StringEntity;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    /** イベントハンドラー */
    private EventHandler eventHandler = null;

    /** Device Connect ManagerへアクセスするためのSDK. */
    private DConnectSDK mDConnectSDK;

    /** コンテキスト. */
    private Context mContext;

    /**
     * リトライを行う間隔(SECOND).
     */
    private static final int RETRY_INTERVAL = 10;

    /**
     * イベントが有効化フラグ.
     * イベントが有効の場合にはtrue、それ以外はfalse。
     */
    private boolean mActiveWebSocket;

    /**
     * リトライを行うためのスレッド管理クラス.
     */
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

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
        void onEvent(DConnectEventMessage event);
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
     * APIの情報
     */
    public static class APIInfo {
        public String name;
        public String method;
        public String path;
        public List<APIParam> params;
    }

    /**
     * APIパラメータ情報
     */
    public static class APIParam {
        public String type;
        public boolean required;
        public String name;
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

    /** DConnectHelperの基底Exception */
    public abstract class DConnectHelperException extends Exception {
        public int errorCode = 0;
    }

    /** Resultが不正 */
    public class DConnectInvalidResultException extends DConnectHelperException {}

    /** 認証失敗 */
    public class DConnectAuthFailedException extends DConnectHelperException {}

    //endregion
    //---------------------------------------------------------------------------------------
    //region Methods

    public void setContext(Context context) {
        mContext = context;

        if (context == null) {
            mDConnectSDK = null;
        } else {
            mDConnectSDK = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
            mDConnectSDK.setOrigin(context.getPackageName());

            SettingData setting = SettingData.getInstance(context);
            if (setting.accessToken != null) {
                mDConnectSDK.setAccessToken(setting.accessToken);
            }
        }
    }

    /**
     * イベントハンドラーを設定する.
     *
     * @param handler ハンドラー
     */
    public void setEventHandler(EventHandler handler) {
        this.eventHandler = handler;
    }

    /**
     * 接続先情報を設定する.
     *
     * @param ssl SSL通信を行う場合true
     * @param host ホスト名
     * @param port ポート番号
     */
    public void setHostInfo(boolean ssl, String host, int port) {
        mDConnectSDK.setSSL(ssl);
        mDConnectSDK.setHost(host);
        mDConnectSDK.setPort(port);
    }

    public void availability(final FinishCallback<Void> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "availability");

        mDConnectSDK.availability(new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                Exception e = null;
                if (response.getResult() == DConnectMessage.RESULT_ERROR) {
                    e = new Exception(response.getErrorMessage());
                }
                callback.onFinish(null, e);
            }
        });
    }

    /**
     * ServiceDiscoveryを実行する.
     *
     * @param callback コールバック
     */
    public void serviceDiscovery(final FinishCallback<List<ServiceInfo>> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "serviceDiscovery");

        mDConnectSDK.serviceDiscovery(new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                if (response.getResult() == DConnectMessage.RESULT_ERROR) {
                    switch (DConnectMessage.ErrorCode.getInstance(response.getErrorCode())) {
                        case AUTHORIZATION:
                        case EXPIRED_ACCESS_TOKEN:
                        case EMPTY_ACCESS_TOKEN:
                        case SCOPE:
                        case NOT_FOUND_CLIENT_ID:
                            auth(new FinishCallback<AuthInfo>() {
                                @Override
                                public void onFinish(AuthInfo authInfo, Exception error) {
                                    if (error == null) {
                                        serviceDiscovery(callback);
                                    } else {
                                        DConnectHelperException e = new DConnectInvalidResultException();
                                        e.errorCode = response.getInt(DConnectMessage.EXTRA_ERROR_CODE);
                                        callback.onFinish(null, e);
                                    }
                                }
                            });
                            return;
                    }
                }

                if (response.getResult() == DConnectMessage.RESULT_OK) {
                    // サービスリストを取得
                    List<Object> services = response.getList(ServiceDiscoveryProfileConstants.PARAM_SERVICES);
                    if (services == null) {
                        // サービスがない場合
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
                } else {
                    DConnectHelperException e = new DConnectInvalidResultException();
                    e.errorCode = response.getInt(DConnectMessage.EXTRA_ERROR_CODE);
                    callback.onFinish(null, e);
                }
            }
        });
    }

    /**
     * ServiceInformationを実行する.
     *
     * @param serviceId ServiceID
     * @param callback コールバック
     */
    public void serviceInformation(final String serviceId, final FinishCallback<Map<String, List<APIInfo>>> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "serviceInformation");

        mDConnectSDK.getServiceInformation(serviceId, new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                int result = response.getInt(DConnectMessage.EXTRA_RESULT);
                if (result == DConnectMessage.RESULT_ERROR) {
                    switch (DConnectMessage.ErrorCode.getInstance(response.getErrorCode())) {
                        case AUTHORIZATION:
                        case EXPIRED_ACCESS_TOKEN:
                        case EMPTY_ACCESS_TOKEN:
                        case SCOPE:
                        case NOT_FOUND_CLIENT_ID:
                            auth(new FinishCallback<AuthInfo>() {
                                @Override
                                public void onFinish(AuthInfo authInfo, Exception error) {
                                    if (error == null) {
                                        serviceInformation(serviceId, callback);
                                    } else {
                                        DConnectHelperException e = new DConnectInvalidResultException();
                                        e.errorCode = response.getInt(DConnectMessage.EXTRA_ERROR_CODE);
                                        callback.onFinish(null, e);
                                    }
                                }
                            });
                            return;
                        default:
                            DConnectHelperException e = new DConnectInvalidResultException();
                            e.errorCode = response.getInt(DConnectMessage.EXTRA_ERROR_CODE);
                            callback.onFinish(null, e);
                            break;
                    }
                }

                // APIリストを取得
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> profiles = (Map<String, Map<String, Object>>)response.get(ServiceInformationProfileConstants.PARAM_SUPPORT_APIS);
                if (profiles == null) {
                    // サービスがない場合
                    callback.onFinish(null, null);
                    return;
                }
                // 詰め直しして返却
                Map<String, List<APIInfo>> res = new HashMap<>();
                for (Map.Entry<String, Map<String, Object>> profile: profiles.entrySet()) {
                    List<APIInfo> list = new ArrayList<>();
                    String profileName = profile.getKey();
                    if (!(profile.getValue() instanceof Map)) {
                        continue;
                    }
                    for (Map.Entry<String, Object> info: profile.getValue().entrySet()) {
                        if ("paths".equals(info.getKey())) {
                            @SuppressWarnings("unchecked")
                            Map<String, Map<String, Object>> apis = (Map<String, Map<String, Object>>) info.getValue();
                            for (Map.Entry<String, Map<String, Object>> api: apis.entrySet()) {
                                for (Map.Entry<String, Object> method: api.getValue().entrySet()) {
                                    APIInfo apiInfo = new APIInfo();
                                    apiInfo.path = profileName;
                                    if (!api.getKey().endsWith("/")) {
                                        apiInfo.path += api.getKey();
                                    }
                                    apiInfo.method = method.getKey().toUpperCase();
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> methodInfo = (Map<String, Object>) method.getValue();
                                    apiInfo.name = (String) methodInfo.get("summary");
                                    if (apiInfo.name == null || apiInfo.name.length() == 0) {
                                        apiInfo.name = apiInfo.path + " [" + apiInfo.method + "]";
                                    } else {
                                        apiInfo.name += "\n" + apiInfo.path + " [" + apiInfo.method + "]";
                                    }
                                    @SuppressWarnings("unchecked")
                                    List<Map<String, Object>> params = (List<Map<String, Object>>) methodInfo.get("parameters");
                                    if (params != null) {
                                        apiInfo.params = new ArrayList<>();
                                        for (Map<String, Object> paramMap: params) {
                                            APIParam param = new APIParam();
                                            param.name = paramMap.get("name").toString();
                                            param.type = paramMap.get("type").toString();
                                            param.required = paramMap.get("required").toString().equals("true");
                                            apiInfo.params.add(param);
                                        }
                                    }
                                    list.add(apiInfo);
                                }
                            }
                        }
                    }
                    res.put(profileName, list);
                }
                callback.onFinish(res, null);
            }
        });
    }

    private void auth(final FinishCallback<AuthInfo> callback) {
        final SettingData setting = SettingData.getInstance(mContext);
        String appName = mContext.getString(R.string.app_name);
        String scopes[] = setting.getScopes();
        auth(appName, scopes, callback);
    }

    /**
     * Local OAuth認証を行う.
     *
     * @param appName アプリ名
     * @param scopes スコープ
     * @param callback 完了コールバック
     */
    public void auth(String appName, String[] scopes, final FinishCallback<AuthInfo> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "auth");

        mDConnectSDK.authorization(appName, scopes, new DConnectSDK.OnAuthorizationListener() {
            @Override
            public void onResponse(final String clientId, final String accessToken) {
                mDConnectSDK.setAccessToken(accessToken);

                SettingData setting = SettingData.getInstance(mContext);
                setting.clientId = clientId;
                setting.accessToken = accessToken;
                setting.save();

                callback.onFinish(new AuthInfo(clientId, accessToken), null);
            }

            @Override
            public void onError(final int errorCode, final String errorMessage) {
                DConnectHelperException e = new DConnectAuthFailedException();
                e.errorCode = errorCode;
                if (BuildConfig.DEBUG) Log.e(TAG, "Error on auth:" + errorCode);
                callback.onFinish(null, e);
            }
        });
    }

    /**
     * メッセージ送信する.
     *
     * @param serviceId ServiceID
     * @param channelId ChannelID
     * @param text Text
     */
    public void sendMessage(String serviceId, String channelId, String text, String resource, final FinishCallback<Void> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "sendMessage:" + channelId + ":" + text);
        // 接続情報
        ConnectionParam connectionParam = new ConnectionParam(
                "POST",
                "messageHook",
                "message"
        );
        // パラメータ
        Map<String, Object> params = new HashMap<>();
        params.put(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
        if (text != null) {
            params.put("text", text);
        }
        params.put("channelId", channelId);
        if (resource != null && resource.length() > 0) {
            params.put("resource", resource);
            params.put("mimeType", "image");
        }

        // 接続
        new HttpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TaskParam(connectionParam, params) {
            @Override
            public void callBack(DConnectMessage message) {
                // コールバックがないので処理する意味がない
                if (callback == null) {
                    return;
                }
                // エラーチェック
                int result = message.getInt(DConnectMessage.EXTRA_RESULT);
                if (result == DConnectMessage.RESULT_ERROR) {
                    DConnectHelperException e = new DConnectInvalidResultException();
                    e.errorCode = message.getInt(DConnectMessage.EXTRA_ERROR_CODE);
                    callback.onFinish(null, e);
                    return;
                }
                callback.onFinish(null, null);
            }
        });
    }

    /**
     * リクエスト送信する.
     *
     * @param method Method
     * @param path Path
     * @param serviceId ServiceID
     * @param params パラメータ
     * @param callback Callback
     */
    public void sendRequest(String method, String path, String serviceId, Map<String, Object> params, final FinishCallback<Map<String, Object>> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "sendRequest");

        // 接続情報
        ConnectionParam connectionParam = new ConnectionParam(
                method,
                path
        );
        // パラメータ
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(DConnectMessage.EXTRA_SERVICE_ID, serviceId);

        new HttpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TaskParam(connectionParam, params) {
            @Override
            public void callBack(DConnectMessage message) {
                // コールバックがないので処理する意味がない
                if (callback == null) {
                    return;
                }
                // エラーチェック
                int result = message.getInt(DConnectMessage.EXTRA_RESULT);
                if (result == DConnectMessage.RESULT_ERROR) {
                    DConnectHelperException e = new DConnectInvalidResultException();
                    e.errorCode = message.getInt(DConnectMessage.EXTRA_ERROR_CODE);
                    callback.onFinish(message, e);
                    return;
                }
                callback.onFinish(message, null);
            }
        });
    }

    /**
     * WebSocketを開く.
     */
    public synchronized void openWebSocket() {
        mActiveWebSocket = true;
        connectWebSocket();
    }

    /**
     * WebSocketを閉じる.
     */
    public synchronized void closeWebSocket() {
        mActiveWebSocket = false;
        unregisterEvent();
        mDConnectSDK.disconnectWebSocket();
    }

    /**
     * WebSocketの接続状態を確認する.
     * @return 接続されている場合はtrue、それ以外はfalse
     */
    public synchronized boolean isOpenWebSocket() {
        return mActiveWebSocket && mDConnectSDK.isConnectedWebSocket();
    }

    /**
     * WebSocketの接続処理を行う.
     * <p>
     * 接続が切れた場合には、{@link #RETRY_INTERVAL}秒後に再接続を試みる。
     * </p>
     */
    private void connectWebSocket() {
        mDConnectSDK.connectWebSocket(new DConnectSDK.OnWebSocketListener() {
            @Override
            public void onOpen() {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "WebSocket is opened.");
                }
                registerEvent();
            }

            @Override
            public void onClose() {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "WebSocket is closed.");
                }
            }

            @Override
            public void onError(Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "WebSocket occurred a exception. ", e);
                }
                auth(new FinishCallback<AuthInfo>() {
                    @Override
                    public void onFinish(AuthInfo authInfo, Exception error) {
                        connectWebSocket();
                    }
                });
            }
        });
    }

    /**
     * イベント登録・解除する.
     * @param profile Profile
     * @param attribute Attribute
     * @param serviceId ServiceID
     * @param unregist 登録の場合はtrue、解除の場合はfalse
     * @param callback Callback
     */
    private void registerEvent(String profile, String attribute, String serviceId, boolean unregist, final FinishCallback<Void> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "registerEvent:" + profile + ":" + attribute);

        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(profile);
        builder.setAttribute(attribute);
        builder.setServiceId(serviceId);

        if (!unregist) {
            mDConnectSDK.addEventListener(builder.build(), new DConnectSDK.OnEventListener() {
                @Override
                public void onMessage(DConnectEventMessage message) {
                    eventHandler.onEvent(message);
                }

                @Override
                public void onResponse(final DConnectResponseMessage response) {
                    // エラーチェック
                    int result = response.getResult();
                    if (result == DConnectMessage.RESULT_ERROR) {
                        DConnectHelperException e = new DConnectInvalidResultException();
                        e.errorCode = response.getErrorCode();
                        callback.onFinish(null, e);
                        return;
                    }
                    callback.onFinish(null, null);
                }
            });
        } else {
            mDConnectSDK.removeEventListener(builder.build());
        }
    }

    /**
     * messageHookプロファイルにイベントの登録する.
     */
    private void registerEvent() {
        SettingData setting = SettingData.getInstance(mContext);
        DConnectHelper.INSTANCE.registerEvent("messageHook", "onmessage", setting.serviceId, false, new DConnectHelper.FinishCallback<Void>() {
            @Override
            public void onFinish(Void aVoid, Exception error) {
                if (error != null) {
                    mExecutor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            if (mActiveWebSocket) {
                                registerEvent();
                            }
                        }
                    }, RETRY_INTERVAL, TimeUnit.SECONDS);
                }
            }
        });
    }

    /**
     * messageHookプロファイルへのイベントの解除する.
     */
    private void unregisterEvent() {
        SettingData setting = SettingData.getInstance(mContext);
        DConnectHelper.INSTANCE.registerEvent("messageHook", "onmessage", setting.serviceId, true, new DConnectHelper.FinishCallback<Void>() {
            @Override
            public void onFinish(Void aVoid, Exception error) {
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
        public String profileName;
        public String attributeName;
        public String method;
        public String path;
        public ConnectionParam(String method, String profileName, String attributeName) {
            this.method = method;
            this.profileName = profileName;
            this.attributeName = attributeName;
        }
        public ConnectionParam(String method, String path) {
            this.method = method;
            this.path = path;
        }
    }

    /**
     * Task用パラメータ
     */
    private class TaskParam {
        /** 接続情報 */
        public ConnectionParam connection;
        /** その他パラメータ */
        public Map<String, Object> params;
        /** コールバック */
        public void callBack(DConnectMessage message) {}

        public TaskParam(ConnectionParam url, Map<String, Object > params) {
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

        private DConnectResponseMessage executeRequest() {
            DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();

            MultipartEntity entity = null;
            ConnectionParam conn = param.connection;
            if (conn.path == null) {
                builder.setProfile(conn.profileName);
                builder.setAttribute(conn.attributeName);
            } else {
                builder.setPath(conn.path);
            }

            boolean isQuery = conn.method.equals("GET") || conn.method.equals("DELETE");
            if (isQuery) {
                // GET/DELETEはQueryをURIに付加
                for (String key: param.params.keySet()) {
                    builder.addParameter(key, (String) param.params.get(key));
                }
            } else {
                entity = new MultipartEntity();
                for (String key: param.params.keySet()) {
                    entity.add(key, new StringEntity((String) param.params.get(key)));
                }
            }

            DConnectResponseMessage message = new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
            if (conn.method.equals("GET")) {
                message = mDConnectSDK.get(builder.build());
            } else if (conn.method.equals("PUT")) {
                message = mDConnectSDK.put(builder.build(), entity);
            } else if (conn.method.equals("POST")) {
                message = mDConnectSDK.post(builder.build(), entity);
            } else if (conn.method.equals("DELETE")) {
                message = mDConnectSDK.delete(builder.build());
            }

            return message;
        }

        /**
         * 実行するパスから使用するプロファイルを抽出してスコープに追加します.
         */
        private void addNewProfileName() {
            SettingData setting = SettingData.getInstance(mContext);
            ConnectionParam conn = param.connection;
            if (conn.path == null) {
                if (conn.profileName != null) {
                    setting.scopes.add(conn.profileName);
                }
            } else {
                String[] segments = conn.path.split("/");
                if (segments.length > 1) {
                    setting.scopes.add(segments[1]);
                }
            }
            setting.save();
        }

        /**
         * Local OAuthを実行します.
         * @return 実行結果
         */
        private DConnectResponseMessage authorization() {
            final SettingData setting = SettingData.getInstance(mContext);
            String appName = mContext.getString(R.string.app_name);
            String scopes[] = setting.getScopes();
            DConnectResponseMessage response = mDConnectSDK.authorization(appName, scopes);
            if (response.getResult() == DConnectMessage.RESULT_OK) {
                String accessToken = response.getString(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN);
                mDConnectSDK.setAccessToken(accessToken);
                setting.accessToken = accessToken;
                setting.save();
            }
            return response;
        }

        @Override
        protected DConnectMessage doInBackground(TaskParam... params) {
            param = params[0];

            DConnectResponseMessage response = executeRequest();
            if (response.getResult() == DConnectMessage.RESULT_ERROR) {
                switch (DConnectMessage.ErrorCode.getInstance(response.getErrorCode())) {
                    case SCOPE:
                        addNewProfileName();
                    case AUTHORIZATION:
                    case EXPIRED_ACCESS_TOKEN:
                    case EMPTY_ACCESS_TOKEN:
                    case NOT_FOUND_CLIENT_ID: {
                        DConnectResponseMessage resp = authorization();
                        if (resp.getResult() == DConnectMessage.RESULT_OK) {
                            response = executeRequest();
                        }
                    }   break;
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(DConnectMessage message) {
            param.callBack(message);
        }
    }

    //endregion
    //---------------------------------------------------------------------------------------

}
