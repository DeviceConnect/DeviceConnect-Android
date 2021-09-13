package org.deviceconnect.android.deviceplugin.host.recorder;

import android.util.Log;

import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.rtsp.RtspServer;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.RtspSession;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.AudioStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.audio.MicAACLATMStream;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

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


    public AbstractRTSPPreviewServer(HostMediaRecorder recorder, String encoderId) {
        super(recorder, encoderId);
    }

    @Override
    public String getUri() {
        return "rtsp://localhost:" + getEncoderSettings().getPort();
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public boolean isRunning() {
        return mRtspServer != null;
    }

    @Override
    public void start(final OnStartCallback callback) {
        if (mRtspServer == null) {
            mRtspServer = new RtspServer();
            mRtspServer.setServerName(SERVER_NAME);
            mRtspServer.setServerPort(getEncoderSettings().getPort());
            mRtspServer.setCallback(mCallback);
            try {
                mRtspServer.start();
            } catch (Exception e) {
                callback.onFailed(e);
                return;
            }
        }
        callback.onSuccess();
    }

    @Override
    public void stop() {
        if (mRtspServer != null) {
            mRtspServer.stop();
            mRtspServer = null;
        }
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
    protected VideoEncoder getVideoEncoder() {
        if (mRtspServer != null) {
            RtspSession session = mRtspServer.getRtspSession();
            if (session != null) {
                VideoStream stream = session.getVideoStream();
                if (stream  != null) {
                    return stream.getVideoEncoder();
                }
            }
        }
        return null;
    }

    @Override
    protected AudioEncoder getAudioEncoder() {
        if (mRtspServer != null) {
            RtspSession session = mRtspServer.getRtspSession();
            if (session != null) {
                AudioStream stream = session.getAudioStream();
                if (stream  != null) {
                    return stream.getAudioEncoder();
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
     * エンコーダに設定を反映し、再スタートします.
     */
    private void restartAudioStream() {
        if (mRtspServer != null) {
            RtspSession session = mRtspServer.getRtspSession();
            if (session != null) {
                session.restartAudioStream();
            }
        }
    }

    /**
     * 映像用の VideoStream を作成します.
     *
     * @return VideoStream のインスタンス
     */
    protected VideoStream createVideoStream() {
        return null;
    }

    /**
     * 音声用の AudioStream を作成します.
     *
     * @return AudioStream のインスタンス
     */
    protected AudioStream createAudioStream() {
        HostMediaRecorder recorder = getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();
        if (settings.isAudioEnabled()) {
            return new MicAACLATMStream(5004);
        }
        return null;
    }

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

            VideoStream videoStream = createVideoStream();
            if (videoStream != null) {
                setVideoQuality(videoStream.getVideoEncoder().getVideoQuality());
                session.setVideoMediaStream(videoStream);
            }

            AudioStream audioStream = createAudioStream();
            if (audioStream != null) {
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
