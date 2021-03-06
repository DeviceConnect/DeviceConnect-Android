package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libmedia.streaming.video.SurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

public class UvcVideoEncoder extends SurfaceVideoEncoder {
    /**
     * 映像のエンコード設定.
     */
    private final CameraVideoQuality mVideoQuality;

    public UvcVideoEncoder(UvcRecorder recorder) {
        this(recorder, "video/avc");
    }

    public UvcVideoEncoder(UvcRecorder recorder, String mimeType) {
        super(recorder.getSurfaceDrawingThread());
        mVideoQuality = new CameraVideoQuality(mimeType);
    }

    // VideoEncoder

    @Override
    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    // SurfaceVideoEncoder

    @Override
    protected void onStartSurfaceDrawing() {
    }

    @Override
    protected void onStopSurfaceDrawing() {
    }
}
