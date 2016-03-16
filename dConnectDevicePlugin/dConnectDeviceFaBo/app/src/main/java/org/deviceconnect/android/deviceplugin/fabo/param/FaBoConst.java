/*
 FaBoConst.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fabo.param;

/**
 * FaBoデバイスプラグインの汎用定数.
 */
public class FaBoConst {

    /** Command of open usb.  */
    public static final String DEVICE_TO_ARDUINO_OPEN_USB
            = "org.deviceconnect.gpio.usb.open";

    /** Result of usb connection.  */
    public static final String DEVICE_TO_ARDUINO_OPEN_USB_RESULT
            = "org.deviceconnect.gpio.usb.open.result";

    /** USBが未接続. */
    public static final int CAN_NOT_FIND_USB = 1;

    /** USBに接続失敗. */
    public static final int FAILED_OPEN_USB = 2;

    /** Arduinoに接続成功. */
    public static final int SUCCESS_CONNECT_ARDUINO = 2;

    /** Arduinoに接続失敗. */
    public static final int FAILED_CONNECT_ARDUINO = 3;

    /** Firmataに接続成功. */
    public static final int SUCCESS_CONNECT_FIRMATA = 5;

    /** Firmataに接続失敗. */
    public static final int FAILED_CONNECT_FIRMATA = 6;

}