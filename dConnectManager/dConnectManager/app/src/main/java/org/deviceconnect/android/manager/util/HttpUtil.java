/*
 HttpUtil.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.util;

import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.manager.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class HttpUtil {
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
    private static final int READ_TIMEOUT = 30 * 1000;

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
     * コンストラクタ.
     * ユーティリティクラスなので、インスタンスは作成させない。
     */
    private HttpUtil() {
    }

    private static byte[] connect(String method, String uri, Map<String, String> headers, String body) {
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
            conn.setRequestMethod(method);
            conn.setDoInput(true);
            conn.setDoOutput(METHOD_POST.equals(method));
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

            conn.setRequestProperty("Origin", "http://localhost");

            conn.connect();

            if (body != null && (METHOD_POST.equals(method) || METHOD_PUT.equals(method))) {
                OutputStream os = conn.getOutputStream();
                os.write(body.getBytes());
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
                    baos.write(buf, 0, len);
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
        return baos.toByteArray();
    }

    public static byte[] get(final String uri) {
        return connect(METHOD_GET, uri, null, null);
    }

    private static HttpsURLConnection makeHttpsURLConnection(final URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(final String hostname, final SSLSession sslSession) {
                return true;
            }
        });

        KeyManager[] keyManagers = null;
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
        sslcontext.init(keyManagers, transManagers, new SecureRandom());
        connection.setSSLSocketFactory(sslcontext.getSocketFactory());

        return connection;
    }

}
