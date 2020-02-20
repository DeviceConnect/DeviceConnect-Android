package org.deviceconnect.android.deviceplugin.host.recorder;

import android.os.Bundle;

/**
 * デバイスのカメラを使用したLive Streaming用のインターフェース
 */
public interface HostDeviceLiveStreamRecorder {
    void createLiveStreamingClient(final String broadcastURI);
    void setVideoParams(int width, int height, int bitrate, int framerate);
    void liveStreamingStart();
    void liveStreamingStop();
}
