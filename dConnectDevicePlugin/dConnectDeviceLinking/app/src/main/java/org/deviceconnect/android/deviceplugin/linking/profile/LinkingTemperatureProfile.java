package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.TemperatureData;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.TemperatureProfile;
import org.deviceconnect.message.DConnectMessage;

public class LinkingTemperatureProfile extends TemperatureProfile {

    @Override
    protected boolean onGetTemperature(Intent request, Intent response, String serviceId) {
        LinkingBeacon beacon = getLinkingBeacon(response, serviceId);
        if (beacon == null) {
            return true;
        }

        TemperatureData temperatureData = beacon.getTemperatureData();
        if (temperatureData == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        setResult(response, DConnectMessage.RESULT_OK);
        setTemperature(response, temperatureData.getValue());
        setTemperatureType(response, TemperatureType.TYPE_CELSIUS);
        setTimeStamp(response, temperatureData.getTimeStamp());

        return true;
    }

    private LinkingBeacon getLinkingBeacon(Intent response, String serviceId) {
        LinkingBeaconManager mgr = getLinkingBeaconManager();
        LinkingBeacon beacon = LinkingBeaconUtil.findLinkingBeacon(mgr, serviceId);
        if (beacon == null) {
            MessageUtils.setNotSupportProfileError(response);
            return null;
        }

        if (!beacon.isOnline()) {
            MessageUtils.setIllegalDeviceStateError(response, beacon.getDisplayName() + " is offline.");
            return null;
        }
        return beacon;
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
