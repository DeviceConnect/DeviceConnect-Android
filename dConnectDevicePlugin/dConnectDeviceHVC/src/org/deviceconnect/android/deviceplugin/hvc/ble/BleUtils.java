/*
 BleUtils
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.ble;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * A class containing utility methods related to BLE.
 * @author NTT DOCOMO, INC.
 */
public final class BleUtils {

    /**
     * Constructor.
     */
    private BleUtils() {
    }

    /**
     * Checks whether device(smart phone) supports BLE.
     * @param context context of application
     * @return Returns true if the device supports BLE, else
     * false.
     */
    public static boolean isBLESupported(final Context context) {
        return Build.VERSION.SDK_INT >= 18
                && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Gets instance of BluetoothManager.
     * @param context context of application
     * @return Instance of BluetoothManager or null if the BluetoothManager does not exist.
     */
    public static BluetoothManager getManager(final Context context) {
        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }
}
