package org.deviceconnect.android.deviceplugin.sw.service;


import android.bluetooth.BluetoothDevice;

import org.deviceconnect.android.deviceplugin.sw.SWConstants;

class MN2Service extends SWService {

    protected MN2Service(final BluetoothDevice device) {
        super(device, WatchType.MN2);
    }

    @Override
    public String getHostPackageName() {
        return SWConstants.PACKAGE_SMART_WATCH;
    }
}
