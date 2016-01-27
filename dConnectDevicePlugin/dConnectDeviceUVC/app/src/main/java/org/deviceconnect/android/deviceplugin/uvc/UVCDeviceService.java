/*
 UVCDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;


import org.deviceconnect.android.deviceplugin.uvc.activity.ErrorDialogActivity;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCServiceInformationProfile;
import org.deviceconnect.android.deviceplugin.uvc.profile.UVCSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

import java.util.logging.Logger;

/**
 * UVC Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCDeviceService extends DConnectMessageService
    implements UVCDeviceManager.DeviceListener {

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private UVCDeviceManager mDeviceMgr;

    @Override
    public void onCreate() {
        super.onCreate();

        mDeviceMgr = getDeviceManager();
        mDeviceMgr.addDeviceListener(this);
        mDeviceMgr.start();

        addProfile(new UVCMediaStreamRecordingProfile(mDeviceMgr));
    }

    @Override
    public void onDestroy() {
        mDeviceMgr.stop();
        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new UVCSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new UVCServiceInformationProfile(this);
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new UVCServiceDiscoveryProfile(this);
    }

    public UVCDeviceManager getDeviceManager() {
        UVCDeviceApplication app = (UVCDeviceApplication) getApplication();
        return app.getDeviceManager();
    }

    @Override
    public void onAttach(final UVCDevice device) {
        if (device.initialize()) {
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
    public void onOpen(final UVCDevice device) {
        // Nothing to do.
    }

    @Override
    public void onClose(final UVCDevice device) {
        // Nothing to do.
    }
}
