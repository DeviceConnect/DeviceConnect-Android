package org.deviceconnect.android.deviceplugin.theta.core;


import org.deviceconnect.android.deviceplugin.theta.utils.MotionJpegInputStream;

import java.io.IOException;
import java.io.InputStream;

public abstract class LivePreviewTask implements Runnable {

    private boolean mIsStarted;

    private final LiveCamera mLiveCamera;

    public LivePreviewTask(final LiveCamera liveCamera) {
        mLiveCamera = liveCamera;
    }

    public void stop() {
        if (!mIsStarted) {
            return;
        }
        mIsStarted = false;
    }

    protected abstract void onFrame(byte[] frame);

    @Override
    public void run() {
        mIsStarted = true;
        InputStream is = null;
        MotionJpegInputStream mjpeg = null;
        try {
            is = mLiveCamera.getLiveStream();
            mjpeg = new MotionJpegInputStream(is);
            byte[] frame;

            while (mIsStarted && (frame = mjpeg.readFrame()) != null) {
                onFrame(frame);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mIsStarted = false;
            try {
                if (is != null) {
                    is.close();
                }
                if (mjpeg != null) {
                    mjpeg.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
