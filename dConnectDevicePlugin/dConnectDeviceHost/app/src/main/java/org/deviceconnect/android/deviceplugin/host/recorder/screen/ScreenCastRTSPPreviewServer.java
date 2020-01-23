package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.media.AudioFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.rtsp.RtspServer;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.RtspSession;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.AudioStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.MicAACLATMStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.io.IOException;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class ScreenCastRTSPPreviewServer extends ScreenCastPreviewServer {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "ScreenCastRTSP";

    /**
     * RTSP のマイムタイプを定義します.
     */
    static final String MIME_TYPE = "video/x-rtp";

    /**
     * RTSP のサーバ名を定義します.
     */
    private static final String SERVER_NAME = "Android Host Screen RTSP Server";

    /**
     * 画面のキャプチャを管理するクラス.
     */
    private ScreenCastManager mScreenCastMgr;

    /**
     * RTSP サーバ.
     */
    private RtspServer mRtspServer;

    ScreenCastRTSPPreviewServer(Context context,
                                AbstractPreviewServerProvider serverProvider,
                                ScreenCastManager screenCastMgr) {
        super(context, serverProvider);
        mScreenCastMgr = screenCastMgr;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        if (mRtspServer == null) {
            mRtspServer = new RtspServer();
            mRtspServer.setServerName(SERVER_NAME);
            mRtspServer.setServerPort(20000);
            mRtspServer.setCallback(mCallback);
            try {
                mRtspServer.start();
            } catch (IOException e) {
                callback.onFail();
                return;
            }
        }
        callback.onStart("rtsp://localhost:20000");
    }

    @Override
    public void stopWebServer() {
        if (mRtspServer != null) {
            mRtspServer.stop();
            mRtspServer = null;
        }
    }

    @Override
    protected void onConfigChange() {
        if (DEBUG) {
            Log.d(TAG, "ScreenCastRTSPPreviewServer#onConfigChange");
        }

        if (mRtspServer != null) {
            new Thread(() -> {
                if (mRtspServer != null) {
                    mRtspServer.getRtspSession().getVideoStream().getVideoEncoder().restart();
                }
            }).start();
        }
    }

    @Override
    public int getQuality() {
        return 0; // Not support.
    }

    @Override
    public void setQuality(int quality) {
        // Not support.
    }

    private final RtspServer.Callback mCallback = new RtspServer.Callback() {
        @Override
        public void createSession(RtspSession session) {
            if (DEBUG) {
                Log.d(TAG, "RtspServer.Callback#createSession()");
            }

            ScreenCastVideoStream videoStream = new ScreenCastVideoStream(mScreenCastMgr);
            videoStream.setDestinationPort(5006);

            HostDeviceRecorder.PictureSize previewSize = getRotatedPreviewSize();

            VideoQuality videoQuality = videoStream.getVideoEncoder().getVideoQuality();
            videoQuality.setVideoWidth(320);
            videoQuality.setVideoHeight(560);
            videoQuality.setBitRate(1024 * 1024);
            videoQuality.setFrameRate(30);
            videoQuality.setIFrameInterval(2);

            session.setVideoMediaStream(videoStream);

            if (!isMuted()) {
                AudioStream audioStream = new MicAACLATMStream();
                audioStream.setDestinationPort(5004);

                AudioEncoder audioEncoder = audioStream.getAudioEncoder();
                AudioQuality audioQuality = audioEncoder.getAudioQuality();
                audioQuality.setChannel(AudioFormat.CHANNEL_IN_MONO);
                audioQuality.setSamplingRate(48000);
                audioQuality.setBitRate(64 * 1024);
                audioQuality.setUseAEC(true);

                session.setAudioMediaStream(audioStream);
            }

            registerConfigChangeReceiver();
        }

        @Override
        public void releaseSession(RtspSession session) {
            if (DEBUG) {
                Log.d(TAG, "RtspServer.Callback#releaseSession()");
            }

            unregisterConfigChangeReceiver();
        }
    };
}
