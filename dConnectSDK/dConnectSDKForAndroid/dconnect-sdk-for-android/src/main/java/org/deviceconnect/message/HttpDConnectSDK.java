/*
 HttpDConnectSDK.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.sdk.BuildConfig;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static android.R.attr.key;

/**
 * HTTP通信を使用してDevice Connect Managerと通信を行うSDKクラス.
 * @author NTT DOCOMO, INC.
 */
class HttpDConnectSDK extends DConnectSDK {
    /**
     * デバック用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバック用タグを定義します.
     */
    private static final String TAG = "DConnectSDK";

    /**
     * バッファサイズを定義します.
     */
    private static final int BUF_SIZE = 4096;

    /**
     * 成功となるレスポンスコードを定義します.
     */
    private static final int SUCCESS_RESPONSE_CODE = 200;

    /**
     * 接続のタイムアウト.
     */
    private static final int CONNECT_TIMEOUT = 10 * 1000;

    /**
     * 読み込みのタイムアウト時間.
     */
    private static final int READ_TIMEOUT = 30 * 1000;

    /**
     * WebSocketと接続を行うクラス.
     */
    private DConnectWebSocketClient mWebSocketClient;

    /**
     * マルチパートのバウンダリーに付加するハイフンを定義.
     */
    private final static String TWO_HYPHEN = "--";

    /**
     * マルチパートの改行コードを定義.
     */
    private final static String EOL = "\r\n";

    private Context mContext;

    /**
     * {@link DConnectSDKFactory}で生成させるためにpackageスコープにしておく。
     */
    HttpDConnectSDK(final Context context) {
        mContext = context;
    }

