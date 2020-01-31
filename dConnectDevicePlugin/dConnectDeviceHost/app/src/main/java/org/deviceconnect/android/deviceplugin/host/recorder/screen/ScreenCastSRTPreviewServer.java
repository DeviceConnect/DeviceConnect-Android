/*
 ScreenCastSRTPreviewServer.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;
import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * スクリーンキャストを SRT で配信するサーバー.
 *
 * @author NTT DOCOMO, INC.
 */
public class ScreenCastSRTPreviewServer extends AbstractPreviewServer {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "CameraSRT";

    // 参照: https://www.iana.org/assignments/media-types/video/MP2T
    public static final String MIME_TYPE = "video/MP2T";

    private ScreenCastManager mScreenCastMgr;

    private SRTServer mSRTServer;

    private Timer mStatsTimer;

    public ScreenCastSRTPreviewServer(final Context context,
                                      final AbstractPreviewServerProvider serverProvider,
                                      final ScreenCastManager screenCastMgr) {
        super(context, serverProvider);
        mScreenCastMgr = screenCastMgr;
        setPort(23456);
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
            } catch (IOException e) {
                if (DEBUG) {
                    Log.d(TAG, "Failed to start SRT server.", e);
                }
                callback.onFail();
            }
        }
        if (mStatsTimer == null) {
            mStatsTimer = new Timer();
            mStatsTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    for (SRTSocket socket : mSRTServer.getSocketList()) {
                        socket.dumpStats();
                    }
                }
            }, 0, 5 * 1000); // TODO build.gralde ログ出力フラグとインターバルを設定
        }
        callback.onStart("srt://localhost:" + mSRTServer.getServerPort());
    }

    @Override
    public void stopWebServer() {
        if (mSRTServer != null) {
            mSRTServer.stop();
            mSRTServer = null;
        }
        if (mStatsTimer != null) {
            mStatsTimer.cancel();
            mStatsTimer = null;
        }
    }

    private final SRTServer.Callback mCallback = new SRTServer.Callback() {
        @Override
        public void createSession(final SRTSession session) {
            HostDeviceRecorder.PictureSize size = getServerProvider().getPreviewSize();

            ScreenCastVideoEncoder videoEncoder = new ScreenCastVideoEncoder(mScreenCastMgr);
            VideoQuality videoQuality = videoEncoder.getVideoQuality();
            videoQuality.setVideoWidth(size.getWidth());
            videoQuality.setVideoHeight(size.getHeight());
            videoQuality.setBitRate(getServerProvider().getPreviewBitRate());
            videoQuality.setFrameRate((int) getServerProvider().getMaxFrameRate());
            videoQuality.setIFrameInterval(2);
            session.setVideoEncoder(videoEncoder);

            registerConfigChangeReceiver();
        }

        @Override
        public void releaseSession(final SRTSession session) {
            unregisterConfigChangeReceiver();
        }
    };
}
