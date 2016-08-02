package org.deviceconnect.android.deviceplugin.pebble.service;


import android.bluetooth.BluetoothDevice;

import org.deviceconnect.android.deviceplugin.pebble.PebbleDeviceService;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleBatteryProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleCanvasProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleKeyEventProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleNotificationProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleSettingProfile;
import org.deviceconnect.android.deviceplugin.pebble.profile.PebbleVibrationProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.Locale;

public class PebbleService extends DConnectService {
    /**
     * サービスIDのプレフィックス.
     */
    public static final String PREFIX_SERVICE_ID = "Pebble";

    public PebbleService(final BluetoothDevice device, final PebbleDeviceService deviceService) {
        super(createServiceId(device));
        setName(device.getName());
        setNetworkType(NetworkType.BLUETOOTH);

        addProfile(new PebbleNotificationProfile());
        addProfile(new PebbleDeviceOrientationProfile(deviceService));
        addProfile(new PebbleVibrationProfile());
        addProfile(new PebbleBatteryProfile(deviceService));
        addProfile(new PebbleSettingProfile());
        addProfile(new PebbleCanvasProfile());
        addProfile(new PebbleKeyEventProfile(deviceService));
    }

    public static String createServiceId(final BluetoothDevice device) {
        return createServiceId(device.getAddress());
    }

    public static String createServiceId(final String macAddress) {
        String serviceId = macAddress.replace(":", "")
            .toLowerCase(Locale.getDefault());
        return PREFIX_SERVICE_ID + serviceId;
    }

}
