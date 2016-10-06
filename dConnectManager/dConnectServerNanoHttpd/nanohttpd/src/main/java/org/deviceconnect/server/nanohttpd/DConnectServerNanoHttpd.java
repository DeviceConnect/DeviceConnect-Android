/*
 DConnectServerNanoHttpd.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.nanohttpd;

import android.content.Context;

import org.deviceconnect.server.DConnectServer;
import org.deviceconnect.server.DConnectServerConfig;
import org.deviceconnect.server.DConnectServerError;
import org.deviceconnect.server.http.HttpRequest;
import org.deviceconnect.server.http.HttpResponse;
import org.deviceconnect.server.nanohttpd.logger.AndroidHandler;
import org.deviceconnect.server.nanohttpd.security.Firewall;
import org.deviceconnect.server.nanohttpd.util.KeyStoreManager;
import org.deviceconnect.server.websocket.DConnectWebSocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import javax.net.ssl.SSLServerSocketFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.NanoWSD;

/**
 * Device Connect サーバー NanoHTTPD.
 * 
 * @author NTT DOCOMO, INC.
 */
public class DConnectServerNanoHttpd extends DConnectServer {

    /** ログ用タグ. */
    private static final String TAG = "DConnectServerNanoHttpd";

    /** バージョン. */
    private static final String VERSION = "1.0.1";

    /** WebSocketのKeepAlive処理のインターバル. */
    private static final int WEBSOCKET_KEEP_ALIVE_INTERVAL = 3000;

    /** サーバーオブジェクト. */
    private NanoServer mServer;

    /** コンテキストオブジェクト. */
    private Context mContext;

    /** Firewall. */
    private Firewall mFirewall;

    /**
     * Keep-Aliveの状態定数.
     * 
     * @author NTT DOCOMO, INC.
     * 
     */
    private enum KeepAliveState {
        /** クライアントの返事待ち状態. */
        WAITING_PONG,

