/*
 HttpUtil.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.util;

import org.deviceconnect.android.deviceplugin.awsiot.remote.BuildConfig;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * HTTP通信を行うためのユーティリティクラス.
 */
public final class HttpUtil {
    /** デバック用フラグ. */
    private static final boolean DEBUG = BuildConfig.DEBUG;
    /** デバック用タグを定義します. */
    private static final String TAG = "HTTP";
    /** バッファサイズを定義します. */
    private static final int BUF_SIZE = 4096;
    /** エラーとなるレスポンスコードを定義します. */
    private static final int ERROR_RESPONSE_CODE = 400;
    /** 接続のタイムアウト. */
    private static final int CONNECT_TIMEOUT = 30 * 1000;
    /** 読み込みのタイムアウト時間. */
    private static final int READ_TIMEOUT = 3 * 60 * 1000;
    /** POSTメソッドを定義します. */
    private static final String METHOD_POST = "POST";
    /** GETメソッドを定義します. */
    private static final String METHOD_GET = "GET";
    /** PUTメソッドを定義します. */
    private static final String METHOD_PUT = "PUT";
    /** DELETEメソッドを定義します. */
    private static final String METHOD_DELETE = "DELETE";
    /** マルチパートで使用するハイフンの定義. */
    private final static String TWO_HYPHEN = "--";
    /** マルチパートで使用する改行コードの定義. */
    private final static String EOL = "\r\n";
    /** マルチパートのバウンダリーの定義. */
    private final static String BOUNDARY = String.format("%x", new Random().hashCode());

    private HttpUtil() {
    }

