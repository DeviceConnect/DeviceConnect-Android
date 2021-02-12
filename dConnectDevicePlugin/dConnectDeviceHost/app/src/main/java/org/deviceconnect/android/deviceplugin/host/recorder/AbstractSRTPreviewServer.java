package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.util.SRTSettings;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;

import java.io.IOException;

public abstract class AbstractSRTPreviewServer extends AbstractPreviewServer {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "CameraSRT";

    /**
     * プレビュー配信サーバのマイムタイプを定義.
     */
    private static final String MIME_TYPE = "video/MP2T";

    /**
     * SRTの設定.
     */
    private final SRTSettings mSettings;

    /**
     * SRT サーバ.
     */
    private SRTServer mSRTServer;

    public AbstractSRTPreviewServer(Context context, HostMediaRecorder recorder) {
        this(context, recorder, false);
    }

    public AbstractSRTPreviewServer(Context context, HostMediaRecorder recorder, boolean useSSL) {
        super(context, recorder, useSSL);
        mSettings = new SRTSettings(context);
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
                mSRTServer.setStatsInterval(BuildConfig.STATS_INTERVAL);
                mSRTServer.setShowStats(DEBUG);
                mSRTServer.setCallback(mCallback);
                mSRTServer.setSocketOptions(mSettings.loadSRTSocketOptions());
                mSRTServer.start();
            } catch (Exception e) {
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
    public boolean requestSyncFrame() {
        SRTServer server = mSRTServer;
        if (server != null) {
            SRTSession session = server.getSRTSession();
            if (session != null) {
                VideoEncoder videoEncoder = session.getVideoEncoder();
                if (videoEncoder != null) {
                    videoEncoder.requestSyncKeyFrame();
                    return true;
                }
            }
        }
        return false;
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
    protected VideoQuality getVideoQuality() {
        if (mSRTServer != null) {
            SRTSession session = mSRTServer.getSRTSession();
            if (session != null) {
                VideoEncoder videoEncoder = session.getVideoEncoder();
                if (videoEncoder != null) {
                    return videoEncoder.getVideoQuality();
                }
            }
        }
        return null;
    }

    @Override
    protected AudioQuality getAudioQuality() {
        if (mSRTServer != null) {
            SRTSession session = mSRTServer.getSRTSession();
            if (session != null) {
                AudioEncoder audioEncoder = session.getAudioEncoder();
                if (audioEncoder != null) {
                    return audioEncoder.getAudioQuality();
                }
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
     * SRT 用の映像エンコーダを作成します.
     *
     * @return SRT 用の映像エンコーダ
     */
    protected VideoEncoder createVideoEncoder() {
        return null;
    }

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
