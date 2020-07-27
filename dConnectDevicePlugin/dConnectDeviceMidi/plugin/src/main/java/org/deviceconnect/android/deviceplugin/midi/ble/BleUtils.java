/*
 BleUtils
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.midi.ble.activity.BleEnableActivity;

/**
 * A class containing utility methods related to BLE.
 * @author NTT DOCOMO, INC.
 */
public final class BleUtils {

    // 1800 Generic Access
    public static final String SERVICE_GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_DEVICE_NAME = "00002a00-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_APPEARANCE = "00002a01-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_PERIPHERAL_PRIVACY_FLAG = "00002a02-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_RECONNECTION_ADDRESS = "00002a03-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS = "00002a04-0000-1000-8000-00805f9b34fb";

    // 1801 Generic Attribute
    public static final String SERVICE_GENERIC_ATTRIBUTE = "00001801-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SERVICE_CHANGED = "00002a05-0000-1000-8000-00805f9b34fb";

    // 1802 Immediate Alert
    public static final String SERVICE_IMMEDIATE_ALERT = "00001802-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_ALERT_LEVEL = "00002a06-0000-1000-8000-00805f9b34fb";
    // StickNFindではCHAR_ALERT_LEVELに0x01をWriteすると光り、0x02では音が鳴り、0x03では光って鳴る。

    // 180a Device Information
    public static final String SERVICE_DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_MANUFACTURER_NAME_STRING = "00002a29-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_MODEL_NUMBER_STRING = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SERIAL_NUMBER_STRING = "00002a25-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_HARDWARE_REVISION_STRING = "00002a27-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_FIRMWARE_REVISION_STRING = "00002a26-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SOFTWARE_REVISION_STRING = "00002a28-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SYSTEM_ID = "00002a23-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_IEEE_11073_20601_REGULATORY_CERTIFICATION_DATA_LIST = "00002a2A-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_PNP_ID = "00002a50-0000-1000-8000-00805f9b34fb";

    // 180F Battery Service
    public static final String SERVICE_BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";

    // 180D Heart Reate Service
    public static final String SERVICE_HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BODY_SENSOR_LOCATION = "00002a38-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_HEART_RATE_CONTROL_POINT = "00002a39-0000-1000-8000-00805f9b34fb";

    // MIDI
    public static final String SERVICE_MIDI_SERVICE = "03B80E5A-EDE8-4B33-A751-6CE34EC4C700";

    /**
     * Defined the permission of BLE scan.
     */
    public static final String[] BLE_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private BleUtils() {
    }

    /**
     * Checks whether device(smart phone) supports BLE.
     * @param context context of application
     * @return Returns true if the device supports BLE, else
     * false.
     */
    public static boolean isBLESupported(final Context context) {
        return Build.VERSION.SDK_INT >= 18 &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
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

    public static void requestBluetoothEnabled(final Context context, final BleRequestCallback callback) {
        Handler handler = new Handler(Looper.getMainLooper());
        BleEnableActivity.requestEnableBluetooth(context, new ResultReceiver(handler) {
            @Override
            protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                if (resultCode == Activity.RESULT_OK) {
                    callback.onSuccess();
                } else {
                    callback.onFail("Denied to enable a bluetooth settings.");
                }
            }
        });
    }

    /**
     * BLEパーミッションの許可を要求します.
     * @param context コンテキスト
     * @param callback BLEパーミッション許可要求結果を通知するコールバック
     */
    public static void requestBLEPermission(final Context context, final BleRequestCallback callback) {
        if (isBLEPermission(context)) {
            callback.onSuccess();
        } else {
            PermissionUtility.requestPermissions(context, new Handler(Looper.getMainLooper()),
                    BLE_PERMISSIONS,
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess();
                        }
                        @Override
                        public void onFail(final String deniedPermission) {
                            callback.onFail(deniedPermission);
                        }
                    });
        }
    }

    /**
     * BLEパーミッションの許可結果を通知するコールバック.
     */
    public interface BleRequestCallback {
        /**
         * 許可が降りた場合に呼び出されるメソッド.
         */
        void onSuccess();

        /**
         * 許可が降りなかった場合に呼び出されるメソッド.
         * @param deniedPermission 拒否内容
         */
        void onFail(final String deniedPermission);
    }
}