    /**
     * 指定されたファイルを読み込みます.
     * @param file ファイル
     * @return ファイルのデータ
     * @throws IOException ファイルの読み込みに失敗した場合に発生
     */
    private static byte[] getBytes(final File file) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return getBytes(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 指定したストリームを読み込みます.
     * @param in ストリーム
     * @return ストリームのデータ
     * @throws IOException ストリームの読み込みに失敗した場合に発生
     */
    private static byte[] getBytes(final InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        byte[] buf = new byte[1024];
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        return out.toByteArray();
    }

    /**
     * マルチパートのBody部分を書き込みます.
     * @param os 書き込む先のストリーム
     * @param body 書き込むデータ
     * @throws IOException 書き込みに失敗した場合に発生
     */
    private static void multipart(final OutputStream os, final Map<String, Object> body) throws IOException {
        for (Map.Entry<String, Object> data : body.entrySet()) {
            String key = data.getKey();
            Object val = data.getValue();
            os.write(String.format("%s%s%s", TWO_HYPHEN, BOUNDARY, EOL).getBytes());
            if (val instanceof String) {
                os.write(String.format("Content-Disposition: form-data; name=\"%s\"%s", key, EOL).getBytes());
                os.write(EOL.getBytes());
                os.write(((String) val).getBytes());
                os.write(EOL.getBytes());
            } else if (val instanceof byte[]) {
                os.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", key, key, EOL).getBytes());
                os.write(EOL.getBytes());
                os.write(((byte[])val));
                os.write(EOL.getBytes());
            } else if (val instanceof File) {
                os.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", key, key, EOL).getBytes());
                os.write(EOL.getBytes());
                os.write(getBytes((File) val));
                os.write(EOL.getBytes());
            } else if (val instanceof InputStream) {
                os.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", key, key, EOL).getBytes());
                os.write(EOL.getBytes());
                os.write(getBytes((InputStream) val));
                os.write(EOL.getBytes());
            }
        }
        os.write(String.format("%s%s%s%s", TWO_HYPHEN, BOUNDARY, TWO_HYPHEN, EOL).getBytes());
    }

    /**
     * HTTPサーバに接続を行います.
     * <p>
     *     HTTPサーバに接続が失敗した場合には<code>null</code>を返却します。
     * </p>
     * @param method HTTPメソッド
     * @param uri HTTPサーバへのURI
     * @param headers ヘッダー
     * @param body ボディ
     * @return HTTPサーバからのレスポンス
     */
    public static byte[] connect(final String method, final String uri, final Map<String, String> headers, final Object body) {
        if (DEBUG) {
            Log.d(TAG, "connect: method=" + method + " uri=" + uri);
            if (headers != null) {
                for (String key : headers.keySet()) {
                    Log.d(TAG, "header: " + key + ": " + headers.get(key));
                }
            }
            if (body != null) {
                Log.d(TAG, "body: " + body);
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(uri).openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod(method);
            conn.setDoInput(true);
            conn.setDoOutput(body != null);
            if (headers != null) {
                for (String key : headers.keySet()) {
                    conn.setRequestProperty(key, headers.get(key));
                }
            }

            // 4.x系はkeep-aliveを行うと例外が発生するため、offにする
            // 参考: http://osa030.hatenablog.com/entry/2015/05/22/181155
//            if (Build.VERSION.SDK_INT > 13 && Build.VERSION.SDK_INT < 19) {
                conn.setRequestProperty("Connection", "close");
//            }

            // データがMapの場合にはマルチパート
            if (body instanceof Map) {
                conn.setRequestProperty("Content-Type", String.format("multipart/form-data; boundary=%s", BOUNDARY));
            }

            conn.connect();

            if (body != null) {
                OutputStream os = conn.getOutputStream();
                if (body instanceof byte[]) {
                    os.write((byte[]) body);
                } else if (body instanceof String) {
                    os.write(((String) body).getBytes());
                } else if (body instanceof Map<?, ?>) {
                    multipart(os, (Map<String, Object>) body);
                }
                os.flush();
                os.close();
            }

            int resp = conn.getResponseCode();

            if (DEBUG) {
                Log.d(TAG, "response code=" + resp);
            }

            if (resp < ERROR_RESPONSE_CODE) {
                InputStream in = conn.getInputStream();
                int len;
                byte[] buf = new byte[BUF_SIZE];
                while ((len = in.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                in.close();
            } else {
                if (DEBUG) {
                    Log.w(TAG, "Failed to connect the server. response=" + resp);
                }
                return null;
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to connect the server.", e);
            }
            return null;
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to connect the server.", e);
            }
            return null;
        } catch (OutOfMemoryError e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to connect the server.", e);
            }
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return outputStream.toByteArray();
    }

    /**
     * GETメソッドでHTTPサーバにアクセスします.
     * @param uri HTTPサーバのURI
     * @return HTTPサーバのレスポンス
     */
    public static byte[] get(final String uri) {
        return get(uri, null);
    }

    public static byte[] get(final String uri, final Map<String, String> headers) {
        return connect(METHOD_GET, uri, headers, null);
    }

    /**
     * PUTメソッドでHTTPサーバにアクセスします.
     * @param uri HTTPサーバのURI
     * @param body 送信するデータ
     * @return HTTPサーバのレスポンス
     */
    public static byte[] put(final String uri, final Objects body) {
        return put(uri, null, body);
    }

    public static byte[] put(final String uri, final Map<String, String> headers, final Object body) {
        return connect(METHOD_PUT, uri, headers, body);
    }

    /**
     * POSTメソッドでHTTPサーバにアクセスします.
     * @param uri HTTPサーバのURI
     * @param body 送信するデータ
     * @return HTTPサーバのレスポンス
     */
    public static byte[] post(final String uri, final Object body) {
        return put(uri, null, body);
    }

    public static byte[] post(final String uri, final Map<String, String> headers, final Object body) {
        return connect(METHOD_POST, uri, headers, body);
    }

    /**
     * DELETEメソッドでHTTPサーバにアクセスします.
     * @param uri HTTPサーバのURI
     * @return HTTPサーバのレスポンス
     */
    public static byte[] delete(final String uri) {
        return delete(uri, null);
    }

    public static byte[] delete(final String uri, final Map<String, String> headers) {
        return connect(METHOD_DELETE, uri, headers, null);
    }

    /**
     * POSTメソッドでHTTPサーバにマルチパートのデータを送信します.
     * @param uri HTTPサーバのURI
     * @param headers ヘッダー
     * @param body 送信するデータ
     * @return HTTPサーバのレスポンス
     */
    public static byte[] postMultipart(final String uri, final Map<String, String> headers, final Map<String, Object> body) {
        return connect(METHOD_POST, uri, headers, body);
    }

    /**
     * PUTメソッドでHTTPサーバにマルチパートのデータを送信します.
     * @param uri HTTPサーバのURI
     * @param headers ヘッダー
     * @param body 送信するデータ
     * @return HTTPサーバのレスポンス
     */
    public static byte[] putMultipart(final String uri, final Map<String, String> headers, final Map<String, Object> body) {
        return connect(METHOD_PUT, uri, headers, body);
    }
}
