/*
 SWService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw;

import org.deviceconnect.android.deviceplugin.sw.profile.SWCanvasProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWKeyEventProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWNotificationProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWSystemProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWTouchProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWVibrationProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 */
public class SWService extends DConnectMessageService {

    @Override
    public void onCreate() {
        super.onCreate();

        EventManager.INSTANCE.setController(new MemoryCacheController());

        addProfile(new SWDeviceOrientationProfile());
        addProfile(new SWNotificationProfile());
        addProfile(new SWVibrationProfile());
        addProfile(new SWCanvasProfile());
        addProfile(new SWTouchProfile());
        addProfile(new SWKeyEventProfile());
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SWSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this) { };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new SWServiceDiscoveryProfile(this);
    }
}
