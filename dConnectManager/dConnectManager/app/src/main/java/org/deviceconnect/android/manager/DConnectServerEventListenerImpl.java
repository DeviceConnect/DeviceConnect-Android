/*
 DConnectServerEventListenerImpl.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.deviceconnect.android.localoauth.ClientPackageInfo;
import org.deviceconnect.android.manager.event.EventBroker;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.server.DConnectServerError;
import org.deviceconnect.server.DConnectServerEventListener;
import org.deviceconnect.server.http.HttpRequest;
import org.deviceconnect.server.http.HttpResponse;
import org.deviceconnect.server.http.HttpResponse.StatusCode;
import org.deviceconnect.server.websocket.DConnectWebSocket;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.oauth.PackageInfoOAuth;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Webサーバからのイベントを受領するクラス.
 * @author NTT DOCOMO, INC.
 */
class DConnectServerEventListenerImpl implements DConnectServerEventListener {
    /**
     * HTTPサーバからリクエストのマップ.
     */
    private final Map<Integer, Intent> mRequestMap = new ConcurrentHashMap<>();

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /** JSONレスポンス用のContent-Type. */
    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    /** HTTPリクエストのセグメント数(APIのみ) {@value}.  */
    private static final int SEGMENT_API = 1;
    /** HTTPリクエストのセグメント数(Profileのみ) {@value}.  */
    private static final int SEGMENT_PROFILE = 2;
    /** HTTPリクエストのセグメント数(ProfilesとAttribute) {@value}. */
    private static final int SEGMENT_ATTRIBUTE = 3;
    /** HTTPリクエストのセグメント数(ProfileとInterfacesとAttribute) {@value}. */
    private static final int SEGMENT_INTERFACES = 4;

    /** ポーリング時間(ms). */
    private static final int POLLING_WAIT_TIME = 10000;
    /** デフォルトのタイムアウト時間(ms). */
    private static final int DEFAULT_RESTFUL_TIMEOUT = 180000;
    /** タイムアウト時間(ms). */
    private int mTimeout = DEFAULT_RESTFUL_TIMEOUT;

    /** このクラスが属するコンテキスト. */
    private Context mContext;

    /** WebSocket管理クラス. */
    private WebSocketInfoManager mWebSocketInfoManager;

    /** ファイルを管理するためのクラス. */
    private FileManager mFileMgr;

    /** ロックオブジェクト. */
    private final Object mLockObj = new Object();

    /**
     * コンストラクタ.
     * @param managerService DeviceConnectManager本体サービス
     */
    DConnectServerEventListenerImpl(final DConnectService managerService) {
        mContext = managerService;
        mWebSocketInfoManager = managerService.getWebSocketInfoManager();
    }

    /**
     * ファイルを操作するためのマネージャー.
     * @param fileMgr マネージャー
     */
    void setFileManager(final FileManager fileMgr) {
        mFileMgr = fileMgr;
    }

    /**
     * Device Connect Managerからレスポンスを受け取る.
     *
     * @param intent レスポンス
     */
    void onResponse(final Intent intent) {
        int requestCode = intent.getIntExtra(
                IntentDConnectMessage.EXTRA_REQUEST_CODE, Integer.MIN_VALUE);
        mRequestMap.put(requestCode, intent);
        // レスポンスを受け取ったのでスレッドを再開
        synchronized (mLockObj) {
            mLockObj.notifyAll();
        }
    }

    @Override
    public void onError(final DConnectServerError error) {
        mLogger.severe(error.toString());
        // HTTPサーバが起動できなかったので、終了する
        ((Service) mContext).stopSelf();
    }

    @Override
    public void onServerLaunched() {
        if (BuildConfig.DEBUG) {
            mLogger.info("HttpServer was started.");
        }
    }

    @Override
    public void onWebSocketConnected(final DConnectWebSocket webSocket) {
        if (BuildConfig.DEBUG) {
            mLogger.info("onWebSocketConnected: WebSocket = " + webSocket.toString());
        }
    }

