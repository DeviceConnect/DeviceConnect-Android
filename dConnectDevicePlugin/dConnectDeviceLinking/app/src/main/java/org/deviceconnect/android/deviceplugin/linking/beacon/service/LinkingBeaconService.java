/*
 LinkingBeaconService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.service;

import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.profile.LinkingAtmosphericPressureProfile;
import org.deviceconnect.android.deviceplugin.linking.beacon.profile.LinkingBatteryProfile;
import org.deviceconnect.android.deviceplugin.linking.beacon.profile.LinkingHumidityProfile;
import org.deviceconnect.android.deviceplugin.linking.beacon.profile.LinkingKeyEventProfile;
import org.deviceconnect.android.deviceplugin.linking.beacon.profile.LinkingProximityProfile;
import org.deviceconnect.android.deviceplugin.linking.beacon.profile.LinkingTemperatureProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;

public class LinkingBeaconService extends DConnectService {

    private LinkingBeacon mBeacon;

    public LinkingBeaconService(final DConnectMessageService service, final LinkingBeacon beacon) {
        super(beacon.getServiceId());

        setName(beacon.getDisplayName());
        setNetworkType(NetworkType.BLE);

        mBeacon = beacon;

        addProfile(new LinkingAtmosphericPressureProfile());
        addProfile(new LinkingBatteryProfile(service));
        addProfile(new LinkingHumidityProfile());
        addProfile(new LinkingKeyEventProfile(service));
        addProfile(new LinkingProximityProfile(service));
        addProfile(new LinkingTemperatureProfile());
    }

    @Override
    public boolean isOnline() {
        return mBeacon.isOnline();
    }

    public void setLinkingBeacon(final LinkingBeacon beacon) {
        mBeacon = beacon;
    }

    public LinkingBeacon getLinkingBeacon() {
        return mBeacon;
    }
}
