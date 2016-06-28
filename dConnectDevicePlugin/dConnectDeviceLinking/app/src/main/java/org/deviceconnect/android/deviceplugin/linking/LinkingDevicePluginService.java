/*
 LinkingDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingAtmosphericPressureProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingBatteryProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingHumidityProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingKeyEventProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingLightProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingNotificationProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingProximityProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingSystemProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingTemperatureProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingVibrationProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * Linking device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingDevicePluginService extends DConnectMessageService {

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());
        addProfile(new LinkingLightProfile());
        addProfile(new LinkingDeviceOrientationProfile(this));
        addProfile(new LinkingVibrationProfile());
        addProfile(new LinkingNotificationProfile());
        addProfile(new LinkingProximityProfile(this));
        addProfile(new LinkingKeyEventProfile(this));
        addProfile(new LinkingBatteryProfile(this));
        addProfile(new LinkingAtmosphericPressureProfile());
        addProfile(new LinkingHumidityProfile());
        addProfile(new LinkingTemperatureProfile());
        addProfile(new LinkingProfile());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT.equals(action) ||
                    LinkingBeaconUtil.ACTION_BEACON_SCAN_STATE.equals(action)) {
                LinkingApplication app = (LinkingApplication)  getApplication();
                LinkingBeaconManager mgr = app.getLinkingBeaconManager();
                mgr.onReceivedBeacon(intent);
                return START_STICKY;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new LinkingSystemProfile();
    }

}
