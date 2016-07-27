package org.deviceconnect.android.deviceplugin.sw;


import android.bluetooth.BluetoothDevice;
import android.content.Context;

import org.deviceconnect.android.deviceplugin.bluetooth.BluetoothDeviceManager;

import java.util.logging.Logger;

class SWDeviceManager extends BluetoothDeviceManager {

    private final Logger mLogger = Logger.getLogger(SWConstants.LOGGER_NAME);

    public SWDeviceManager(final Context context) {
        super(context, new DeviceFilter() {
            @Override
            public boolean filter(final BluetoothDevice device) {
                return isSmartWatch(device);
            }
        });
        setLogger(mLogger);
    }

    private static boolean isSmartWatch(final BluetoothDevice device) {
        if (device.getName() == null) {
            return false;
        }
        return device.getName().startsWith(SWConstants.DEVICE_NAME_PREFIX);
    }
}
