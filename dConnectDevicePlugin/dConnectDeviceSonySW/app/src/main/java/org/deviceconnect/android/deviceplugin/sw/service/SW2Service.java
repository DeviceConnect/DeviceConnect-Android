package org.deviceconnect.android.deviceplugin.sw.service;


import android.bluetooth.BluetoothDevice;

import org.deviceconnect.android.deviceplugin.sw.SWConstants;
import org.deviceconnect.android.deviceplugin.sw.profile.SWKeyEventProfile;

class SW2Service extends SWService {

    protected SW2Service(final BluetoothDevice device) {
        super(device, WatchType.SW2);
        addProfile(new SWKeyEventProfile());
    }

    @Override
    public String getHostPackageName() {
        return SWConstants.PACKAGE_SMART_WATCH_2;
    }
}
