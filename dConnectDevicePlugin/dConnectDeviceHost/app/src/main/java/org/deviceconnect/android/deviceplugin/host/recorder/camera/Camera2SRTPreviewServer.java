package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.RtspSession;
import org.deviceconnect.android.libmedia.streaming.util.IpAddressManager;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;
import org.deviceconnect.android.libsrt.server.video.VideoStream;

import java.io.IOException;
import java.net.InetAddress;

public class Camera2SRTPreviewServer extends AbstractPreviewServer {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "CameraSRT";

    private static final String MIME_TYPE = "video/x-srt"; // TODO SRTプロトコルの場合のマイムタイプを決定

    private Camera2Recorder mRecorder;

    private SRTServer mSRTServer;

    private final IpAddressManager mAddressManager = new IpAddressManager();

    Camera2SRTPreviewServer(final Context context,
                                   final AbstractPreviewServerProvider serverProvider,
                                   final Camera2Recorder recorder) {
        super(context, serverProvider);
        mRecorder = recorder;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        if (mSRTServer == null) {

            // TODO Wi-Fi に接続されてないくても起動できるようにする
            mAddressManager.storeIPAddress();
            InetAddress ipAddress = mAddressManager.getWifiIPv4Address();
            if (ipAddress == null) {
                callback.onFail();
                return;
            }

            mSRTServer = new SRTServer(ipAddress.getHostAddress(), 12345);
            mSRTServer.setStatsEnabled(DEBUG); // TODO build.gradle の定数で指定できるようにする
            mSRTServer.setCallback(mCallback);
            try {
                mSRTServer.start();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.d(TAG, "Failed to start SRT server.", e);
                }
                callback.onFail();
            }
        }
        callback.onStart("srt://" + mSRTServer.getServerAddress() + ":" + mSRTServer.getServerPort());
    }

    @Override
    public void stopWebServer() {
        if (mSRTServer != null) {
            mSRTServer.stop();
            mSRTServer = null;
        }
        unregisterConfigChangeReceiver();
    }

    @Override
    public void onConfigChange() {
        if (mSRTServer != null) {
            new Thread(() -> {
                if (mSRTServer != null) {
                    SRTSession session = mSRTServer.getSRTSession();
                    if (session != null) {
                        session.getVideoStream().getVideoEncoder().restart();
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

            SRTCameraVideoStream videoStream = new SRTCameraVideoStream(mRecorder);
            VideoQuality videoQuality = videoStream.getVideoEncoder().getVideoQuality();
            HostDeviceRecorder.PictureSize previewSize = getRotatedPreviewSize();
            videoQuality.setVideoWidth(previewSize.getHeight());
            videoQuality.setVideoHeight(previewSize.getWidth());
            videoQuality.setBitRate(getServerProvider().getPreviewBitRate());
            videoQuality.setFrameRate((int) getServerProvider().getMaxFrameRate());
            videoQuality.setIFrameInterval(2);
            session.setVideoStream(videoStream);

            registerConfigChangeReceiver();

        }

        @Override
        public void releaseSession(final SRTSession session) {
            if (DEBUG) {
                Log.d(TAG, "RtspServer.Callback#releaseSession()");
            }

            unregisterConfigChangeReceiver();
        }
    };

    private class SRTCameraVideoStream extends VideoStream {

        /**
         * 映像用エンコーダ.
         */
        private final VideoEncoder mVideoEncoder;

        SRTCameraVideoStream(final Camera2Recorder camera2Recorder) {
            mVideoEncoder = new CameraVideoEncoder(camera2Recorder);
        }

        @Override
        public VideoEncoder getVideoEncoder() {
            return mVideoEncoder;
        }
    }
}
