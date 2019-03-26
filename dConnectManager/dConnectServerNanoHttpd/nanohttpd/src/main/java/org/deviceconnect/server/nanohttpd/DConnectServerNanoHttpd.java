/*
 DConnectServerNanoHttpd.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.nanohttpd;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.server.DConnectServer;
import org.deviceconnect.server.DConnectServerConfig;
import org.deviceconnect.server.DConnectServerError;
import org.deviceconnect.server.http.HttpRequest;
import org.deviceconnect.server.http.HttpResponse;
import org.deviceconnect.server.nanohttpd.accesslog.AccessLog;
import org.deviceconnect.server.nanohttpd.accesslog.AccessLogProvider;
import org.deviceconnect.server.nanohttpd.logger.AndroidHandler;
import org.deviceconnect.server.nanohttpd.security.Firewall;
import org.deviceconnect.server.websocket.DConnectWebSocket;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLServerSocketFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.NanoWSD;

import static fi.iki.elonen.NanoHTTPD.Response.Status.BAD_REQUEST;

/**
 * Device Connect サーバー NanoHTTPD.
 * 
 * @author NTT DOCOMO, INC.
 */
public class DConnectServerNanoHttpd extends DConnectServer {

    /**
     * ログ出力用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * ログ用タグ.
     */
    private static final String TAG = "DConnectServerNanoHttpd";

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("dconnect.server");

    /**
     * ヘッダーの最大サイズを定義.
     */
    private static final int MAX_HEADER_SIZE = 1024;

    /**
     * リクエストを読み込むためのバッファサイズを定義.
     */
    private static final int REQUEST_BUFFER_LEN = 1024;

    /**
     * メモリ上に格納しておく上限サイズを定義.
     */
    private static final int MEMORY_STORE_LIMIT = 1024;

    /**
     * Content-Dispositionヘッダーを見つける正規表現を定義.
     */
    private static final String CONTENT_DISPOSITION_REGEX = "([ |\t]*Content-Disposition[ |\t]*:)(.*)";

    /**
     * Content-Dispositionヘッダーを見つけるパターンを定義.
     */
    private static final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile(CONTENT_DISPOSITION_REGEX, Pattern.CASE_INSENSITIVE);

    /**
     * Content-Typeヘッダーを見つける正規表現を定義.
     */
    private static final String CONTENT_TYPE_REGEX = "([ |\t]*content-type[ |\t]*:)(.*)";

    /**
     * Content-Typeヘッダーを見つけるパターンを定義.
     */
    private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(CONTENT_TYPE_REGEX, Pattern.CASE_INSENSITIVE);

    /**
     * Content-Dispositionヘッダーの値を見つける正規表現を定義.
     */
    private static final String CONTENT_DISPOSITION_ATTRIBUTE_REGEX = "[ |\t]*([a-zA-Z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]";

    /**
     * Content-Dispositionヘッダーの値を見つけるパターンを定義.
     */
    private static final Pattern CONTENT_DISPOSITION_ATTRIBUTE_PATTERN = Pattern.compile(CONTENT_DISPOSITION_ATTRIBUTE_REGEX);

    /**
     * バージョン.
     */
    private static final String VERSION = "2.0.0";

    /**
     * WebSocketのKeepAlive処理のインターバル(ms).
     */
    private static final int WEBSOCKET_KEEP_ALIVE_INTERVAL = 3000;

    /**
     * application/jsonのContent-Typeを定義.
     */
    private static final String MIME_APPLICATION_JSON = "application/json; charset=UTF-8";

    /**
     * サーバーオブジェクト.
     */
    private NanoServer mServer;

    /**
     * コンテキストオブジェクト.
     */
    private Context mContext;

    /**
     * アクセスログ管理クラス.
     */
    private AccessLogProvider mAccessLogProvider;

    /**
     * SSLサーバーソケットファクトリ.
     */
    private SSLServerSocketFactory mServerSocketFactory;

    /**
     * Keep-Aliveの状態定数.
     *
     * @author NTT DOCOMO, INC.
     */
    private enum KeepAliveState {
        /**
         * クライアントの返事待ち状態.
         */
        WAITING_PONG,

        /**
         * pong受信完了状態.
         */
        GOT_PONG,
    }

    /**
     * 設定値を元にサーバーを構築します.
     *
     * このコンストラクタを使用する場合は、SSL通信は行いません.
     *
     * @param config  サーバー設定。
     * @param context コンテキストオブジェクト。
     * @throws IllegalArgumentException SSL設定をONにしていた場合
     */
    public DConnectServerNanoHttpd(final DConnectServerConfig config, final Context context) {
        this(config, context, null);
    }

