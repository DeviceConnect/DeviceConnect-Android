package org.deviceconnect.android.deviceplugin.theta.core.preview.camera;

import org.deviceconnect.android.deviceplugin.theta.core.LivePreviewTask;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class  ThetaMJPEGEncoder extends MJPEGEncoder {
    /**
     * カメラ操作クラス.
     */
    private ThetaDevice mThetaRecorder;
    private LivePreviewTask mLivePreviewTask;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    ThetaMJPEGEncoder(ThetaDevice thetaRecorder) {
        mThetaRecorder = thetaRecorder;
    }

    @Override
    public void start() {
        if (mLivePreviewTask == null) {
            mLivePreviewTask = new LivePreviewTask(mThetaRecorder) {
                @Override
                protected void onFrame(final byte[] frame) {
                    postJPEG(frame);
                }
            };
            mExecutor.execute(mLivePreviewTask);
        }
    }

    @Override
    public void stop() {
        if (mLivePreviewTask != null) {
            mLivePreviewTask.stop();
            mLivePreviewTask = null;
        }
    }
}
