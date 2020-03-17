/*
 UVCDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;


import org.deviceconnect.android.deviceplugin.uvc.activity.ErrorDialogActivity;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCSystemProfile;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.logging.Logger;

/**
 * UVC Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCDeviceService extends DConnectMessageService {

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private UVCDeviceManager mDeviceMgr;

    @Override
    public void onCreate() {
        super.onCreate();

        mDeviceMgr = ((UVCDeviceApplication) getApplication()).getDeviceManager();
        mDeviceMgr.addDeviceListener(mDeviceListener);
        mDeviceMgr.addConnectionListener(mConnectionListener);
        mDeviceMgr.start();
    }

    @Override
    public void onDestroy() {
        for (DConnectService service : getServiceProvider().getServiceList()) {
            if (service instanceof  UVCService) {
                ((UVCService) service).closeUVCDevice();
            }
        }
        mDeviceMgr.removeDeviceListener(mDeviceListener);
        mDeviceMgr.removeConnectionListener(mConnectionListener);
        mDeviceMgr.stop();
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
        resetPluginResource();
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
            UVCService service = (UVCService) getServiceProvider().getService(device.getId());
            if (service != null) {
                service.closeUVCDevice();
                service.setOnline(false);
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
}
