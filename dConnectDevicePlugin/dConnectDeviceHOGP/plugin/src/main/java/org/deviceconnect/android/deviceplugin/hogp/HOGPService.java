/*
 HOGPService.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp;

import android.bluetooth.BluetoothDevice;

import org.deviceconnect.android.deviceplugin.hogp.profiles.HOGPJoystickProfile;
import org.deviceconnect.android.deviceplugin.hogp.profiles.HOGPKeyboardProfile;
import org.deviceconnect.android.deviceplugin.hogp.profiles.HOGPMouseProfile;
import org.deviceconnect.android.deviceplugin.hogp.profiles.HOGPServiceInformation;
import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;
import org.deviceconnect.android.service.DConnectService;

/**
 * HOGPで接続されているデバイスのサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPService extends DConnectService {

    /**
     * 接続されているBluetoothデバイス.
     */
    private BluetoothDevice mDevice;

    /**
     * コンストラクタ.
     * @param device 接続されているデバイス
     */
    HOGPService(final BluetoothDevice device, final HOGPSetting setting) {
        super(device.getAddress());
        setNetworkType(NetworkType.BLE);

        mDevice = device;

        if (setting.getMouseMode() != HOGPServer.MouseMode.NONE) {
            addProfile(new HOGPMouseProfile());
        }

        if (setting.isEnabledKeyboard()) {
            addProfile(new HOGPKeyboardProfile());
        }

        if (setting.isEnabledJoystick()) {
            addProfile(new HOGPJoystickProfile());
        }

        addProfile(new HOGPServiceInformation());
    }

    @Override
    public String getName() {
        String name = mDevice.getName();
        if (name != null) {
            return name;
        }
        return mDevice.getAddress();
    }

    /**
     * 接続されているBluetoothデバイス
     * @return Bluetoothデバイス
     */
    public BluetoothDevice getDevice() {
        return mDevice;
    }
}
