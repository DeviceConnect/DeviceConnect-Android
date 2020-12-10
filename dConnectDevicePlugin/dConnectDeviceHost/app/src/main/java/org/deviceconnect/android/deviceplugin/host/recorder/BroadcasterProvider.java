package org.deviceconnect.android.deviceplugin.host.recorder;

import java.util.List;

public interface BroadcasterProvider {
    /**
     * ブロードキャスターのリストを取得します.
     *
     * @return ブロードキャスターのリスト
     */
    List<Broadcaster> getBroadcasters();

    /**
     * ブロードキャスターを開始します.
     *
     * @return 起動したプレビュー配信サーバのリスト
     */
    List<Broadcaster> startBroadcaster();

    /**
     * ブロードキャスターを停止します.
     */
    void stopBroadcaster();
}
