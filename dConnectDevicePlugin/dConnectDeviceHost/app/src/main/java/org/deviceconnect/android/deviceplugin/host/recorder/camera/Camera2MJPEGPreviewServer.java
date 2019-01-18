/*
 CameraPreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperException;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapper;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;



class Camera2MJPEGPreviewServer implements PreviewServer {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "host.dplugin";

    private static final String MIME_TYPE = "video/x-mjpeg";

    private final Camera2Recorder mRecorder;

    private final Object mLockObj = new Object();

    private MixedReplaceMediaServer mServer;

    private ImageReader mPreviewReader;

    private HandlerThread mPreviewThread;

    private Handler mPreviewHandler;

    private final MixedReplaceMediaServer.Callback mMediaServerCallback = new MixedReplaceMediaServer.Callback() {
        @Override
        public boolean onAccept() {
            try {
                if (DEBUG) {
                    Log.d(TAG, "MediaServerCallback.onAccept: recorder=" + mRecorder.getName());
                }
                CameraWrapper camera = mRecorder.getCameraWrapper();
                mPreviewReader = camera.createPreviewReader(ImageFormat.JPEG);
                mPreviewReader.setOnImageAvailableListener(mPreviewListener, mPreviewHandler);
                mRecorder.startPreview(mPreviewReader.getSurface());
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to start preview.", e);
                return false;
            }
        }
    };

    private ImageReader.OnImageAvailableListener mPreviewListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(final ImageReader reader) {
            try {
                // ビットマップ取得
                final Image image = reader.acquireNextImage();
                if (image == null || image.getPlanes() == null) {
                    return;
                }

                byte[] jpeg = Camera2Recorder.convertToJPEG(image);
                jpeg = mRecorder.rotateJPEG(jpeg, 100); // NOTE: swap width and height.
                image.close();

                offerMedia(jpeg);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send preview frame.", e);
            }
        }
    };

    Camera2MJPEGPreviewServer(final Camera2Recorder recorder) {
        mRecorder = recorder;
    }

    private void offerMedia(final byte[] jpeg) {
        MixedReplaceMediaServer server = mServer;
        if (server != null) {
            server.offerMedia(jpeg);
        }
    }

    @Override
    public int getQuality() {
        return mRecorder.getCameraWrapper().getPreviewJpegQuality();
    }

    @Override
    public void setQuality(int quality) {
        mRecorder.getCameraWrapper().setPreviewJpegQuality(quality);
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        synchronized (mLockObj) {
            mPreviewThread = new HandlerThread("MotionJPEG");
            mPreviewThread.start();
            mPreviewHandler = new Handler(mPreviewThread.getLooper());

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

            CameraWrapper camera = mRecorder.getCameraWrapper();
            if (camera != null) {
                try {
                    camera.stopPreview();
                } catch (CameraWrapperException e) {
                    Log.e(TAG, "Failed to stop preview.", e);
                }
            }
            if (mPreviewReader != null) {
                mPreviewReader.close();
                mPreviewReader = null;
            }
            mPreviewThread.quit();
            mPreviewThread = null;
            mPreviewHandler = null;

            mRecorder.hideNotification();
        }
    }
}
