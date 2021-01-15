package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.rtsp.RtspServer;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.RtspSession;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.AudioStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.MicAACLATMStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.VideoStream;

import java.io.IOException;

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
     * RTSP サーバ.
     */
    private RtspServer mRtspServer;

    ScreenCastRTSPPreviewServer(Context context, ScreenCastRecorder recorder, int port) {
        super(context, recorder);
        setPort(port);
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
    public boolean requestSyncFrame() {
        RtspServer server = mRtspServer;
        if (server != null) {
            RtspSession session = server.getRtspSession();
            if (session != null) {
                VideoStream videoStream = session.getVideoStream();
                if (videoStream != null) {
                    videoStream.getVideoEncoder().requestSyncKeyFrame();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onConfigChange() {
        setEncoderQuality();

        if (mRtspServer != null) {
            RtspSession session = mRtspServer.getRtspSession();
            if (session != null) {
                session.restartVideoStream();
            }
        }
    }

    @Override
    public void setMute(boolean mute) {
        super.setMute(mute);

        if (mRtspServer != null) {
            RtspSession session = mRtspServer.getRtspSession();
            if (session != null) {
                AudioStream stream = session.getAudioStream();
                if (stream  != null) {
                    stream.setMute(mute);
                }
            }
        }
    }

    /**
     * エンコーダの設定を行います.
     */
    private void setEncoderQuality() {
        if (mRtspServer != null) {
            RtspSession session = mRtspServer.getRtspSession();
            if (session != null) {
                VideoStream videoStream = session.getVideoStream();
                if (videoStream != null) {
                    setVideoQuality(videoStream.getVideoEncoder().getVideoQuality());
                }

                AudioStream audioStream = session.getAudioStream();
                if (audioStream != null) {
                    setAudioQuality(audioStream.getAudioEncoder().getAudioQuality());
                }
            }
        }
    }

    /**
     * RtspServer からのイベントを受け取るためのコールバック.
     */
    private final RtspServer.Callback mCallback = new RtspServer.Callback() {
        @Override
        public void createSession(RtspSession session) {
            if (DEBUG) {
                Log.d(TAG, "RtspServer.Callback#createSession()");
            }

            ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();
            HostMediaRecorder.Settings settings = recorder.getSettings();

            VideoStream videoStream;
            if ("video/hevc".equals(settings.getPreviewEncoder())) {
                videoStream = new ScreenCastH265VideoStream(recorder, 5006);
            } else {
                videoStream = new ScreenCastH264VideoStream(recorder, 5006);
            }
            setVideoQuality(videoStream.getVideoEncoder().getVideoQuality());
            session.setVideoMediaStream(videoStream);

            if (recorder.getSettings().isAudioEnabled()) {
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
