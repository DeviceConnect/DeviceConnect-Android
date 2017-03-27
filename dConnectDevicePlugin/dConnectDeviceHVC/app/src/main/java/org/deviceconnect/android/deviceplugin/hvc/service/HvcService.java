package org.deviceconnect.android.deviceplugin.hvc.service;


import android.bluetooth.BluetoothDevice;

import org.deviceconnect.android.deviceplugin.hvc.profile.HvcHumanDetectionProfile;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcServiceInformationProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.Locale;

public class HvcService extends DConnectService {

    public HvcService(final BluetoothDevice foundDevice) {
        super(createServiceId(foundDevice));
        setName(foundDevice.getName());
        setNetworkType(NetworkType.BLE);
        addProfile(new HvcServiceInformationProfile());
        addProfile(new HvcHumanDetectionProfile());
    }

    public static String createServiceId(final BluetoothDevice foundDevice) {
        String address = foundDevice.getAddress();
        return address.replace(":", "").toLowerCase(Locale.ENGLISH);
    }
}
