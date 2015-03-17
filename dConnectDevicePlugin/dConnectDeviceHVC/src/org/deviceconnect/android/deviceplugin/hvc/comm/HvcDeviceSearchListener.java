/*
 HvcDeviceSearchListener.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.comm;

import java.util.List;

import android.bluetooth.BluetoothDevice;

/**
 * HVC Device search listener.
 * 
 * @author NTT DOCOMO, INC.
 */
public interface HvcDeviceSearchListener {
    /**
     * device search finish.
     * @param devices found devices.
     */
    void onDeviceSearchFinish(final List<BluetoothDevice> devices);
    /**
     * search timeout.
     */
    void onDeviceSearchTimeout();
    /**
     * disconnect.
     */
    void onDeviceSearchDisconnect();
}
