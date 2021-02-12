package org.deviceconnect.android.deviceplugin.uvc.recorder.mjpeg;

import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libuvc.Parameter;

import java.io.IOException;

public class UvcMJPEGEncoder extends MJPEGEncoder {

    private final UvcH264Recorder mRecorder;

    UvcMJPEGEncoder(UvcH264Recorder recorder) {
        mRecorder = recorder;
    }

    @Override
    public void start() {
        mRecorder.getUVCCamera().setPreviewCallback(frame -> {
            try {
                postJPEG(frame.getBuffer());
            } finally {
                frame.release();
            }
        });

        try {
            UvcH264Recorder.UvcSettings settings = (UvcH264Recorder.UvcSettings) mRecorder.getSettings();
            Parameter p = settings.getParameter();
            p.setUseH264(false);
            mRecorder.getUVCCamera().startVideo(p);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void stop() {
        try {
            mRecorder.getUVCCamera().stopVideo();
        } catch (IOException e) {
            // ignore.
        }
    }
}
