/*
 AWSIotDeviceManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot;

import org.deviceconnect.android.deviceplugin.awsiot.cores.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.cores.util.AWSIotUtil;

import java.util.ArrayList;
import java.util.List;

public class AWSIotDeviceManager {

    private List<TempDevice> mDeviceList = new ArrayList<>();

    public String generateServiceId(final RemoteDeviceConnectManager remote, final String serviceId) {
        for (TempDevice t : mDeviceList) {
            if (remote.equals(t.mRemote) && serviceId.equals(t.mServiceId)) {
                return t.mId;
            }
        }

        TempDevice t = new TempDevice();
        t.mRemote = remote;
        t.mServiceId = serviceId;
        t.mId = AWSIotUtil.md5(remote.getServiceId() + serviceId);
        mDeviceList.add(t);
        return t.mId;
    }

    public String getServiceId(final String serviceId) {
        for (TempDevice t : mDeviceList) {
            if (serviceId.equals(t.mId)) {
                return t.mServiceId;
            }
        }
        return null;
    }

    public RemoteDeviceConnectManager findManagerById(final String serviceId) {
        for (TempDevice t : mDeviceList) {
            if (serviceId.equals(t.mId)) {
                return t.mRemote;
            }
        }
        return null;
    }

    private class TempDevice {
        RemoteDeviceConnectManager mRemote;
        String mServiceId;
        String mId;
    }
}
