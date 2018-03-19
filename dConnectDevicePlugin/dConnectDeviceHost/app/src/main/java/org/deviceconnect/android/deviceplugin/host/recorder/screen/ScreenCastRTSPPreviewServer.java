package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.rtsp.RtspServerImpl;
import net.majorkernelpanic.streaming.video.SurfaceH264Stream;
import net.majorkernelpanic.streaming.video.SurfaceVideoStream;
import net.majorkernelpanic.streaming.video.VideoQuality;
import net.majorkernelpanic.streaming.video.VideoStream;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;


class ScreenCastRTSPPreviewServer extends ScreenCastPreviewServer implements RtspServer.Delegate {

    static final String MIME_TYPE = "video/x-rtp";

    private static final String SERVER_NAME = "Android Host Screen RTSP Server";

    private final ScreenCastManager mScreenCastMgr;

    private final Object mLockObj = new Object();

    private Socket mClientSocket;

    private SurfaceH264Stream mVideoStream;

    private ScreenCast mScreenCast;

    private RtspServer mRtspServer;

    private boolean mIsStartedCast;

    ScreenCastRTSPPreviewServer(final Context context,
                                final AbstractPreviewServerProvider serverProvider,
                                final ScreenCastManager screenCastMgr) {
        super(context, serverProvider);
        mScreenCastMgr = screenCastMgr;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        synchronized (mLockObj) {
            registerConfigChangeReceiver();

            if (mRtspServer == null) {
                mRtspServer = new RtspServerImpl(SERVER_NAME);
                mRtspServer.setPort(20000);
                mRtspServer.setDelegate(ScreenCastRTSPPreviewServer.this);
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
            }
            stopScreenCast();
            mClientSocket = null;
            unregisterConfigChangeReceiver();
        }
    }

    @Override
    protected void onConfigChange() {
        synchronized (mLockObj) {
            if (mIsStartedCast) {
                mScreenCast.stopCast();

                HostDeviceRecorder.PictureSize previewSize = getRotatedPreviewSize();
                mScreenCast = mScreenCastMgr.createScreenCast(mVideoStream.getInputSurface(), previewSize);
                mScreenCast.startCast();
            }
        }
    }

    @Override
    public Session generateSession(final String uri, final Socket clientSocket) {
        try {
            return startScreenCast(clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Session startScreenCast(final Socket clientSocket) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        HostDeviceRecorder.PictureSize previewSize = getRotatedPreviewSize();

        VideoQuality videoQuality = new VideoQuality();
        videoQuality.resX = previewSize.getWidth();
        videoQuality.resY = previewSize.getHeight();
        videoQuality.bitrate = mServerProvider.getPreviewBitRate();
        videoQuality.framerate = (int) mServerProvider.getMaxFrameRate();

        synchronized (mLockObj) {
            mClientSocket = clientSocket;
            mVideoStream = new SurfaceH264Stream(prefs, videoQuality);
            mScreenCast = mScreenCastMgr.createScreenCast(mVideoStream.getInputSurface(), previewSize);
            mScreenCast.startCast();
            mIsStartedCast = true;
        }

        SessionBuilder builder = new SessionBuilder();
        builder.setContext(mContext);
        builder.setVideoStream(mVideoStream);
        builder.setVideoQuality(videoQuality);

        Session session = builder.build();
        session.setOrigin(clientSocket.getLocalAddress().getHostAddress());
        if (session.getDestination() == null) {
            session.setDestination(clientSocket.getInetAddress().getHostAddress());
        }
        return session;
    }

    private void stopScreenCast() {
        synchronized (mLockObj) {
            if (mIsStartedCast) {
                mVideoStream.stop();
                mVideoStream = null;
                mScreenCast.stopCast();
                mScreenCast = null;
                mIsStartedCast = false;
            }
        }
    }
}
