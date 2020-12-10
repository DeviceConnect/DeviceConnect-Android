package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libmedia.streaming.video.SurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

/**
 * カメラの映像をエンコードするクラス.
 */
public class CameraVideoEncoder extends SurfaceVideoEncoder {
    /**
     * カメラ操作クラス.
     */
    private Camera2Recorder mCamera2Recorder;

    /**
     * 映像のエンコード設定.
     */
    private CameraVideoQuality mVideoQuality;

    public CameraVideoEncoder(Camera2Recorder camera2Recorder) {
        this(camera2Recorder, "video/avc");
    }

    public CameraVideoEncoder(Camera2Recorder camera2Recorder, String mimeType) {
        super(camera2Recorder.getCameraSurfaceDrawingThread());
        mCamera2Recorder = camera2Recorder;
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
