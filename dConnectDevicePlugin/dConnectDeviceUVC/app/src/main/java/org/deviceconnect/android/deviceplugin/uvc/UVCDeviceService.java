/*
 UVCDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.util.Log;

import androidx.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCSystemProfile;
import org.deviceconnect.android.deviceplugin.uvc.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcRecorder;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;
import org.deviceconnect.android.libusb.UsbSerialPortManager;
import org.deviceconnect.android.libuvc.UVCCamera;
import org.deviceconnect.android.libuvc.UVCCameraManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.android.ssl.KeyStoreCallback;
import org.deviceconnect.android.ssl.KeyStoreError;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

/**
 * UVC Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCDeviceService extends DConnectMessageService {

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    /**
     * デフォルトのパスワードを定義します.
     */
    private static final String DEFAULT_PASSWORD = "0000";

    /**
     * SSL コンテキスト.
     */
    private SSLContext mSSLContext;

    /**
     * USB管理クラス.
     */
    private UsbSerialPortManager mUsbSerialPortManager;

    /**
     * UVCカメラを管理するクラス.
     */
    private UVCCameraManager mUVCCameraManager;

    @Override
    public void onCreate() {
        super.onCreate();
        setUseLocalOAuth(false);
        initManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposeManager();
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerUninstalled");
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerTerminated");
        }
    }
    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onDevicePluginReset");
        }
        resetPluginResource();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new UVCSystemProfile();
    }

    @Override
    protected String getCertificateAlias() {
        return "org.deviceconnect.android.deviceplugin.uvc";
    }

    @Override
    protected boolean usesAutoCertificateRequest() {
        // SSL の証明書を使用するので true を返却
        return true;
    }

    @Override
    protected void onKeyStoreUpdated(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
        try {
            if (keyStore == null || getPluginContext() == null) {
                return;
            }
            mSSLContext = createSSLContext(keyStore, DEFAULT_PASSWORD);
        } catch (GeneralSecurityException e) {
            mLogger.log(Level.SEVERE, "Failed to update keystore", e);
        }
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
    }

    /**
     * USBを管理するクラスを初期化します.
     */
    private void initManager() {
        mUsbSerialPortManager = new UsbSerialPortManager(this);
        mUVCCameraManager = new UVCCameraManager(mUsbSerialPortManager);
        mUVCCameraManager.setOnEventListener(new UVCCameraManager.OnEventListener() {
            @Override
            public void onConnected(final UVCCamera uvcCamera) {
                Log.e("ABC", "######## onConnected: " + uvcCamera);

                UVCService service = new UVCService(getApplicationContext(), uvcCamera);
                getSSLContext(new SSLContextCallback() {
                    @Override
                    public void onGet(SSLContext sslContext) {
                        for (UvcRecorder recorder : service.getUvcRecorderList()) {
                            for (PreviewServer server : recorder.getServerProvider().getServers()) {
                                if (sslContext != null && server.useSSLContext()) {
                                    server.setSSLContext(sslContext);
                                }
                            }
                        }
                    }
                    @Override
                    public void onError() {
                    }
                });
                getServiceProvider().addService(service);

                Log.e("ABC", "######## onConnected: " + getServiceProvider().getServiceList().size());
            }

            @Override
            public void onDisconnected(final UVCCamera uvcCamera) {
                Log.e("ABC", "######## onDisconnected: " + uvcCamera);
                getServiceProvider().removeService("UVC-" + uvcCamera.getDeviceId());
                Log.e("ABC", "######## onDisconnected: " + getServiceProvider().getServiceList().size());
            }

            @Override
            public void onError(final Exception e) {
                Log.e("ABC", "######## onError: ", e);
            }

            @Override
            public void onRequestPermission(UsbSerialPortManager.PermissionCallback callback) {
                Log.e("ABC", "######## onPermission: ");

                PermissionUtility.requestPermissions(getApplicationContext(),
                        new Handler(Looper.getMainLooper()),
                        new String[]{Manifest.permission.CAMERA},
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                callback.allow();
                            }

                            @Override
                            public void onFail(@NonNull String s) {
                                callback.deny();
                            }
                        });
            }
        });
        mUVCCameraManager.startMonitoring();
    }

    /**
     * USBを管理するクラスの後始末を行います.
     */
    private void disposeManager() {
        if (mUVCCameraManager != null) {
            mUVCCameraManager.stopMonitoring();
            mUVCCameraManager.dispose();
            mUVCCameraManager = null;
        }
        if (mUsbSerialPortManager != null) {
            mUsbSerialPortManager.stopUsbMonitoring();
            mUsbSerialPortManager.dispose();
            mUsbSerialPortManager = null;
        }
    }

    // SSL

    /**
     * SSLContext を提供するインターフェース.
     */
    public interface SSLContextCallback {
        void onGet(SSLContext context);
        void onError();
    }

    public void getSSLContext(final SSLContextCallback callback) {
        final SSLContext sslContext = mSSLContext;
        if (sslContext != null) {
            callback.onGet(sslContext);
        } else {
            requestKeyStore(getIPAddress(this), new KeyStoreCallback() {
                @Override
                public void onSuccess(final KeyStore keyStore, final Certificate certificate, final Certificate certificate1) {
                    try {
                        mSSLContext = createSSLContext(keyStore, DEFAULT_PASSWORD);
                        callback.onGet(mSSLContext);
                    } catch (GeneralSecurityException e) {
                        callback.onError();
                    }
                }
                @Override
                public void onError(final KeyStoreError keyStoreError) {
                    callback.onError();
                }
            });
        }
    }

    /**
     * Gets the ip address.
     *
     * @param context Context of application
     * @return Returns ip address
     */
    public static String getIPAddress(final Context context) {
        Context appContext = context.getApplicationContext();
        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager cManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cManager.getActiveNetworkInfo();
        String en0Ip = null;
        if (network != null) {
            if (network.getType() == ConnectivityManager.TYPE_ETHERNET) {
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (inetAddress instanceof Inet4Address
                                    && !inetAddress.getHostAddress().equals("127.0.0.1")) {
                                en0Ip = inetAddress.getHostAddress();
                                break;
                            }
                        }
                    }
                } catch (SocketException e) {
                    Log.e("Host", "Get Ethernet IP Error", e);
                }
            }
        }

        if (en0Ip != null) {
            return en0Ip;
        } else {
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        }
    }
}
