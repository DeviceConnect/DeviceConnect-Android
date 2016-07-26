/*
 UVCDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;


import org.deviceconnect.android.deviceplugin.uvc.activity.ErrorDialogActivity;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.logging.Logger;

/**
 * UVC Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCDeviceService extends DConnectMessageService
    implements UVCDeviceManager.DeviceListener, UVCDeviceManager.ConnectionListener {

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private UVCDeviceManager mDeviceMgr;

    private DConnectProfile mMediaStreamRecordinrProfile;

    @Override
    public void onCreate() {
        super.onCreate();

        mDeviceMgr = getDeviceManager();
        mDeviceMgr.addDeviceListener(this);
        mDeviceMgr.addConnectionListener(this);
        mDeviceMgr.start();

        mMediaStreamRecordinrProfile = new UVCMediaStreamRecordingProfile(mDeviceMgr);
    }

    @Override
    public void onDestroy() {
        mDeviceMgr.removeDeviceListener(this);
        mDeviceMgr.removeConnectionListener(this);
        mDeviceMgr.stop();
        super.onDestroy();
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        mLogger.info("Plug-in : onManagerUninstalled");
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        mLogger.info("Plug-in : onManagerTerminated");
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        mLogger.info("Plug-in : onDevicePluginReset");
        resetPluginResource();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        ((UVCMediaStreamRecordingProfile) mMediaStreamRecordinrProfile).stopPreviewAllUVCDevice();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new UVCSystemProfile();
    }

    public UVCDeviceManager getDeviceManager() {
        UVCDeviceApplication app = (UVCDeviceApplication) getApplication();
        return app.getDeviceManager();
    }

    @Override
    public void onFound(final UVCDevice device) {
        DConnectService service = getServiceProvider().getService(device.getId());
        if (service == null) {
            service = new DConnectService(device.getId());
            service.setName("UVC: " + device.getName());
            service.addProfile(mMediaStreamRecordinrProfile);
            getServiceProvider().addService(service);
        }

        if (device.connect()) {
            mLogger.severe("UVC device has been initialized: " + device.getName());
            if (!device.canPreview()) {
                mLogger.info("UVC device CANNOT start preview: " + device.getName());
                ErrorDialogActivity.showNotSupportedError(this, device);
            } else {
                mLogger.info("UVC device can start preview: " + device.getName());
            }
        } else {
            mLogger.severe("UVC device COULD NOT be initialized: " + device.getName());
        }
    }

    @Override
    public void onConnect(final UVCDevice device) {
        DConnectService service = getServiceProvider().getService(device.getId());
        if (service != null) {
            service.setOnline(true);
        }
    }

    @Override
    public void onConnectionFailed(final UVCDevice device) {
        // NOP.
    }

    @Override
    public void onDisconnect(final UVCDevice device) {
        DConnectService service = getServiceProvider().getService(device.getId());
        if (service != null) {
            service.setOnline(false);
        }
    }
}
