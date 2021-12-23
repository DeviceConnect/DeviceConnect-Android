package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libmedia.streaming.video.SurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

public class ScreenCastVideoEncoder extends SurfaceVideoEncoder {
    /**
     * エンコードするための情報を格納するクラス.
     */
    private final VideoQuality mVideoQuality;

    public ScreenCastVideoEncoder(ScreenCastRecorder recorder) {
        this(recorder, "video/avc");
    }

    public ScreenCastVideoEncoder(ScreenCastRecorder recorder, String mimeType) {
        super(recorder.getSurfaceDrawingThread());
        mVideoQuality = new CameraVideoQuality(mimeType);
    }

    // VideoEncoder

    @Override
    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }
}
