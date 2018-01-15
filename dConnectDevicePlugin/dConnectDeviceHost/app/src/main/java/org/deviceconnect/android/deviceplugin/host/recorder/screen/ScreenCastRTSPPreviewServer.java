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

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;

import java.io.IOException;
import java.net.Socket;


public class ScreenCastRTSPPreviewServer implements PreviewServer, RtspServer.Delegate {

    private static final String MIME_TYPE = "video/x-rtp";

    private static final String SERVER_NAME = "Android Host Screen RTSP Server";

    private final Context mContext;

    private final AbstractPreviewServerProvider mServerProvider;

    private final ScreenCastManager mScreenCastMgr;

    private final Object mLockObj = new Object();

    private ScreenCast mScreenCast;

    private RtspServer mRtspServer;

    ScreenCastRTSPPreviewServer(final Context context,
                                final AbstractPreviewServerProvider serverProvider,
                                final ScreenCastManager screenCastMgr) {
        mContext = context;
        mServerProvider = serverProvider;
        mScreenCastMgr = screenCastMgr;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        mScreenCastMgr.requestPermission(new ScreenCastManager.PermissionCallback() {
            @Override
            public void onAllowed() {
                synchronized (mLockObj) {
                    if (mRtspServer == null) {
                        mRtspServer = new RtspServerImpl(SERVER_NAME);
                        mRtspServer.setPort(20000);
                        mRtspServer.setDelegate(ScreenCastRTSPPreviewServer.this);
                        mRtspServer.start();
                    }
                    String uri = "rtsp://localhost:" + mRtspServer.getPort();
                    callback.onStart(uri);
                }
            }

            @Override
            public void onDisallowed() {
                callback.onFail();
            }
        });
    }

    @Override
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mScreenCast != null) {
                mScreenCast.stopCast();
                mScreenCast = null;
            }
        }
    }

    @Override
    public Session generateSession(final String uri, final Socket client) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            HostDeviceRecorder.PictureSize previewSize = mServerProvider.getPreviewSize();

            VideoQuality videoQuality = new VideoQuality();
            videoQuality.resX = previewSize.getWidth();
            videoQuality.resY = previewSize.getHeight();
            videoQuality.bitrate = mServerProvider.getPreviewBitRate();
            videoQuality.framerate = 30; //(int) mServerProvider.getPreviewMaxFrameRate(); //TODO

            SurfaceVideoStream videoStream = new SurfaceH264Stream(prefs, videoQuality);
            mScreenCast = mScreenCastMgr.createScreenCast(videoStream.getInputSurface(), previewSize);
            mScreenCast.startCast();

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
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
