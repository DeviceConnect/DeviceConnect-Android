package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.AtmosphericPressureData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.AtmosphericPressureProfile;
import org.deviceconnect.message.DConnectMessage;

public class LinkingAtmosphericPressureProfile extends AtmosphericPressureProfile {

    @Override
    protected boolean onGetAtmosphericPressure(Intent request, Intent response, String serviceId) {
        LinkingBeaconManager mgr = getLinkingBeaconManager();
        LinkingBeacon beacon = LinkingBeaconUtil.findLinkingBeacon(mgr, serviceId);
        if (beacon == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        if (!beacon.isOnline()) {
            MessageUtils.setIllegalDeviceStateError(response, beacon.getDisplayName() + " is offline.");
            return true;
        }

        AtmosphericPressureData atmosphericPressureData = beacon.getAtmosphericPressureData();
        if (atmosphericPressureData == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        setResult(response, DConnectMessage.RESULT_OK);
        setAtmosphericPressure(response, atmosphericPressureData.getValue());
        setTimeStamp(response, atmosphericPressureData.getTimeStamp());

        return true;
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDeviceService service = (LinkingDeviceService) getContext();
        return (LinkingApplication) service.getApplication();
    }
}
