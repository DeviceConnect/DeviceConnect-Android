package org.deviceconnect.android.deviceplugin.uvc.recorder.uvc;

import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libuvc.Parameter;

import java.io.IOException;

public class UvcMJPEGEncoder extends MJPEGEncoder {

    private final UvcRecorder mRecorder;

    public UvcMJPEGEncoder(UvcRecorder recorder) {
        mRecorder = recorder;
    }

    @Override
    public void start() {
        mRecorder.getUVCCamera().setPreviewCallback((frame) -> {
            try {
                postJPEG(frame.getBuffer());
            } finally {
                frame.release();
            }
        });

        try {
            UvcRecorder.UvcSettings settings = (UvcRecorder.UvcSettings) mRecorder.getSettings();
            Parameter p = settings.getParameter();
            if (p == null) {
                throw new RuntimeException("UVC parameter not found.");
            }
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
