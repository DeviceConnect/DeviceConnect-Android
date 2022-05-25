package org.deviceconnect.server.nanohttpd;

import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
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
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * HTTP通信を行うためのユーティリティクラス.
 */
public final class HttpUtils {
    /**
     * デバック用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバック用タグを定義します.
     */
    private static final String TAG = "DConnect";

    /**
     * バッファサイズを定義します.
     */
    private static final int BUF_SIZE = 4096;

    /**
     * エラーとなるレスポンスコードを定義します.
     */
    private static final int ERROR_RESPONSE_CODE = 400;

    /**
     * 接続のタイムアウト.
     */
    private static final int CONNECT_TIMEOUT = 10 * 1000;

    /**
     * 読み込みのタイムアウト時間.
     */
    private static final int READ_TIMEOUT = 3 * 60 * 1000;

    /**
     * POSTメソッドを定義します.
     */
    private static final String METHOD_POST = "POST";

    /**
     * PUTメソッドを定義します.
     */
    private static final String METHOD_PUT = "PUT";

    /**
     * GETメソッドを定義します.
     */
    private static final String METHOD_GET = "GET";

    /**
     * DELETEメソッドを定義します.
     */
    private static final String METHOD_DELETE = "DELETE";

    /**
     * マルチパートのバウンダリーに付加するハイフンを定義.
     */
    private final static String TWO_HYPHEN = "--";

    /**
     * マルチパートの改行コードを定義.
     */
    private final static String EOL = "\r\n";

    /**
     * コンストラクタ.
     * ユーティリティクラスなので、インスタンスは作成させない。
     */
    private HttpUtils() {
    }

