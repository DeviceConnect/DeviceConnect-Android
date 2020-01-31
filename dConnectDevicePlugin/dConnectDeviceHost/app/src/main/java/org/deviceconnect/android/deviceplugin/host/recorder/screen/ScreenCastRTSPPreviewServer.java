package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.media.AudioFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
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
class ScreenCastRTSPPreviewServer extends AbstractPreviewServer {
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
        setPort(20000);
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
            mRtspServer.setServerPort(getPort());
            mRtspServer.setCallback(mCallback);
            try {
                mRtspServer.start();
            } catch (IOException e) {
                callback.onFail();
                return;
            }
        }
        callback.onStart("rtsp://localhost:" + getPort());
    }

    @Override
    public void stopWebServer() {
        if (mRtspServer != null) {
            mRtspServer.stop();
            mRtspServer = null;
        }
        unregisterConfigChangeReceiver();
    }

    @Override
    public void onConfigChange() {
        if (DEBUG) {
            Log.d(TAG, "ScreenCastRTSPPreviewServer#onConfigChange");
        }

        if (mRtspServer != null) {
            new Thread(() -> {
                if (mRtspServer != null) {
                    RtspSession session = mRtspServer.getRtspSession();
                    if (session != null) {
                        session.getVideoStream().getVideoEncoder().restart();
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
     * ミュート設定をエンコーダに設定します.
     *
     * @param mute ミュートする場合はtrue、それ以外はfalse
     */
    private void setMute(boolean mute) {
        if (mRtspServer != null) {
            new Thread(() -> {
                if (mRtspServer != null) {
                    RtspSession session = mRtspServer.getRtspSession();
                    if (session != null) {
                        AudioStream stream = session.getAudioStream();
                        if (stream  != null) {
                            stream.getAudioEncoder().setMute(mute);
                            stream.getAudioEncoder().restart();
                        }
                    }
                }
            }).start();
        }
    }

    private final RtspServer.Callback mCallback = new RtspServer.Callback() {
        @Override
        public void createSession(RtspSession session) {
            if (DEBUG) {
                Log.d(TAG, "RtspServer.Callback#createSession()");
            }

            ScreenCastVideoStream videoStream = new ScreenCastVideoStream(mScreenCastMgr);
            videoStream.setDestinationPort(5006);

            HostDeviceRecorder.PictureSize size = getServerProvider().getPreviewSize();

            VideoQuality videoQuality = videoStream.getVideoEncoder().getVideoQuality();
            videoQuality.setVideoWidth(size.getWidth());
            videoQuality.setVideoHeight(size.getHeight());
            videoQuality.setBitRate(getServerProvider().getPreviewBitRate());
            videoQuality.setFrameRate((int) getServerProvider().getMaxFrameRate());
            videoQuality.setIFrameInterval(2);

            session.setVideoMediaStream(videoStream);

            // TODO 音声の設定を外部から設定できるようにすること。

            AudioStream audioStream = new MicAACLATMStream();
            audioStream.setDestinationPort(5004);

            AudioEncoder audioEncoder = audioStream.getAudioEncoder();
            audioEncoder.setMute(isMuted());
            AudioQuality audioQuality = audioEncoder.getAudioQuality();
            audioQuality.setChannel(AudioFormat.CHANNEL_IN_MONO);
            audioQuality.setSamplingRate(8000);
            audioQuality.setBitRate(64 * 1024);
            audioQuality.setUseAEC(true);

            session.setAudioMediaStream(audioStream);

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