        /** pong受信完了状態. */
        GOT_PONG,
    }
    /**
     * 設定値を元にサーバーを構築します.
     * 
     * @param config サーバー設定。
     * @param context コンテキストオブジェクト。
     */
    public DConnectServerNanoHttpd(final DConnectServerConfig config, final Context context) {
        super(config);

        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        mContext = context;

        if (BuildConfig.DEBUG) {
            Handler handler = new AndroidHandler(TAG);
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.WARNING);
        }
    }

    @Override
    public synchronized void start() {
        if (mServer != null) {
            throw new IllegalStateException("Server is already running.");
        }

        if (!checkDocumentRoot()) {
            if (mListener != null) {
                mListener.onError(DConnectServerError.LAUNCH_FAILED);
            }
            return;
        }

        mServer = new NanoServer(mConfig.getHost(), mConfig.getPort());

        if (mConfig.isSsl()) {
            SSLServerSocketFactory factory = createServerSocketFactory();
            if (factory == null) {
                if (mListener != null) {
                    mListener.onError(DConnectServerError.LAUNCH_FAILED);
                }
                return;
            }

            mServer.makeSecure(factory, null);
        }

        // Androidで利用する場合にMainThreadで利用できない処理がNanoServer#start()にあるため
        // 別スレッドで処理を実行する
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mServer.start();
                    if (mListener != null) {
                        mListener.onServerLaunched();
                    }
                } catch (IOException e) {
                    if (mListener != null) {
                        mListener.onError(DConnectServerError.LAUNCH_FAILED);
                    }
                    mLogger.warning("Exception in the DConnectServerNanoHttpd#start() method. " + e.toString());
                }
            }
        }).start();
    }

    @Override
    public synchronized void shutdown() {
        if (!isRunning()) {
            if (mListener != null) {
                mListener.onError(DConnectServerError.SHUTDOWN_FAILED);
            }
            return;
        }

        if (mSockets != null) {
            for (Entry<String, DConnectWebSocket> data : mSockets.entrySet()) {
                if (data.getValue() instanceof NanoWSD.WebSocket) {
                    try {
                        ((NanoWSD.WebSocket) data.getValue()).close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "Server was shutdown.", false);
                    } catch (IOException e) {
                        mLogger.warning("Exception in the DConnectServerNanoHttpd#shutdown() method. " + e.toString());
                    }
                }
            }
            mSockets.clear();
        }

        mServer.stop();
        mServer = null;
    }

    @Override
    public synchronized boolean isRunning() {
        return mServer != null && mServer.isAlive();
    }

    @Override
    public void disconnectWebSocket(final String sessionKey) {
        if (sessionKey == null) {
            return;
        }

        DConnectWebSocket webSocket = mSockets.get(sessionKey);
        if (webSocket != null && webSocket instanceof NanoWSD.WebSocket) {
            try {
                ((NanoWebSocket) webSocket).close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "Disconnected by the user.", false);
            } catch (IOException e) {
                mLogger.warning("Exception in the DConnectServerNanoHttpd#shutdown() method. " + e.toString());
            }
        }
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * 証明書を読み込みFactoryクラスを生成する.
     * 
     * @return 読み込み成功時はSSLServerSocketFactoryを、その他はnullを返す。
     */
    private SSLServerSocketFactory createServerSocketFactory() {
        SSLServerSocketFactory retval = null;
        do {
            KeyStoreManager storeManager = new KeyStoreManager();
            try {
                storeManager.initialize(mContext, false);
            } catch (GeneralSecurityException e) {
                mLogger.warning("Exception in the DConnectServerNanoHttpd#createServerSocketFactory() method. "
                        + e.toString());
                break;
            }

            retval = storeManager.getServerSocketFactory();
        } while (false);
        return retval;
    }

    /**
     * 設定されたドキュメントルートが正しいかチェックする.
     * 
     * @return 正しい場合true、不正な場合falseを返す。
     */
    private boolean checkDocumentRoot() {
        boolean retval = true;
        File documentRoot = new File(mConfig.getDocumentRootPath());
        if (!documentRoot.exists() || !documentRoot.isDirectory()) {
            mLogger.warning("Invalid document root path : " + documentRoot.getPath());
            retval = false;
        }
        return retval;
    }

    /**
     * NanoHttpサーバーの実継承クラス.
     * 
     * @author NTT DOCOMO, INC.
     * 
     */
    private class NanoServer extends NanoWSD {

        /** WebSocketのコネクションカウンター. */
        private int mWebSocketCount;

        /**
         * コンストラクタ.
         * @param hostname ホスト名
         * @param port ポート
         */
        public NanoServer(final String hostname, final int port) {
            super(hostname, port);

            mFirewall = new Firewall(mConfig.getIPWhiteList());

            mimeTypes();
        }

        @Override
        protected ClientHandler createClientHandler(final Socket finalAccept, final InputStream inputStream) {
            ClientHandler clientHandler = super.createClientHandler(finalAccept, inputStream);
            if (mFirewall != null && !mFirewall.isWhiteIP(finalAccept.getInetAddress().getHostAddress())) {
                clientHandler.close();
            }
            return clientHandler;
        }

        @Override
        public Response serve(final IHTTPSession session) {
            Response nanoRes;
            do {
                if (isWebsocketRequested(session)) {
                    nanoRes = parseOpenWebSocket(session);
                    break;
                }

                if (session.getMethod() == Method.GET) {
                    nanoRes = checkStaticFile(session);
                    if (nanoRes != null) {
                        break;
                    }
                }

                HttpRequest req = new HttpRequest();
                nanoRes = createRequest(session, req);
                if (nanoRes != null) {
                    // Device Connect 用のリクエストが生成できない場合は何かしらのエラー、または別対応が入るので
                    // dConnectManagerへの通知はしない。
                    break;
                }

                HttpResponse res = new HttpResponse();
                if (mListener != null && mListener.onReceivedHttpRequest(req, res)) {
                    nanoRes = newFixedLengthResponse(res);
                } else {
                    nanoRes = super.serve(session);
                }
            } while (false);

            addCORSHeaders(session.getHeaders(), nanoRes);

            return nanoRes;
        }

        @Override
        protected WebSocket openWebSocket(final IHTTPSession handshake) {
            // ここでコネクション数制限をかけてnullを返しても、呼び出しもとで
            // nullチェックをしていないため、更に上位の場所で制限をかける。
            return new NanoWebSocket(handshake);
        }

        /**
         * レスポンスにCORSヘッダーを追加します.
         * @param queryHeaders リクエストデータにあるヘッダー一覧
         * @param nanoRes CORSヘッダーを格納するレスポンスデータ
         * @return CORSヘッダーを格納したレスポンスデータ
         */
        private Response addCORSHeaders(final Map<String, String> queryHeaders, final Response nanoRes) {
            nanoRes.addHeader("Access-Control-Allow-Origin", "*");

            // クロスドメインでアクセスする際にプリフライトリクエストで使用するHTTPヘッダを送信される。
            // このヘッダを受けた場合、Access-Control-Allow-Headersでそれらを許可する必要がある。
            // また、X-Requested-WithというヘッダーでXMLHttpRequestが使えるかの問い合わせがくる場合が
            // あるため、XMLHttpRequestを許可しておく。
            String requestHeaders = queryHeaders.get("access-control-request-headers");
            if (requestHeaders != null) {
                requestHeaders = "XMLHttpRequest, " + requestHeaders;
            } else {
                requestHeaders = "XMLHttpRequest";
            }

            nanoRes.addHeader("Access-Control-Allow-Headers", requestHeaders);
            return nanoRes;
        }

        /**
         * WebSocketへの昇格処理を行う.
         * @param session リクエストデータ
         * @return レスポンスデータ
         */
        private Response parseOpenWebSocket(IHTTPSession session) {
            Response nanoRes;

            if (!countupWebSocket()) {
                nanoRes = newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "Server can't create more connections.");
                return nanoRes;
            }

            // WebSocketを開く処理&レスポンスはNanoWSDに任せ、セッションキーが送られてから
            // 独自のセッション管理を行う。
            nanoRes = super.serve(session);

            if (nanoRes.getStatus() != Status.SWITCH_PROTOCOL) {
                // 不正なWebSocketのリクエストの場合はカウントを取り消す
                countdownWebSocket();
            }

            return nanoRes;
        }

        /**
         * HttpResponseからNanoHTTPD.Responseに変換する.
         * @param res HttResponse
         * @return 変換されたNanoHTTPD.Response
         */
        private Response newFixedLengthResponse(final HttpResponse res) {
            Response nanoRes;ByteArrayInputStream stream;
            int length;

            if (res.getBody() != null) {
                stream = new ByteArrayInputStream(res.getBody());
                length = res.getBody().length;
            } else {
                stream = new ByteArrayInputStream(new byte[0]);
                length = 0;
            }

            nanoRes = newFixedLengthResponse(getStatus(res.getCode()), res.getContentType(), stream, length);

            Map<String, String> headers = res.getHeaders();
            for (Entry<String, String> head : headers.entrySet()) {
                nanoRes.addHeader(head.getKey(), head.getValue());
            }
            return nanoRes;
        }

        /**
         * HttpRequest.StatusCodeをNanoHTTPD.Statusに変換する.
         *
         * @param code ステータスコード
         * @return ステータスコード
         */
        private Status getStatus(final HttpResponse.StatusCode code) {
            int codeNum = code.getCode();

            for (Status status : Status.values()) {
                if (status.getRequestStatus() == codeNum) {
                    return status;
                }
            }
            // NanoHTTPDで対応していない物は全てエラーとして扱う
            return Status.INTERNAL_ERROR;
        }

        /**
         * 静的コンテンツへのリクエストかどうかをチェックする.
         * 
         * @param session HTTPリクエストデータ
         * @return Device Connect へのリクエストの場合はnullを返す。
         */
        private Response checkStaticFile(final IHTTPSession session) {
            Response retValue = null;

            do {
                String mime = session.getHeaders().get("content-type");
                // httpの仕様より、content-typeでMIME Typeが特定できない場合はURIから
                // MIME Typeを推測する。
                if (mime == null || !MIME_TYPES.containsValue(mime)) {
                    mime = getMimeTypeFromURI(session.getUri());
                }

                // MIMEタイプがファイルで無い場合はdConnectへのリクエストかどうかの
                // チェックに回す。
                if (mime == null) {
                    break;
                }

                // 静的コンテンツへのアクセスの場合はdocument rootからファイルを検索する。
                File file = new File(mConfig.getDocumentRootPath(), session.getUri());

                if (!file.exists()) {
                    retValue = newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, Status.NOT_FOUND.getDescription());
                    break;
                } else if (file.isDirectory()) {
                    break;
                } else if (!isReadableFile(file)) {
                    retValue = newFixedLengthResponse(Status.FORBIDDEN, MIME_PLAINTEXT, Status.FORBIDDEN.getDescription());
                    break;
                }

                // If-None-Match対応
                String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length())
                        .hashCode());
                if (etag.equals(session.getHeaders().get("if-none-match"))) {
                    retValue = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                } else {
                    try {
                        retValue = newFixedLengthResponse(Status.OK, mime, new FileInputStream(file), file.length());
                        retValue.addHeader("Content-Length", "" + file.length());
                        retValue.addHeader("ETag", etag);
                    } catch (FileNotFoundException e) {
                        retValue = newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, Status.NOT_FOUND.getDescription());
                        break;
                    }
                }

                // ByteRangeへの対応は必須ではないため、noneを指定して対応しないことを伝える。
                // 対応が必要な場合はbyteを設定して実装すること。
                retValue.addHeader("Accept-Ranges", "none");

            } while (false);
            return retValue;
        }

        /**
         * URIからMIMEタイプを推測する.
         * 
         * @param uri リクエストURI
         * @return MIMEタイプが推測できた場合MIMEタイプ文字列を、その他はnullを返す
         */
        private String getMimeTypeFromURI(final String uri) {

            int dot = uri.lastIndexOf('.');
            String mime = null;
            if (dot >= 0) {
                mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase(Locale.ENGLISH));
            }

            return mime;
        }

        /**
         * WebSocketのコネクションカウンタを1増やす.
         * 
         * @return コネクション数が上限に達していない場合true、上限に達した場合はfalseを返す
         */
        private synchronized boolean countupWebSocket() {
            if (mConfig.getMaxWebSocketConnectionSize() <= mWebSocketCount) {
                mLogger.exiting(getClass().getName(), "countupWebSocket", false);
                return false;
            }

            mWebSocketCount++;
            return true;
        }

        /**
         * WebSocketのコネクションカウンタを1減らす.
         */
        private synchronized void countdownWebSocket() {
            if (mWebSocketCount > 0) {
                mWebSocketCount--;
            }
        }

        /**
         * IHTTPSessionからHttpRequestを生成する.
         * 
         * @param session リクエストデータ
         * @param req リクエストデータ
         * @return Device Connect 用リクエストデータ。Device Connect へリクエストを渡さない場合はnullを返す。
         */
        private Response createRequest(final IHTTPSession session, final HttpRequest req) {
            String method = null;
            switch (session.getMethod()) {
            case GET:
                method = HttpRequest.HTTP_METHOD_GET;
                break;
            case POST:
                method = HttpRequest.HTTP_METHOD_POST;
                break;
            case DELETE:
                method = HttpRequest.HTTP_METHOD_DELETE;
                break;
            case PUT:
                method = HttpRequest.HTTP_METHOD_PUT;
                break;
            case OPTIONS:
                // クロスドメイン対応としてOPTIONSがきたらDevice Connect で対応しているメソッドを返す
                // Device Connect 対応外のメソッドだがエラーにはしないのでここで処理を終了。
                Response res = newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_PLAINTEXT, "");
                res.addHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE");
                return res;
            default:
                mLogger.warning("This http method is not treated by Device Connect  : " + session.getMethod());
                break;
            }

            if (method == null) {
                return newFixedLengthResponse(Status.NOT_IMPLEMENTED, NanoHTTPD.MIME_PLAINTEXT, "Not allowed HTTP method.");
            }

            if (!session.getHeaders().containsKey("host")) {
                return newFixedLengthResponse(Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Bad Request.");
            }

            String http = mConfig.isSsl() ? "https://" : "http://";
            String uri = http + session.getHeaders().get("host") + session.getUri();
            if (session.getQueryParameterString() != null && session.getQueryParameterString().length() != 0) {
                uri += "?" + session.getQueryParameterString();
            }

            req.setBody(parseBody(session));
            req.setMethod(method);
            req.setUri(uri);
            req.setHeaders(session.getHeaders());

            return null;
        }

        /**
         * リクエストからBodyを抜き出す.
         * 
         * @param session リクエストデータ
         * @return HTTPリクエストのBodyデータ
         */
        private byte[] parseBody(final IHTTPSession session) {
            // NanoHTTPDのparseBodyではマルチパートの場合に自動的に一時ファイルに
            // データを格納するようになっているため、独自にBodyを抜き出す。

            if (!(session instanceof HTTPSession)) {
                mLogger.warning("session is not HTTPSession.");
                return null;
            }
            
            Map<String, String> headers = session.getHeaders();
            if (!session.getMethod().equals(Method.PUT) 
                    && !session.getMethod().equals(Method.POST)
                    && !headers.containsKey("content-length")) {
                return null;
            }

            long size = 0;
            if (headers.containsKey("content-length")) {
                size = Integer.parseInt(headers.get("content-length"));
            }

            try {
                int len = 0;
                byte[] buf = new byte[512];
                InputStream is = session.getInputStream();
                ByteArrayOutputStream bout = new ByteArrayOutputStream();

                while (len >= 0 && size > 0) {
                    len = is.read(buf, 0, (int) Math.min(size, 512));
                    size -= len;
                    if (len > 0) {
                        bout.write(buf, 0, len);
                    }
                }

                if (size != 0) {
                    throw new RuntimeException("Invalid content-length.");
                }

                return bout.toByteArray();
            } catch (IOException e) {
                mLogger.warning("Exception in the NanoServer#parseBody() method. " + e.toString());
            }

            return null;
        }

        /**
         * ファイルが読み込み可能なファイルかチェックする.
         * 
         * @param file チェック対象のファイル。
         * @return 読み込めるファイルの場合trueを、その他はfalseを返す。
         */
        private boolean isReadableFile(final File file) {
            boolean retval;
            try {
                // ../ などのDocument Rootより上の階層にいくファイルパスをチェックし
                // 不正なリクエストを拒否する。
                File root = new File(mConfig.getDocumentRootPath());
                String rootAbPath = root.getCanonicalPath() + "/";
                String fileAbPath = file.getCanonicalPath();
                retval = fileAbPath.contains(rootAbPath) && file.canRead();
            } catch (IOException e) {
                mLogger.warning("Exception in the NanoServer#isDeployedInDocumentRoot() method. " + e.toString());
                retval = false;
            }
            return retval;
        }
    }

    /**
     * WebSocket.
     * 
     * @author NTT DOCOMO, INC.
     * 
     */
    private class NanoWebSocket extends NanoWSD.WebSocket implements DConnectWebSocket {

        /** KeepAlive実行用のタイマー. */
        private Timer mKeepAliveTimer;

        /** Keep-Aliveのタスク. */
        private final KeepAliveTask mKeepAliveTask;

        /** ID. */
        private final UUID mId = UUID.randomUUID();

        /**
         * コンストラクタ.
         * @param handshakeRequest リクエスト
         */
        public NanoWebSocket(final IHTTPSession handshakeRequest) {
            super(handshakeRequest);
            mKeepAliveTask = new KeepAliveTask();
            mKeepAliveTimer = new Timer();
            mKeepAliveTimer.scheduleAtFixedRate(mKeepAliveTask, WEBSOCKET_KEEP_ALIVE_INTERVAL,
                    WEBSOCKET_KEEP_ALIVE_INTERVAL);

            mSockets.put(getId(), this);
            if (mListener != null) {
                mListener.onWebSocketConnected(this);
            }
            mWebSockets.add(this);
        }

        @Override
        public String getId() {
            return mId.toString();
        }

        @Override
        public String getUri() {
            return getHandshakeRequest().getUri();
        }

        @Override
        public String getClientOrigin() {
            NanoHTTPD.IHTTPSession request = getHandshakeRequest();
            String origin = request.getHeaders().get("origin");
            if (origin == null) {
                origin = request.getHeaders().get("X-GotAPI-Origin");
            }
            return origin;
        }

        @Override
        protected void onOpen() {
            mLogger.fine("NanoWebSocket#onOpen()");
        }

        @Override
        protected void onPong(final NanoWSD.WebSocketFrame pongFrame) {
            synchronized (mKeepAliveTask) {
                if (mKeepAliveTask.getState() == KeepAliveState.WAITING_PONG) {
                    mKeepAliveTask.setState(KeepAliveState.GOT_PONG);
                }
            }
        }

        @Override
        protected void onMessage(final NanoWSD.WebSocketFrame messageFrame) {
            String jsonText = messageFrame.getTextPayload();
            if (jsonText == null || jsonText.length() == 0) {
                mLogger.warning("onMessage: jsonText is null.");
                return;
            }

            try {
                JSONObject json = new JSONObject(jsonText);
                String sessionKey = json.getString(WEBSOCKET_PARAM_KEY_SESSION_KEY);

                if (sessionKey != null) {
                    // 同じクライアントからは１つのセッションしか張らせない。
                    if (mSessionKey != null) {
                        mSockets.remove(mSessionKey);
                    }

                    mSessionKey = sessionKey;
                    mSockets.put(sessionKey, this);
                    if (mListener != null) {
                        NanoHTTPD.IHTTPSession request = getHandshakeRequest();
                        String origin = request.getHeaders().get("origin");
                        if (origin == null) {
                            origin = request.getHeaders().get("X-GotAPI-Origin");
                        }
                        mListener.onWebSocketConnected(origin + getHandshakeRequest().getUri(), mSessionKey);
                    }
                }
            } catch (JSONException e) {
                mLogger.warning("Exception in the NanoWebSocket#onMessage() method." + e.toString());
            }
        }

        private void closeWebSocket() {
            mSockets.remove(getId());
            mLogger.fine("WebSocket closed. id = " + getId());
            if (mListener != null) {
                mListener.onWebSocketDisconnected(getId());
            }
            if (mServer != null) {
                mServer.countdownWebSocket();
            }

            mKeepAliveTimer.cancel();
        }

        @Override
        protected void onClose(final CloseCode code, final String reason, final boolean initiatedByRemote) {
            closeWebSocket();
        }

        @Override
        protected void onException(final IOException e) {
            mLogger.warning("Exception in the NanoWebSocket#onException() method. " + e.toString());
            e.printStackTrace();
        }

        @Override
        public void sendEvent(final String event) {
            try {
                send(event);
            } catch (IOException e) {
                mLogger.warning("Exception in the NanoWebSocket#sendEvent() method. " + e.toString());
                if (mListener != null) {
                    mListener.onError(DConnectServerError.SEND_EVENT_FAILED);
                    if (mSessionKey != null) {
                        mListener.onWebSocketDisconnected(mSessionKey);
                    }
                }
            }
        }

        /**
         * Keep-Alive用タイマータスク.
         * 
         * @author NTT DOCOMO, INC.
         * 
         */
        private class KeepAliveTask extends TimerTask {

            /** 処理状態. */
            private KeepAliveState mState;

            /**
             * コンストラクタ.
             */
            public KeepAliveTask() {
                setState(KeepAliveState.GOT_PONG);
            }

            /**
             * 状態を変更する.
             * 
             * @param state 状態
             */
            public void setState(final KeepAliveState state) {
                mState = state;
            }

            /**
             * 状態を取得する.
             * 
             * @return 状態
             */
            public KeepAliveState getState() {
                return mState;
            }

            @Override
            public void run() {
                try {

                    synchronized (this) {
                        if (mState == KeepAliveState.GOT_PONG) {
                            setState(KeepAliveState.WAITING_PONG);
                            ping("".getBytes());
                        } else {
                            close(NanoWSD.WebSocketFrame.CloseCode.GoingAway, "Client is dead.", false);
                        }
                    }

                } catch (IOException e) {
                    // 例外が発生したらタスクを終了し、タイムアウトに任せる
                    cancel();
                }
            }
        }
    }

    @Override
    public void disconnectWebSocket(final String webSocketId) {
        for (NanoWebSocket socket : mWebSockets) {
            if (webSocketId.equals(socket.getId())) {
                try {
                    socket.close(CloseCode.GoingAway, "User disconnect");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    @Override
    public String getVersion() {
        return VERESION;
    }
}
