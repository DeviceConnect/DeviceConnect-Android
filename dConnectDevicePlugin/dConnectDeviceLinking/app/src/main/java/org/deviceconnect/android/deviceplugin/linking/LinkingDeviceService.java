/*
 LinkingDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking;

import org.deviceconnect.android.deviceplugin.linking.profile.LinkingDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingLightProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingNotificationProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingProximityProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingServiceInformationProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingSystemProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingVibrationProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * Linking device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingDeviceService extends DConnectMessageService {

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());
        addProfile(new LinkingLightProfile());
        addProfile(new LinkingDeviceOrientationProfile());
        addProfile(new LinkingVibrationProfile());
        addProfile(new LinkingNotificationProfile());
        addProfile(new LinkingProximityProfile());
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new LinkingSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new LinkingServiceInformationProfile(this);
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new LinkingServiceDiscoveryProfile(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
