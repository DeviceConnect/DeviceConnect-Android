/*
 LinkingDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking;

import org.deviceconnect.android.api.EndPoint;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
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
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.NotificationProfileConstants;
import org.deviceconnect.profile.ProximityProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.profile.VibrationProfileConstants;

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

    @Override
    public EndPoint findEndPoint(final String serviceId) {
        LinkingDevice device = LinkingUtil.getLinkingDevice(getContext(), serviceId);
        if (device == null) {
            return null;
        }

        EndPoint.Builder endPoint = new EndPoint.Builder()
            .addApi(DConnectMessage.METHOD_GET,
                ServiceDiscoveryProfileConstants.PATH_PROFILE)
            .addApi(DConnectMessage.METHOD_GET,
                ServiceInformationProfileConstants.PATH_PROFILE)
            .addApi(DConnectMessage.METHOD_PUT,
                SystemProfileConstants.PATH_WAKEUP)
            .addApi(DConnectMessage.METHOD_POST,
                NotificationProfileConstants.PATH_NOTIFY)
            .addApi(DConnectMessage.METHOD_GET,
                ProximityProfileConstants.PATH_ON_DEVICE_PROXIMITY)
            .addApi(DConnectMessage.METHOD_PUT,
                ProximityProfileConstants.PATH_ON_DEVICE_PROXIMITY)
            .addApi(DConnectMessage.METHOD_DELETE,
                ProximityProfileConstants.PATH_ON_DEVICE_PROXIMITY);

        if (LinkingUtil.hasSensor(device)) {
            endPoint.addApi(DConnectMessage.METHOD_GET,
                DeviceOrientationProfileConstants.PATH_ON_DEVICE_ORIENTATION)
                .addApi(DConnectMessage.METHOD_PUT,
                    DeviceOrientationProfileConstants.PATH_ON_DEVICE_ORIENTATION)
                .addApi(DConnectMessage.METHOD_DELETE,
                    DeviceOrientationProfileConstants.PATH_ON_DEVICE_ORIENTATION);
        }
        if (LinkingUtil.hasLED(device)) {
            endPoint.addApi(DConnectMessage.METHOD_GET, "/gotapi/light")
                .addApi(DConnectMessage.METHOD_POST, "/gotapi/light")
                .addApi(DConnectMessage.METHOD_DELETE, "/gotapi/light");
        }
        if (LinkingUtil.hasVibration(device)) {
            endPoint.addApi(DConnectMessage.METHOD_PUT,
                VibrationProfileConstants.PATH_VIBRATE)
                .addApi(DConnectMessage.METHOD_PUT,
                    VibrationProfileConstants.PATH_VIBRATE);
        }
        return endPoint.build();
    }
}
