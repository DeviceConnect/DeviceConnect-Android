/*
 ResourceDownloader.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services.qr.profiles;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * リソースをダウンロードを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class ResourceDownloader {
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
     * ダウンロードを行うスレッド.
     */
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    /**
     * ダウンロードを登録します.
     *
     * @param uri ダウンロード先のURI
     * @param callback ダウンロード結果を通知するコールバック
     */
    void download(final String uri, final Callback callback) {
        mExecutorService.submit(() -> {
            try {
                callback.onComplete(decodeBitmap(connect(uri)));
            } catch (OutOfMemoryError e) {
                callback.onComplete(null);
            } catch (Exception e) {
                callback.onComplete(null);
            }
        });
    }

    /**
     * バイト配列を Bitmap に変換します.
     *
     * @param data バイト配列
     * @return Bitmapのインスタンス、バイト配列が画像ではない場合にはnull
     */
    private Bitmap decodeBitmap(final byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /**
     * 勝手サーバ証明書を許諾する HttpsURLConnection を生成する.
     *
     * @param url 接続先のURL
     * @return HttpsURLConnectionのインスタンス
     * @throws IOException HttpsURLConnectionの生成に失敗した場合に発生
     * @throws NoSuchAlgorithmException SSLの暗号化に失敗した場合に発生
     * @throws KeyManagementException Keyの管理に失敗した場合の発生
     */
    private HttpsURLConnection makeHttpsURLConnection(final URL url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setHostnameVerifier((hostname, sslSession) -> true);

        TrustManager[] transManagers = {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
                        // ignore.
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
     * 指定したURIに接続を行い通信結果を返却する.
     *
     * @param uri 通信先のURI
     * @return 通信結果
     * @throws IOException HttpsURLConnectionの生成に失敗した場合に発生
     * @throws NoSuchAlgorithmException SSLの暗号化に失敗した場合に発生
     * @throws KeyManagementException Keyの管理に失敗した場合の発生
     */
    private byte[] connect(String uri) throws IOException, NoSuchAlgorithmException, KeyManagementException {
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
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(false);

            // 4.x系はkeep-aliveを行うと例外が発生するため、offにする
            // 参考: http://osa030.hatenablog.com/entry/2015/05/22/181155
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2 &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                conn.setRequestProperty("Connection", "close");
            }

            conn.setUseCaches(false);
            conn.connect();

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
     * リソースのダウンロード結果を通知するコールバック.
     */
    public interface Callback {
        /**
         * ダウンロードした画像リソースを通知します.
         * <p>
         * ダウンロードに失敗した場合やリソースが画像でない場合には null を通知します。
         * </p>
         * @param bitmap ダウンロードした画像リソース
         */
        void onComplete(Bitmap bitmap);
    }
}
