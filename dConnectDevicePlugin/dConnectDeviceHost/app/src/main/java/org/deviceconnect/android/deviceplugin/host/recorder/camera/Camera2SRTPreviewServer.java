package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
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
        setPort(port);
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
            mSRTServer = new SRTServer(getPort());
            mSRTServer.setCallback(mCallback);
            try {
                mSRTServer.start();
                mSRTServer.startStatsTimer();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.d(TAG, "Failed to start SRT server.", e);
                }
                callback.onFail();
            }
        }
        callback.onStart(getUri());
    }

    @Override
    public void stopWebServer() {
        if (mSRTServer != null) {
            mSRTServer.stopStatsTimer();
            mSRTServer.stop();
            mSRTServer = null;
        }
    }

    @Override
    public void onConfigChange() {
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

    private final SRTServer.Callback mCallback = new SRTServer.Callback() {
        @Override
        public void createSession(final SRTSession session) {
            if (DEBUG) {
                Log.d(TAG, "RtspServer.Callback#createSession()");
            }

            postOnCameraStarted();

            Camera2Recorder recorder = (Camera2Recorder) getRecorder();

            HostDeviceRecorder.PictureSize size = recorder.getPreviewSize();

            CameraVideoEncoder encoder = new CameraVideoEncoder(mRecorder);
            VideoQuality videoQuality = encoder.getVideoQuality();
            videoQuality.setVideoWidth(size.getWidth());
            videoQuality.setVideoHeight(size.getHeight());
            videoQuality.setBitRate(recorder.getPreviewBitRate());
            videoQuality.setFrameRate((int) recorder.getMaxFrameRate());
            videoQuality.setIFrameInterval(2);
            session.setVideoEncoder(encoder);
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
