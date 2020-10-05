/*
 CameraPreviewServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServer;
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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Locale;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import static org.deviceconnect.android.deviceplugin.host.recorder.util.SSLUtils.DEFAULT_P12;
import static org.deviceconnect.android.deviceplugin.host.recorder.util.SSLUtils.DEFAULT_SSL_PASSWORD;

/**
 * カメラのプレビューをMJPEG形式で配信するサーバー.
 *
 * {@link SurfaceTexture} をもとに実装.
 */
class Camera2SSLMJPEGPreviewServer extends Camera2MJPEGPreviewServer {

    /**
     * サーバー名を定義します.
     */
    private static final String SERVER_NAME = "Android Host Camera2 SSL MJPEG Server";

    /**
     * MotionJPEG 配信サーバ.
     */
    private MJPEGServer mMJPEGServer;

    /**
     * SSL Context.
     */
    private SSLContext mSSLContext;

    Camera2SSLMJPEGPreviewServer(Context context, Camera2Recorder recorder, int port, OnEventListener listener) {
        super(context, recorder, port, listener);
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

    // PreviewServer

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

}
