package org.deviceconnect.android.deviceplugin.host.recorder;

public interface BroadcasterProvider {
    /**
     * ブロードキャスターのリストを取得します.
     *
     * @return ブロードキャスターのリスト
     */
    Broadcaster getBroadcaster();

    /**
     * ブロードキャスト中か確認します.
     *
     * @return ブロードキャスト中は true、それ以外は false
     */
    boolean isRunning();

    /**
     * ブロードキャスターを開始します.
     *
     * @param broadcastURI 配信先の URI
     */
    Broadcaster startBroadcaster(String broadcastURI);

    /**
     * ブロードキャスターを停止します.
     */
    void stopBroadcaster();

    /**
     * 設定が変更されたことを通知します.
     */
    void onConfigChange();

    /**
     * Recorder をミュート状態にする.
     */
    void setMute(boolean mute);

    /**
     * BroadcasterProvider で発生したイベントを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    void setOnEventListener(OnEventListener listener);

    /**
     * イベントを通知するリスナー.
     */
    interface OnEventListener {
        /**
         * Broadcaster が開始されたことを通知します.
         *
         * @param broadcaster 開始した Broadcaster
         */
        void onStarted(Broadcaster broadcaster);

        /**
         * Broadcaster が停止されたことを通知します.
         *
         * @param broadcaster 停止した Broadcaster
         */
        void onStopped(Broadcaster broadcaster);
        void onError(Broadcaster broadcaster, Exception e);
    }
}
