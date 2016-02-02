/*
 LinkingServiceDiscoveryProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManagerFactory;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.LightProfileConstants;
import org.deviceconnect.profile.VibrationProfileConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Discovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    public LinkingServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        List<LinkingDevice> list = LinkingManagerFactory.createManager(getContext().getApplicationContext()).getDevices();
        if (list.size() == 0) {
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
        Bundle[] services = new Bundle[list.size()];
        int index = 0;
        for (LinkingDevice device : list) {
            Bundle service = new Bundle();
            ServiceDiscoveryProfile.setId(service, device.getBdAddress());
            ServiceDiscoveryProfile.setName(service, device.getDisplayName());
            ServiceDiscoveryProfile.setType(service, NetworkType.BLE);
            ServiceDiscoveryProfile.setOnline(service, device.isConnected());
            DConnectProfileProvider provider = getProfileProvider();
            List<String> scopes = new ArrayList<>();
            for (DConnectProfile profile : provider.getProfileList()) {
                scopes.add(profile.getProfileName());
            }
            if (!LinkingUtil.hasLED(device)) {
                scopes.remove(LightProfileConstants.PROFILE_NAME);
            }
            if (!LinkingUtil.hasVibration(device)) {
                scopes.remove(VibrationProfileConstants.PROFILE_NAME);
            }
            if (!LinkingUtil.hasSensor(device)) {
                scopes.remove(DeviceOrientationProfileConstants.PROFILE_NAME);
            }
            String[] array = new String[scopes.size()];
            array = scopes.toArray(array);
            service.putStringArray(PARAM_SCOPES, array);
            services[index] = service;
            index++;
        }
        setServices(response, services);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

}
