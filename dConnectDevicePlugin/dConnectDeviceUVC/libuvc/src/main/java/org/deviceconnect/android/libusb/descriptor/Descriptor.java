/*
 Descriptor.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libusb.descriptor;

/**
 * ディスクリプタを定義するインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
public interface Descriptor {
    int DEVICE = 0x01;
    int CONFIGURATION = 0x02;
    int STRING = 0x03;
    int INTERFACE = 0x04;
    int ENDPOINT = 0x05;
    int DEVICE_QUALIFIER = 0x06;
    int OTHER_SPEED_CONFIGURATION = 0x07;
    int INTERFACE_POWER = 0x08;

    // USB 2.0
    int OTG = 0x09;
    int INTERFACE_ASSOCIATION = 0x0B;

    int CS_INTERFACE = 0x24;
    int CS_ENDPOINT = 0x25;
}
