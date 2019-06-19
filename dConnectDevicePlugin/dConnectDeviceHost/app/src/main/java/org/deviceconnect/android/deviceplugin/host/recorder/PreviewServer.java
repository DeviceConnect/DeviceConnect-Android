/*
 PreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;


public interface PreviewServer {

    String getMimeType();

    /**
     * サーバを開始します.
     * @param callback 開始結果を通知するコールバック
     */
    void startWebServer(OnWebServerStartCallback callback);

    /**
     * サーバを停止します.
     */
    void stopWebServer();

    /**
     * プレビューの品質を取得します.
     * @return 1-100
     */
    int getQuality();

    /**
     * プレビューの品質を設定します.
     * @param quality 1-100
     */
    void setQuality(int quality);

    /**
     * 端末の画面が回転したタイミングで実行されるメソッド.
     * @param degree 0, 90, 180, 270 のいずれか.
     */
    void onDisplayRotation(int degree);
    /**
     * Recorderをmute状態にする.
     */
    void mute();

    /**
     * Recorderのmute状態を解除する.
     */
    void unMute();

    /**
     * Recorderのmute状態を返す.
     * @return mute状態
     */
    boolean isMuted();
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