    /**
     * 設定値を元にサーバーを構築します.
     *
     * SSL設定がONの場合は、SSLServerSocketFactoryを指定する必要があります.
     *
     * @param config  サーバー設定。
     * @param context コンテキストオブジェクト。
     * @param socketFactory SSLサーバーソケットファクトリー
     * @throws IllegalArgumentException SSL設定がONのときに socketFactory に<coce>null</coce>を指定した場合
     */
    public DConnectServerNanoHttpd(final DConnectServerConfig config, final Context context,
                                   final SSLServerSocketFactory socketFactory) {
        super(config);

        if (context == null) {
            throw new IllegalArgumentException("context must not be null.");
        }
        mContext = context;

        if (config.isSsl() && socketFactory == null) {
            throw new IllegalArgumentException("keyStoreManager must not be null if SSL is enabled.");
        }
        mServerSocketFactory = socketFactory;
        mAccessLogProvider = new AccessLogProvider(context);

        if (BuildConfig.DEBUG) {
            Handler handler = new AndroidHandler(TAG);
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.WARNING);
            mLogger.setUseParentHandlers(false);
        } else {
            mLogger.setLevel(Level.OFF);
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

        if (!checkCacheDir()) {
            if (mListener != null) {
                mListener.onError(DConnectServerError.LAUNCH_FAILED);
            }
            return;
        }

        mServer = new NanoServer(mConfig.getHost(), mConfig.getPort());

        // キャッシュのパスが設定されていた場合には、指定したフォルダを使用する
        if (mConfig.getCachePath() != null) {
            mServer.setTempFileManagerFactory(new NanoTempFileManagerFactory(mConfig.getCachePath()));
        }

        // SSLが有効になっている場合には、SSL用の設定を行う
        if (mConfig.isSsl()) {
            SSLServerSocketFactory factory = mServerSocketFactory;
            if (factory == null) {
                if (mListener != null) {
                    mListener.onError(DConnectServerError.LAUNCH_FAILED);
                }
                return;
            }

            mServer.makeSecure(factory, null);
        }

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

        synchronized (mSockets) {
            for (Entry<String, DConnectWebSocket> data : mSockets.entrySet()) {
                try {
                    data.getValue().disconnect();
                } catch (Exception e) {
                    // ignore.
                }
            }
        }
        mSockets.clear();

        mServer.stop();
        mServer = null;
    }

    @Override
    public synchronized boolean isRunning() {
        return mServer != null && mServer.isAlive();
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * SSLサーバーソケットファクトリーを設定する.
     *
     * 指定したファクトリーからサーバーソケットを生成し直したい場合は,
     * 一旦 {@link #shutdown()} してから再度 {@link #start()} を実行すること.
     *
     * @param socketFactory サーバーソケットファクトリー
     */
    public void setSSLServerSocketFactory(final SSLServerSocketFactory socketFactory) {
        mServerSocketFactory = socketFactory;
    }

    /**
     * 設定されたドキュメントルートが正しいかチェックする.
     * <p>
     * ドキュメントルートパスに file:///android_asset が指定された場合には assets フォルダをドキュメントルートとして使用します。
     * </p>
     * @return 正しい場合true、不正な場合falseを返す。
     */
    private boolean checkDocumentRoot() {
        if (mConfig.getDocumentRootPath() == null) {
            // ドキュメントルートが設定されていない場合は、チェックしない。
            return true;
        }
        boolean retVal = true;
        if (!mConfig.getDocumentRootPath().startsWith(DConnectServerConfig.DOC_ASSETS)) {
            File documentRoot = new File(mConfig.getDocumentRootPath());
            if (!documentRoot.exists() || !documentRoot.isDirectory()) {
                mLogger.warning("Invalid document root path: " + documentRoot.getPath());
                retVal = false;
            }
        }
        return retVal;
    }

    /**
     * 設定されたキャッシュ用フォルダが正しいかチェックする.
     * <p>
     * キャッシュ用フォルダが設定されていない場合には、正しいとしてtrueを返却する。
     * </p>
     * @return 正しい場合true、不正な場合falseを返す。
     */
    private boolean checkCacheDir() {
        if (mConfig.getCachePath() == null) {
            return true;
        }
        File cacheDir = new File(mConfig.getCachePath());
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                mLogger.warning("Invalid cache path: " + cacheDir.getPath());
                return false;
            }
        }
        return true;
    }

    /**
     * NanoHTTPDに定義されていないエラーコードを定義するクラス.
     */
    private enum DConnectStatus implements NanoHTTPD.Response.IStatus {
        ENTITY_TOO_LARGE(413, "Request Entity Too Large");

        private final int requestStatus;
        private final String description;

        DConnectStatus(int requestStatus, String description) {
            this.requestStatus = requestStatus;
            this.description = description;
        }

        @Override
        public String getDescription() {
            return "" + this.requestStatus + " " + this.description;
        }

        @Override
        public int getRequestStatus() {
            return this.requestStatus;
        }
    }

    /**
     * NanoWSDの実継承クラス.
     *
     * @author NTT DOCOMO, INC.
     */
    private class NanoServer extends NanoWSD {
        /**
         * Firewall.
         */
        private Firewall mFirewall;

