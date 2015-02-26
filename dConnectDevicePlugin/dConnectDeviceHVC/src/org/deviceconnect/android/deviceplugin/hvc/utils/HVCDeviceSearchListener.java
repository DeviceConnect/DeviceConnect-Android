package org.deviceconnect.android.deviceplugin.hvc.utils;

import java.util.List;

import android.bluetooth.BluetoothDevice;

public interface HVCDeviceSearchListener {
    void onDeviceSearchFinish(List<BluetoothDevice> devices);
    void onDeviceSearchTimeout();
    void onDeviceSearchDisconnect();
}
