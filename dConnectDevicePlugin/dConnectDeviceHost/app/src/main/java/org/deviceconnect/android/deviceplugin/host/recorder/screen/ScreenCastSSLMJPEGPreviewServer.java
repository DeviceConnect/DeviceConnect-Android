package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;

import java.io.IOException;

import javax.net.ssl.SSLContext;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenCastSSLMJPEGPreviewServer extends ScreenCastMJPEGPreviewServer {
    /**
     * MJPEG を配信するサーバ.
     */
    private MJPEGServer mMJPEGServer;
    /**
     * SSL Context.
     */
    private SSLContext mSSLContext;
    ScreenCastSSLMJPEGPreviewServer(Context context, SSLContext sslContext, ScreenCastRecorder recorder, int port) {
        super(context, recorder, port);
        mSSLContext = sslContext;
    }

    @Override
    public String getUri() {
        return mMJPEGServer == null ? null : mMJPEGServer.getUri();
    }


    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        if (mMJPEGServer == null) {
            mMJPEGServer = new MJPEGServer();
            if (mSSLContext != null) {
                mMJPEGServer.setSSLContext(mSSLContext);
            }
            mMJPEGServer.setServerName("HostDevicePlugin Server");
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
        setEncoderQuality();

        if (mMJPEGServer != null) {
            new Thread(() -> {
                if (mMJPEGServer != null) {
                    mMJPEGServer.restartEncoder();
                }
            }).start();
        }
    }


    /**
     * エンコーダーの設定を行います.
     */
    private void setEncoderQuality() {
        if (mMJPEGServer != null) {
            MJPEGEncoder encoder = mMJPEGServer.getMJPEGEncoder();
            if (encoder != null) {
                setMJPEGQuality(encoder.getMJPEGQuality());
            }
        }
    }
}
