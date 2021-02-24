/*
 UVCDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCSystemProfile;
import org.deviceconnect.android.deviceplugin.uvc.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcRecorder;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;
import org.deviceconnect.android.deviceplugin.uvc.util.UVCRegistry;
import org.deviceconnect.android.libusb.UsbSerialPortManager;
import org.deviceconnect.android.libuvc.UVCCamera;
import org.deviceconnect.android.libuvc.UVCCameraManager;
import org.deviceconnect.android.libuvc.utils.WeakReferenceList;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
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
     * USB 管理クラス.
     */
    private UsbSerialPortManager mUsbSerialPortManager;

    /**
     * UVC カメラを管理するクラス.
     */
    private UVCCameraManager mUVCCameraManager;

    /**
     * UVC のリスト管理クラス.
     */
    private UVCRegistry mUVCRegistry;

    /**
     * イベントを通知するリスナーを格納するリスト.
     */
    private final WeakReferenceList<OnEventListener> mOnEventListeners = new WeakReferenceList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        setUseLocalOAuth(checkUseLocalOAuth());
        init();
        initUVCCameraManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposeUVCCameraManager();
        mOnEventListeners.clear();
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

    @Override
    public void setUseLocalOAuth(boolean a) {
        super.setUseLocalOAuth(a);
    }

    /**
     * イベントを通知するリスナーを追加します.
     *
     * 追加したリスナーは、{@link #removeOnEventListener(OnEventListener)} で削除してください。
     *
     * @param listener 追加するリスナー
     */
    public void addOnEventListener(OnEventListener listener) {
        mOnEventListeners.add(listener);
    }

    /**
     * イベントを通知するリスナーを削除します.
     *
     * @param listener 削除するリスナー
     */
    public void removeOnEventListener(OnEventListener listener) {
        mOnEventListeners.remove(listener);
    }

    /**
     * UVC の接続イベントを通知します.
     *
     * @param service 接続した UVC サービス
     */
    private void postOnConnected(UVCService service) {
        for (OnEventListener l : mOnEventListeners) {
            l.onConnected(service);
        }
    }

    /**
     * UVC の切断イベントを通知します.
     *
     * @param service 切断した UVC サービス
     */
    private void postOnDisconnected(UVCService service) {
        for (OnEventListener l : mOnEventListeners) {
            l.onDisconnected(service);
        }
    }

    /**
     * 初期化処理を行います.
     */
    private void init() {
        mUVCRegistry = new UVCRegistry(this);

        for (UVCRegistry.UVC uvc : mUVCRegistry.getUVCList()) {
            UVCService service = new UVCService(getApplicationContext(), uvc.getDeviceId());
            service.setName(uvc.getName());
            service.setOnline(false);
            getServiceProvider().addService(service);
        }
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        for (DConnectService service : getServiceProvider().getServiceList()) {
            if (service instanceof UVCService) {
                ((UVCService) service).disconnect();
            }
        }
        disposeUVCCameraManager();
        initUVCCameraManager();
    }

    /**
     * USBを管理するクラスを初期化します.
     */
    private void initUVCCameraManager() {
        mUsbSerialPortManager = new UsbSerialPortManager(this);
        mUVCCameraManager = new UVCCameraManager(mUsbSerialPortManager);
        mUVCCameraManager.setOnEventListener(new UVCCameraManager.OnEventListener() {
            @Override
            public void onConnected(final UVCCamera uvcCamera) {
                connectUVCCamera(uvcCamera);
            }

            @Override
            public void onDisconnected(final UVCCamera uvcCamera) {
                disconnectUVCCamera(uvcCamera);
            }

            @Override
            public void onError(final Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.w("UVC", "error", e);
                }
            }

            @Override
            public void onRequestPermission(UsbSerialPortManager.PermissionCallback callback) {
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
    private void disposeUVCCameraManager() {
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

    private synchronized void connectUVCCamera(UVCCamera uvcCamera) {
        UVCService service = findUVCServiceByUVC(uvcCamera);
        if (service == null) {
            service = new UVCService(getApplicationContext(), createUVCServiceId(uvcCamera));
            service.connect(uvcCamera);
            getServiceProvider().addService(service);
            mUVCRegistry.addUVC(service.getId(), service.getName());
        } else {
            service.connect(uvcCamera);
        }
        setSSLContext(service);
        postOnConnected(service);
    }

    private synchronized void disconnectUVCCamera(UVCCamera uvcCamera) {
        UVCService service = findUVCServiceByUVC(uvcCamera);
        if (service != null) {
            service.disconnect();
        }
        postOnDisconnected(service);
    }

    private String createUVCServiceId(UVCCamera uvcCamera) {
        return "uvc-" + uvcCamera.getDeviceId();
    }

    private UVCService findUVCServiceByUVC(UVCCamera uvcCamera) {
        return findUVCServiceById(createUVCServiceId(uvcCamera));
    }

    public UVCService findUVCServiceById(String id) {
        if (id != null) {
            DConnectService service = getServiceProvider().getService(id);
            if (service instanceof UVCService) {
                return (UVCService) service;
            }
        }
        return null;
    }

    public UVCService getActiveUVCService() {
        for (DConnectService service : getServiceProvider().getServiceList()) {
            if (service instanceof UVCService && service.isOnline()) {
                return (UVCService) service;
            }
        }
        return null;
    }

    // SSL

    private void setSSLContext(UVCService service) {
        final SSLContext sslContext = mSSLContext;
        if (sslContext != null) {
            setSSLContext(service, sslContext);
        } else {
            requestKeyStore(getIPAddress(this), new KeyStoreCallback() {
                @Override
                public void onSuccess(final KeyStore keyStore, final Certificate certificate, final Certificate certificate1) {
                    try {
                        mSSLContext = createSSLContext(keyStore, DEFAULT_PASSWORD);
                        setSSLContext(service, mSSLContext);
                    } catch (Exception e) {
                        // ignore.
                    }
                }
                @Override
                public void onError(final KeyStoreError keyStoreError) {
                }
            });
        }
    }

    private void setSSLContext(UVCService service, SSLContext sslContext) {
        for (UvcRecorder recorder : service.getUvcRecorderList()) {
            for (PreviewServer server : recorder.getServerProvider().getServers()) {
                if (server.useSSLContext()) {
                    server.setSSLContext(sslContext);
                }
            }
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
                    // ignore.
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

    /**
     * ユーザ認可の設定を取得します.
     *
     * @return ユーザ認可の設定
     */
    private boolean checkUseLocalOAuth() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean("uvc_settings_auth", false);
    }

    /**
     * UVC 接続・切断イベントを通知するリスナー.
     */
    public interface OnEventListener {
        /**
         * UVC が接続したことを通知します.
         *
         * @param service 接続した UVC サービス
         */
        void onConnected(UVCService service);

        /**
         * UVC が切断されたことを通知します.
         *
         * @param service 切断された UVC サービス
         */
        void onDisconnected(UVCService service);
    }
}
