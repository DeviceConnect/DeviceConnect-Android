/*
 UVCServiceDiscoveryProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.profile;


import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.uvc.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceService;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * UVC ServiceDiscovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    private static final String NETWORK_TYPE_USB = "usb";

    private final UVCDeviceManager mDeviceMgr;

    public UVCServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
        mDeviceMgr = ((UVCDeviceService) provider).getDeviceManager();
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        List<UVCDevice> devices = mDeviceMgr.getDeviceList();
        List<Bundle> services = new ArrayList<Bundle>();
        for (UVCDevice device : devices) {
            if (!device.isOpen()) {
                continue;
            }
            Bundle service = new Bundle();
            setId(service, device.getId());
            setName(service, "UVC: " + device.getName());
            setType(service, NETWORK_TYPE_USB);
            ServiceDiscoveryProfile.setOnline(service, true);
            ServiceDiscoveryProfile.setScopes(service, getProfileProvider());
            services.add(service);
        }
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(ServiceDiscoveryProfile.PARAM_SERVICES,
            services.toArray(new Bundle[services.size()]));
        return true;
    }

}
