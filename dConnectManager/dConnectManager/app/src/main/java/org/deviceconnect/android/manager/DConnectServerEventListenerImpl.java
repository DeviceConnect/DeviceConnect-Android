/*
 DConnectServerEventListenerImpl.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.deviceconnect.android.localoauth.ClientPackageInfo;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.manager.event.EventBroker;
import org.deviceconnect.android.manager.profile.DConnectFilesProfile;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Webサーバからのイベントを受領するクラス.
 * @author NTT DOCOMO, INC.
 */
public class DConnectServerEventListenerImpl implements DConnectServerEventListener {
    /**
     * HTTPサーバからリクエストのマップ.
     */
    private final Map<Integer, Intent> mRequestMap = new ConcurrentHashMap<Integer, Intent>();

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /** JSONレスポンス用のCotnentType. */
    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    /** HTTPリクエストのセグメント数(Profileのみ) {@value}.  */
    private static final int SEGMENT_PROFILE = 2;
    /** HTTPリクエストのセグメント数(ProfilesとAttribute) {@value}. */
    private static final int SEGMENT_ATTRIBUTE = 3;
    /** HTTPリクエストのセグメント数(ProfileとInterfacesとAttribute) {@value}. */
    private static final int SEGMENT_INTERFACES = 4;

    /** バッファサイズ. */
    private static final int BUF_SIZE = 4096;

    /** ポーリング時間(ms). */
    private static final int POLLING_WAIT_TIME = 10000;
    /** デフォルトのタイムアウト時間(ms). */
    private static final int DEFAULT_RESTFUL_TIMEOUT = 180000;
    /** タイムアウト時間. */
    private int mTimeout = DEFAULT_RESTFUL_TIMEOUT;

    /** このクラスが属するコンテキスト. */
    private Context mContext;

    /** ファイルを管理するためのクラス. */
    private FileManager mFileMgr;

    /** ロックオブジェクト. */
    private final Object mLockObj = new Object();

    /**
     * コンストラクタ.
     * @param context このクラスが属するコンテキスト
     */
    public DConnectServerEventListenerImpl(final Context context) {
        mContext = context;
    }

    /**
     * ファイルを操作するためのマネージャー.
     * @param fileMgr マネージャー
     */
    public void setFileManager(final FileManager fileMgr) {
        mFileMgr = fileMgr;
    }

    /**
     * Device Connect Managerからレスポンスを受け取る.
     *
     * @param intent レスポンス
     */
    public void onResponse(final Intent intent) {
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
        mLogger.info("HttpServer was started.");
    }

    @Override
    public void onWebSocketConnected(final DConnectWebSocket webSocket) {
        if (BuildConfig.DEBUG) {
            mLogger.info("onWebSocketConnected: id = " + webSocket.getId());
        }
    }

    @Override
    public void onWebSocketDisconnected(final String webSocketId) {
        if (BuildConfig.DEBUG) {
            mLogger.info("onWebSocketDisconnected: id = " + webSocketId);
        }

        DConnectService service = (DConnectService) mContext;
        DConnectApplication app = (DConnectApplication) service.getApplication();
        EventBroker eventBroker = service.getEventBroker();
        WebSocketInfo disconnected = null;
        for (WebSocketInfo info : app.getWebSocketInfoManager().getWebSocketInfos()) {
            if (info.getRawId().equals(webSocketId)) {
                disconnected = info;
                break;
            }
        }
        if (disconnected != null) {
            eventBroker.removeEventSession(disconnected.getReceiverId());
            app.getWebSocketInfoManager().removeWebSocketInfo(disconnected.getReceiverId());
        }
    }

    @Override
    public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
        try {
            mLogger.info("onWebSocketMessage: message = " + message);

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
                    webSocket.disconnectWebSocket();
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
                    ((DConnectService) mContext).sendDisconnectWebSocket(eventKey);
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

    private boolean requiresOrigin() {
        return ((DConnectService) mContext).requiresOrigin();
    }

    private boolean usesLocalOAuth() {
        return ((DConnectService) mContext).usesLocalOAuth();
    }

    private WebSocketInfoManager getWebSocketInfoManager() {
        DConnectApplication app = (DConnectApplication) ((DConnectService) mContext).getApplication();
        return app.getWebSocketInfoManager();
    }

    private void sendSuccess(final DConnectWebSocket webSocket) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("result", 0);
        webSocket.sendEvent(message.toString());
    }

