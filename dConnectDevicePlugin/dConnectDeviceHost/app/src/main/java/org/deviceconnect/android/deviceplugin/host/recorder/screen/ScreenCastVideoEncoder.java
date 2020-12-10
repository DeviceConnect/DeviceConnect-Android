package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.media.MediaCodecInfo;

import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.io.IOException;

public class ScreenCastVideoEncoder extends VideoEncoder {
    /**
     * エンコードするための情報を格納するクラス.
     */
    private VideoQuality mVideoQuality;

    /**
     * Android 端末の画面をキャストを管理するクラス.
     */
    private ScreenCastManager mScreenCastMgr;

    /**
     * Android 端末の画面をキャストするためのクラス.
     */
    private ScreenCast mScreenCast;

    public ScreenCastVideoEncoder(ScreenCastManager screenCastManager) {
        this(screenCastManager, "video/avc");
    }

    public ScreenCastVideoEncoder(ScreenCastManager screenCastManager, String mimeType) {
        mScreenCastMgr = screenCastManager;
        mVideoQuality = new VideoQuality(mimeType);
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

    @Override
    protected int getDisplayRotation() {
        return mScreenCastMgr.getDisplayRotation();
    }

    @Override
    public boolean isSwappedDimensions() {
        return mScreenCastMgr.isSwappedDimensions();
    }

    // MediaEncoder

    @Override
    protected void prepare() throws IOException {
        super.prepare();

        if (mScreenCast != null) {
            mScreenCast.stopCast();
        }

        int w = mVideoQuality.getVideoWidth();
        int h = mVideoQuality.getVideoHeight();
        if (isSwappedDimensions()) {
            w = mVideoQuality.getVideoHeight();
            h = mVideoQuality.getVideoWidth();
        }

        mScreenCast = mScreenCastMgr.createScreenCast(mMediaCodec.createInputSurface(), w, h);
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
