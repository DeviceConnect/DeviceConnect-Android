/*
 ScreenCastSRTPreviewServer.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.media.AudioFormat;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSetting;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;

import java.io.IOException;

/**
 * スクリーンキャストを SRT で配信するサーバー.
 *
 * @author NTT DOCOMO, INC.
 */
class ScreenCastSRTPreviewServer extends AbstractPreviewServer {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "CameraSRT";

    // 参照: https://www.iana.org/assignments/media-types/video/MP2T
    public static final String MIME_TYPE = "video/MP2T";

    private ScreenCastManager mScreenCastMgr;

    private SRTServer mSRTServer;

    ScreenCastSRTPreviewServer(final Context context, final ScreenCastRecorder recorder, final int port) {
        super(context, recorder);
        mScreenCastMgr = recorder.getScreenCastMgr();
        setPort(RecorderSetting.getInstance(getContext()).getPort(recorder.getId(), MIME_TYPE, port));
    }

    @Override
    public String getUri() {
        return "srt://localhost:" + getPort();
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        if (mSRTServer == null) {
            try {
                mSRTServer = new SRTServer(getPort());
                mSRTServer.setShowStats(DEBUG);
                mSRTServer.setCallback(mCallback);
                mSRTServer.start();
            } catch (IOException e) {
                callback.onFail();
                return;
            }
        }
        callback.onStart(getUri());
    }

    @Override
    public void stopWebServer() {
        if (mSRTServer != null) {
            mSRTServer.stop();
            mSRTServer = null;
        }
    }

    @Override
    public void onConfigChange() {
        if (DEBUG) {
            Log.d(TAG, "ScreenCastRTSPPreviewServer#onConfigChange");
        }

        if (mSRTServer != null) {
            new Thread(() -> {
                if (mSRTServer != null) {
                    SRTSession session = mSRTServer.getSRTSession();
                    if (session != null) {
                        session.restartVideoEncoder();
                    }
                }
            }).start();
        }
    }


    @Override
    public void mute() {
        super.mute();
        setMute(true);
    }

    @Override
    public void unMute() {
        super.unMute();
        setMute(false);
    }

    /**
     * AudioEncoder にミュート設定を行います.
     *
     * @param mute ミュート設定
     */
    private void setMute(boolean mute) {
        if (mSRTServer != null) {
            new Thread(() -> {
                if (mSRTServer != null) {
                    SRTSession session = mSRTServer.getSRTSession();
                    if (session != null) {
                        AudioEncoder audioEncoder = session.getAudioEncoder();
                        if (audioEncoder  != null) {
                            audioEncoder.setMute(mute);
                        }
                    }
                }
            }).start();
        }
    }

    private final SRTServer.Callback mCallback = new SRTServer.Callback() {
        @Override
        public void createSession(final SRTSession session) {
            ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();

            HostMediaRecorder.PictureSize size = recorder.getPreviewSize();

            ScreenCastVideoEncoder videoEncoder = new ScreenCastVideoEncoder(mScreenCastMgr);
            VideoQuality videoQuality = videoEncoder.getVideoQuality();
            videoQuality.setVideoWidth(size.getWidth());
            videoQuality.setVideoHeight(size.getHeight());
            videoQuality.setBitRate(recorder.getPreviewBitRate());
            videoQuality.setFrameRate((int) recorder.getMaxFrameRate());
            videoQuality.setIFrameInterval(recorder.getIFrameInterval());
            session.setVideoEncoder(videoEncoder);

            // TODO 音声の設定を外部から設定できるようにすること。

            AudioEncoder audioEncoder = new MicAACLATMEncoder();
            audioEncoder.setMute(isMuted());

            AudioQuality audioQuality = audioEncoder.getAudioQuality();
            audioQuality.setChannel(AudioFormat.CHANNEL_IN_MONO);
            audioQuality.setSamplingRate(8000);
            audioQuality.setBitRate(64 * 1024);
            audioQuality.setUseAEC(true);

            session.setAudioEncoder(audioEncoder);
        }

        @Override
        public void releaseSession(final SRTSession session) {
        }
    };
}