    /**
     * SSLに対応したHttpsURLConnectionを生成する.
     * @param url 接続先のURL
     * @return HttpsURLConnectionのインスタンス。
     * @throws IOException 生成に失敗した場合に発生
     * @throws NoSuchAlgorithmException SSLを生成するためのアルゴリズムが見つからない場合に発生
     * @throws KeyManagementException SSLのキーの管理に失敗した場合に発生
     */
    private static HttpsURLConnection makeHttpsURLConnection(final URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
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
        SSLSocketFactory socketFactory = sslcontext.getSocketFactory();
        if (socketFactory == null) {
            throw new IOException("Failed to create SSLSocketFactory object.");
        }
        connection.setSSLSocketFactory(socketFactory);
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
    private static int writeMultipart(final OutputStream out, final Map<String, Object> dataMap, final String boundary, final boolean writeFlag) throws IOException {
        int contentLength = 0;
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        for (Map.Entry<String, Object> data : dataMap.entrySet()) {
            String key = data.getKey();
            Object val = data.getValue();

            if (val instanceof String) {
                writer.write(String.format("%s%s%s", TWO_HYPHEN, boundary, EOL));
                writer.write(String.format("Content-Disposition: form-data; name=\"%s\"%s", key, EOL));
                writer.write(EOL);
                writer.write((String) val);
                writer.write(EOL);
            } else if (val instanceof byte[]) {
                contentLength += ((byte[]) val).length;

                writer.write(String.format("%s%s%s", TWO_HYPHEN, boundary, EOL));
                writer.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", key, "byte", EOL));
                writer.write(String.format("Content-Type: application/octet-stream%s", EOL));
                writer.write(String.format("Content-Transfer-Encoding: binary%s", EOL));
                writer.write(EOL);
                writer.flush();

                if (writeFlag) {
                    out.write((byte[]) val);
                }

                writer.write(EOL);
            } else if (val instanceof File) {
                File file = (File) val;

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
     * コンテンツのサイズを計算する.
     * @param dataMap データ一覧
     * @param boundary バウンダリ
     * @return コンテンツサイズ
     * @throws IOException サイズの計算に失敗した場合に発生
     */
    private static int calcContentLength(final Map<String, Object> dataMap, final String boundary) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int size = writeMultipart(out, dataMap, boundary, false);
        return out.size() + size;
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
    private static void writeFile(final OutputStream out, final File file) throws IOException {
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
     * 指定されたサーバと通信を行い結果をresponseに格納する.
     * @param response レスポンスを格納するクラス
     * @param method HTTPメソッド
     * @param uri 接続先のURI
     * @param headers HTTPヘッダー
     * @param body HTTPボディ
     * @param invalidDataFlag 不正なデータを作成するフラグ
     */
    private static void connect(final Response response, final String method, final String uri, final Map<String, String> headers, final Object body, final boolean invalidDataFlag) {
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
        HttpURLConnection conn = null;
        try {
            if (uri.startsWith("https://")) {
                conn = makeHttpsURLConnection(new URL(uri));
            } else {
                conn = (HttpURLConnection) new URL(uri).openConnection();
            }
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod(method);
            conn.setDoInput(true);
            conn.setDoOutput(METHOD_POST.equals(method) || METHOD_PUT.equals(method));
            if (headers != null) {
                for (String key : headers.keySet()) {
                    conn.setRequestProperty(key, headers.get(key));
                }
            }

            // 4.x系はkeep-aliveを行うと例外が発生するため、offにする
            // 参考: http://osa030.hatenablog.com/entry/2015/05/22/181155
            if (Build.VERSION.SDK_INT > 13 && Build.VERSION.SDK_INT < 19) {
                conn.setRequestProperty("Connection", "close");
            }

            // マルチパートのContentTypeを設定する
            if (body != null && body instanceof Map) {
                if (invalidDataFlag) {
                    // invalidDataがtrueの場合にはmultipartのデータの一部を不正なデータにする
                    conn.setRequestProperty("Content-Type", String.format("multipart/form-data"));
                    conn.setFixedLengthStreamingMode(calcContentLength((Map) body, boundary));
                } else {
                    conn.setRequestProperty("Content-Type", String.format("multipart/form-data; boundary=%s", boundary));
                    conn.setFixedLengthStreamingMode(calcContentLength((Map) body, boundary));
                }
            }

            // キャッシュをOFF
            conn.setUseCaches(false);

            // オリジンの設定
            conn.setRequestProperty("Origin", "http://localhost");
            conn.connect();

            if (body != null && (METHOD_POST.equals(method) || METHOD_PUT.equals(method))) {
                OutputStream os = conn.getOutputStream();
                if (body instanceof byte[]) {
                    os.write((byte[]) body);
                } else if (body instanceof String) {
                    os.write(((String) body).getBytes());
                } else if (body instanceof File) {
                    writeFile(os, (File) body);
                } else if (body instanceof Map) {
                    writeMultipart(os, (Map) body, boundary, true);
                }
                os.flush();
                os.close();
            }

            int statusCode = conn.getResponseCode();
            response.setStatusCode(statusCode);

            if (DEBUG) {
                Log.d(TAG, "response code=" + statusCode);
                Log.d(TAG, "response length=" + conn.getContentLength());
            }

            if (statusCode < ERROR_RESPONSE_CODE) {
                File tempFile = File.createTempFile("temp", ".dat");
                FileOutputStream out = new FileOutputStream(tempFile);

                InputStream in = conn.getInputStream();
                int len;
                byte[] buf = new byte[BUF_SIZE];
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();

                response.setBody(tempFile);
            } else {
                if (DEBUG) {
                    Log.w(TAG, "Failed to connect the server. response=" + statusCode);
                }

                File tempFile = File.createTempFile("temp", ".dat");
                FileOutputStream out = new FileOutputStream(tempFile);

                InputStream in = conn.getErrorStream();
                int len;
                byte[] buf = new byte[BUF_SIZE];
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();

                response.setBody(tempFile);
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to connect the server.", e);
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to connect the server.", e);
            }
        } catch (OutOfMemoryError e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to connect the server.", e);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * HTTP接続を行う.
     * @param method HTTPメソッド(GET, POST, PUT, DELETE)
     * @param uri 接続先のURI
     * @param headers ヘッダー
     * @param body ボディ
     * @return レスポンス
     */
    public static Response connect(final String method, final String uri, final Map<String, String> headers, final Object body) {
        return connect(method, uri, headers, body, false);
    }

    /**
     * HTTP接続を行う.
     * @param method HTTPメソッド(GET, POST, PUT, DELETE)
     * @param uri 接続先のURI
     * @param headers ヘッダー
     * @param body ボディ
     * @param invalidDataFlag 不正なデータ作成フラグ
     * @return レスポンス
     */
    public static Response connect(final String method, final String uri, final Map<String, String> headers, final Object body, final boolean invalidDataFlag) {
        Response response = new Response();
        connect(response, method, uri, headers, body, invalidDataFlag);
        return response;
    }

    /**
     * GETメソッドで通信を行いレスポンスを取得する.
     * @param uri 接続先のURI
     * @return レスポンス
     */
    public static Response get(final String uri) {
        return connect(METHOD_GET, uri, null, null, false);
    }

    /**
     * POSTメソッドで通信を行いレスポンスを取得する.
     * @param uri 接続先のURI
     * @param body 送信するHTTPボディ
     * @return レスポンス
     */
    public static Response post(final String uri, final Object body) {
        return connect(METHOD_POST, uri, null, body, false);
    }

    /**
     * POSTメソッドで通信を行いレスポンスを取得する.
     * @param uri 接続先のURI
     * @param body 送信するHTTPボディ
     * @param invalidDataFlag 不正なデータ作成フラグ
     * @return レスポンス
     */
    public static Response post(final String uri, final Object body, final boolean invalidDataFlag) {
        return connect(METHOD_POST, uri, null, body, invalidDataFlag);
    }

    /**
     * DELETEメソッドで通信を行いレスポンスを取得する.
     * @param uri 接続先のURI
     * @return レスポンス
     */
    public static Response delete(final String uri) {
        return connect(METHOD_DELETE, uri, null, null, false);
    }

    /**
     * HTTPレスポンスを格納するクラス.
     */
    public static class Response {
        /**
         * ステータスコード.
         */
        private int mStatusCode;

        /**
         * レスポンスのボディを保存するファイル.
         */
        private File mBody;

        /**
         * ステータスコードを取得する.
         * @return ステータスコード
         */
        public int getStatusCode() {
            return mStatusCode;
        }

        /**
         * ステータスコードを設定する.
         * @param statusCode ステータスコード
         */
        void setStatusCode(final int statusCode) {
            mStatusCode = statusCode;
        }

        /**
         * レスポンスのボディへのファイルを取得する.
         * @return レスポンスのボディへのファイル
         */
        public File getBody() {
            return mBody;
        }

        /**
         * レスポンスのボディへのファイルを設定する.
         * @param body レスポンスのボディへのファイル
         */
        void setBody(final File body) {
            mBody = body;
        }

        public JSONObject getJSONObject() {
            if (mBody == null) {
                return null;
            }
            return getResponse(mBody);
        }

        private JSONObject getResponse(final File file) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int len;
            byte[] buf = new byte[4096];

            InputStream in = null;
            try {
                in = new FileInputStream(file);
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                String json = new String(out.toByteArray());
                return new JSONObject(json);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}
