/*
 UVCServiceInformationProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.profile;


import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceService;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;

/**
 * UVC ServiceInformation Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCServiceInformationProfile extends ServiceInformationProfile{

    private static final String PARAM_USB = "usb";

    private final UVCDeviceManager mDeviceMgr;

    public UVCServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
        mDeviceMgr = ((UVCDeviceService) provider).getDeviceManager();
    }

    @Override
    protected boolean onGetInformation(final Intent request, final Intent response,
                                       final String serviceId) {
        super.onGetInformation(request, response, serviceId);
        Bundle connect = response.getBundleExtra(PARAM_CONNECT);
        connect.putBoolean(PARAM_USB, mDeviceMgr.getDevice(serviceId) != null);
        return true;
    }
}
