/*
 KadecotService
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.service;


import org.deviceconnect.android.deviceplugin.kadecot.KadecotDeviceService;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.ENLObject;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotDevice;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotHomeAirConditionerProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.KadecotLightProfile;
import org.deviceconnect.android.deviceplugin.kadecot.profile.original.AirConditionerProfile;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.service.DConnectService;

public class KadecotService extends DConnectService {

    /** Index of prefix. */
    private static final int IDX_PREFIX = 0;

    /** Index of kadecot deviceId. */
    private static final int IDX_DEVICEID = 1;

    /** Index of profile name. */
    private static final int IDX_PROFILENAME = 2;

    private final KadecotDevice mDevice;

    public KadecotService(final KadecotDevice kadecotDevice, final ENLObject object) {
        super(kadecotDevice.getServiceId());
        mDevice = kadecotDevice;
        if (kadecotDevice.getNickname() != null) {
            setName(kadecotDevice.getNickname() + "_" + kadecotDevice.getDeviceId());
        } else {
            setName(object.exchangeServiceId(kadecotDevice.getDeviceType()) + " (Kadecot)");
        }

        setNetworkType(NetworkType.WIFI);
        setOnline(true);

        String serviceId = kadecotDevice.getServiceId();
        if (isSupported(LightProfile.PROFILE_NAME, serviceId)) {
            addProfile(new KadecotLightProfile());
        } else if (isSupported(AirConditionerProfile.PROFILE_NAME, serviceId)) {
            addProfile(new KadecotHomeAirConditionerProfile());
        }
    }

    private static boolean isSupported(final String profileName, final String serviceId) {
        String[] element = KadecotDeviceService.getElementFromServiceId(serviceId);
        return profileName.equals(element[IDX_PROFILENAME]);
    }

    public boolean hasDeviceId(final String deviceId) {
        String[] element = KadecotDeviceService.getElementFromServiceId(getId());
        return deviceId.equals(element[IDX_DEVICEID]);
    }

    public KadecotDevice getDevice() {
        return mDevice;
    }
}
