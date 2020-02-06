package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.media.AudioFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
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
class Camera2RTSPPreviewServer extends Camera2PreviewServer {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "CameraRTSP";

    /**
     * マイムタイプを定義します.
     */
    private static final String MIME_TYPE = "video/x-rtp";

    /**
     * サーバー名を定義します.
     */
    private static final String SERVER_NAME = "Android Host Camera2 RTSP Server";

    /**
     * カメラを操作するレコーダ.
     */
    private Camera2Recorder mRecorder;

    /**
     * RTSP 配信サーバ.
     */
    private RtspServer mRtspServer;

    Camera2RTSPPreviewServer(Context context, Camera2Recorder recorder, int port) {
        super(context, recorder);
        mRecorder = recorder;
        setPort(port);
    }

    Camera2RTSPPreviewServer(Context context, Camera2Recorder recorder, int port, OnEventListener onEventListener) {
        super(context, recorder);
        mRecorder = recorder;
        setPort(port);
        setOnEventListener(onEventListener);
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
     * AudioEncoder にミュート設定を行います.
     *
     * @param mute ミュート設定
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

            // カメラを開始することを通知
            postOnCameraStarted();


            Camera2Recorder recorder = (Camera2Recorder) getRecorder();

            HostDeviceRecorder.PictureSize size = recorder.getPreviewSize();

            CameraVideoStream videoStream = new CameraVideoStream(mRecorder);
            videoStream.setDestinationPort(5006);

            VideoQuality videoQuality = videoStream.getVideoEncoder().getVideoQuality();
            videoQuality.setVideoWidth(size.getWidth());
            videoQuality.setVideoHeight(size.getHeight());
            videoQuality.setBitRate(recorder.getPreviewBitRate());
            videoQuality.setFrameRate((int) recorder.getMaxFrameRate());
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
        }

        @Override
        public void releaseSession(RtspSession session) {
            if (DEBUG) {
                Log.d(TAG, "RtspServer.Callback#releaseSession()");
            }

            // カメラを停止したことを通知
            postOnCameraStopped();
        }
    };
}
