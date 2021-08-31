package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.graphics.Rect;

import org.deviceconnect.android.deviceplugin.host.recorder.util.MovingRectThread;
import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libmedia.streaming.video.SurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

/**
 * カメラの映像をエンコードするクラス.
 */
public class CameraVideoEncoder extends SurfaceVideoEncoder {
    /**
     * 映像のエンコード設定.
     */
    private final CameraVideoQuality mVideoQuality;

    public CameraVideoEncoder(Camera2Recorder recorder) {
        this(recorder, "video/avc");
    }

    public CameraVideoEncoder(Camera2Recorder recorder, String mimeType) {
        super(recorder.getSurfaceDrawingThread());
        mVideoQuality = new CameraVideoQuality(mimeType);
    }

    // VideoEncoder

    @Override
    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    private final MovingRectThread.OnEventListener mMovingRectThreadOnEventListener = (rect) -> {
        VideoQuality videoQuality = getVideoQuality();
        if (videoQuality != null) {
            videoQuality.setCropRect(new Rect(rect));
        }
    };

    private MovingRectThread mMovingRectThread;

    public void startMovingRectThread() {
        if (mMovingRectThread != null) {
            return;
        }

        mMovingRectThread = new MovingRectThread();
        mMovingRectThread.addOnEventListener(mMovingRectThreadOnEventListener);
        mMovingRectThread.start();
    }

    public void stopMovingRectThread() {
        if (mMovingRectThread != null) {
            mMovingRectThread.stop();
            mMovingRectThread = null;
        }
    }
}
