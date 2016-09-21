/*
 SWUtil.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw.profile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import org.deviceconnect.android.deviceplugin.sw.SWConstants;

import java.util.Locale;
import java.util.Set;

/**
 * ユーティリティクラス.
 * @author NTT DOCOMO, INC.
 */
public final class SWUtil {

    /**
     * プライベートコンストラクタ.
     * ユーティリティクラスのため、インスタンスを生成させない.
     */
    private SWUtil() {
    }

    /**
     * ペアリング済みのBluetoothデバイス一覧上で指定されたデバイスを検索する.
     * 
     * @param serviceId サービスID
     * @return BluetoothDevice 指定されたデバイスがペアリング中であれば対応する{@link BluetoothDevice}、そうでない場合はnull
     */
    public static BluetoothDevice findSmartWatch(final String serviceId) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return null;
        }
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice device : bondedDevices) {
                String deviceName = device.getName();
                if (deviceName.startsWith(SWConstants.DEVICE_NAME_PREFIX)) {
                    String otherServiceId = device.getAddress().replace(":", "").toLowerCase(Locale.ENGLISH);
                    if (otherServiceId.equals(serviceId)) {
                        return device;
                    }
                }
            }
        }
        return null;
    }

    /**
     * ホストアプリケーションネームの返却.
     * 
     * @param deviceName デバイスネーム
     * @return ホストアプリケーションネーム
     */
    public static String toHostAppPackageName(final String deviceName) {
        if (SWConstants.DEVICE_NAME_SMART_WATCH.equals(deviceName)) {
            return SWConstants.PACKAGE_SMART_WATCH;
        }
        if (SWConstants.DEVICE_NAME_SMART_WATCH_2.equals(deviceName)) {
            return SWConstants.PACKAGE_SMART_WATCH_2;
        }
        return null;
    }
}