        /**
         * コンストラクタ.
         *
         * @param hostname ホスト名
         * @param port     ポート
         */
        NanoServer(final String hostname, final int port) {
            super(hostname, port);
            mFirewall = new Firewall(mConfig.getIPWhiteList());
            try {
                mimeTypes();
            } catch (Exception e){
                // ignore
            }
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
            if (!checkHeaderSize(session)) {
                // NanoHTTPDでは、バッファサイズを超えたHTTPヘッダーが送られてくると
                // 挙動がおかしくなるのでここでエラーを返却して対応する。
                Response response = newFixedLengthResponse(DConnectStatus.ENTITY_TOO_LARGE, MIME_APPLICATION_JSON,
                        "{\"result\" : 1, \"errorCode\" : 1, \"errorMessage\" : \"Request Entity Too Large.\"}");
                response.closeConnection(true);
                return response;
            }

            if (isWebsocketRequested(session)) {
                Map<String, String> headers = session.getHeaders();
                if (!NanoWSD.HEADER_WEBSOCKET_VERSION_VALUE.equalsIgnoreCase(headers.get(NanoWSD.HEADER_WEBSOCKET_VERSION))) {
                    return newFixedLengthResponse(BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                            "Invalid Websocket-Version " + headers.get(NanoWSD.HEADER_WEBSOCKET_VERSION));
                }

                if (!headers.containsKey(NanoWSD.HEADER_WEBSOCKET_KEY)) {
                    return newFixedLengthResponse(BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Missing Websocket-Key");
                }

                // TODO: WebSocketの最大個数をチェックする
                WebSocket webSocket = openWebSocket(session);
                Response handshakeResponse = webSocket.getHandshakeResponse();
                try {
                    handshakeResponse.addHeader(NanoWSD.HEADER_WEBSOCKET_ACCEPT, makeAcceptKey(headers.get(NanoWSD.HEADER_WEBSOCKET_KEY)));
                } catch (NoSuchAlgorithmException e) {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                            "The SHA-1 Algorithm required for websockets is not available on the server.");
                }

                if (headers.containsKey(NanoWSD.HEADER_WEBSOCKET_PROTOCOL)) {
                    handshakeResponse.addHeader(NanoWSD.HEADER_WEBSOCKET_PROTOCOL, headers.get(NanoWSD.HEADER_WEBSOCKET_PROTOCOL).split(",")[0]);
                }

                return handshakeResponse;
            } else {
                Response nanoRes = serveHttp(session);
                addCORSHeaders(session.getHeaders(), nanoRes);
                return nanoRes;
            }
        }

        @Override
        protected Response serveHttp(final IHTTPSession session) {
            if (session.getMethod() == Method.OPTIONS) {
                // クロスドメイン対応としてOPTIONSがきたらDevice Connect で対応しているメソッドを返す
                // Device Connect 対応外のメソッドだがエラーにはしないのでここで処理を終了。
                Response res = newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_PLAINTEXT, "");
                res.addHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE");
                return res;
            }

