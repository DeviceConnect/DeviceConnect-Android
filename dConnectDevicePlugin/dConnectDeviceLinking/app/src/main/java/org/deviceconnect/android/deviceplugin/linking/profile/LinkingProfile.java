package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;

public class LinkingProfile extends DConnectProfile {

    public static final String PROFILE_NAME = "linking";

    public static final String ATTRIBUTE_START_SCAN = "startScan";
    public static final String ATTRIBUTE_STOP_SCAN = "stopScan";

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onPostRequest(Intent request, Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (ATTRIBUTE_START_SCAN.equals(attribute)) {
            result = onPostStartScan(request, response, getServiceID(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    @Override
    protected boolean onDeleteRequest(Intent request, Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (ATTRIBUTE_STOP_SCAN.equals(attribute)) {
            result = onDeleteStopScan(request, response, getServiceID(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    private boolean onPostStartScan(Intent request, Intent response, String serviceId) {
        if (Util.getServiceType(serviceId) == Util.LINKING_APP) {
            LinkingBeaconManager mgr = getLinkingBeaconManager();
            mgr.startBeaconScan();
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            MessageUtils.setNotSupportProfileError(response);
        }
        return true;
    }

    private boolean onDeleteStopScan(Intent request, Intent response, String serviceId) {
        if (Util.getServiceType(serviceId) == Util.LINKING_APP) {
            LinkingBeaconManager mgr = getLinkingBeaconManager();
            mgr.stopBeaconScan();
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            MessageUtils.setNotSupportProfileError(response);
        }
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
