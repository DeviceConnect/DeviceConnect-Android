package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.media.MediaCodecInfo;

import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.io.IOException;

public class ScreenCastVideoEncoder extends VideoEncoder {

    private VideoQuality mVideoQuality;

    private ScreenCastManager mScreenCastMgr;

    private ScreenCast mScreenCast;

    ScreenCastVideoEncoder(ScreenCastManager screenCastManager) {
        mVideoQuality = new VideoQuality("video/avc");
        mScreenCastMgr = screenCastManager;
    }

    // VideoEncoder

    @Override
    public int getColorFormat() {
        return MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    }

    @Override
    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    // MediaEncoder

    @Override
    protected void prepare() throws IOException {
        super.prepare();

        mScreenCast = mScreenCastMgr.createScreenCast(mMediaCodec.createInputSurface(),
                mVideoQuality.getVideoWidth(), mVideoQuality.getVideoHeight());
        mScreenCast.startCast();
    }

    @Override
    protected void startRecording() {
    }

    @Override
    protected void stopRecording() {
    }

    @Override
    protected void release() {
        if (mScreenCast != null) {
            mScreenCast.stopCast();
            mScreenCast = null;
        }
        super.release();
    }
}
