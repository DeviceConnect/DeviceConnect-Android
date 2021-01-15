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

import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGQuality;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLContext;

/**
 * カメラのプレビューをMJPEG形式で配信するサーバー.
 *
 * {@link SurfaceTexture} をもとに実装.
 */
class Camera2MJPEGPreviewServer extends Camera2PreviewServer {
    /**
     * Motion JPEG のマイムタイプを定義します.
     */
    protected static final String MIME_TYPE = "video/x-mjpeg";

    /**
     * サーバー名を定義します.
     */
    private static final String SERVER_NAME = "Android Host Camera2 MJPEG Server";

    /**
     * MotionJPEG 配信サーバ.
     */
    private MJPEGServer mMJPEGServer;

    Camera2MJPEGPreviewServer(Context context, Camera2Recorder recorder, int port, boolean useSSL) {
        super(context, recorder, useSSL);
        setPort(port);
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
            SSLContext sslContext = getSSLContext();
            if (useSSLContext() && sslContext == null) {
                callback.onFail();
                return;
            }

            mMJPEGServer = new MJPEGServer();
            mMJPEGServer.setServerName(SERVER_NAME);
            mMJPEGServer.setServerPort(getPort());
            mMJPEGServer.setCallback(mCallback);
            if (useSSLContext()) {
                mMJPEGServer.setSSLContext(sslContext);
            }
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
    public boolean requestSyncFrame() {
        // 何もしない
        return false;
    }

    @Override
    public long getBPS() {
        return mMJPEGServer != null ? mMJPEGServer.getBPS() : 0;
    }

    @Override
    public void onConfigChange() {
        setEncoderQuality();
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
     * エンコーダの設定を行います.
     */
    private void setEncoderQuality() {
        if (mMJPEGServer != null) {
            MJPEGEncoder encoder = mMJPEGServer.getMJPEGEncoder();
            if (encoder != null) {
                setMJPEGQuality(encoder.getMJPEGQuality());
            }
        }
    }

    /**
     * MJPEG の設定を行います.
     *
     * @param quality 設定を行う MJPEGQuality
     */
    private void setMJPEGQuality(MJPEGQuality quality) {
        Camera2Recorder recorder = (Camera2Recorder) getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();

        quality.setWidth(settings.getPreviewSize().getWidth());
        quality.setHeight(settings.getPreviewSize().getHeight());
        quality.setFrameRate(settings.getPreviewMaxFrameRate());
        quality.setQuality(settings.getPreviewQuality());
    }

    /**
     * MJPEGServer からのイベントを受け取るためのコールバック.
     */
    private final MJPEGServer.Callback mCallback = new MJPEGServer.Callback() {
        @Override
        public boolean onAccept(Socket socket) {
            if (DEBUG) {
                Log.d(TAG, "MJPEGServer.Callback#onAccept: ");
                Log.d(TAG, "  socket: " + socket);
            }
            // 特に制限を付けないので、常に true を返却
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

            Camera2Recorder recorder = (Camera2Recorder) getRecorder();
            CameraMJPEGEncoder encoder = new CameraMJPEGEncoder(recorder);
            setMJPEGQuality(encoder.getMJPEGQuality());
            return encoder;
        }

        @Override
        public void releaseMJPEGEncoder(MJPEGEncoder encoder) {
            if (DEBUG) {
                Log.d(TAG, "MJPEGServer.Callback#releaseMJPEGEncoder: ");
            }
        }
    };
}
