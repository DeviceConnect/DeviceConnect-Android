package org.deviceconnect.android.deviceplugin.host.recorder;

public interface BroadcasterProvider {
    /**
     * ブロードキャスターのリストを取得します.
     *
     * @return ブロードキャスターのリスト
     */
    Broadcaster getBroadcaster();

    /**
     * ブロードキャスターを開始します.
     *
     * @return 起動したプレビュー配信サーバのリスト
     */
    void startBroadcaster(String broadcastURI, OnBroadcasterListener listener);

    /**
     * ブロードキャスターを停止します.
     */
    void stopBroadcaster();

    /**
     * ブロードキャスト中か確認します.
     *
     * @return ブロードキャスト中は true、それ以外は false
     */
    boolean isRunning();

    interface OnBroadcasterListener {
        void onStarted();
        void onStopped();
        void onError(Exception e);
    }
}
