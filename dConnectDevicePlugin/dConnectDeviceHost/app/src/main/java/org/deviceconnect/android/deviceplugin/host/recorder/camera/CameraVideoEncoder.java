package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.view.Surface;

import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperException;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;
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

    CameraVideoEncoder(Camera2Recorder camera2Recorder) {
        mCamera2Recorder = camera2Recorder;
        mVideoQuality = new CameraVideoQuality("video/avc");
        mVideoQuality.setRotation(Camera2Wrapper.Rotation.FREE);
    }

    // VideoEncoder

    @Override
    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    @Override
    protected int getDisplayRotation() {
        return mCamera2Recorder.getRotation();
    }

    // SurfaceVideoEncoder

    @Override
    protected void onStartSurfaceDrawing() {
        try {
            mCamera2Recorder.startPreview(new Surface(getSurfaceTexture()));
        } catch (CameraWrapperException e) {
            // ignore.
        }
    }

    @Override
    protected void onStopSurfaceDrawing() {
        try {
            mCamera2Recorder.stopPreview();
        } catch (CameraWrapperException e) {
            // ignore
        }
    }
}
