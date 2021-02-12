/*
 UsbDeviceClassFilter.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libusb;

import android.hardware.usb.UsbDevice;

import java.util.Objects;

/**
 * デバイスクラスでフィルタリングするためのクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class UsbDeviceClassFilter implements UsbDeviceFilter {

    private int mClassType;

    UsbDeviceClassFilter(int classType) {
        mClassType = classType;
    }

    @Override
    public boolean checkFilter(UsbDevice device) {
        int classType = device.getDeviceClass();
        return mClassType == classType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsbDeviceClassFilter that = (UsbDeviceClassFilter) o;
        return mClassType == that.mClassType;
    }

    @Override
    public int hashCode() {

        return Objects.hash(mClassType);
    }
}
