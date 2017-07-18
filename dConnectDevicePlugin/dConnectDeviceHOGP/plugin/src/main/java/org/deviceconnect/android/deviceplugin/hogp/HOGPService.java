package org.deviceconnect.android.deviceplugin.hogp;

import android.bluetooth.BluetoothDevice;

import org.deviceconnect.android.deviceplugin.hogp.profiles.HOGPHogpProfile;
import org.deviceconnect.android.service.DConnectService;

public class HOGPService extends DConnectService {

    private BluetoothDevice mDevice;

    HOGPService(final BluetoothDevice device) {
        super(device.getAddress());
        mDevice = device;

        addProfile(new HOGPHogpProfile());
    }

    @Override
    public String getName() {
        String name = mDevice.getName();
        if (name != null) {
            return null;
        }
        return mDevice.getAddress();
    }
}
