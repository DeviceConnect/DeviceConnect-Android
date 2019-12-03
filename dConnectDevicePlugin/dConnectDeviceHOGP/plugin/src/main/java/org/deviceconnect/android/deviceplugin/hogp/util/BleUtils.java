/*
 BleUtils.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

/**
 * BLEを操作するためのユーティリティクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class BleUtils {

    /**
     * BLEがサポートされているか確認します.
     *
     * @param context このアプリケーションが属するコンテキスト
     * @return サポートされている場合はtrue、それ以外はfalse
     */
    public static boolean isBleSupported(@NonNull final Context context) {
        try {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                    context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) &&
                    getAdapter(context) != null;
        } catch (final Throwable ignored) {
            // ignore exception
        }
        return false;
    }

    /**
     * BLEペリフェラルがサポートされているか確認を行います.
     *
     * @param context このアプリケーションが属するコンテキスト
     * @return サポートされている場合はtrue、それ以外はfalse
     */
    @SuppressLint("NewApi")
    public static boolean isBlePeripheralSupported(@NonNull final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }

        BluetoothAdapter bluetoothAdapter = getAdapter(context);
        return bluetoothAdapter != null && bluetoothAdapter.isMultipleAdvertisementSupported();
    }

    /**
     * Bluetooth設定が有効になっているか確認を行います.
     *
     * @param context このアプリケーションが属するコンテキスト
     * @return Bluetoothが有効になっている場合はtrue
     */
    public static boolean isBluetoothEnabled(@NonNull final Context context) {
        BluetoothAdapter bluetoothAdapter = getAdapter(context);
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Bluetoothの名前を取得します.
     * @param context このアプリケーションが属するコンテキスト
     * @return Bluetoothの名前、名前が取得できない場合はnullを返却します。
     */
    public static String getBluetoothName(@NonNull final Context context) {
        BluetoothAdapter bluetoothAdapter = getAdapter(context);
        return bluetoothAdapter != null ? bluetoothAdapter.getName() : null;
    }

    /**
     * BluetoothAdapterをAndroid OS別に取得します.
     * @param context このアプリケーションが属するコンテキスト
     * @return BluetoothAdapterのインスタンス
     */
    private static BluetoothAdapter getAdapter(final Context context) {
        BluetoothAdapter bluetoothAdapter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.
                    getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                return null;
            }
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return bluetoothAdapter;
    }

    /**
     * Bluetooth有効リクエストコード.
     */
    public static final int REQUEST_CODE_BLUETOOTH_ENABLE = 0xb1e;

    /**
     * Bluetoothを有効要求を行います.
     * <p>
     * 引数に渡されたActivityにonActivityResultを実装して、REQUEST_CODE_BLUETOOTH_ENABLEのリクエストコードに対する応答の処理を実装します。
     * </p>
     * @param activity the activity
     */
    public static void enableBluetooth(@NonNull final Activity activity) {
        activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_CODE_BLUETOOTH_ENABLE);
    }
}
