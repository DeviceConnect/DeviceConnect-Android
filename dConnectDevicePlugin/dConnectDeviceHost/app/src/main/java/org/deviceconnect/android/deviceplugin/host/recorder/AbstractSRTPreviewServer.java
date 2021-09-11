package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;

public abstract class AbstractSRTPreviewServer extends AbstractPreviewServer {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "CameraSRT";

    /**
     * プレビュー配信サーバのマイムタイプを定義.
     */
    private static final String MIME_TYPE = "video/MP2T";

    /**
     * SRT サーバ.
     */
    private SRTServer mSRTServer;

    public AbstractSRTPreviewServer(HostMediaRecorder recorder, String encoderId) {
        super(recorder, encoderId);
    }

    @Override
    public String getUri() {
        return "srt://localhost:" + getEncoderSettings().getPort();
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public boolean isRunning() {
        return mSRTServer != null;
    }

    @Override
    public void start(final OnStartCallback callback) {
        if (mSRTServer == null) {
            try {
                HostMediaRecorder.EncoderSettings settings = getEncoderSettings();
                mSRTServer = new SRTServer(getEncoderSettings().getPort());
                mSRTServer.setStatsInterval(BuildConfig.STATS_INTERVAL);
                mSRTServer.setShowStats(DEBUG);
                mSRTServer.setCallback(mCallback);
                mSRTServer.setSocketOptions(settings.getSRTSocketOptions());
                mSRTServer.start();
            } catch (Exception e) {
                callback.onFailed(e);
                return;
            }
        }
        callback.onSuccess();
    }

    @Override
    public void stop() {
        if (mSRTServer != null) {
            mSRTServer.stop();
            mSRTServer = null;
        }
    }

    @Override
    public long getBPS() {
        // TODO
        return 0;
    }

    @Override
    public void onConfigChange() {
        super.onConfigChange();
        restartVideoEncoder();
    }

    @Override
    public void setMute(boolean mute) {
        super.setMute(mute);

        if (mSRTServer != null) {
            SRTSession session = mSRTServer.getSRTSession();
            if (session != null) {
                AudioEncoder audioEncoder = session.getAudioEncoder();
                if (audioEncoder  != null) {
                    audioEncoder.setMute(mute);
                }
            }
        }
    }

    @Override
    protected VideoEncoder getVideoEncoder() {
        if (mSRTServer != null) {
            SRTSession session = mSRTServer.getSRTSession();
            if (session != null) {
                return session.getVideoEncoder();
            }
        }
        return null;
    }

    @Override
    protected AudioEncoder getAudioEncoder() {
        if (mSRTServer != null) {
            SRTSession session = mSRTServer.getSRTSession();
            if (session != null) {
                return session.getAudioEncoder();
            }
        }
        return null;
    }

    /**
     * エンコーダの設定を反映して、再スタートします.
     */
    private void restartVideoEncoder() {
        if (mSRTServer != null) {
            SRTSession session = mSRTServer.getSRTSession();
            if (session != null) {
                session.restartVideoEncoder();
            }
        }
    }

    /**
     * エンコーダの設定を反映して、再スタートします.
     */
    private void restartAudioEncoder() {
        if (mSRTServer != null) {
            SRTSession session = mSRTServer.getSRTSession();
            if (session != null) {
                session.restartAudioEncoder();
            }
        }
    }

    /**
     * SRT 用の映像エンコーダを作成します.
     *
     * null を返却した場合には、映像は配信しません。
     *
     * このメソッドを実装することでエンコーダを切り替えます。
     *
     * @return SRT 用の映像エンコーダ
     */
    protected VideoEncoder createVideoEncoder() {
        return null;
    }

    /**
     * SRT 用の音声エンコーダを作成します.
     *
     * null を返却した場合には、音声は配信しません。
     *
     * デフォルトで、 aac のエンコーダを作成して返却します。
     * aac 以外のエンコーダを実装する場合には、このメソッドをオーバーライドします。
     *
     * @return SRT 用の音声エンコーダ
     */
    protected AudioEncoder createAudioEncoder() {
        HostMediaRecorder recorder = getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();
        if (settings.isAudioEnabled()) {
            return new MicAACLATMEncoder();
        }
        return null;
    }

    /**
     * SRTServer からのイベントを受け取るためのコールバック.
     */
    private final SRTServer.Callback mCallback = new SRTServer.Callback() {
        @Override
        public void createSession(final SRTSession session) {
            if (DEBUG) {
                Log.d(TAG, "SRTServer.Callback#createSession()");
            }

            VideoEncoder videoEncoder = createVideoEncoder();
            if (videoEncoder != null) {
                setVideoQuality(videoEncoder.getVideoQuality());
                session.setVideoEncoder(videoEncoder);
            }

            AudioEncoder audioEncoder = createAudioEncoder();
            if (audioEncoder != null) {
                audioEncoder.setMute(isMuted());
                setAudioQuality(audioEncoder.getAudioQuality());
                session.setAudioEncoder(audioEncoder);
            }
        }

        @Override
        public void releaseSession(final SRTSession session) {
            if (DEBUG) {
                Log.d(TAG, "SRTServer.Callback#releaseSession()");
            }
        }
    };
}
