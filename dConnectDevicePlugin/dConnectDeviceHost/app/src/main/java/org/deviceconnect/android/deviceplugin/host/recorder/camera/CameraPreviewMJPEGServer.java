/*
 CameraPreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;


class CameraPreviewMJPEGServer implements PreviewServer {

    private static final String MIME_TYPE = "video/x-mjpeg";

    private final CameraOverlay mCameraOverlay;

    private final AbstractPreviewServerProvider mServerProvider;

    private final Object mLockObj = new Object();

    private MixedReplaceMediaServer mServer;

    CameraPreviewMJPEGServer(final CameraOverlay cameraOverlay,
                             final AbstractPreviewServerProvider serverProvider) {
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
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("HostDevicePlugin Server");
                mServer.setContentType("image/jpg");
                final String ip = mServer.start();

                if (!mCameraOverlay.isShow()) {
                    mCameraOverlay.show(new CameraOverlay.Callback() {
                        @Override
                        public void onSuccess() {
                            mServerProvider.sendNotification();
                            mCameraOverlay.setPreviewMode(true);
                            mCameraOverlay.setServer(mServer);
                            callback.onStart(ip);
                        }

                        @Override
                        public void onFail() {
                            callback.onFail();
                        }
                    });
                } else {
                    mCameraOverlay.setPreviewMode(true);
                    mCameraOverlay.setServer(mServer);
                    callback.onStart(ip);
                }
            } else {
                callback.onStart(mServer.getUrl());
            }
        }
    }

    @Override
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
            mCameraOverlay.hide();
            mServerProvider.hideNotification();
        }
    }
}