    private void sendError(final DConnectWebSocket webSocket,
                             final int errorCode,
                             final String errorMessage) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("result", 1);
        message.put("errorCode", errorCode);
        message.put("errorMessage", errorMessage);
        webSocket.sendEvent(message.toString());
    }

    private boolean isValidAccessToken(final String accessToken, final String origin) {
        ClientPackageInfo client = LocalOAuth2Main.findClientPackageInfoByAccessToken(accessToken);
        if (client == null) {
            return false;
        }
        PackageInfoOAuth oauth = client.getPackageInfo();
        if (oauth == null) {
            return false;
        }
        return oauth.getPackageName().equals(origin);
    }

    @Override
    public void onResetEventSessionKey(final String sessionKey) {
        if (BuildConfig.DEBUG) {
            mLogger.info("onResetEventSessionKey: sessionKey :" + sessionKey);
        }
    }

    @Override
    public boolean onReceivedHttpRequest(final HttpRequest request,
                                         final HttpResponse response) {
        final int requestCode = UUID.randomUUID().hashCode();
        Uri uri = Uri.parse(request.getUri());
        List<String> segments = uri.getPathSegments();
        Set<String> keyvalue = uri.getQueryParameterNames();
        String contentType = getContentType(request.getHeaders());
        String method = request.getMethod();

        String api = null;
        String httpMethod = null;
        String profile = null;
        String interfaces = null;
        String attribute = null;

        long start = System.currentTimeMillis();

        if (BuildConfig.DEBUG) {
            mLogger.info(String.format("@@@ Request URI: %s %s", method, uri));
        }

        if (segments.size() == SEGMENT_PROFILE) {
            api = segments.get(0);
            profile = segments.get(1);
        } else if (segments.size() == SEGMENT_ATTRIBUTE && !isMethod(segments.get(1))) {
            // パスが3つあり、HTTPメソッドがパスに指定されていない
            api = segments.get(0);
            profile = segments.get(1);
            attribute = segments.get(2);
        } else if (segments.size() == SEGMENT_ATTRIBUTE && isMethod(segments.get(1))) {
            // パスが3つあり、HTTPメソッドがパスに指定される
            api = segments.get(0);
            httpMethod = segments.get(1);
            profile = segments.get(2);
        } else if (segments.size() == SEGMENT_INTERFACES && !isMethod(segments.get(1))) {
            // パスが4つあり、HTTPメソッドがパスに指定されていない
            api = segments.get(0);
            profile = segments.get(1);
            interfaces = segments.get(2);
            attribute = segments.get(3);
        } else if (segments.size() == SEGMENT_INTERFACES && isMethod(segments.get(1))) {
            // パスが4つあり、HTTPメソッドがパスに指定される
            api = segments.get(0);
            httpMethod = segments.get(1);
            profile = segments.get(2);
            attribute = segments.get(3);
        } else if (segments.size() == SEGMENT_INTERFACES && isMethod(segments.get(1))) {
            // パスが5つあり、HTTPメソッドがパスに指定される
            api = segments.get(0);
            httpMethod = segments.get(1);
            profile = segments.get(2);
            interfaces = segments.get(3);
            attribute = segments.get(4);
        }
        if (api == null || !api.equals("gotapi")) {
            // ルートが存在しない、もしくはgotapiでない場合は404
            response.setCode(StatusCode.NOT_FOUND);
            return true;
        }
        // プロファイルが存在しない場合にはエラー
        if (profile == null) {
            try {
                setEmptyProfile(response);
            } catch (UnsupportedEncodingException e) {
                setErrorResponse(response);
            } catch (JSONException e) {
                setErrorResponse(response);
            }
            return true;
        } else if (isMethod(profile)) { //Profile名がhttpMethodの場合
            try {
                setInvalidProfile(response);
            } catch (UnsupportedEncodingException e) {
                setErrorResponse(response);
            } catch (JSONException e) {
                setErrorResponse(response);
            }
            return true;
        }

        // Httpメソッドに対応するactionを取得
        String action = DConnectUtil.convertHttpMethod2DConnectMethod(request.getMethod());
        if (action == null) {
            response.setCode(StatusCode.NOT_IMPLEMENTED);
            return true;
        }

        // URLにmethodが指定されている場合は、そちらのHTTPメソッドを優先する
        if (httpMethod != null && action.equals(IntentDConnectMessage.ACTION_GET)) {
            action = DConnectUtil.convertHttpMethod2DConnectMethod(httpMethod.toUpperCase());
        } else if (httpMethod != null && !action.equals(IntentDConnectMessage.ACTION_GET)) {
            // 元々のHTTPリクエストがGET以外の場合はエラーを返す.
            try {
                setInvalidURL(response);
            } catch (UnsupportedEncodingException e) {
                setErrorResponse(response);
            } catch (JSONException e) {
                setErrorResponse(response);
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
        if (keyvalue != null) {
            for (String key : keyvalue) {
                intent.putExtra(key, uri.getQueryParameter(key));
            }
        }

        // アプリケーションのオリジン解析
        parseOriginHeader(request, intent);

        // Bodyの解析
        if (hasMultipart(contentType)) {
            parseMultipart(request, intent);
        } else {
            parseBody(request, intent, isUrlEncoded(contentType));
        }

        intent.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(DConnectService.EXTRA_INNER_TYPE,
                DConnectService.INNER_TYPE_HTTP);
        mContext.startService(intent);

        // レスポンスが返ってくるまで待つ
        // ただし、タイムアウト時間を設定しておき、永遠には待たない。
        Intent resp = waitForResponse(requestCode);
        try {
            if (resp == null) {
                // ここのエラーはタイムアウトの場合のみ
                setTimeoutResponse(response);
            } else {
                convertResponse(response, profile, attribute, resp);
            }
        } catch (JSONException e) {
            setErrorResponse(response);
        } catch (UnsupportedEncodingException e) {
            setErrorResponse(response);
        }

        if (BuildConfig.DEBUG) {
            mLogger.info(String.format("@@@ Request URI END(%d): %s %s",
                    (System.currentTimeMillis() - start), method, request.getUri()));
        }
        return true;
    }

    /**
     * レスポンスが返ってくるまで待ちます.
     * ただし、タイムアウトなどを起こした場合にはnullが返却される。
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
     * タイムアウトエラーのレスポンスを作成する.
     * @param response レスポンスを格納するインスタンス
     * @throws JSONException JSON変換に失敗した場合には発生
     * @throws UnsupportedEncodingException 文字コード(UTF8)がサポートされていない場合に発生
     */
    private void setTimeoutResponse(final HttpResponse response)
            throws JSONException, UnsupportedEncodingException {
        setErrorResponseJSON(response,
                DConnectMessage.ErrorCode.TIMEOUT.getCode(),
                DConnectMessage.ErrorCode.TIMEOUT.toString());
    }

    /**
     * プロファイルが空の場合のエラーレスポンスを作成する.
     * @param response レスポンスを格納するインスタンス
     * @throws JSONException JSON変換に失敗した場合には発生
     * @throws UnsupportedEncodingException 文字コード(UTF8)がサポートされていない場合に発生
     */
    private void setEmptyProfile(final HttpResponse response)
            throws JSONException, UnsupportedEncodingException {
        setErrorResponseJSON(response,
                DConnectMessage.ErrorCode.NOT_SUPPORT_PROFILE.getCode(),
                DConnectMessage.ErrorCode.NOT_SUPPORT_PROFILE.toString());
    }
    /**
     * URLが不正の場合のエラーレスポンスを作成する.
     * @param response レスポンスを格納するインスタンス
     * @throws JSONException JSON変換に失敗した場合には発生
     * @throws UnsupportedEncodingException 文字コード(UTF8)がサポートされていない場合に発生
     */
    private void setInvalidURL(final HttpResponse response)
            throws JSONException, UnsupportedEncodingException {
        setErrorResponseJSON(response,
                DConnectMessage.ErrorCode.INVALID_URL.getCode(),
                DConnectMessage.ErrorCode.INVALID_URL.toString());
    }
    /**
     * Profileが不正の場合のエラーレスポンスを作成する.
     * @param response レスポンスを格納するインスタンス
     * @throws JSONException JSON変換に失敗した場合には発生
     * @throws UnsupportedEncodingException 文字コード(UTF8)がサポートされていない場合に発生
     */
    private void setInvalidProfile(final HttpResponse response)
            throws JSONException, UnsupportedEncodingException {
        setErrorResponseJSON(response,
                DConnectMessage.ErrorCode.INVALID_PROFILE.getCode(),
                DConnectMessage.ErrorCode.INVALID_PROFILE.toString());
    }
    /**
     * エラーのレスポンスを作成する.
     * <p>
     * このメソッドは、JSONの例外が発生したり、UTF-8の文字列がサポートされていなかったり
     * した場合に作成されるレスポンスです。<br>
     * そのために簡易的なエラーメッセージしか作成しないようにしてあります。
     * </p>
     * @param response レスポンスを格納するHttpレスポンス
     */
    private void setErrorResponse(final HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"" + DConnectMessage.EXTRA_RESULT +  "\":"
                + DConnectMessage.RESULT_ERROR + ",");
        sb.append("\"" + DConnectMessage.EXTRA_ERROR_CODE + "\": "
                + DConnectMessage.ErrorCode.UNKNOWN.getCode() + ",");
        sb.append("\"" + DConnectMessage.EXTRA_ERROR_MESSAGE + "\":\""
                + DConnectMessage.ErrorCode.UNKNOWN.toString() + "\"");
        sb.append("}");
        response.setContentType(CONTENT_TYPE_JSON);
        response.setBody(sb.toString().getBytes());
    }
    /**
     * エラーのレスポンスのテンプレート.
     * @param response レスポンスを格納するHttpレスポンス
     * @param code エラーコード
     * @param message エラーメッセージ
     */
    private void setErrorResponseJSON(final HttpResponse response,
                                      final int code, final String message)
                                         throws JSONException, UnsupportedEncodingException {
        JSONObject root = new JSONObject();
        root.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
        root.put(DConnectMessage.EXTRA_ERROR_CODE, code);
        root.put(DConnectMessage.EXTRA_ERROR_MESSAGE, message);
        response.setContentType(CONTENT_TYPE_JSON);
        response.setBody(root.toString().getBytes("UTF-8"));
    }
    /**
     * HTTPヘッダーからContent-Typeを取得する.
     * Content-Typeが設定されていない場合にはnullを返却する.
     * @param headers ヘッダー一覧
     * @return Content-Typeの値
     */
    private String getContentType(final Map<String, String> headers) {
        for (String key : headers.keySet()) {
            if (key.toLowerCase(Locale.getDefault()).equals("content-type")) {
                return headers.get(key);
            }
        }
        return null;
    }

    /**
     * Content-Typeにマルチパートが入っているかをチェックする.
     * @param contentType コンテンツタイプ
     * @return マルチパートが入っている場合はtrue,それ以外はfalse
     */
    private boolean hasMultipart(final String contentType) {
        return contentType != null && contentType.contains("multipart/form-data");
    }

    /**
     * 指定されたContent-TypeがURLエンコードされているかチェックする.
     * @param contentType コンテンツタイプ
     * @return エンコードされている場合はtrue、それ以外はfalse
     */
    private boolean isUrlEncoded(final String contentType) {
        return contentType != null && contentType.contains("application/x-www-form-urlencoded");
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
     * BodyのKey-Valueを解釈し、Intentに格納する.
     * @param request HTTPリクエスト
     * @param intent key-valueを格納するIntent
     * @param isUrlEncoded URLエンコードフラグ(エンコードされている場合はtrue,それ以外はfalse)
     */
    private void parseBody(final HttpRequest request, final Intent intent, final boolean isUrlEncoded) {
        if (request.getBody() != null) {
            try {
                String body = new String(request.getBody(), "UTF-8");
                String[] split = body.split("&");
                if (split != null) {
                    for (int i = 0; i < split.length; i++) {
                        String[] kv = split[i].split("=");
                        if (kv.length == 2) {
                            String key = kv[0];
                            String value = kv[1];
                            if (isUrlEncoded) {
                                key = URLDecoder.decode(kv[0], "UTF-8");
                                value = URLDecoder.decode(kv[1], "UTF-8");
                            }
                            intent.putExtra(key, value);
                        }
                    }
                }
            } catch (UnsupportedEncodingException e) {
                mLogger.warning("Exception in parseBody");
            }
        }
    }

    /**
     * マルチパートを解析する.
     *
     * 許容するマルチパートのデータは1個まで。
     *
     * @see <a
     *      href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2">
     *      http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2</a>
     *
     * @param request Httpリクエスト
     * @param intent 変換したデータを格納するIntent
     */
    private void parseMultipart(final HttpRequest request, final Intent intent) {
        InputStream is = null;
        try {
            Map<String, String> headers = request.getHeaders();
            MimeStreamParser parser = new MimeStreamParser();
            final StringBuilder sb = new StringBuilder();
            for (String key : headers.keySet()) {
                sb.append(key + ": " + headers.get(key)).append("\r\n");
            }
            sb.append("\r\n");

            is = new ByteArrayInputStream(request.getBody());

            final String[] filename = new String[1];
            final ByteArrayOutputStream data = new ByteArrayOutputStream();
            parser.setContentHandler(new AbstractContentHandler() {
                /** 変数を格納するためのパートを表す. */
                private static final int STATE_VALUE = 0;
                /** ファイルを格納するためのパートを表す. */
                private static final int STATE_FILE = 1;
                /** 処理名を格納する変数. */
                private String mName;
                /** 処理の状態を格納する変数. */
                private int mState;
                @Override
                public void body(final BodyDescriptor bd, final InputStream in)
                        throws MimeException, IOException {
                    if (mName != null) {
                        if (mState == STATE_VALUE) {
                            intent.putExtra(mName, new String(loadBytes(in)));
                        } else if (mState == STATE_FILE) {
                            data.write(loadBytes(in));
                        } else {
                            mLogger.warning("Unknown state. state=" + mState);
                        }
                    }
                }
                @Override
                public void startHeader() throws MimeException {
                    mName = null;
                    mState = STATE_VALUE;
                }
                @Override
                public void field(final Field field) throws MimeException {
                    if ("Content-Disposition".equalsIgnoreCase(field.getName())) {
                        // [参考] HTTP/1.1のヘッダのフィールド名は case-insensitive.
                        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2

                        String[] str = field.getBody().split(";");
                        for (int i = 0; i < str.length; i++) {
                            String[] params = str[i].trim().split("=");
                            // パラメータ名の仕様については、本関数のドキュメントに載せたURL
                            // (HTMLフォームに関するW3Cのドキュメント)を参照。
                            if (params.length == 2) {
                                if (params[0].trim().equals("name")) {
                                    mName = params[1].trim();
                                    mName = mName.replaceAll("\"", "");
                                } else if (params[0].trim().equals("filename")) {
                                    filename[0] = params[1].trim();
                                    filename[0] = filename[0].replaceAll("\"", "");
                                    mState = STATE_FILE;
                                }
                            }
                        }
                    }
                }
            });
            parser.parse(new SequenceInputStream(new ByteArrayInputStream(sb
                    .toString().getBytes("US-ASCII")), is));
            if (filename[0] != null) {
                String tmpUri = mFileMgr.saveFile(filename[0], data.toByteArray());
                intent.putExtra(FileProfileConstants.PARAM_FILE_NAME, filename[0]);
                intent.putExtra(FileProfileConstants.PARAM_URI, tmpUri);
            }
        } catch (final MimeException e) {
            mLogger.warning("Exception in parseMultipart." + e.getMessage());
        } catch (IOException e) {
            mLogger.warning("Exception in parseMultipart." + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    mLogger.warning("Exception in parseMultipart." + e.getMessage());
                }
            }
        }
    }

    /**
     * 指定されたストリームを読み込みbyte配列にする.
     * @param in ストリーム
     * @return byte配列
     * @throws IOException ストリームの読み込みに失敗した場合に発生
     */
    private byte[] loadBytes(final InputStream in) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len;
        byte[] buf = new byte[BUF_SIZE];
        while ((len = in.read(buf)) > 0) {
            baos.write(buf, 0, len);
        }
        return baos.toByteArray();
    }

    /**
     * HTTPのレスポンスを組み立てる.
     * @param response 返答を格納するレスポンス
     * @param prof profile
     * @param att attribute
     * @param resp response用のIntent
     * @throws JSONException JSONの解析に失敗した場合
     * @throws UnsupportedEncodingException 文字列のエンコードに失敗した場合
     */
    private void convertResponse(final HttpResponse response, final String prof,
                                 final String att, final Intent resp) throws JSONException, UnsupportedEncodingException {
        if (DConnectFilesProfile.PROFILE_NAME.equals(prof)) {
            byte[] data = resp.getByteArrayExtra(DConnectFilesProfile.PARAM_DATA);
            if (data == null) {
                response.setCode(StatusCode.NOT_FOUND);
            } else {
                String mimeType = resp.getStringExtra(DConnectFilesProfile.PARAM_MIME_TYPE);
                if (mimeType != null) {
                    response.setContentType(mimeType);
                }
                response.setBody(data);
            }
        } else {
            JSONObject root = new JSONObject();
            DConnectUtil.convertBundleToJSON(root, resp.getExtras());
            response.setContentType(CONTENT_TYPE_JSON);
            response.setBody(root.toString().getBytes("UTF-8"));
        }
    }

    /**
     * DeviceConnectがサポートしているOne ShotのHTTPメソッドかどうか.
     * @param method HTTPメソッド
     * @return true:DeviceConnectがサポートしているOne shotのHTTPメソッドである。<br>
     *         false:DeviceConnectがサポートしているOne shotのHTTPメソッドではない。
     */
    private boolean isMethod(final String method) {
        return method.toUpperCase().equals(DConnectMessage.METHOD_GET)
                || method.toUpperCase().equals(DConnectMessage.METHOD_POST)
                || method.toUpperCase().equals(DConnectMessage.METHOD_PUT)
                || method.toUpperCase().equals(DConnectMessage.METHOD_DELETE);
    }
}
