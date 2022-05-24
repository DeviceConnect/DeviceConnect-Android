/*
 HttpDConnectSDK.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.message.entity.BinaryEntity;
import org.deviceconnect.message.entity.Entity;
import org.deviceconnect.message.entity.FileEntity;
import org.deviceconnect.message.entity.MultipartEntity;
import org.deviceconnect.message.entity.StringEntity;
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
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
     * 接続のタイムアウト(ms).
     */
    private static final int CONNECT_TIMEOUT = 30 * 1000;

    /**
     * 読み込みのタイムアウト時間(ms).
     */
    private static final int READ_TIMEOUT = 3 * 60 * 1000;

    /**
     * WebSocketと接続を行うクラス.
     */
    private final DConnectWebSocketClient mWebSocketClient = new DConnectWebSocketClient();

    /**
     * マルチパートのバウンダリーに付加するハイフンを定義.
     */
    private final static String TWO_HYPHEN = "--";

    /**
     * マルチパートの改行コードを定義.
     */
    private final static String EOL = "\r\n";

    /**
     * {@link DConnectSDKFactory}で生成させるためにpackageスコープにしておく。
     */
    HttpDConnectSDK() {
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

        TrustManager[] transManagers = {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
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
     * コンテンツのサイズを計算する.
     * @param dataMap データ一覧
     * @param boundary バウンダリ
     * @return コンテンツサイズ
     * @throws IOException サイズの計算に失敗した場合に発生
     */
    private int calcContentLength(final Map<String, Entity> dataMap, final String boundary) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int size = writeMultipart(out, dataMap, boundary, false);
        return out.size() + size;
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
    private int writeMultipart(final OutputStream out, final Map<String, Entity> dataMap, final String boundary, final boolean writeFlag) throws IOException {
        int contentLength = 0;
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        for (Map.Entry<String, Entity> data : dataMap.entrySet()) {
            String key = data.getKey();
            Entity val = data.getValue();

            if (val instanceof StringEntity) {
                writer.write(String.format("%s%s%s", TWO_HYPHEN, boundary, EOL));
                writer.write(String.format("Content-Disposition: form-data; name=\"%s\"%s", key, EOL));
                writer.write(EOL);
                writer.write(((StringEntity) val).getContent());
                writer.write(EOL);
            } else if (val instanceof BinaryEntity) {
                contentLength += (((BinaryEntity) val).getContent()).length;

                writer.write(String.format("%s%s%s", TWO_HYPHEN, boundary, EOL));
                writer.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", key, ((BinaryEntity) val).getName(), EOL));
                writer.write(String.format("Content-Type: application/octet-stream%s", EOL));
                writer.write(String.format("Content-Transfer-Encoding: binary%s", EOL));
                writer.write(EOL);
                writer.flush();

                if (writeFlag) {
                    out.write(((BinaryEntity) val).getContent());
                }

                writer.write(EOL);
            } else if (val instanceof FileEntity) {
                File file = ((FileEntity) val).getContent();

                contentLength += file.length();

                writer.write(String.format("%s%s%s", TWO_HYPHEN, boundary, EOL));
                writer.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", key, file.getName(), EOL));
                writer.write(String.format("Content-Type: application/octet-stream%s", EOL));
                writer.write(String.format("Content-Transfer-Encoding: binary%s", EOL));
                writer.write(EOL);
                writer.flush();

                if (writeFlag) {
                    writeFile(out, file);
                }

                writer.write(EOL);
            } else {
                throw new IllegalArgumentException("data is not String or File. key=" + key + " value=" + val);
            }
            writer.flush();
        }

        writer.write(String.format("%s%s%s%s", TWO_HYPHEN, boundary, TWO_HYPHEN, EOL));
        writer.flush();

        return contentLength;
    }

    /**
     * 指定されたストリームにファイルデータを書き込む.
     * <p>
     * 指定されたファイルが見つからない場合にはIOExceptionを発生する。
     * </p>
     * @param out データを書き込むストリーム
     * @param file 書き込みファイル
     * @throws IOException 読み込むファイルが見つからない場合に発生
     */
    private void writeFile(final OutputStream out, final File file) throws IOException {
        byte[] buf = new byte[4096];
        int len;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            while ((len = fis.read(buf)) > 0) {
                out.write(buf, 0, len);
                out.flush();
            }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 指定したURIに接続を行い通信結果を返却する.
     *
     * @param method HTTPメソッド
     * @param uri 通信先のURI
     * @param headers HTTPリクエストに追加するヘッダーリスト(ヘッダーを追加しない場合にはnull)
     * @param body HTTPリクエストに追加するボディデータ(ボディを追加しない場合にはnull)
     * @return 通信結果
     * @throws IOException HttpsURLConnectionの生成に失敗した場合に発生
     * @throws NoSuchAlgorithmException SSLの暗号化に失敗した場合に発生
     * @throws KeyManagementException Keyの管理に失敗した場合の発生
     */
    private byte[] connect(final Method method, final String uri, final Map<String, String> headers, final Entity body)
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        if (DEBUG) {
            Log.d(TAG, "connect: method=" + method + " uri=" + uri);
            if (headers != null) {
                for (String key : headers.keySet()) {
                    Log.d(TAG, "header: " + key + "=" + headers.get(key));
                }
            }
            if (body != null) {
                Log.d(TAG, "body: " + body);
            }
        }

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

            // マルチパートのContentTypeを設定する
            if (body instanceof MultipartEntity) {
                conn.setRequestProperty("Content-Type", String.format("multipart/form-data; boundary=%s", boundary));
                conn.setFixedLengthStreamingMode(calcContentLength(((MultipartEntity) body).getContent(), boundary));
            }
            conn.setUseCaches(false);

            conn.connect();

            // Bodyにデータが存在する場合には、データを書き込む
            if (body != null && (Method.POST.equals(method) || Method.PUT.equals(method))) {
                OutputStream os = conn.getOutputStream();
                if (body instanceof BinaryEntity) {
                    os.write(((BinaryEntity) body).getContent());
                } else if (body instanceof StringEntity) {
                    os.write(((StringEntity) body).getContent().getBytes());
                } else if (body instanceof FileEntity) {
                    writeFile(os, ((FileEntity) body).getContent());
                } else if (body instanceof MultipartEntity) {
                    writeMultipart(os, ((MultipartEntity) body).getContent(), boundary, true);
                }
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

                InputStream in = conn.getErrorStream();
                int len;
                byte[] buf = new byte[BUF_SIZE];
                while ((len = in.read(buf)) > 0) {
                    baos.write(buf, 0, len);
                }
                in.close();
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
            return new DConnectResponseMessage(DConnectMessage.ErrorCode.ACCESS_FAILED);
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
                                                  final Map<String, String> headers, final Entity body) {
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
            return createTimeoutResponse();
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

        URIBuilder builder = createURIBuilder();
        builder.setScheme(isSSL() ? "wss" : "ws");
        builder.setPath("/gotapi/websocket");

        mWebSocketClient.setOnWebSocketListener(listener);
        mWebSocketClient.connect(builder.toString(), getOrigin(), getAccessToken());
    }

    @Override
    public void disconnectWebSocket() {
        mWebSocketClient.close();
    }

    @Override
    public boolean isConnectedWebSocket() {
        return mWebSocketClient.isConnected();
    }

    @Override
    public void addEventListener(final Uri uri, final OnEventListener listener) {

        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }

        if (listener == null) {
            throw new NullPointerException("listener is null.");
        }

        if (mWebSocketClient.hasEventListener(uri)) {
            mWebSocketClient.addEventListener(uri, listener);

            // 既にリスナーが登録されているので、ここでレスポンスを返しておく
            try {
                String json = "{\"result\" : 0}";
                DConnectResponseMessage responseMessage = new DConnectResponseMessage(json);
                listener.onResponse(responseMessage);
            } catch (JSONException e) {
                // ignore.
            }
        } else {
            put(uri, null, new OnResponseListener() {
                @Override
                public void onResponse(final DConnectResponseMessage response) {
                    if (response.getResult() == DConnectMessage.RESULT_OK) {
                        mWebSocketClient.addEventListener(uri, listener);
                    }
                    listener.onResponse(response);
                }
            });
        }
    }

    @Override
    public void removeEventListener(final Uri uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }

        mWebSocketClient.removeEventListener(uri);

        delete(uri, new OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
            }
        });
    }

    @Override
    public void removeEventListener(Uri uri, OnEventListener listener) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }

        if (listener == null) {
            throw new NullPointerException("listener is null.");
        }

        mWebSocketClient.removeEventListener(uri, listener);

        // リスナーが空の場合は停止命令を行う
        if (!mWebSocketClient.hasEventListener(uri)) {
            delete(uri, new OnResponseListener() {
                @Override
                public void onResponse(final DConnectResponseMessage response) {
                }
            });
        }
    }
}
