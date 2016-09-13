/*
 BeaconUtil.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.profile;

import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.ProximityProfile;

import java.util.List;

public final class BeaconUtil {

    private BeaconUtil() {
    }

    public static boolean isEmptyEvent(final LinkingBeaconManager manager) {
        List<Event> events;
        for (LinkingBeacon beacon : manager.getLinkingBeacons()) {
            events = EventManager.INSTANCE.getEventList(
                    beacon.getServiceId(),
                    KeyEventProfile.PROFILE_NAME, null,
                    KeyEventProfile.ATTRIBUTE_ON_DOWN);
            if (!events.isEmpty()) {
                return false;
            }

            events = EventManager.INSTANCE.getEventList(
                    beacon.getServiceId(),
                    ProximityProfile.PROFILE_NAME, null,
                    ProximityProfile.ATTRIBUTE_ON_DEVICE_PROXIMITY);
            if (!events.isEmpty()) {
                return false;
            }

            events = EventManager.INSTANCE.getEventList(
                    beacon.getServiceId(),
                    BatteryProfile.PROFILE_NAME, null,
                    BatteryProfile.ATTRIBUTE_ON_BATTERY_CHANGE);
            if (!events.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
