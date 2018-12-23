/*
 CameraPreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.support.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.deviceconnect.android.deviceplugin.host.recorder.camera.CameraOverlay.FACING_DIRECTION_BACK;
import static org.deviceconnect.android.deviceplugin.host.recorder.camera.CameraOverlay.FACING_DIRECTION_FRONT;


class Camera2MJPEGPreviewServer implements CameraPreviewServer {

    private static final String MIME_TYPE = "video/x-mjpeg";

    private HostDeviceCamera2Recorder mCameraRecorder;

    private final AbstractPreviewServerProvider mServerProvider;

    private final Object mLockObj = new Object();

    private final Context mContext;

    private CameraDevice mCamera;

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

    Camera2MJPEGPreviewServer(final Context context,
                              final HostDeviceCamera2Recorder recorder) {
        mContext = context;
        mCameraRecorder = recorder;
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
        }
    }
}
