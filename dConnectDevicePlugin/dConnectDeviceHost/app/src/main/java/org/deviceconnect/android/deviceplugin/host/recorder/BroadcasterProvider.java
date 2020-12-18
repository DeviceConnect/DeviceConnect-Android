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
}
