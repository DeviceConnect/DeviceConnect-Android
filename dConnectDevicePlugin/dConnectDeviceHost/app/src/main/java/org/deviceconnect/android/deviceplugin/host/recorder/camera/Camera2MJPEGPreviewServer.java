/*
 CameraPreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;


class Camera2MJPEGPreviewServer implements CameraPreviewServer {

    private static final String MIME_TYPE = "video/x-mjpeg";

    private final AbstractPreviewServerProvider mServerProvider;

    private final Object mLockObj = new Object();

    private MixedReplaceMediaServer mServer;

    private PreviewServerListener mPreviewServerListener;

    private final MixedReplaceMediaServer.Callback mMediaServerCallback = () -> {
        if (mPreviewServerListener != null) {
            return mPreviewServerListener.onAccept();
        }
        return false;
    };

    interface PreviewServerListener {
        boolean onAccept();
        void onStart();
        void onStop();
    }

    Camera2MJPEGPreviewServer(final Camera2PhotoRecorder recorder) {
        mServerProvider = recorder;
    }

    public void setPreviewServerListener(final PreviewServerListener listener) {
        mPreviewServerListener = listener;
    }

    public void offerMedia(final byte[] jpeg) {
        MixedReplaceMediaServer server = mServer;
        if (server != null) {
            server.offerMedia(jpeg);
        }
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        synchronized (mLockObj) {
            final String uri;
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("HostDevicePlugin Server");
                mServer.setContentType("image/jpg");
                mServer.setCallback(mMediaServerCallback);
                uri = mServer.start();

                if (mPreviewServerListener != null) {
                    mPreviewServerListener.onStart();
                }
            } else {
                uri = mServer.getUrl();
            }
            callback.onStart(uri);
        }
    }

    @Override
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
            mServerProvider.hideNotification();

            if (mPreviewServerListener != null) {
                mPreviewServerListener.onStop();
            }
        }
    }
}
