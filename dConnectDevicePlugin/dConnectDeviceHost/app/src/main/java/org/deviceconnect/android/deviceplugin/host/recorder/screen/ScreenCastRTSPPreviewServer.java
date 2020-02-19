package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSetting;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.rtsp.RtspServer;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.RtspSession;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.AudioStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.MicAACLATMStream;

import java.io.IOException;

import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenCastRTSPPreviewServer extends ScreenCastPreviewServer {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "ScreenCastRTSP";

    /**
     * RTSP のマイムタイプを定義します.
     */
    private static final String MIME_TYPE = "video/x-rtp";

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

    ScreenCastRTSPPreviewServer(Context context, ScreenCastRecorder recorder, int port) {
        super(context, recorder);
        mScreenCastMgr = recorder.getScreenCastMgr();
        setPort(RecorderSetting.getInstance(getContext()).getPort(recorder.getId(), MIME_TYPE, port));
    }

    @Override
    public String getUri() {
        return "rtsp://localhost:" + getPort();
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
        callback.onStart(getUri());
    }

    @Override
    public void stopWebServer() {
        if (mRtspServer != null) {
            mRtspServer.stop();
            mRtspServer = null;
        }
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
                        session.restartVideoStream();
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
                            stream.setMute(mute);
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

            ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();

            // スクリーンキャストの設定が変更されて再開される場合が存在するので、
            // ここで、prepare を override しておき、スクリーンキャストの設定を反映させます。
            ScreenCastVideoStream videoStream = new ScreenCastVideoStream(mScreenCastMgr, 5006) {
                @Override
                void prepareVideoEncoder() {
                    setVideoQuality(getVideoEncoder().getVideoQuality());
                }
            };
            setVideoQuality(videoStream.getVideoEncoder().getVideoQuality());
            session.setVideoMediaStream(videoStream);

            if (recorder.isAudioEnabled()) {
                AudioStream audioStream = new MicAACLATMStream(5004);
                AudioEncoder audioEncoder = audioStream.getAudioEncoder();
                audioEncoder.setMute(isMuted());
                setAudioQuality(audioEncoder.getAudioQuality());
                session.setAudioMediaStream(audioStream);
            }
        }

        @Override
        public void releaseSession(RtspSession session) {
            if (DEBUG) {
                Log.d(TAG, "RtspServer.Callback#releaseSession()");
            }
        }
    };
}
