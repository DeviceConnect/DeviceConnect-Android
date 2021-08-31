package org.deviceconnect.android.deviceplugin.host.recorder;

public interface Broadcaster {
    /**
     * 名前を取得します.
     *
     * @return 名前
     */
    String getName();

    /**
     * マイムタイプを取得します.
     *
     * @return マイムタイプ
     */
    String getMimeType();

    /**
     * ブロードキャスト先の URI を取得します.
     *
     * @return ブロードキャスト先の URI
     */
    String getBroadcastURI();

    /**
     * ブロードキャストのイベントを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    void setOnEventListener(OnEventListener listener);

    /**
     * ブロードキャスト中か確認します.
     *
     * @return ブロードキャスト中の場合は true、それ以外は false
     */
    boolean isRunning();

    /**
     * ブロードキャストを開始します.
     */
    void start(OnStartCallback callback);

    /**
     * ブロードキャストを停止します.
     */
    void stop();

    /**
     * ミュート設定を行います.
     *
     * @param mute ミュートにする場合にはtrue、それ以外はfalse
     */
    void setMute(boolean mute);

    /**
     * ミュート設定を取得します.
     *
     * @return ミュートの場合はtrue、それ以外はfalse
     */
    boolean isMute();

    /**
     * 設定が変更されたことを通知します.
     */
    void onConfigChange();

    /**
     * 配信先を解放します.
     */
    void release();

    /**
     * ブロードキャストの開始結果を通知するコールバック.
     */
    interface OnStartCallback {
        /**
         * ブロードキャストに成功したことを通知します.
         */
        void onSuccess();

        /**
         * ブロードキャストに失敗したことを通知します.
         *
         * @param e 失敗原因の例外
         */
        void onFailed(Exception e);
    }

    /**
     * ブロードキャストのイベントを通知するリスナー.
     */
    interface OnEventListener {

        /**
         * ブロードキャストが開始されたことを通知します.
         */
        void onStarted();

        /**
         * ブロードキャストが停止されたことを通知します.
         */
        void onStopped();

        /**
         * ブロードキャストでエラーが発生したことを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(Exception e);
    }
}