    /**
     * 勝手サーバ証明書を許諾するHttpsURLConnectionを生成する.
     *
     * @param url 接続先のURL
     * @return HttpsURLConnectionのインスタンス
     * @throws IOException HttpsURLConnectionの生成に失敗した場合に発生
     * @throws NoSuchAlgorithmException SSLの暗号化に失敗した場合に発生
     * @throws KeyManagementException Keyの管理に失敗した場合の発生
     */
    private HttpsURLConnection makeHttpsURLConnection(final URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(final String hostname, final SSLSession sslSession) {
                return true;
            }
        });

        TrustManager[] transManagers = {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        SSLContext sslcontext = SSLContext.getInstance("SSL");
        sslcontext.init(null, transManagers, new SecureRandom());
        connection.setSSLSocketFactory(sslcontext.getSocketFactory());

        return connection;
    }

    /**
     * マルチパートを指定したストリームに書き込む.
     * <p>
     * writeFlagがfalseの場合には、コンテンツのデータはストリームに書き込まない。<br>
     * Content-Lengthのサイズを算出する時に使用します。
     * </p>
     * @param out マルチパートを書き込むストリーム
     * @param dataMap マルチパートに書き込むデータ
     * @param boundary マルチパートのバウンダリー
     * @param writeFlag コンテンツデータを書き込むフラグ
     * @return コンテンツデータサイズ
     * @throws IOException ファイルのオープンやストリームの書き込みに失敗した場合に発生
     */
    private int writeMultipart(final OutputStream out, final Map<String, String> dataMap, final String boundary, final boolean writeFlag) throws IOException {
        int contentLength = 0;
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        for (Map.Entry<String, String> data : dataMap.entrySet()) {
            String key = data.getKey();
            String val = data.getValue();

            File file = new File(val);
            if (val.startsWith("content://")) {
                Uri uri = Uri.parse(val);

                writer.write(String.format("%s%s%s", TWO_HYPHEN, boundary, EOL));
                writer.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", key, uri.getLastPathSegment(), EOL));
                writer.write(String.format("Content-Type: application/octet-stream%s", EOL));
                writer.write(String.format("Content-Transfer-Encoding: binary%s", EOL));
                writer.write(EOL);
                writer.flush();

                ContentResolver resolver = mContext.getContentResolver();
                InputStream in = null;
                int len;
                byte[] buffer = new byte[4096];
                try {
                    in = resolver.openInputStream(uri);
                    while ((len = in.read(buffer)) != -1) {
                        if (writeFlag) {
                            out.write(buffer, 0, len);
                        }
                        contentLength += len;
                    }
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }

                writer.write(EOL);
            } else if (!file.isFile()) {
                writer.write(String.format("%s%s%s", TWO_HYPHEN, boundary, EOL));
                writer.write(String.format("Content-Disposition: form-data; name=\"%s\"%s", key, EOL));
                writer.write(EOL);
                writer.write(val);
                writer.write(EOL);
            } else {
                contentLength += file.length();

                writer.write(String.format("%s%s%s", TWO_HYPHEN, boundary, EOL));
                writer.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", key, file.getName(), EOL));
                writer.write(String.format("Content-Type: application/octet-stream%s", EOL));
                writer.write(String.format("Content-Transfer-Encoding: binary%s", EOL));
                writer.write(EOL);
                writer.flush();

                if (writeFlag) {
                    int len;
                    byte[] buffer = new byte[4096];
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                        while ((len = fis.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                    } finally {
                        if (fis != null) {
                            fis.close();
                        }
                    }
                }

                writer.write(EOL);
            }
            writer.flush();
        }

        writer.write(String.format("%s%s%s%s", TWO_HYPHEN, boundary, TWO_HYPHEN, EOL));
        writer.flush();

        return contentLength;
    }

    /**
     * マルチパートのContent-Lengthを算出する.
     * @param dataMap マルチパートに書き込むデータ
     * @param boundary マルチパートのバウンダリー
     * @return コンテンツのサイズ
     * @throws IOException ファイルの読み込みに失敗した場合に発生
     */
    private int calcMultipart(final Map<String, String> dataMap, final String boundary) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int contentLength = writeMultipart(out, dataMap, boundary, false);
        return contentLength + out.size();
    }

    /**
     * 指定したURIに接続を行い通信結果を返却する.
     *
     * @param method HTTPメソッド
     * @param uri 通信先のURI
     * @param headers HTTPリクエストに追加するヘッダーリスト(ヘッダーを追加しない場合にはnull)
     * @param dataMap HTTPリクエストに追加するボディデータ(ボディを追加しない場合にはnull)
     * @return 通信結果
     * @throws IOException HttpsURLConnectionの生成に失敗した場合に発生
     * @throws NoSuchAlgorithmException SSLの暗号化に失敗した場合に発生
     * @throws KeyManagementException Keyの管理に失敗した場合の発生
     */
    private byte[] connect(final Method method, final String uri, final Map<String, String> headers, final Map<String, String> dataMap)
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        if (DEBUG) {
            Log.d(TAG, "connect: method=" + method + " uri=" + uri);
            if (headers != null) {
                for (String key : headers.keySet()) {
                    Log.d(TAG, "header: " + key + "=" + headers.get(key));
                }
            }
            if (dataMap != null) {
                Log.d(TAG, "body: " + dataMap);
            }
        }

        int contentLength = 0;
        String boundary = String.format("%x", new Random().hashCode());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpURLConnection conn = null;
        try {
            if (uri.startsWith("https://")) {
                conn = makeHttpsURLConnection(new URL(uri));
            } else {
                conn = (HttpURLConnection) new URL(uri).openConnection();
            }
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod(method.getValue());
            conn.setDoInput(true);
            conn.setDoOutput(Method.POST.equals(method) || Method.PUT.equals(method));
            if (headers != null) {
                for (String key : headers.keySet()) {
                    conn.setRequestProperty(key, headers.get(key));
                }
            }

            // GotAPI 1.1からヘッダーにオリジンが必須になったので、ここで追加を行う
            // 参考: http://technical.openmobilealliance.org/Technical/technical-information/release-program/current-releases/generic-open-terminal-api-framework-1-1
            if (getOrigin() != null) {
                conn.setRequestProperty(DConnectMessage.HEADER_GOTAPI_ORIGIN, getOrigin());
            }

            // 4.x系はkeep-aliveを行うと例外が発生するため、offにする
            // 参考: http://osa030.hatenablog.com/entry/2015/05/22/181155
            if (Build.VERSION.SDK_INT > 13 && Build.VERSION.SDK_INT < 19) {
                conn.setRequestProperty("Connection", "close");
            }

            // マルチパートのデータを生成する
            if (dataMap != null && !dataMap.isEmpty()) {
                contentLength = calcMultipart(dataMap, boundary);
                if (contentLength > 0) {
                    conn.setRequestProperty("Content-Type", String.format("multipart/form-data; boundary=%s", boundary));
                    conn.setRequestProperty("Content-Length", String.valueOf(contentLength));
                }
            }

            conn.connect();

            // コンテンツサイズが0以上の場合には、データを書き込む
            if (contentLength > 0 && (Method.POST.equals(method) || Method.PUT.equals(method))) {
                OutputStream os = conn.getOutputStream();
                writeMultipart(os, dataMap, boundary, true);
                os.flush();
                os.close();
            }

            int resp = conn.getResponseCode();
            if (resp == SUCCESS_RESPONSE_CODE) {
                InputStream in = conn.getInputStream();
                int len;
                byte[] buf = new byte[BUF_SIZE];
                while ((len = in.read(buf)) > 0) {
                    baos.write(buf, 0, len);
                }
                in.close();
            } else {
                if (DEBUG) {
                    Log.w(TAG, "Failed to connect the server. response=" + resp);
                }
                return null;
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return baos.toByteArray();
    }

    /**
     * 指定されたデータからDConnectResponseMessageを生成する.
     * @param result 通信結果のデータ
     * @return DConnectResponseMessageのインスタンス
     */
    private DConnectResponseMessage createMessage(final byte[] result) {
        if (result == null) {
            return createErrorMessage(DConnectMessage.ErrorCode.UNKNOWN.getCode(), "Failed to connect a manager.");
        }
        try {
            return new DConnectResponseMessage(new String(result, "UTF-8"));
        } catch (JSONException e) {
            return createErrorMessage(DConnectMessage.ErrorCode.UNKNOWN.getCode(), e.getMessage());
        } catch (UnsupportedEncodingException e) {
            return createErrorMessage(DConnectMessage.ErrorCode.UNKNOWN.getCode(), e.getMessage());
        }
    }

    @Override
    protected DConnectResponseMessage sendRequest(final Method method, final Uri uri,
                                                  final Map<String, String> headers, final Map<String, String> body) {
        if (method == null) {
            throw new NullPointerException("method is null.");
        }

        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }

        try {
            return createMessage(connect(method, uri.toString(), headers, body));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("uri is invalid.");
        } catch (SocketTimeoutException e) {
            return createTimeout();
        } catch (Exception e) {
            return createErrorMessage(DConnectMessage.ErrorCode.UNKNOWN.getCode(), e.getMessage());
        } catch (OutOfMemoryError e) {
            return createErrorMessage(DConnectMessage.ErrorCode.UNKNOWN.getCode(), e.getMessage());
        }
    }

    @Override
    public void connectWebSocket(final OnWebSocketListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener is null.");
        }

        if (getOrigin() == null) {
            throw new IllegalStateException("origin is not set.");
        }

        if (mWebSocketClient != null) {
            return;
        }

        URIBuilder builder = createURIBuilder();
        builder.setScheme(isSSL() ? "wss" : "ws");
        builder.setPath("/gotapi/websocket");

        // TODO: WebSocketの接続に失敗した時にmWebSocketClientを初期化しないと接続できない。
        mWebSocketClient = new DConnectWebSocketClient();
        mWebSocketClient.setOnWebSocketListener(listener);
        mWebSocketClient.connect(builder.toString(), getOrigin(), getAccessToken());
    }

    @Override
    public void disconnectWebSocket() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
            mWebSocketClient = null;
        }
    }

    @Override
    public void addEventListener(final Uri uri, final OnEventListener listener) {

        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }

        if (listener == null) {
            throw new NullPointerException("listener is null.");
        }

        put(uri, null, new OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                if (response.getResult() == DConnectMessage.RESULT_OK && mWebSocketClient != null) {
                    mWebSocketClient.addEventListener(uri, listener);
                }
                listener.onResponse(response);
            }
        });
    }

    @Override
    public void removeEventListener(final Uri uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        delete(uri, new OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
            }
        });
        if (mWebSocketClient != null) {
            mWebSocketClient.removeEventListener(uri);
        }
    }
}
