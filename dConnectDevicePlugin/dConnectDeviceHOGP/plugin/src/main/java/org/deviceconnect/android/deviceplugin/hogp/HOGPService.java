package org.deviceconnect.android.deviceplugin.hogp;

import android.bluetooth.BluetoothDevice;

import org.deviceconnect.android.deviceplugin.hogp.profiles.HOGPKeyboardProfile;
import org.deviceconnect.android.deviceplugin.hogp.profiles.HOGPMouseProfile;
import org.deviceconnect.android.service.DConnectService;

/**
 * HOGPで接続されているデバイスのサービス.
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
    HOGPService(final BluetoothDevice device) {
        super(device.getAddress());
        mDevice = device;

        addProfile(new HOGPMouseProfile());
        addProfile(new HOGPKeyboardProfile());
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
