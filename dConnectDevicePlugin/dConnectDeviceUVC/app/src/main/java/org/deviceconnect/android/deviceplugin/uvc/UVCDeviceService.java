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
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.uvc.activity.ErrorDialogActivity;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCSystemProfile;
import org.deviceconnect.android.deviceplugin.uvc.recorder.SSLUtils;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ssl.KeyStoreCallback;
import org.deviceconnect.android.ssl.KeyStoreError;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
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
        requestKeyStore(SSLUtils.getIPAddress(getApplicationContext()), new KeyStoreCallback() {
            @Override
            public void onSuccess(KeyStore keyStore, Certificate certificate, Certificate certificate1) {
                try {
                    mSSLContext = getPluginContext().createSSLContext(keyStore, "0000");
                } catch (GeneralSecurityException e) {
                }
            }

            @Override
            public void onError(KeyStoreError keyStoreError) {
            }
        });
        registerReceiver(mPermissionReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));

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
        UVCService service = new UVCService(mSSLContext, mDeviceMgr, device);
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
}
