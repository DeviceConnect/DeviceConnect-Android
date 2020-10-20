package org.deviceconnect.android.deviceplugin.theta.core.preview.omni;


import org.deviceconnect.android.deviceplugin.theta.core.preview.omni.projector.ProjectionScreen;
import org.deviceconnect.android.deviceplugin.theta.core.preview.omni.projector.Projector;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class OmnidirectionalImageMJPEGEncoder extends MJPEGEncoder {
    private Projector mProjector;
    private static boolean mIsPreview;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    OmnidirectionalImageMJPEGEncoder(Projector projector) {
        mProjector = projector;
        mIsPreview = true;
        mExecutor.execute(() -> {
            while(mIsPreview) {
                mProjector.setScreen(new ProjectionScreen() {
                    @Override
                    public void onStart(final Projector projector) {
                    }

                    @Override
                    public void onProjected(final Projector projector, final byte[] frame) {
                        postJPEG(frame);
                    }

                    @Override
                    public void onStop(final Projector projector) {
                    }
                });
            }
        });
    }


    @Override
    public void start() {
    }

    @Override
    public void stop() {
        mIsPreview = false;
        if (mProjector == null) {
            return;
        }
        mProjector.stop();
        mProjector = null;
    }
}
