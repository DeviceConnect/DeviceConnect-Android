/*
 BleDeviceAdapterFactory
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.ble;

import android.content.Context;

/**
 *
 * @author NTT DOCOMO, INC.
 */
public interface BleDeviceAdapterFactory {
    BleDeviceAdapter createAdapter(Context context);
}
