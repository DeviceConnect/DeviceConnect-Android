/*
 UsbDeviceFilter.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libusb;

import android.hardware.usb.UsbDevice;

/**
 * USBのフィルタリングするためのインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
interface UsbDeviceFilter {
    boolean checkFilter(UsbDevice device);
}
