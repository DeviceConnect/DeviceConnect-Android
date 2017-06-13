/*
 FaBoDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fabo;

import org.deviceconnect.android.deviceplugin.fabo.device.FaBoDeviceControl;
import org.deviceconnect.android.deviceplugin.fabo.device.arduino.FaBoUsbDeviceControl;

/**
 * 本デバイスプラグインのプロファイルをDeviceConnectに登録するサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class FaBoArduinoDeviceService extends FaBoDeviceService {
    @Override
    protected FaBoDeviceControl createFaBoDeviceControl() {
        return new FaBoUsbDeviceControl(this);
    }
}
