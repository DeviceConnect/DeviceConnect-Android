/*
 PreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.core.preview;

import javax.net.ssl.SSLContext;

/**
 * プレビュー配信用サーバを定義するインターフェース.
 */
public interface PreviewServer {
    /**
     * サーバが配信するプレビューのマイムタイプを取得します.
     *
     * @return マイムタイプ
     */
    String getMimeType();

    /**
     * サーバへの URL を取得します.
     *
     * @return サーバへの URL
     */
    String getUri();

    /**
     * プレビュー配信サーバのポート番号を取得します.
     *
     * @return ポート番号
     */
    int getPort();

    /**
     * プレビュー配信サーバのポート番号を設定します.
     *
     * @param port ポート番号
     */
    void setPort(int port);

    /**
     * サーバを開始します.
     *
     * @param callback 開始結果を通知するコールバック
     */
    void startWebServer(OnWebServerStartCallback callback);

    /**
     * サーバを停止します.
     */
    void stopWebServer();

    /**
     * 画面が回転されたことを通知します.
     */
    void onConfigChange();

    /**
     * Recorder をミュート状態にする.
     */
    void mute();

    /**
     * Recorder のミュート状態を解除する.
     */
    void unMute();

    /**
     * Recorder のミュート状態を返す.
     * @return mute状態
     */
    boolean isMuted();

    /**
     * 映像のエンコーダーに対して sync frame の即時生成を要求する.
     *
     * @return 即時生成を受け付けた場合は<code>true</code>, そうでない場合は<code>false</code>
     */
    boolean requestSyncFrame();

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