    @Override
    public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
        if (BuildConfig.DEBUG) {
            mLogger.info("onWebSocketDisconnected: WebSocket = " + webSocket.toString());
        }

        DConnectService service = (DConnectService) mContext;
        EventBroker eventBroker = service.getEventBroker();
        WebSocketInfo disconnected = null;
        for (WebSocketInfo info : getWebSocketInfoManager().getWebSocketInfos()) {
            if (info.getRawId().equals(webSocket.getId())) {
                disconnected = info;
                break;
            }
        }
        if (disconnected != null) {
            eventBroker.removeEventSession(disconnected.getOrigin());
            getWebSocketInfoManager().removeWebSocketInfo(disconnected.getOrigin());
        }
    }

    @Override
    public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
        if (BuildConfig.DEBUG) {
            mLogger.info("onWebSocketMessage: message = " + message);
        }

        try {
            JSONObject json = new JSONObject(message);
            String uri = webSocket.getUri();
            String origin = webSocket.getClientOrigin();
            String eventKey;
            if (uri.equalsIgnoreCase("/gotapi/websocket")) { // MEMO パスの大文字小文字を無視
                String accessToken = json.optString(DConnectMessage.EXTRA_ACCESS_TOKEN);
                if (accessToken == null) {
                    mLogger.warning("onWebSocketMessage: accessToken is not specified");
                    sendError(webSocket, 1, "accessToken is not specified.");
                    return;
                }
                if (requiresOrigin()) {
                    if (origin == null) {
                        sendError(webSocket, 2, "origin is not specified.");
                        return;
                    }
                    if (usesLocalOAuth() && !isValidAccessToken(accessToken, origin)) {
                        sendError(webSocket, 3, "accessToken is invalid.");
                        return;
                    }
                } else {
                    if (origin == null) {
                        origin = DConnectService.ANONYMOUS_ORIGIN;
                    }
                }
                eventKey = origin;
                // NOTE: 既存のイベントセッションを保持する.
                if (getWebSocketInfoManager().getWebSocketInfo(eventKey) != null) {
                    sendError(webSocket, 4, "already established.");
                    webSocket.disconnect();
                    return;
                }
                sendSuccess(webSocket);
            } else {
                if (origin == null) {
                    origin = DConnectService.ANONYMOUS_ORIGIN;
                }
                eventKey = json.optString(DConnectMessage.EXTRA_SESSION_KEY);
                // NOTE: 既存のイベントセッションを破棄する.
                if (getWebSocketInfoManager().getWebSocketInfo(eventKey) != null) {
                    ((DConnectService) mContext).disconnectWebSocketWithReceiverId(eventKey);
                }
            }
            if (eventKey == null) {
                mLogger.warning("onWebSocketMessage: Failed to generate eventKey: uri = " + uri +  ", origin = " + origin);
                return;
            }

            getWebSocketInfoManager().addWebSocketInfo(eventKey, origin + uri, webSocket.getId());
        } catch (JSONException e) {
            mLogger.warning("onWebSocketMessage: Failed to parse message as JSON object: " + message);
        }
    }

    @Override
    public boolean onReceivedHttpRequest(final HttpRequest request, final HttpResponse response) {
        final int requestCode = UUID.randomUUID().hashCode();

        String[] paths = parsePath(request);
        Map<String, String> parameters = request.getQueryParameters();
        Map<String, String> files = request.getFiles();
        String method = request.getMethod().name();

        String api = null;
        String httpMethod = null;
        String profile = null;
        String interfaces = null;
        String attribute = null;
        boolean existMethod = isHttpMethodIncluded(paths);

        long start = System.currentTimeMillis();

        if (BuildConfig.DEBUG) {
            mLogger.info(String.format("@@@ Request URI: %s %s", method, request.getUri()));
        }

        if (existMethod) {
            // HTTPメソッドがパスに含まれている
            if (paths.length == SEGMENT_API) {
                api = paths[0];
            } else if (paths.length == SEGMENT_PROFILE) {
                api = paths[0];
                profile = paths[1];
            } else if (paths.length == SEGMENT_ATTRIBUTE) {
                api = paths[0];
                httpMethod = paths[1];
                profile = paths[2];
            } else if (paths.length == SEGMENT_INTERFACES) {
                api = paths[0];
                httpMethod = paths[1];
                profile = paths[2];
                attribute = paths[3];
            } else if (paths.length == (SEGMENT_INTERFACES + 1)) {
                api = paths[0];
                httpMethod = paths[1];
                profile = paths[2];
                interfaces = paths[3];
                attribute = paths[4];
            }
        } else {
            // HTTPメソッドがパスに含まれていない
            if (paths.length == SEGMENT_API) {
                api = paths[0];
            } else if (paths.length == SEGMENT_PROFILE) {
                api = paths[0];
                profile = paths[1];
            } else if (paths.length == SEGMENT_ATTRIBUTE) {
                api = paths[0];
                profile = paths[1];
                attribute = paths[2];
            } else if (paths.length == SEGMENT_INTERFACES) {
                api = paths[0];
                profile = paths[1];
                interfaces = paths[2];
                attribute = paths[3];
            }
        }

        if (api == null) {
            // apiが存在しない場合はエラー
            response.setCode(StatusCode.BAD_REQUEST);
            setErrorResponse(response, 19, "api is empty.");
            return true;
        }

        // プロファイルが存在しない場合にはエラー
        if (profile == null) {
            response.setCode(StatusCode.BAD_REQUEST);
            setErrorResponse(response, 19, "profile is empty.");
            return true;
        } else if (isMethod(profile)) { // Profile名がhttpMethodの場合
            response.setCode(StatusCode.BAD_REQUEST);
            setInvalidProfile(response);
            return true;
        }

        // Httpメソッドに対応するactionを取得
        String action = DConnectUtil.convertHttpMethod2DConnectMethod(method);
        if (action == null) {
            response.setCode(StatusCode.NOT_IMPLEMENTED);
            setErrorResponse(response, 1, "Not implements a http method.");
            return true;
        }

       // URLにmethodが指定されている場合は、そちらのHTTPメソッドを優先する
        if (httpMethod != null) {
            if (action.equals(IntentDConnectMessage.ACTION_GET)) {
                action = DConnectUtil.convertHttpMethod2DConnectMethod(httpMethod.toUpperCase());
            } else {
                // 元々のHTTPリクエストがGET以外の場合はエラーを返す.
                setInvalidURL(response);
                return true;
            }
        }

        // files の時は、Device Connect Managerまでは渡さずに、ここで処理を行う
        if ("files".equalsIgnoreCase(profile)) {
            if (request.getMethod().equals(HttpRequest.Method.GET)) {
                String uri = parameters.get("uri");
                try {
                    ContentResolver r = mContext.getContentResolver();
                    response.setBody(r.openInputStream(Uri.parse(uri)));
                    response.setContentLength(-1);
                    response.setCode(StatusCode.OK);
                } catch (Exception e) {
                    response.setCode(StatusCode.NOT_FOUND);
                    setErrorResponse(response, 1, "Not found a resource.");
                }
            } else {
                response.setCode(StatusCode.BAD_REQUEST);
                setErrorResponse(response, 1, "Not implements a method.");
           }
            return true;
        }

        Intent intent = new Intent(action);
        intent.setClass(mContext, DConnectService.class);
        intent.putExtra(IntentDConnectMessage.EXTRA_API, api);
        intent.putExtra(IntentDConnectMessage.EXTRA_PROFILE, profile);
        if (interfaces != null) {
            intent.putExtra(IntentDConnectMessage.EXTRA_INTERFACE, interfaces);
        }
        if (attribute != null) {
            intent.putExtra(IntentDConnectMessage.EXTRA_ATTRIBUTE, attribute);
        }
        if (parameters != null) {
            for (String key : parameters.keySet()) {
                intent.putExtra(key, parameters.get(key));
            }
        }
        if (files != null && parameters != null) {
            // TODO: 複数ファイルがあった時に、どのようにプラグイン渡すか検討が必要
            for (String key : files.keySet()) {
                String v = files.get(key);
                if (v != null && !v.isEmpty()) {
                    String uri = mFileMgr.getContentUri() + "/" + v.substring(v.lastIndexOf('/') + 1);
                    String fileName = parameters.get(key);
                    if (fileName != null) {
                        intent.putExtra(FileProfileConstants.PARAM_FILE_NAME, fileName);
                    }
                    intent.putExtra(FileProfileConstants.PARAM_URI, uri);
                }
            }
        }

        // アプリケーションのオリジン解析
        parseOriginHeader(request, intent);

        intent.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(DConnectService.EXTRA_INNER_TYPE, DConnectService.INNER_TYPE_HTTP);

        try {
            mContext.startService(intent);
        } catch (Exception e){
            setErrorResponse(response, DConnectMessage.ErrorCode.ACCESS_FAILED);
            return true;
        }

        // レスポンスが返ってくるまで待つ
        // ただし、タイムアウト時間を設定しておき、永遠には待たない。
        Intent resp = waitForResponse(requestCode);
        try {
            if (resp == null) {
                // ここのエラーはタイムアウトの場合のみ
                setTimeoutResponse(response);
            } else {
                convertResponse(response, resp);
            }
        } catch (JSONException e) {
            setJSONFormatError(response);
        } catch (UnsupportedEncodingException e) {
            setUnknownError(response);
        }

        if (BuildConfig.DEBUG) {
            mLogger.info(String.format(Locale.getDefault(), "@@@ Request URI END(%d): %s %s",
                    (System.currentTimeMillis() - start), method, request.getUri()));
        }
        return true;
    }

    /**
     * レスポンスが返ってくるまで待ちます.
     * <p>
     * ただし、タイムアウトなどを起こした場合にはnullが返却される。
     * </p>
     * @param requestCode リクエストコード
     * @return レスポンス用のIntent
     */
    private Intent waitForResponse(final int requestCode) {
        final long now = System.currentTimeMillis();
        while (mRequestMap.get(requestCode) == null
                && (System.currentTimeMillis() - now) < mTimeout) {
            synchronized (mLockObj) {
                try {
                    mLockObj.wait(POLLING_WAIT_TIME);
                } catch (InterruptedException e) {
                    mLogger.warning("Exception ouccered in wait.");
                }
            }
        }
        return mRequestMap.remove(requestCode);
    }

    /**
     * Origin要求の設定を取得します.
     * @return Originを要求する場合はtrue、それ以外はfalse
     */
    private boolean requiresOrigin() {
        return ((DConnectService) mContext).requiresOrigin();
    }

    /**
     * Local OAuth設定を取得します.
     * @return Local OAuthが有効の場合はtrue、それ以外はfalse
     */
    private boolean usesLocalOAuth() {
        return ((DConnectService) mContext).usesLocalOAuth();
    }

    /**
     * WebSocketInfoManagerを取得します.
     * @return WebSocketInfoManagerのインスタンス
     */
    private WebSocketInfoManager getWebSocketInfoManager() {
        return mWebSocketInfoManager;
    }

    /**
     * WebSocketに成功メッセージを送信します.
     * @param webSocket メッセージを送信するWebSocket
     */
    private void sendSuccess(final DConnectWebSocket webSocket) {
        webSocket.sendMessage("{\"result\":0}");
    }

    /**
     * WebSocketにエラーを送信します.
     * @param webSocket エラーを送信するWebSocket
     * @param errorCode エラーコード
     * @param errorMessage エラーメッセージ
     */
    private void sendError(final DConnectWebSocket webSocket, final int errorCode, final String errorMessage) {
        String message = "{\"result\":1,\"errorCode\":" + errorCode + ",\"errorMessage\":\"" + errorMessage + "\"}";
        webSocket.sendMessage(message);
    }

    /**
     * アクセストークンとOriginの組み合わせが妥当かチェックします.
     * @param accessToken アクセストークン
     * @param origin オリジン
     * @return 妥当な場合はtrue、それ以外はfalse
     */
    private boolean isValidAccessToken(final String accessToken, final String origin) {
        ClientPackageInfo client = ((DConnectMessageService) mContext).getLocalOAuth2Main().findClientPackageInfoByAccessToken(accessToken);
        if (client == null) {
            return false;
        }
        PackageInfoOAuth oauth = client.getPackageInfo();
        return oauth != null && oauth.getPackageName().equals(origin);
    }

    /**
     * URLパスを「/」で分割した配列を作成します.
     * <p>
     *     分割できない場合には、0の配列を返却します。
     * </p>
     * @param request Httpリクエスト
     * @return パスの配列
     */
    private String[] parsePath(final HttpRequest request) {
        String path = request.getUri();
        if (path == null || !path.contains("/")) {
            return new String[0];
        }
        return path.substring(1).split("/");
    }

    /**
     * タイムアウトエラーのレスポンスを作成する.
     * @param response レスポンスを格納するインスタンス
     */
    private void setTimeoutResponse(final HttpResponse response) {
        setErrorResponse(response, DConnectMessage.ErrorCode.TIMEOUT);
   }

    /**
     * プロファイルが空の場合のエラーレスポンスを作成する.
     *
     * @param response レスポンスを格納するインスタンス
     */
    private void setEmptyProfile(final HttpResponse response) {
        setErrorResponse(response, DConnectMessage.ErrorCode.NOT_SUPPORT_PROFILE);
    }
    /**
     * URLが不正の場合のエラーレスポンスを作成する.
     * @param response レスポンスを格納するインスタンス
     */
    private void setInvalidURL(final HttpResponse response) {
        setErrorResponse(response, DConnectMessage.ErrorCode.INVALID_URL);
    }
    /**
     * Profileが不正の場合のエラーレスポンスを作成する.
     * @param response レスポンスを格納するインスタンス
     */
    private void setInvalidProfile(final HttpResponse response){
        setErrorResponse(response,DConnectMessage.ErrorCode.INVALID_PROFILE);
    }
    /**
     * 原因不明エラーが発生した場合のエラーレスポンスを作成する.
     *
     * @param response レスポンスを格納するインスタンス
     */
    private void setUnknownError(final HttpResponse response) {
        setErrorResponse(response, DConnectMessage.ErrorCode.UNKNOWN);
    }

    /**
     * JSON変換エラーが発生した場合のエラーレスポンスを作成する.
     *
     * @param response レスポンスを格納するインスタンス
     */
    private void setJSONFormatError(final HttpResponse response) {
        setErrorResponse(response, DConnectMessage.ErrorCode.UNKNOWN.getCode(), "JSON format is invalid");
    }

    /**
     * エラーコードをレスポンスに設定する.
     *
     * @param response レスポンスを格納するインスタンス
     * @param errorCode エラーコード
     */
    private void setErrorResponse(final HttpResponse response, final DConnectMessage.ErrorCode errorCode) {
        setErrorResponse(response, errorCode.getCode(), errorCode.toString());
    }

    /**
     * レスポンスにエラーを設定する.
     *
     * @param response レスポンスを格納するHttpレスポンス
     * @param errorCode エラーコード
     * @param errorMessage エラーメッセージ
     */
    private void setErrorResponse(final HttpResponse response, final int errorCode, final String errorMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"");
        sb.append(DConnectMessage.EXTRA_RESULT);
        sb.append("\":");
        sb.append(DConnectMessage.RESULT_ERROR);
        sb.append(",");
        sb.append("\"");
        sb.append(DConnectMessage.EXTRA_ERROR_CODE);
        sb.append("\": ");
        sb.append(errorCode);
        sb.append(",");
        sb.append("\"");
        sb.append(DConnectMessage.EXTRA_ERROR_MESSAGE);
        sb.append("\":\"");
        sb.append(errorMessage);
        sb.append("\"}");
        response.setContentType(CONTENT_TYPE_JSON);
        response.setBody(sb.toString().getBytes());
    }

    /**
     * HTTPリクエストヘッダからアプリケーションのオリジンを取得する.
     * @param request HTTPリクエスト
     * @param intent key-valueを格納するIntent
     */
    private void parseOriginHeader(final HttpRequest request, final Intent intent) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null) {
            return;
        }
        String nativeOrigin = parseNativeOriginHeader(headers);
        if (nativeOrigin != null) {
            intent.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, nativeOrigin);
            return;
        }
        String webOrigin = parseWebOriginHeader(headers);
        if (webOrigin != null) {
            intent.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, webOrigin);
            intent.putExtra(DConnectService.EXTRA_INNER_APP_TYPE, DConnectService.INNER_APP_TYPE_WEB);
        }
    }

    /**
     * HTTPリクエストヘッダからWebアプリのオリジンを取得する.
     *
     * @param headers HTTPリクエストヘッダ
     * @return Webアプリのオリジン
     */
    private String parseWebOriginHeader(final Map<String, String> headers) {
        for (Entry<String, String> entry :  headers.entrySet()) {
            String key = entry.getKey();
            if (key.equalsIgnoreCase("origin")) {
                String value = entry.getValue();
                if (value != null) {
                    return value;
                }
                break;
            }
        }
        return null;
    }

    /**
     * HTTPリクエストヘッダからAndroidネイティブアプリのオリジンを取得する.
     *
     * @param headers HTTPリクエストヘッダ
     * @return Androidネイティブアプリのオリジン
     */
    private String parseNativeOriginHeader(final Map<String, String> headers) {
        for (Entry<String, String> entry :  headers.entrySet()) {
            String key = entry.getKey();
            if (key.equalsIgnoreCase(DConnectMessage.HEADER_GOTAPI_ORIGIN)) {
                String value = entry.getValue();
                if (value != null) {
                    return value;
                }
                break;
            }
        }
        return null;
    }

    /**
     * HTTPのレスポンスを組み立てる.
     * @param response 返答を格納するレスポンス
     * @param resp response用のIntent
     * @throws JSONException JSONの解析に失敗した場合
     * @throws UnsupportedEncodingException 文字列のエンコードに失敗した場合
     */
    private void convertResponse(final HttpResponse response, final Intent resp)
            throws JSONException, UnsupportedEncodingException {
        JSONObject root = new JSONObject();
        DConnectUtil.convertBundleToJSON(((DConnectService) mContext).getSettings(), root, resp.getExtras());
        response.setContentType(CONTENT_TYPE_JSON);
        response.setBody(root.toString().getBytes("UTF-8"));
    }

    /**
     * セグメントの中にHttpメソッドが含まれているか確認する.
     * @param paths セグメント
     * @return Httpメソッドが含まれている場合はtrue、それ以外はfalse
     */
    private boolean isHttpMethodIncluded(final String[] paths) {
        return paths != null && paths.length >= SEGMENT_ATTRIBUTE && isMethod(paths[1]);
    }

    /**
     * DeviceConnectがサポートしているOne ShotのHTTPメソッドかどうか.
     * @param method HTTPメソッド
     * @return true:DeviceConnectがサポートしているOne shotのHTTPメソッドである。<br>
     *         false:DeviceConnectがサポートしているOne shotのHTTPメソッドではない。
     */
    private boolean isMethod(final String method) {
        return method.equalsIgnoreCase(DConnectMessage.METHOD_GET)
                || method.equalsIgnoreCase(DConnectMessage.METHOD_POST)
                || method.equalsIgnoreCase(DConnectMessage.METHOD_PUT)
                || method.equalsIgnoreCase(DConnectMessage.METHOD_DELETE);
    }
}
