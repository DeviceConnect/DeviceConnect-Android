package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSetting;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;

import java.io.IOException;

public class Camera2SRTPreviewServer extends Camera2PreviewServer {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "CameraSRT";

    /**
     * プレビュー配信サーバのマイムタイプを定義.
     */
    private static final String MIME_TYPE = "video/MP2T";

    /**
     * プレビュー配信を行うレコーダ.
     */
    private Camera2Recorder mRecorder;

    /**
     * SRT サーバ.
     */
    private SRTServer mSRTServer;

    Camera2SRTPreviewServer(final Context context, final Camera2Recorder recorder, final int port, final OnEventListener listener) {
        super(context, recorder);
        mRecorder = recorder;
        setPort(RecorderSetting.getInstance(getContext()).getPort(recorder.getId(), MIME_TYPE, port));
        setOnEventListener(listener);
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
                mSRTServer.setSocketOptions(RecorderSetting.getInstance(getContext()).loadSRTSocketOptions());
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
    public void onConfigChange() {
        setEncoderQuality();
        restartCamera();
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

    // Camera2PreviewServer

    @Override
    void restartCamera() {
        if (mSRTServer != null) {
            SRTSession session = mSRTServer.getSRTSession();
            if (session != null) {
                session.restartVideoEncoder();
            }
        }
    }

    /**
     * AudioEncoder にミュート設定を行います.
     *
     * @param mute ミュート設定
     */
    private void setMute(boolean mute) {
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

    /**
     * エンコーダの設定を行います.
     */
    private void setEncoderQuality() {
        if (mSRTServer != null) {
            SRTSession session = mSRTServer.getSRTSession();
            if (session != null) {
                VideoEncoder videoEncoder = session.getVideoEncoder();
                if (videoEncoder != null) {
                    setVideoQuality(videoEncoder.getVideoQuality());
                }

                AudioEncoder audioEncoder = session.getAudioEncoder();
                if (audioEncoder != null) {
                    setAudioQuality(audioEncoder.getAudioQuality());
                }
            }
        }
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

            postOnCameraStarted();

            Camera2Recorder recorder = (Camera2Recorder) getRecorder();

            CameraVideoEncoder encoder = new CameraVideoEncoder(mRecorder);
            setVideoQuality(encoder.getVideoQuality());
            session.setVideoEncoder(encoder);

            if (recorder.getSettings().isAudioEnabled()) {
                AudioEncoder audioEncoder = new MicAACLATMEncoder();
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

            postOnCameraStopped();
        }
    };
}
