package org.deviceconnect.android.deviceplugin.host.recorder;

import javax.net.ssl.SSLContext;

public interface LiveStreaming {
    /**
     * サーバのIDします.
     *
     * @return サーバID
     */
    String getId();

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
     * 起動中か確認します.
     *
     * @return 起動中の場合は true、それ以外は false
     */
    boolean isRunning();

    /**
     * サーバを開始します.
     *
     * @param callback 開始結果を通知するコールバック
     */
    void start(OnStartCallback callback);

    /**
     * サーバを停止します.
     */
    void stop();

    /**
     * 設定が変更されたことを通知します.
     */
    void onConfigChange();

    /**
     * Recorder をミュート状態にする.
     */
    void setMute(boolean mute);

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
     * プレビューサーバから配信したデータの BPS を取得します.
     *
     * @return プレビューサーバから配信したデータの BPS
     */
    long getBPS();

    /**
     * サーバを解放します.
     */
    void release();

    /**
     * イベントを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    void setOnEventListener(OnEventListener listener);

    /**
     * SSLContext を使用するかどうかのフラグを返す.
     *
     * @return SSLContext を使用する場合は<code>true</code>, そうでない場合は<code>false</code>
     */
    boolean useSSLContext();

    /**
     * SSL コンテキストの設定を行います.
     *
     * @param sslContext SSL コンテキスト
     */
    void setSSLContext(SSLContext sslContext);

    /**
     * SSL コンテキストを取得します.
     *
     * @return SSL コンテキスト
     */
    SSLContext getSSLContext();

    /**
     * 起動結果を通知するコールバック.
     */
    interface OnStartCallback {
        /**
         * 起動成功したことを通知します.
         */
        void onSuccess();

        /**
         * 起動失敗したことを通知します.
         *
         * @param e 失敗原因の例外
         */
        void onFailed(Exception e);
    }

    /**
     * イベントを通知するリスナー.
     */
    interface OnEventListener {

        /**
         * 開始されたことを通知します.
         */
        void onStarted();

        /**
         * 停止されたことを通知します.
         */
        void onStopped();

        /**
         * エラーが発生したことを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(Exception e);
    }
}
