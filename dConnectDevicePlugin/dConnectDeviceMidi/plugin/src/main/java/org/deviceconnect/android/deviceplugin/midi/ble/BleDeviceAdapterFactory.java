/*
 BleDeviceAdapterFactory
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.ble;

import android.content.Context;

/**
 * This factory to create new BleDeviceAdapter.
 * @author NTT DOCOMO, INC.
 */
public interface BleDeviceAdapterFactory {
    /**
     * Create a new BleDeviceAdapter.
     * @param context context of this application
     * @return Instance of BleDeviceAdapter, or null on error
     */
    BleDeviceAdapter createAdapter(Context context);
}
