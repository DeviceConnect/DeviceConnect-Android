package org.deviceconnect.android.deviceplugin.host.recorder;

import org.deviceconnect.android.deviceplugin.host.recorder.util.LiveStreamingClient;

/**
 * デバイスのカメラを使用したLive Streaming用のインターフェース
 */
@SuppressWarnings("unused")
public interface HostDeviceLiveStreamRecorder {
    //クライアントの生成
    void createLiveStreamingClient(final String broadcastURI, LiveStreamingClient.EventListener eventListener);

    //ビデオエンコーダーの設定
    void setVideoEncoder(Integer width, Integer height, Integer bitrate, Integer frameRate);

    //オーディオエンコーダーの設定
    void setAudioEncoder();

    //ストリーミング開始
    void startLiveStreaming();

    //ストリーミング停止
    void stopLiveStreaming();

    boolean isStreaming();
    void setMute(boolean mute);
    boolean isMute();
    boolean isError();
    int getVideoWidth();
    int getVideoHeight();
    int getBitrate();
    int getFrameRate();
    String getLiveStreamingMimeType();
    String getBroadcastURI();
}
