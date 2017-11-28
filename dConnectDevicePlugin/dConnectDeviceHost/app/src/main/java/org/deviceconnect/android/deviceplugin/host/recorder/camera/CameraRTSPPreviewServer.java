/*
 CameraRTSPPreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.hardware.Camera;

import net.majorkernelpanic.streaming.rtsp.RtspServer;


public class CameraRTSPPreviewServer implements CameraPreviewServer {

    private static final String MIME_TYPE = "video/x-rtp";

    private final Object mLockObj = new Object();

    private RtspServer mRtspServer;

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        synchronized (mLockObj) {

        }
    }

    @Override
    public void stopWebServer() {

    }

    @Override
    public void onPreviewFrame(final Camera camera, final int cameraId, final Preview preview, final byte[] frame, final int facingDirection) {

    }
}
