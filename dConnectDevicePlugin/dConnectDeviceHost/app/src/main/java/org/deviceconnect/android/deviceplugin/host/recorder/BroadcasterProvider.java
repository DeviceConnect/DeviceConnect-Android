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
     * @param broadcastURI 配信先の URI
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

    /**
     * Broadcaster のイベントを通知するリスナー.
     */
    interface OnBroadcasterListener {
        void onStarted();
        void onStopped();
        void onError(Exception e);
    }
}
