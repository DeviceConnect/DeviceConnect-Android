/*
 BleUtils
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.util;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Bluetoothのパーミッション確認用クラス.
 * @author NTT DOCOMO, INC.
 */
public final class BleUtils {

    /**
     * BLE検索に必要なパーミッション.
     */
    public static final String[] BLE_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private BleUtils() {
    }

    /**
     * BluetoothのOn/OFFを取得する
     * @param context
     * @return
     */
    public static boolean isEnabled(final Context context) {
        BluetoothManager mgr = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = mgr.getAdapter();
        return adapter.isEnabled();
    }


    /**
     * Bluetooth検索用のユーザ許可しているかどうか.
     * @param context アプリケーションのコンテキスト
     * @return true:パーミッションが許可されている false:パーミッションが許可されていない
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
