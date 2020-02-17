package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.media.AudioFormat;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSetting;
import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;

import java.io.IOException;

public class Camera2SRTPreviewServer extends Camera2PreviewServer {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "CameraSRT";

    private static final String MIME_TYPE = "video/MP2T";

    private Camera2Recorder mRecorder;

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
            new Thread(() -> {
                if (mSRTServer != null) {
                    SRTSession session = mSRTServer.getSRTSession();
                    if (session != null) {
                        session.getVideoEncoder().restart();
                    }
                }
            }).start();
        }
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
            if (DEBUG) {
                Log.d(TAG, "RtspServer.Callback#createSession()");
            }

            postOnCameraStarted();

            Camera2Recorder recorder = (Camera2Recorder) getRecorder();

            CameraVideoEncoder encoder = new CameraVideoEncoder(mRecorder);
            VideoQuality videoQuality = encoder.getVideoQuality();
            videoQuality.setVideoWidth(recorder.getPreviewSize().getWidth());
            videoQuality.setVideoHeight(recorder.getPreviewSize().getHeight());
            videoQuality.setBitRate(recorder.getPreviewBitRate());
            videoQuality.setFrameRate((int) recorder.getMaxFrameRate());
            videoQuality.setIFrameInterval(recorder.getIFrameInterval());
            session.setVideoEncoder(encoder);

            if (recorder.isAudioEnabled()) {
                AudioEncoder audioEncoder = new MicAACLATMEncoder();
                audioEncoder.setMute(isMuted());

                AudioQuality audioQuality = audioEncoder.getAudioQuality();
                audioQuality.setChannel(recorder.getPreviewChannel() == 1 ?
                        AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO);
                audioQuality.setSamplingRate(recorder.getPreviewSampleRate());
                audioQuality.setBitRate(recorder.getPreviewAudioBitRate());
                audioQuality.setUseAEC(recorder.isUseAEC());

                session.setAudioEncoder(audioEncoder);
            }
        }

        @Override
        public void releaseSession(final SRTSession session) {
            if (DEBUG) {
                Log.d(TAG, "RtspServer.Callback#releaseSession()");
            }

            postOnCameraStopped();
        }
    };
}
