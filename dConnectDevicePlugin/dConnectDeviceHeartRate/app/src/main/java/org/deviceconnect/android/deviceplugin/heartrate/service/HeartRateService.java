package org.deviceconnect.android.deviceplugin.heartrate.service;


import android.bluetooth.BluetoothDevice;

import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;
import org.deviceconnect.android.service.DConnectService;

public class HeartRateService extends DConnectService {
    public HeartRateService(final BluetoothDevice device) {
        super(device.getAddress());
        if (device.getName() != null && device.getName().length() > 0) {
            setName(device.getName());
        } else {
            setName(device.getAddress());
        }
        setNetworkType(NetworkType.BLE);
    }

    public HeartRateService(final HeartRateDevice device) {
        super(device.getAddress());
        setName(device.getName());
        setNetworkType(NetworkType.BLE);
    }

}
