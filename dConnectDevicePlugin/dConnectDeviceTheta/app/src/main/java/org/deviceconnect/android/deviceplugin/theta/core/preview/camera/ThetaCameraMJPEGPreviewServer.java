/*
 CameraPreviewServer.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.core.preview.camera;

import android.graphics.SurfaceTexture;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.preview.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.theta.core.preview.PreviewServer;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLContext;

/**
 * カメラのプレビューをMJPEG形式で配信するサーバー.
 *
 * {@link SurfaceTexture} をもとに実装.
 */
class ThetaCameraMJPEGPreviewServer extends ThetaCameraPreviewServer {
    /**
     * Motion JPEG のマイムタイプを定義します.
     */
    protected static final String MIME_TYPE = "video/x-mjpeg";

    /**
     * サーバー名を定義します.
     */
    private static final String SERVER_NAME = "Live Preview Server";

    /**
     * SSLContext を使用するかどうかのフラグ.
     */
    private boolean mUsesSSLContext;

    /**
     * MotionJPEG 配信サーバ.
     */
    private MJPEGServer mMJPEGServer;

    ThetaCameraMJPEGPreviewServer(boolean isSSL, ThetaDevice recorder, int port) {
        super(recorder);
        mUsesSSLContext = isSSL;
        setPort(port);
    }

    // PreviewServer

    @Override
    public boolean usesSSLContext() {
        return mUsesSSLContext;
    }

    @Override
    public String getUri() {
        return mMJPEGServer == null ? null : mMJPEGServer.getUri();
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final PreviewServer.OnWebServerStartCallback callback) {
        if (mMJPEGServer == null) {
            SSLContext sslContext = getSSLContext();
            if (usesSSLContext() && sslContext == null) {
                callback.onFail();
                return;
            }

            mMJPEGServer = new MJPEGServer();
            if (sslContext != null) {
                mMJPEGServer.setSSLContext(sslContext);
            }
            mMJPEGServer.setServerName(SERVER_NAME);
            mMJPEGServer.setServerPath("1");
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
    public boolean requestSyncFrame() {
        // 何もしない
        return false;
    }

    @Override
    public void onConfigChange() {
    }

    // ThetaCameraPreviewServer

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
     * MJPEGServer からのイベントを受け取るためのコールバック.
     */
    protected final MJPEGServer.Callback mCallback = new MJPEGServer.Callback() {
        @Override
        public boolean onAccept(Socket socket) {
            if (AbstractPreviewServer.DEBUG) {
                Log.d(AbstractPreviewServer.TAG, "MJPEGServer.Callback#onAccept: ");
                Log.d(AbstractPreviewServer.TAG, "  socket: " + socket);
            }
            // 特に制限を付けないので、常に true を返却
            return true;
        }

        @Override
        public void onClosed(Socket socket) {
            if (AbstractPreviewServer.DEBUG) {
                Log.d(AbstractPreviewServer.TAG, "MJPEGServer.Callback#onClosed: ");
                Log.d(AbstractPreviewServer.TAG, "  socket: " + socket);
            }
        }

        @Override
        public MJPEGEncoder createMJPEGEncoder() {
            if (AbstractPreviewServer.DEBUG) {
                Log.d(AbstractPreviewServer.TAG, "MJPEGServer.Callback#createMJPEGEncoder: ");
            }

            ThetaDevice recorder = (ThetaDevice) getRecorder();

            MJPEGEncoder encoder = new ThetaMJPEGEncoder(recorder);
            return encoder;
        }

        @Override
        public void releaseMJPEGEncoder(MJPEGEncoder encoder) {
            if (AbstractPreviewServer.DEBUG) {
                Log.d(AbstractPreviewServer.TAG, "MJPEGServer.Callback#releaseMJPEGEncoder: ");
            }
        }
    };
}