            try {
                HttpRequest.Method method = HttpRequest.Method.valueFrom(session.getMethod().name());
                if (method == null) {
                    return newFixedLengthResponse(Status.NOT_IMPLEMENTED, MIME_APPLICATION_JSON,
                            "{\"result\" : 1, \"errorCode\" : 1, \"errorMessage\" : \"Not allowed HTTP method.\"}");
                }

                DConnectHttpRequest request = new DConnectHttpRequest();
                request.setMethod(method);
                request.setUri(session.getUri());
                request.setQuery(session.getParms());
                request.setHeaders(session.getHeaders());
                request.setQueryString(session.getQueryParameterString());
                request.setRemoteIpAddress(session.getRemoteIpAddress());
                request.setRemoteHostName(session.getRemoteHostName());

                parseBody(session, request);

                DConnectHttpResponse response;
                if (mConfig.isEnableAccessLog()) {
                    AccessLog accessLog = createAccessLog(request);
                    response = execute(request);
                    saveAccessLog(response, accessLog);
                } else {
                    response = execute(request);
                }
                return newFixedLengthResponse(response);
            } catch (OutOfMemoryError e) {
                return newFixedLengthResponse(Status.BAD_REQUEST, MIME_APPLICATION_JSON,
                        "{\"result\" : 1, \"errorCode\" : 1, \"errorMessage\" : \"Too large request.\"}");
            } catch (IOException ioe) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_APPLICATION_JSON,
                        "{\"result\" : 1, \"errorCode\" : 1, \"errorMessage\" : \"INTERNAL ERROR: IOException. e=" + ioe.getMessage() + "\"}");
            } catch (ResponseException re) {
                return newFixedLengthResponse(re.getStatus(), MIME_APPLICATION_JSON,
                        "{\"result\" : 1, \"errorCode\" : 1, \"errorMessage\" : \"" + re.getMessage() + "\"}");
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_APPLICATION_JSON,
                        "{\"result\" : 1, \"errorCode\" : 1, \"errorMessage\" : \"INTERNAL ERROR: Exception. e=" + e.getMessage() + "\"}");
            }
        }

        @Override
        protected WebSocket openWebSocket(final IHTTPSession handshake) {
            return new NanoWebSocket(handshake);
        }

        /**
         * HTTPリクエストからアクセスログを作成します.
         *
         * @param request リクエスト
         * @return アクセスログ
         */
        private AccessLog createAccessLog(DConnectHttpRequest request) {
            AccessLog accessLog = mAccessLogProvider.createAccessLog();
            accessLog.setRemoteIpAddress(request.getRemoteIpAddress());
            accessLog.setRemoteHostName(request.getRemoteHostName());
            accessLog.setRequestReceivedTime(System.currentTimeMillis());
            accessLog.setRequestMethod(request.getMethod().name());
            accessLog.setRequestHeader(request.getHeaders());
            accessLog.setRequestPath(getRequestPath(request));

            if (checkMethodPutPost(request.getMethod())) {
                Map<String, String> queryParameters = request.getQueryParameters();
                if (queryParameters != null) {
                    StringBuilder body = new StringBuilder();
                    for (String key : queryParameters.keySet()) {
                        if (body.length() > 0) {
                            body.append("&");
                        }
                        if (!key.equals("postData")) {
                            body.append(key).append("=").append(queryParameters.get(key));
                        } else {
                            body.append(key).append("=").append("[File Data]");
                        }
                    }
                    accessLog.setRequestBody(body.toString());
                }
            }

            return accessLog;
        }

        /**
         * HTTPメソッドが PUT もしくは POST か確認します.
         *
         * @param method HTTPメソッド
         * @return PUTもしくはPOSTの場合はtrue、それ以外はfalse
         */
        private boolean checkMethodPutPost(HttpRequest.Method method) {
            return (method.equals(HttpRequest.Method.PUT) || method.equals(HttpRequest.Method.POST));
        }

        /**
         * HTTPメソッドがGETもしくはDELETE時は、QueryString をパスに付加して取得します.
         * @param request リクエスト
         * @return リクエストのパス
         */
        private String getRequestPath(DConnectHttpRequest request) {
            if (checkMethodPutPost(request.getMethod())) {
                return request.getUri();
            } else {
                return request.getQueryString() == null ?
                        request.getUri() : request.getUri() + "?" + request.getQueryString();
            }
        }

        /**
         * アクセスログにレスポンスの情報を加えて、DBに保存します.
         *
         * @param response レスポンス
         * @param accessLog アクセスログ
         */
        private void saveAccessLog(DConnectHttpResponse response, AccessLog accessLog) {
            String contentType = response.getContentType();
            if (contentType != null && contentType.startsWith("application/json")) {
                accessLog.setResponseBody(new String(response.getBody()));
            }
            accessLog.setResponseContentType(contentType);
            accessLog.setResponseStatusCode(response.getStatusCode().getCode());
            accessLog.setResponseSendTime(System.currentTimeMillis());
            mAccessLogProvider.add(accessLog);
        }

        /**
         * リクエストを Listener に通知して、実行します.
         *
         * @param request リクエスト
         * @return レスポンス
         */
        private DConnectHttpResponse execute(final HttpRequest request) {
            DConnectHttpResponse response = new DConnectHttpResponse();
            if (mListener == null || !mListener.onReceivedHttpRequest(request, response)) {
                byte[] body = "{\"result\" : 1, \"errorCode\" : 1, \"errorMessage\" : \"Not found.\"}".getBytes();
                response.setCode(HttpResponse.StatusCode.NOT_FOUND);
                response.setContentType(MIME_APPLICATION_JSON);
                response.setBody(body);
                response.setContentLength(body.length);
            }
            return response;
        }

        /**
         * ヘッダーサイズを確認する.
         * @param session HTTPセッション
         * @return ヘッダーサイズがバッファよりも大きい場合にはtrue、それ以外はfalse
         */
        private boolean checkHeaderSize(final IHTTPSession session) {
            try {
                int splitbyte = getSplitbyte(session);
                int rlen = getRlen(session);
                if (splitbyte == 0 && rlen == HTTPSession.BUFSIZE) {
                    return false;
                }
            } catch (NoSuchFieldException e) {
                return false;
            } catch (IllegalAccessException e) {
                return false;
            }
            return true;
        }

        /**
         * HTTPSession#splitbyteの値を取得する.
         * <p>
         * privateのフィールドにアクセスして、値を取得します。
         * </p>
         * @param session HTTPセッション
         * @return splitbyteの値
         * @throws NoSuchFieldException
         * @throws IllegalAccessException
         */
        private int getSplitbyte(final IHTTPSession session) throws NoSuchFieldException, IllegalAccessException {
            Class c = session.getClass();
            Field fld = c.getDeclaredField("splitbyte");
            fld.setAccessible(true);
            return (Integer) fld.get(session);
        }

        /**
         * HTTPSession#rlenの値を取得する.
         * <p>
         * privateのフィールドにアクセスして、値を取得します。
         * </p>
         * @param session HTTPセッション
         * @return splitbyteの値
         * @throws NoSuchFieldException
         * @throws IllegalAccessException
         */
        private int getRlen(final IHTTPSession session) throws NoSuchFieldException, IllegalAccessException {
            Class c = session.getClass();
            Field fld = c.getDeclaredField("rlen");
            fld.setAccessible(true);
            return (Integer) fld.get(session);
        }

        /**
         * Httpリクエストのbodyを解析して、DConnectHttpRequestに値を格納します.
         *
         * @param session Httpリクエストのセッションデータ
         * @param request Httpリクエストを格納するインスタンス
         * @throws IOException セッションのアクセスに失敗した場合
         * @throws ResponseException レスポンスの作成に失敗した場合
         */
        private void parseBody(final IHTTPSession session, final DConnectHttpRequest request) throws IOException, ResponseException {
            Map<String, String> headers = session.getHeaders();
            if (!session.getMethod().equals(Method.PUT)
                    && !session.getMethod().equals(Method.POST)
                    && !headers.containsKey("content-length")) {
                return;
            }

            Map<String, String> files = new HashMap<>();
            RandomAccessFile randomAccessFile = null;
            try {
                long size = getBodySize(session);
                ByteArrayOutputStream baos = null;
                DataOutput requestDataOutput;
                // Store the request in memory or a file, depending on size
                if (size < MEMORY_STORE_LIMIT) {
                    baos = new ByteArrayOutputStream();
                    requestDataOutput = new DataOutputStream(baos);
                } else {
                    randomAccessFile = getTmpBucket(session);
                    requestDataOutput = randomAccessFile;
                }

                InputStream inputStream = session.getInputStream();
                int len = 0;
                byte[] buf = new byte[REQUEST_BUFFER_LEN];
                while (len >= 0 && size > 0) {
                    len = inputStream.read(buf, 0, (int) Math.min(size, REQUEST_BUFFER_LEN));
                    size -= len;
                    if (len > 0) {
                        requestDataOutput.write(buf, 0, len);
                    }
                }

                TempBuffer tmpBuf;
                if (baos != null) {
                    tmpBuf = new TempByteBuffer(ByteBuffer.wrap(baos.toByteArray(), 0, baos.size()));
                } else {
                    try {
                        tmpBuf = new TempByteBuffer(randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length()));
                    } catch (IOException e) {
                        tmpBuf = new TempFileBuffer(randomAccessFile);
                    }
                    randomAccessFile.seek(0);
                }

                if (Method.POST.equals(session.getMethod()) || Method.PUT.equals(session.getMethod())) {
                    ContentType contentType = new ContentType(session.getHeaders().get("content-type"));
                    if (contentType.isMultipart()) {
                        String boundary = contentType.getBoundary();
                        if (boundary == null) {
                            throw new ResponseException(BAD_REQUEST,
                                    "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
                        }
                        decodeMultipartFormData(session, contentType, tmpBuf, request.getQueryParameters(), files);
                    } else {
                        byte[] postBytes = new byte[tmpBuf.remaining()];
                        tmpBuf.get(postBytes);
                        // MEMO: contentTypeの文字コードを設定するとデフォルトでASCIIになり文字化けを起こす
//                      String postLine = new String(postBytes, contentType.getEncoding()).trim();
                        String postLine = new String(postBytes, mConfig.getCharset()).trim();
                        if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType.getContentType())) {
                            decodeParms(postLine, request.getQueryParameters());
                        } else if (postLine.length() != 0) {
                            files.put("postData", postLine);
                        }
                    }
                }

                request.setFiles(files);
            } finally {
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * Content-Lengthのサイズを取得する.
         * <p>
         * Content-Lengthが存在しない場合には、0を返却する。
         * </p>
         * @param session Httpリクエストのセッションデータ
         * @return Content-Lengthのサイズ
         */
        private long getBodySize(final IHTTPSession session) {
            long size = 0;
            if (session.getHeaders().containsKey("content-length")) {
                size = Integer.parseInt(session.getHeaders().get("content-length"));
            }
            return size;
        }

        /**
         * TempFileManagerのインスタンスを取得する.
         * <p>
         * TempFileManagerのインスタンスはprivateになっているために通常はアクセスできない。
         * その問題を回避するためにリフレクションを使用している。
         * </p>
         * <p>
         * MEMO: RetroGuardなどでミニファイされた場合には、動作しなくなるので注意
         * </p>
         * @param session Httpリクエストのセッションデータ
         * @return TempFileManagerのインスタンス
         */
        private TempFileManager getTempFileManager(final IHTTPSession session) {
            try {
                Class c = HTTPSession.class;
                Field fld = c.getDeclaredField("tempFileManager");
                fld.setAccessible(true);
                return (TempFileManager) fld.get(session);
            } catch (NoSuchFieldException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            }
        }

        /**
         * multipartで送られてきデータを一時的に格納するファイルを作成する.
         * @param session Httpリクエストのセッションデータ
         * @return 一時的なファイル
         */
        private RandomAccessFile getTmpBucket(final IHTTPSession session) {
            try {
                TempFileManager mgr = getTempFileManager(session);
                if (mgr == null) {
                    throw new RuntimeException("Cannot get a TempFileManager.");
                }
                TempFile tempFile = mgr.createTempFile(null);
                return new RandomAccessFile(tempFile.getName(), "rw");
            } catch (Exception e) {
                throw new Error(e); // we won't recover, so throw an error
            }
        }

        /**
         * Find the byte positions where multipart boundaries start. This reads
         * a large block at a time and uses a temporary buffer to optimize
         * (memory mapped) file access.
         */
        private int[] getBoundaryPositions(final TempBuffer b, final byte[] boundary) throws IOException {
            int[] res = new int[0];
            if (b.remaining() < boundary.length) {
                return res;
            }

            int search_window_pos = 0;
            byte[] search_window = new byte[4 * 1024 + boundary.length];

            int first_fill = (b.remaining() < search_window.length) ? b.remaining() : search_window.length;
            b.get(search_window, 0, first_fill);
            int new_bytes = first_fill - boundary.length;

            do {
                // Search the search_window
                for (int j = 0; j < new_bytes; j++) {
                    for (int i = 0; i < boundary.length; i++) {
                        if (search_window[j + i] != boundary[i])
                            break;
                        if (i == boundary.length - 1) {
                            // Match found, add it to results
                            int[] new_res = new int[res.length + 1];
                            System.arraycopy(res, 0, new_res, 0, res.length);
                            new_res[res.length] = search_window_pos + j;
                            res = new_res;
                        }
                    }
                }
                search_window_pos += new_bytes;

                // Copy the end of the buffer to the start
                System.arraycopy(search_window, search_window.length - boundary.length, search_window, 0, boundary.length);

                // Refill search_window
                new_bytes = search_window.length - boundary.length;
                new_bytes = (b.remaining() < new_bytes) ? b.remaining() : new_bytes;
                b.get(search_window, boundary.length, new_bytes);
            } while (new_bytes > 0);
            return res;
        }

        /**
         * 改行コードまでオフセットを移動する.
         * @param partHeaderBuff データ
         * @param index オフセット
         * @return 移動したインデックス
         */
        private int scipOverNewLine(final byte[] partHeaderBuff, int index) {
            while (partHeaderBuff[index] != '\n') {
                index++;
            }
            return ++index;
        }

        /**
         * Decodes parameters in percent-encoded URI-format ( e.g.
         * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
         * Map. NOTE: this doesn't support multiple identical keys due to the
         * simplicity of Map.
         */
        private void decodeParms(final String parms, final Map<String, String> p) {
            if (parms == null) {
                return;
            }

            StringTokenizer st = new StringTokenizer(parms, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf('=');
                if (sep >= 0) {
                    p.put(decodePercent(e.substring(0, sep)).trim(), decodePercent(e.substring(sep + 1)));
                } else {
                    p.put(decodePercent(e).trim(), "");
                }
            }
        }

        /**
         * multipartをデコードする.
         * @param session Httpリクエストのセッションデータ
         * @param contentType コンテントタイプ
         * @param fbuf bodyデータ
         * @param parms queryデータ
         * @param files multipartのファイルパスを格納するマップ
         * @throws ResponseException レスポンスの作成に失敗した場合
         */
        private void decodeMultipartFormData(final IHTTPSession session, final ContentType contentType, final TempBuffer fbuf,
                                             final Map<String, String> parms, final Map<String, String> files) throws ResponseException {
            int pcount = 0;
            try {
                int[] boundaryIdxs = getBoundaryPositions(fbuf, contentType.getBoundary().getBytes());
                if (boundaryIdxs.length < 2) {
                    throw new ResponseException(BAD_REQUEST,
                            "BAD REQUEST: Content type is multipart/form-data but contains less than two boundary strings.");
                }

                byte[] partHeaderBuff = new byte[MAX_HEADER_SIZE];
                for (int boundaryIdx = 0; boundaryIdx < boundaryIdxs.length - 1; boundaryIdx++) {
                    fbuf.position(boundaryIdxs[boundaryIdx]);
                    int len = (fbuf.remaining() < MAX_HEADER_SIZE) ? fbuf.remaining() : MAX_HEADER_SIZE;
                    fbuf.get(partHeaderBuff, 0, len);
                    BufferedReader in =
                            new BufferedReader(new InputStreamReader(
                                    new ByteArrayInputStream(partHeaderBuff, 0, len),
                                        Charset.forName(contentType.getEncoding())), len);

                    int headerLines = 0;
                    // First line is boundary string
                    String mpline = in.readLine();
                    headerLines++;
                    if (mpline == null || !mpline.contains(contentType.getBoundary())) {
                        throw new ResponseException(BAD_REQUEST,
                                "BAD REQUEST: Content type is multipart/form-data but chunk does not start with boundary.");
                    }

                    String partName = null, fileName = null, partContentType = null;
                    // Parse the reset of the header lines
                    mpline = in.readLine();
                    headerLines++;
                    while (mpline != null && mpline.trim().length() > 0) {
                        Matcher matcher = CONTENT_DISPOSITION_PATTERN.matcher(mpline);
                        if (matcher.matches()) {
                            String attributeString = matcher.group(2);
                            matcher = CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString);
                            while (matcher.find()) {
                                String key = matcher.group(1);
                                if ("name".equalsIgnoreCase(key)) {
                                    partName = matcher.group(2);
                                } else if ("filename".equalsIgnoreCase(key)) {
                                    fileName = matcher.group(2);
                                    // add these two line to support multiple
                                    // files uploaded using the same field Id
                                    if (!fileName.isEmpty()) {
                                        if (pcount > 0)
                                            partName = partName + String.valueOf(pcount++);
                                        else
                                            pcount++;
                                    }
                                }
                            }
                        }
                        matcher = CONTENT_TYPE_PATTERN.matcher(mpline);
                        if (matcher.matches()) {
                            partContentType = matcher.group(2).trim();
                        }
                        mpline = in.readLine();
                        headerLines++;
                    }
                    int partHeaderLength = 0;
                    while (headerLines-- > 0) {
                        partHeaderLength = scipOverNewLine(partHeaderBuff, partHeaderLength);
                    }
                    // Read the part data
                    if (partHeaderLength >= len - 4) {
                        throw new ResponseException(Response.Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
                    }
                    int partDataStart = boundaryIdxs[boundaryIdx] + partHeaderLength;
                    int partDataEnd = boundaryIdxs[boundaryIdx + 1] - 4;

                    fbuf.position(partDataStart);
                    if (partContentType == null) {
                        // Read the part into a string
                        byte[] data_bytes = new byte[partDataEnd - partDataStart];
                        fbuf.get(data_bytes);
                        // MEMO: デフォルトの文字コードでマルチパートの文字列は取得する
//                        parms.put(partName, new String(data_bytes, contentType.getEncoding()));
                        parms.put(partName, new String(data_bytes, mConfig.getCharset()));
                    } else {
                        // Read it into a file
                        String path = saveTmpFile(session, fbuf, partDataStart, partDataEnd - partDataStart, fileName);
                        if (!files.containsKey(partName)) {
                            files.put(partName, path);
                        } else {
                            int count = 2;
                            while (files.containsKey(partName + count)) {
                                count++;
                            }
                            files.put(partName + count, path);
                        }
                        // MEMO: パラメータ名はクエリに追加しない
//                        parms.put(partName, fileName);
                    }
                }
            } catch (ResponseException re) {
                throw re;
            } catch (Exception e) {
                throw new ResponseException(Response.Status.INTERNAL_ERROR, "INTERNAL ERROR: Exception. e=" + e.toString());
            }
        }

        /**
         * Retrieves the content of a sent file and saves it to a temporary
         * file. The full path to the saved file is returned.
         */
        private String saveTmpFile(final IHTTPSession session, final TempBuffer b, final int offset, final int len, final String filename_hint) {
            String path = "";
            if (len > 0) {
                FileOutputStream fileOutputStream = null;
                try {
                    TempFileManager mgr = getTempFileManager(session);
                    if (mgr == null) {
                        throw new RuntimeException("Cannot get a TempFileManager.");
                    }
                    TempFile tempFile = mgr.createTempFile(filename_hint);
                    fileOutputStream = new FileOutputStream(tempFile.getName());
                    b.write(fileOutputStream, offset, len);
                    path = tempFile.getName();
                } catch (Exception e) { // Catch exception if any
                    throw new Error(e); // we won't recover, so throw an error
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return path;
        }

        /**
         * レスポンスにCORSヘッダーを追加します.
         *
         * @param queryHeaders リクエストデータにあるヘッダー一覧
         * @param nanoRes      CORSヘッダーを格納するレスポンスデータ
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
         * HttpResponseからNanoHTTPD.Responseに変換する.
         *
         * @param res HttResponse
         * @return 変換されたNanoHTTPD.Response
         */
        private Response newFixedLengthResponse(final DConnectHttpResponse res) {
            HttpResponse.StatusCode statusCode = res.getStatusCode();
            Response nanoRes = newFixedLengthResponse(getStatus(statusCode), res.getContentType(), res.getInputStream(), res.getContentLength());
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
    }

    /**
     * NanoWSD.WebSocketの実装クラス.
     *
     * @author NTT DOCOMO, INC.
     */
    private class NanoWebSocket extends NanoWSD.WebSocket implements DConnectWebSocket {

        /**
         * KeepAlive実行用のタイマー.
         */
        private Timer mKeepAliveTimer;

        /**
         * Keep-Aliveのタスク.
         */
        private final KeepAliveTask mKeepAliveTask;

        /**
         * WebSocketを識別するID.
         */
        private final UUID mId = UUID.randomUUID();

        /**
         * コンストラクタ.
         *
         * @param handshakeRequest リクエスト
         */
        NanoWebSocket(final IHTTPSession handshakeRequest) {
            super(handshakeRequest);
            mKeepAliveTask = new KeepAliveTask();
            mKeepAliveTimer = new Timer();
            mKeepAliveTimer.scheduleAtFixedRate(mKeepAliveTask,
                    WEBSOCKET_KEEP_ALIVE_INTERVAL, WEBSOCKET_KEEP_ALIVE_INTERVAL);

            mSockets.put(getId(), this);
            if (mListener != null) {
                mListener.onWebSocketConnected(this);
            }
        }

        // Implements DConnectWebSocket

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
            String origin = request.getHeaders().get("x-gotapi-origin");
            if (origin == null) {
                origin = request.getHeaders().get("origin");
            }
            return origin;
        }

        @Override
        public void sendMessage(final String message) {
            try {
                if (isOpen()) {
                    send(message);
                }
            } catch (IOException e) {
                mLogger.warning("Exception in the NanoWebSocket#sendMessage() method. " + e.toString());
                if (mListener != null) {
                    mListener.onError(DConnectServerError.SEND_EVENT_FAILED);
                    mListener.onWebSocketDisconnected(this);
                }
            }
        }

        @Override
        public void sendMessage(final byte[] payload) {
            try {
                if (isOpen()) {
                    send(payload);
                }
            } catch (IOException e) {
                mLogger.warning("Exception in the NanoWebSocket#sendMessage() method. " + e.toString());
                if (mListener != null) {
                    mListener.onError(DConnectServerError.SEND_EVENT_FAILED);
                    mListener.onWebSocketDisconnected(this);
                }
            }
        }

        @Override
        public void disconnect() {
            try {
                close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "Disconnect WebSocket.", false);
            } catch (IOException e) {
                mLogger.warning("Exception in the NanoWebSocket#disconnect() method. " + e.toString());
            }
        }

        // Implements NanoWSD.WebSocket

        @Override
        protected void onOpen() {
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

            if (mListener != null) {
                mListener.onWebSocketMessage(this, jsonText);
            }
        }

        @Override
        protected void onClose(final NanoWSD.WebSocketFrame.CloseCode code, final String reason, final boolean initiatedByRemote) {
            mLogger.fine("WebSocket closed. id = " + getId());

            mSockets.remove(getId());
            if (mListener != null) {
                mListener.onWebSocketDisconnected(this);
            }
            mKeepAliveTimer.cancel();
        }

        @Override
        protected void onException(final IOException e) {
            mLogger.warning("Exception in the NanoWebSocket#onException() method. " + e.toString());
        }

        @Override
        public String toString() {
            return "{ id=" + getId() + " origin=" + getClientOrigin() + ", uri=" + getUri() + " }";
        }

        /**
         * Keep-Alive用タイマータスク.
         *
         * @author NTT DOCOMO, INC.
         */
        private class KeepAliveTask extends TimerTask {

            /**
             * 処理状態.
             */
            private KeepAliveState mState;

            /**
             * コンストラクタ.
             */
            KeepAliveTask() {
                setState(KeepAliveState.GOT_PONG);
            }

            /**
             * 状態を変更する.
             *
             * @param state 状態
             */
            void setState(final KeepAliveState state) {
                mState = state;
            }

            /**
             * 状態を取得する.
             *
             * @return 状態
             */
            KeepAliveState getState() {
                return mState;
            }

            @Override
            public void run() {
                try {
                    synchronized (this) {
                        if (mState == KeepAliveState.GOT_PONG) {
                            setState(KeepAliveState.WAITING_PONG);
                            ping("DConnectServer".getBytes());
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

    /**
     * NanoHTTPDが使用するファイルを管理するクラスを作成するファクトリー.
     *
     * @author NTT DOCOMO, INC.
     */
    private class NanoTempFileManagerFactory implements NanoHTTPD.TempFileManagerFactory {
        /**
         * 一時的にファイルを保持するフォルダへのパス.
         */
        private final File mCacheDir;

        /**
         * コンストラクタ.
         * @param dir 一時的にファイルを保持するフォルダへのパス.
         */
        NanoTempFileManagerFactory(final String dir) {
            mCacheDir = new File(dir);
        }

        @Override
        public NanoHTTPD.TempFileManager create() {
            return new NanoTempFileManager(mCacheDir);
        }
    }

    /**
     * NanoHTTPDが使用するファイルを管理するクラス.
     *
     * @author NTT DOCOMO, INC.
     */
    private class NanoTempFileManager implements NanoHTTPD.TempFileManager {
        /**
         * 一時的にファイルを保持するフォルダへのパス.
         */
        private final File mCacheDir;

        /**
         * 一時的に作成したファイル一覧.
         */
        private final List<NanoHTTPD.TempFile> mTempFiles = new ArrayList<>();

        /**
         * コンストラクタ.
         * @param cacheDir 一時的にファイルを保持するフォルダへのパス.
         */
        NanoTempFileManager(final File cacheDir) {
            mCacheDir = cacheDir;

            if (!cacheDir.exists()) {
                if (!cacheDir.mkdirs()) {
                    if (DEBUG) {
                        Log.e(TAG, "Failed to create a dir. path=" + cacheDir);
                    }
                }
            }
        }

        @Override
        public void clear() {
            for (NanoHTTPD.TempFile file : mTempFiles) {
                try {
                    file.delete();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
            mTempFiles.clear();
        }

        @Override
        public NanoHTTPD.TempFile createTempFile(final String filename_hint) throws Exception {
            NanoHTTPD.TempFile tempFile = new DConnectTempFile(mCacheDir);
            mTempFiles.add(tempFile);
            return tempFile;
        }

        /**
         * 一時的なファイルを管理するクラス.
         */
        private class DConnectTempFile implements NanoHTTPD.TempFile {

            /**
             * ファイル.
             */
            private final File mFile;

            /**
             * ファイルへの書き込み用ストリーム.
             */
            private final OutputStream mOutputStream;

            /**
             * コンストラクタ.
             * @param tempDir キャッシュ用フォルダ
             * @throws IOException ファイルの作成に失敗した場合
             */
            private DConnectTempFile(final File tempDir) throws IOException {
                mFile = File.createTempFile("DConnectHTTPD-", "", tempDir);
                mOutputStream = new FileOutputStream(mFile);
            }

            @Override
            public void delete() throws Exception {
                if (mOutputStream != null) {
                    mOutputStream.close();
                }

                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!mFile.delete()) {
                            mLogger.warning("Failed to delete file." + mFile.getName());
                        }
                    }
                }, 30 * 1000);
            }

            @Override
            public String getName() {
                return mFile.getAbsolutePath();
            }

            @Override
            public OutputStream open() throws Exception {
                return mOutputStream;
            }
        }
    }

    private interface TempBuffer {
        TempBuffer position(int position) throws IOException;
        int remaining() throws IOException;
        TempBuffer get(byte[] dst, int dstOffset, int byteCount) throws IOException;
        TempBuffer get(byte[] dst) throws IOException;

        void write(FileOutputStream out, int offset, int length) throws IOException;
    }

    private class TempFileBuffer implements TempBuffer {

        private RandomAccessFile mRandomAccessFile;
        private long mLimit;
        private long mPosition;

        TempFileBuffer(final RandomAccessFile file) throws IOException {
            mRandomAccessFile = file;
            mPosition = 0;
            mLimit = file.length();
        }

        @Override
        public TempBuffer position(final int position) throws IOException {
            mPosition = position;
            mRandomAccessFile.seek(position);
            return this;
        }

        @Override
        public int remaining() {
            return (int) (mLimit - mPosition);
        }

        @Override
        public TempBuffer get(final byte[] dst, final int dstOffset, final int byteCount) throws IOException {
            mRandomAccessFile.seek(mPosition);
            mRandomAccessFile.readFully(dst, dstOffset, byteCount);
            mPosition += byteCount;
            return this;
        }

        @Override
        public TempBuffer get(final byte[] dst) throws IOException {
            return get(dst, 0, dst.length);
        }

        @Override
        public void write(final FileOutputStream out, final int offset, final int length) throws IOException {
            mRandomAccessFile.seek(offset);
            byte[] buf = new byte[4096];
            int len = 4096;
            int size = length;
            while (size > 0) {
                mRandomAccessFile.readFully(buf, 0, len);
                out.write(buf, 0, len);
                size -= len;
                if (size < 4096) {
                    len = size;
                }
            }
        }
    }

    private class TempByteBuffer implements TempBuffer {

        private ByteBuffer mByteBuffer;

        TempByteBuffer(ByteBuffer buffer) {
            mByteBuffer = buffer;
        }

        @Override
        public TempBuffer position(final int position) {
            mByteBuffer.position(position);
            return this;
        }

        @Override
        public int remaining() {
            return mByteBuffer.remaining();
        }

        @Override
        public TempBuffer get(final byte[] dst, final int dstOffset, final int byteCount) {
            mByteBuffer.get(dst, dstOffset, byteCount);
            return this;
        }

        @Override
        public TempBuffer get(final byte[] dst) {
            mByteBuffer.get(dst);
            return this;
        }

        @Override
        public void write(final FileOutputStream out, final int offset, final int len) throws IOException {
            ByteBuffer src = mByteBuffer.duplicate();
            FileChannel dest = out.getChannel();
            src.position(offset).limit(offset + len);
            dest.write(src.slice());
        }
    }
}
