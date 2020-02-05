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
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;

import java.io.IOException;

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

    ScreenCastSRTPreviewServer(final Context context, final ScreenCastRecorder recorder) {
        super(context, recorder);
        mScreenCastMgr = recorder.getScreenCastMgr();
        setPort(23456);
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

    private final SRTServer.Callback mCallback = new SRTServer.Callback() {
        @Override
        public void createSession(final SRTSession session) {
            ScreenCastRecorder recorder = (ScreenCastRecorder) getRecorder();

            HostDeviceRecorder.PictureSize size = recorder.getPreviewSize();

            ScreenCastVideoEncoder videoEncoder = new ScreenCastVideoEncoder(mScreenCastMgr);
            VideoQuality videoQuality = videoEncoder.getVideoQuality();
            videoQuality.setVideoWidth(size.getWidth());
            videoQuality.setVideoHeight(size.getHeight());
            videoQuality.setBitRate(recorder.getPreviewBitRate());
            videoQuality.setFrameRate((int) recorder.getMaxFrameRate());
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
