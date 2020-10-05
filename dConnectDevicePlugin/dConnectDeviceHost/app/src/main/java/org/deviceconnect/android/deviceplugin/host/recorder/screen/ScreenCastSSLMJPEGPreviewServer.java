package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSetting;
import org.deviceconnect.android.deviceplugin.host.recorder.util.SSLUtils;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGQuality;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGServer;
import org.deviceconnect.android.ssl.EndPointKeyStoreManager;
import org.deviceconnect.android.ssl.KeyStoreCallback;
import org.deviceconnect.android.ssl.KeyStoreError;
import org.deviceconnect.android.ssl.KeyStoreManager;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;

import javax.net.ssl.SSLContext;

import static org.deviceconnect.android.deviceplugin.host.recorder.util.SSLUtils.DEFAULT_P12;
import static org.deviceconnect.android.deviceplugin.host.recorder.util.SSLUtils.DEFAULT_SSL_PASSWORD;

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
    ScreenCastSSLMJPEGPreviewServer(Context context, ScreenCastRecorder recorder, int port) {
        super(context, recorder, port);
        KeyStoreManager keyStoreMgr = new EndPointKeyStoreManager(context, DEFAULT_P12, DEFAULT_SSL_PASSWORD);
        keyStoreMgr.requestKeyStore(SSLUtils.getIPAddress(context), new KeyStoreCallback() {
            @Override
            public void onSuccess(KeyStore keyStore, Certificate certificate, Certificate certificate1) {
                try {
                    mSSLContext = SSLUtils.createSSLServerSocketFactory(keyStore, DEFAULT_SSL_PASSWORD);
                } catch (GeneralSecurityException e) {
                    if (AbstractPreviewServer.DEBUG) {
                        Log.e(AbstractPreviewServer.TAG, "Make SSLContext Error.", e);
                    }
                }
            }

            @Override
            public void onError(KeyStoreError keyStoreError) {
                if (AbstractPreviewServer.DEBUG) {
                    Log.e(AbstractPreviewServer.TAG, "Make SSLContext Error:" + keyStoreError.toString());
                }
            }
        });
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
