/*
 UVCDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.uvc.activity.ErrorDialogActivity;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCSystemProfile;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;
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

    private UVCDeviceManager mDeviceMgr;

    private SSLContext mSSLContext;
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
            mLogger.log(Level.INFO, "getSSLContext: requestKeyStore: onSuccess: Already created SSL Context: " + sslContext);
            callback.onGet(sslContext);
        } else {
            requestKeyStore(getIPAddress(this), new KeyStoreCallback() {
                public void onSuccess(final KeyStore keyStore, final Certificate certificate, final Certificate certificate1) {
                    try {
                        mLogger.log(Level.INFO, "getSSLContext: requestKeyStore: onSuccess: Creating SSL Context...");
                        mSSLContext = createSSLContext(keyStore, "0000");
                        mLogger.log(Level.INFO, "getSSLContext: requestKeyStore: onSuccess: Created SSL Context: " + mSSLContext);
                        callback.onGet(mSSLContext);
                    } catch (GeneralSecurityException e) {
                        mLogger.log(Level.WARNING, "getSSLContext: requestKeyStore: onSuccess: Failed to create SSL Context", e);
                        callback.onError();
                    }
                }

                public void onError(final KeyStoreError keyStoreError) {
                    mLogger.warning("getSSLContext: requestKeyStore: onError: error = " + keyStoreError);
                    callback.onError();
                }
            });
        }
    }
    private BroadcastReceiver mPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PermissionUtility.requestPermissions(context,
                    new Handler(Looper.getMainLooper()),
                    new String[]{Manifest.permission.CAMERA},
                    new PermissionUtility.PermissionRequestCallback() {

                        @Override
                        public void onSuccess() {
                            mDeviceMgr = ((UVCDeviceApplication) getApplication()).getDeviceManager();
                            mDeviceMgr.addDeviceListener(mDeviceListener);
                            mDeviceMgr.addConnectionListener(mConnectionListener);
                            mDeviceMgr.start();
                        }

                        @Override
                        public void onFail(@NonNull String s) {

                        }
                    });
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mPermissionReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
    }
    @Override
    protected boolean usesAutoCertificateRequest() {
        return true;
    }

    @Override
    protected void onKeyStoreUpdated(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
        try {
            if (keyStore == null || getPluginContext() == null) {
                return;
            }
            mSSLContext = createSSLContext(keyStore, "0000");
        } catch (GeneralSecurityException e) {
            mLogger.log(Level.SEVERE, "Failed to update keystore", e);
        }
    }
    @Override
    public void onDestroy() {
        for (DConnectService service : getServiceProvider().getServiceList()) {
            if (service instanceof  UVCService) {
                ((UVCService) service).closeUVCDevice();
            }
        }
        if (mDeviceMgr != null) {
            mDeviceMgr.removeDeviceListener(mDeviceListener);
            mDeviceMgr.removeConnectionListener(mConnectionListener);
            mDeviceMgr.stop();
        }
        unregisterReceiver(mPermissionReceiver);
        super.onDestroy();
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
        PermissionUtility.requestPermissions(this,
                new Handler(Looper.getMainLooper()),
                new String[]{Manifest.permission.CAMERA},
                new PermissionUtility.PermissionRequestCallback() {

                    @Override
                    public void onSuccess() {
                        resetPluginResource();
                    }

                    @Override
                    public void onFail(@NonNull String s) {

                    }
                });
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new UVCSystemProfile();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        for (DConnectService service : getServiceProvider().getServiceList()) {
            if (service instanceof  UVCService) {
                ((UVCService) service).reset();
            }
        }
    }

    private final UVCDeviceManager.DeviceListener mDeviceListener = new UVCDeviceManager.DeviceListener() {
        @Override
        public void onFound(final UVCDevice device) {
            if (mDeviceMgr.connectDevice(device)) {
                if (!device.canPreview()) {
                    ErrorDialogActivity.showNotSupportedError(getApplicationContext(), device);
                }
            } else {
                mLogger.severe("UVC device COULD NOT be initialized: " + device.getName());
            }
        }
    };

    private final UVCDeviceManager.ConnectionListener mConnectionListener = new UVCDeviceManager.ConnectionListener() {
        @Override
        public void onConnect(final UVCDevice device) {
            UVCService service = getService(device);
            if (service != null) {
                service.openUVCDevice(device);
                service.setOnline(true);
            }
        }

        @Override
        public void onConnectionFailed(final UVCDevice device) {
            // NOP.
        }

        @Override
        public void onDisconnect(final UVCDevice device) {
            if (device != null) {
                UVCService service = (UVCService) getServiceProvider().getService(device.getId());
                if (service != null) {
                    service.closeUVCDevice();
                    service.setOnline(false);
                }
            }
        }
    };

    private UVCService addService(final UVCDevice device) {
        UVCService service = new UVCService(mDeviceMgr, device);
        getServiceProvider().addService(service);
        return service;
    }

    private UVCService getService(final UVCDevice device) {
        UVCService service = (UVCService) getServiceProvider().getService(device.getId());
        if (service == null) {
            service = addService(device);
        }
        return service;
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
            switch (network.getType()) {
                case ConnectivityManager.TYPE_ETHERNET:
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
