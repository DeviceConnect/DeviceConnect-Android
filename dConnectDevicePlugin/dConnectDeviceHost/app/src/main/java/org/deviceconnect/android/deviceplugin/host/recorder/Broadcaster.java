package org.deviceconnect.android.deviceplugin.host.recorder;

public interface Broadcaster extends LiveStreaming {
    /**
     * ブロードキャスト先の URI を取得します.
     *
     * @return ブロードキャスト先の URI
     */
    String getBroadcastURI();
}
