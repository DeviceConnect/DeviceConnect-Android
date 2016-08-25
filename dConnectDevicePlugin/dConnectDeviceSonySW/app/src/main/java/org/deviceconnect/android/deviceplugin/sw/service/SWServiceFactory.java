package org.deviceconnect.android.deviceplugin.sw.service;


import android.bluetooth.BluetoothDevice;

import org.deviceconnect.android.deviceplugin.sw.SWConstants;
import org.deviceconnect.android.service.DConnectService;

public abstract class SWServiceFactory {

    public static DConnectService createService(final BluetoothDevice device) {
        if (SWConstants.DEVICE_NAME_SMART_WATCH.equals(device.getName())) {
            return new MN2Service(device);
        }
        if (SWConstants.DEVICE_NAME_SMART_WATCH_2.equals(device.getName())) {
            return new SW2Service(device);
        }
        return null;
    }

}
