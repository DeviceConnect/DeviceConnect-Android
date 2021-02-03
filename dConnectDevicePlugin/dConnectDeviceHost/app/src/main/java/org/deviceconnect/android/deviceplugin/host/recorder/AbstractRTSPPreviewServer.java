package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.rtsp.RtspServer;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.RtspSession;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.AudioStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.MicAACLATMStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.io.IOException;

public abstract class AbstractRTSPPreviewServer extends AbstractPreviewServer {

    /**
     * マイムタイプを定義します.
     */
    private static final String MIME_TYPE = "video/x-rtp";

    /**
     * サーバー名を定義します.
     */
    private static final String SERVER_NAME = "Android Host RTSP Server";

    /**
     * RTSP 配信サーバ.d
     */
    private RtspServer mRtspServer;

    public AbstractRTSPPreviewServer(Context context, HostMediaRecorder recorder) {
        this(context, recorder, false);
    }

    public AbstractRTSPPreviewServer(Context context, HostMediaRecorder recorder, boolean useSSL) {
        super(context, recorder, useSSL);
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
    public long getBPS() {
        return mRtspServer != null ? mRtspServer.getBPS() : 0;
    }

    @Override
    public void onConfigChange() {
        super.onConfigChange();
        restartVideoStream();
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

    @Override
    protected VideoQuality getVideoQuality() {
        if (mRtspServer != null) {
            RtspSession session = mRtspServer.getRtspSession();
            if (session != null) {
                VideoStream videoStream = session.getVideoStream();
                if (videoStream != null) {
                    return videoStream.getVideoEncoder().getVideoQuality();
                }
            }
        }
        return null;
    }

    @Override
    protected AudioQuality getAudioQuality() {
        if (mRtspServer != null) {
            RtspSession session = mRtspServer.getRtspSession();
            if (session != null) {
                AudioStream audioStream = session.getAudioStream();
                if (audioStream != null) {
                    return audioStream.getAudioEncoder().getAudioQuality();
                }
            }
        }
        return null;
    }

    /**
     * エンコーダに設定を反映し、再スタートします.
     */
    private void restartVideoStream() {
        if (mRtspServer != null) {
            RtspSession session = mRtspServer.getRtspSession();
            if (session != null) {
                session.restartVideoStream();
            }
        }
    }

    /**
     * 映像用の VideoStream を作成します.
     *
     * @return VideoStream のインスタンス
     */
    protected abstract VideoStream createVideoStream();

    /**
     * RtspServer からのイベントを受け取るためのコールバック.
     *
     * <p>
     * RTSP 配信の開始時と停止時に呼び出されます。
     * </p>
     */
    private final RtspServer.Callback mCallback = new RtspServer.Callback() {
        @Override
        public void createSession(RtspSession session) {
            if (DEBUG) {
                Log.d(TAG, "RtspServer.Callback#createSession()");
            }

            HostMediaRecorder recorder = getRecorder();
            HostMediaRecorder.Settings settings = recorder.getSettings();

            VideoStream videoStream = createVideoStream();
            setVideoQuality(videoStream.getVideoEncoder().getVideoQuality());
            session.setVideoMediaStream(videoStream);

            if (settings.isAudioEnabled()) {
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
