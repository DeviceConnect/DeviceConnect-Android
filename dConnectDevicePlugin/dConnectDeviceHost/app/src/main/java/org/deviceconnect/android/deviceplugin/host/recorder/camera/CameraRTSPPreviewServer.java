/*
 CameraRTSPPreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.hardware.Camera;
import android.preference.PreferenceManager;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.rtsp.RtspServerImpl;
import net.majorkernelpanic.streaming.video.CameraH264Stream;
import net.majorkernelpanic.streaming.video.VideoQuality;
import net.majorkernelpanic.streaming.video.VideoStream;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;

import java.net.Socket;
import java.util.concurrent.CountDownLatch;


class CameraRTSPPreviewServer implements CameraPreviewServer, RtspServer.Delegate {

    private static final String MIME_TYPE = "video/x-rtp";

    private static final String SERVER_NAME = "Android Host Camera RTSP Server";

    private final AbstractPreviewServerProvider mServerProvider;

    private final Object mLockObj = new Object();

    private final CameraOverlay mCameraOverlay;

    private final Context mContext;

    private RtspServer mRtspServer;

    CameraRTSPPreviewServer(final Context context,
                            final CameraOverlay cameraOverlay,
                            final AbstractPreviewServerProvider serverProvider) {
        mContext = context;
        mCameraOverlay = cameraOverlay;
        mServerProvider = serverProvider;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        synchronized (mLockObj) {
            if (mRtspServer == null) {
                mRtspServer = new RtspServerImpl(SERVER_NAME);
                mRtspServer.setPort(RtspServer.DEFAULT_PORT);
                mRtspServer.setDelegate(this);
                if (!mRtspServer.start()) {
                    callback.onFail();
                    return;
                }
            }
            String uri = "rtsp://localhost:" + mRtspServer.getPort();
            callback.onStart(uri);
        }
    }

    @Override
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mRtspServer != null) {
                mRtspServer.stop();
                mRtspServer = null;

                mCameraOverlay.removePreviewCallback(this);
                mCameraOverlay.hide();
                mServerProvider.hideNotification();
            }
        }
    }

    @Override
    public void onPreviewFrame(final Camera camera, final int cameraId, final Preview preview,
                               final byte[] frame, final int facingDirection) {
        // NOP.
    }

    @Override
    public Session generateSession(final String uri, final Socket client) {
        if (!showCameraOverlay()) {
            return null;
        }

        HostDeviceRecorder.PictureSize previewSize = mCameraOverlay.getPreviewSize();

        VideoQuality videoQuality = new VideoQuality();
        videoQuality.resX = previewSize.getWidth();
        videoQuality.resY = previewSize.getHeight();
        videoQuality.bitrate = mCameraOverlay.getPreviewBitRate();
        videoQuality.framerate = (int) mCameraOverlay.getPreviewMaxFrameRate();

        VideoStream videoStream = new CameraH264Stream(mCameraOverlay.getCameraId(), mCameraOverlay.getCamera());
        videoStream.setPreferences(PreferenceManager.getDefaultSharedPreferences(mContext));

        SessionBuilder builder = new SessionBuilder();
        builder.setContext(mContext);
        builder.setVideoStream(videoStream);
        builder.setVideoQuality(videoQuality);

        Session session = builder.build();
        session.setOrigin(client.getLocalAddress().getHostAddress());
        if (session.getDestination() == null) {
            session.setDestination(client.getInetAddress().getHostAddress());
        }
        return session;
    }

    private boolean showCameraOverlay() {
        if (!mCameraOverlay.isShow()) {
            if (!mCameraOverlay.setPreviewCallback(this)) {
                return false;
            }

            final CountDownLatch lock = new CountDownLatch(1);
            final Preview[] result = new Preview[1];
            mCameraOverlay.show(new CameraOverlay.Callback() {
                @Override
                public void onSuccess(final Preview preview) {
                    mServerProvider.sendNotification();
                    mCameraOverlay.setPreviewMode(true);
                    result[0] = preview;
                    lock.countDown();
                }

                @Override
                public void onFail() {
                    lock.countDown();
                }
            });
            try {
                lock.await();
            } catch (InterruptedException e) {
                return false;
            }
            return result[0] != null;
        } else {
            mCameraOverlay.setPreviewCallback(this);
            mCameraOverlay.setPreviewMode(true);
            return true;
        }
    }
}
