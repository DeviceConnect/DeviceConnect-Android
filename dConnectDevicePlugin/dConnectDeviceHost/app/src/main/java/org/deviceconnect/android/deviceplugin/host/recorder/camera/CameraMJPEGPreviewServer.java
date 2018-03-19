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

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;

import static org.deviceconnect.android.deviceplugin.host.recorder.camera.CameraOverlay.FACING_DIRECTION_BACK;
import static org.deviceconnect.android.deviceplugin.host.recorder.camera.CameraOverlay.FACING_DIRECTION_FRONT;


class CameraMJPEGPreviewServer implements CameraPreviewServer {

    private static final String MIME_TYPE = "video/x-mjpeg";

    private final AbstractPreviewServerProvider mServerProvider;

    private final Object mLockObj = new Object();

    private final Context mContext;

    private final CameraOverlay mCameraOverlay;

    private MixedReplaceMediaServer mServer;

    private final MixedReplaceMediaServer.Callback mMediaServerCallback = new MixedReplaceMediaServer.Callback() {
        @Override
        public boolean onAccept() {
            if (!mCameraOverlay.isShow()) {
                if (!mCameraOverlay.setPreviewCallback(CameraMJPEGPreviewServer.this)) {
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
                mCameraOverlay.setPreviewCallback(CameraMJPEGPreviewServer.this);
                mCameraOverlay.setPreviewMode(true);
                return true;
            }
        }
    };

    CameraMJPEGPreviewServer(final Context context,
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
            mCameraOverlay.removePreviewCallback(this);
            mCameraOverlay.hide();
            mServerProvider.hideNotification();
        }
    }

    @Override
    public void onPreviewFrame(final Camera camera, final int cameraId, final Preview preview,
                               final byte[] data, final int facingDirection) {
        MixedReplaceMediaServer server = mServer;
        if (server != null) {
            int jpegQuality = 100;
            int format = preview.getPreviewFormat();
            int width = preview.getPreviewWidth();
            int height = preview.getPreviewHeight();

            YuvImage yuvimage = new YuvImage(data, format, width, height, null);
            Rect rect = new Rect(0, 0, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (yuvimage.compressToJpeg(rect, jpegQuality, baos)) {
                byte[] jdata = baos.toByteArray();

                int degree = Preview.getCameraDisplayOrientation(mContext, cameraId);
                if (degree == 0 && facingDirection == FACING_DIRECTION_BACK) {
                    server.offerMedia(jdata);
                } else {
                    try {
                        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
                        bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                        Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFactoryOptions);
                        if (bmp != null) {
                            Matrix m = new Matrix();
                            if (facingDirection == FACING_DIRECTION_FRONT) {
                                m.preRotate(degree);
                                m.preScale(facingDirection, 1);
                            } else {
                                m.postRotate(degree);
                            }
                            Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
                            if (rotatedBmp != null) {
                                baos.reset();
                                if (rotatedBmp.compress(Bitmap.CompressFormat.JPEG, jpegQuality, baos)) {
                                    server.offerMedia(baos.toByteArray());
                                }
                                if (rotatedBmp != null && !rotatedBmp.isRecycled()) {
                                    rotatedBmp.recycle();
                                    rotatedBmp = null;
                                }
                            }
                            if (bmp != null && !bmp.isRecycled()) {
                                bmp.recycle();
                                bmp = null;
                            }
                        }
                    } catch (OutOfMemoryError e) {
                        stopWebServer();
                    }
                }
            }
        }
    }
}
