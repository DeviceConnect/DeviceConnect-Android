/*
 UsbDeviceIdFilter.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libusb;

import android.hardware.usb.UsbDevice;
import android.util.Log;

import java.util.Objects;

/**
 * デバイスIDでフィルタリングするためのクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class UsbDeviceIdFilter implements UsbDeviceFilter {
    private final int mVendorId;
    private final int mProductId;

    UsbDeviceIdFilter(int vendorId, int productId) {
        mVendorId = vendorId;
        mProductId = productId;
    }

    @Override
    public boolean checkFilter(UsbDevice device) {
        int vendorId = device.getVendorId();
        int productId = device.getProductId();
        return (mVendorId == vendorId && mProductId == productId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsbDeviceIdFilter filter = (UsbDeviceIdFilter) o;
        return mVendorId == filter.mVendorId &&
                mProductId == filter.mProductId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mVendorId, mProductId);
    }
}
