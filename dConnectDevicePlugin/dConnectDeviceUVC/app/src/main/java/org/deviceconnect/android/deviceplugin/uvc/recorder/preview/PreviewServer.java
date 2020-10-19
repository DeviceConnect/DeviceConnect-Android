/*
 PreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.recorder.preview;


import javax.net.ssl.SSLContext;

public interface PreviewServer {

    String getUrl();

    String getMimeType();

    /**
     * サーバを開始します.
     * @param callback 開始結果を通知するコールバック
     */
    void start(OnWebServerStartCallback callback);

    /**
     * サーバを停止します.
     */
    void stop();

    /**
     * サーバが開始されているか確認します.
     *
     * @return サーバが開始されている場合はtrue、それ以外はfalse
     */
    boolean isStarted();
    /**
     * SSLContext を使用するかどうかのフラグを返す.
     *
     * @return SSLContext を使用する場合は<code>true</code>, そうでない場合は<code>false</code>
     */
    boolean usesSSLContext();

    void setSSLContext(SSLContext sslContext);

    SSLContext getSSLContext();
    /**
     * Callback interface used to receive the result of starting a web server.
     */
    interface OnWebServerStartCallback {
        /**
         * Called when a web server successfully started.
         *
         * @param uri An ever-updating, static image URI.
         */
        void onStart(String uri);

        /**
         * Called when a web server failed to start.
         */
        void onFail();
    }
}
