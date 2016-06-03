package org.deviceconnect.android.deviceplugin.linking.profile;

import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.profile.AtmosphericPressureProfile;
import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.android.profile.HumidityProfile;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.profile.NotificationProfile;
import org.deviceconnect.android.profile.ProximityProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SettingsProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.TemperatureProfile;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.VibrationProfileConstants;

import java.util.ArrayList;
import java.util.List;

final class Util {

    public static final String LINKING_APP_ID = "linking_app";

    public static final int LINKING_DEVICE = 1;
    public static final int LINKING_BEACON = 2;
    public static final int LINKING_APP = 3;

    private Util() {
    }

    public static int getServiceType(String serviceId) {
        if (LINKING_APP_ID.equals(serviceId)) {
            return LINKING_APP;
        } else if (LinkingBeaconUtil.isLinkingBeaconByServiceId(serviceId)) {
            return LINKING_BEACON;
        } else {
            return LINKING_DEVICE;
        }
    }

    public static String[] createLinkingDeviceScopes(LinkingDevice device) {
        List<String> scopes = new ArrayList<>();
        scopes.add(AuthorizationProfile.PROFILE_NAME);
        scopes.add(KeyEventProfile.PROFILE_NAME);
        scopes.add(NotificationProfile.PROFILE_NAME);
        scopes.add(ProximityProfile.PROFILE_NAME);
        scopes.add(ServiceDiscoveryProfile.PROFILE_NAME);
        scopes.add(ServiceInformationProfile.PROFILE_NAME);
        scopes.add(SettingsProfile.PROFILE_NAME);
        scopes.add(SystemProfile.PROFILE_NAME);
        if (LinkingUtil.hasLED(device)) {
            scopes.add(LightProfile.PROFILE_NAME);
        }
        if (LinkingUtil.hasVibration(device)) {
            scopes.add(VibrationProfileConstants.PROFILE_NAME);
        }
        if (LinkingUtil.hasSensor(device)) {
            scopes.add(DeviceOrientationProfileConstants.PROFILE_NAME);
        }
        return scopes.toArray(new String[scopes.size()]);
    }

    public static String[] createLinkingBeaconScopes() {
        return new String[] {
                AtmosphericPressureProfile.PROFILE_NAME,
                AuthorizationProfile.PROFILE_NAME,
                BatteryProfile.PROFILE_NAME,
                HumidityProfile.PROFILE_NAME,
                KeyEventProfile.PROFILE_NAME,
                ProximityProfile.PROFILE_NAME,
                ServiceDiscoveryProfile.PROFILE_NAME,
                ServiceInformationProfile.PROFILE_NAME,
                SystemProfile.PROFILE_NAME,
                TemperatureProfile.PROFILE_NAME
        };
    }

    public static String[] createLinkingAppScopes() {
        return new String[] {
                AuthorizationProfile.PROFILE_NAME,
                ServiceDiscoveryProfile.PROFILE_NAME,
                ServiceInformationProfile.PROFILE_NAME,
                SystemProfile.PROFILE_NAME,
                LinkingProfile.PROFILE_NAME
        };
    }
}
