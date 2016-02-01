/*
 BleUtils
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.ble;

import android.Manifest;
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
     * Defined the permission of BLE scan.
     */
    public static final String[] BLE_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

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

    /**
     * Checks whether permission allow by user.
     * @param context context of application
     * @return Returns true if permission allow, otherwise false
     */
    public static boolean isBLEPermission(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            boolean result = true;
            for (int i = 0; i < BLE_PERMISSIONS.length; i++) {
                if (context.checkSelfPermission(BLE_PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                    result = false;
                }
            }
            return result;
        }
    }
}
