/*
 UVCDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc;


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
public class UVCDeviceService extends DConnectMessageService {

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private UVCDeviceManager mDeviceMgr;

    @Override
    public void onCreate() {
        super.onCreate();

        addProfile(new UVCMediaStreamRecordingProfile());

        mDeviceMgr = getDeviceManager();
        mDeviceMgr.start();
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

    private UVCDeviceManager getDeviceManager() {
        UVCDeviceApplication app = (UVCDeviceApplication) getApplication();
        return app.getDeviceManager();
    }

}
