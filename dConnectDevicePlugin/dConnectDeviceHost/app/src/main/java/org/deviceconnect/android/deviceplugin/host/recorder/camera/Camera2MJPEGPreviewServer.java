/*
 CameraPreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSetting;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGQuality;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;

import java.io.IOException;
import java.net.Socket;

/**
 * カメラのプレビューをMJPEG形式で配信するサーバー.
 *
 * {@link SurfaceTexture} をもとに実装.
 */
class Camera2MJPEGPreviewServer extends Camera2PreviewServer {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "host.dplugin";

    /**
     * Motion JPEG のマイムタイプを定義します.
     */
    private static final String MIME_TYPE = "video/x-mjpeg";

    /**
     * サーバー名を定義します.
     */
    private static final String SERVER_NAME = "Android Host Camera2 MJPEG Server";

    /**
     * MotionJPEG 配信サーバ.
     */
    private MJPEGServer mMJPEGServer;

    Camera2MJPEGPreviewServer(Context context, Camera2Recorder recorder, int port, OnEventListener listener) {
        super(context, recorder);
        setPort(RecorderSetting.getInstance(getContext()).getPort(recorder.getId(), MIME_TYPE, port));
        setOnEventListener(listener);
    }

    // PreviewServer

    @Override
    public String getUri() {
        return mMJPEGServer == null ? null : mMJPEGServer.getUri();
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        if (mMJPEGServer == null) {
            mMJPEGServer = new MJPEGServer();
            mMJPEGServer.setServerName(SERVER_NAME);
            mMJPEGServer.setServerPort(getPort());
            mMJPEGServer.setCallback(mCallback);
            try {
                mMJPEGServer.start();
            } catch (IOException e) {
                callback.onFail();
                return;
            }
        }
        callback.onStart(getUri());
    }

    @Override
    public void stopWebServer() {
        if (mMJPEGServer != null) {
            mMJPEGServer.stop();
            mMJPEGServer = null;
        }
    }

    @Override
    public void onConfigChange() {
        restartCamera();
    }

    // Camera2PreviewServer

    @Override
    void restartCamera() {
        if (mMJPEGServer != null) {
            new Thread(() -> {
                if (mMJPEGServer != null) {
                    mMJPEGServer.restartEncoder();
                }
            }).start();
        }
    }

    /**
     * JPEG のクオリティを取得します.
     *
     * @return JPEG のクオリティ
     */
    private int getQuality() {
        return RecorderSetting.getInstance(getContext()).getJpegQuality(getRecorder().getId(), 40);
    }

    private final MJPEGServer.Callback mCallback = new MJPEGServer.Callback() {
        @Override
        public boolean onAccept(Socket socket) {
            if (DEBUG) {
                Log.d(TAG, "MJPEGServer.Callback#onAccept: ");
                Log.d(TAG, "  socket: " + socket);
            }
            return true;
        }

        @Override
        public void onClosed(Socket socket) {
            if (DEBUG) {
                Log.d(TAG, "MJPEGServer.Callback#onClosed: ");
                Log.d(TAG, "  socket: " + socket);
            }
        }

        @Override
        public MJPEGEncoder createMJPEGEncoder() {
            if (DEBUG) {
                Log.d(TAG, "MJPEGServer.Callback#createMJPEGEncoder: ");
            }

            postOnCameraStarted();

            Camera2Recorder recorder = (Camera2Recorder) getRecorder();

            HostMediaRecorder.PictureSize size = recorder.getPreviewSize();

            CameraMJPEGEncoder encoder = new CameraMJPEGEncoder(recorder);
            MJPEGQuality quality = encoder.getMJPEGQuality();
            quality.setWidth(size.getWidth());
            quality.setHeight(size.getHeight());
            quality.setQuality(getQuality());
            quality.setFrameRate((int) recorder.getMaxFrameRate());
            return encoder;
        }

        @Override
        public void releaseMJPEGEncoder(MJPEGEncoder encoder) {
            if (DEBUG) {
                Log.d(TAG, "MJPEGServer.Callback#releaseMJPEGEncoder: ");
            }
            postOnCameraStopped();
        }
    };
}
