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

    /** Command of check usb.  */
    public static final String DEVICE_TO_ARDUINO_CHECK_USB
            = "org.deviceconnect.gpio.usb.check";

    /** Command of check usb.  */
    public static final String DEVICE_TO_ARDUINO_CHECK_USB_RESULT
            = "org.deviceconnect.gpio.usb.check.result";

    /** Command of close usb.  */
    public static final String DEVICE_TO_ARDUINO_CLOSE_USB
            = "org.deviceconnect.gpio.usb.close";

    /** Result of usb connection.  */
    public static final String DEVICE_TO_ARDUINO_OPEN_USB_RESULT
            = "org.deviceconnect.gpio.usb.open.result";

    /** USBが未接続. */
    public static final int CAN_NOT_FIND_USB = 1;

    /** USBに接続失敗. */
    public static final int FAILED_OPEN_USB = 2;

    /** Arduinoに接続成功. */
    public static final int SUCCESS_CONNECT_ARDUINO = 3;

    /** Arduinoに接続失敗. */
    public static final int FAILED_CONNECT_ARDUINO = 4;

    /** Firmataに接続成功. */
    public static final int SUCCESS_CONNECT_FIRMATA = 5;

    /** Firmataに接続失敗. */
    public static final int FAILED_CONNECT_FIRMATA = 6;

    /** 初期化中. */
    public static final int STATUS_INIT = 1;

    /** 起動中. */
    public static final int STATUS_RUNNING = 2;

    /** 停止中. */
    public static final int STATUS_STOP = 3;

    /** ACTIVITY非表示. */
    public static final int STATUS_ACTIVITY_STOP = 4;


}