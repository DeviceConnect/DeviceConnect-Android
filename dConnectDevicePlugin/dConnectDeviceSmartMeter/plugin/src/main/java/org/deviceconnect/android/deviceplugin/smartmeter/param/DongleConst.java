/*
 DongleConst.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter.param;

/**
 * USB Dongle の汎用定数.
 *
 * @author NTT DOCOMO, INC.
 */
public class DongleConst {
    /** Command of open usb.  */
    public static final String DEVICE_TO_DONGLE_OPEN_USB
            = "org.deviceconnect.dongle.usb.open";
    /** Command of check usb.  */
    public static final String DEVICE_TO_DONGLE_CHECK_USB
            = "org.deviceconnect.dongle.usb.check";
    /** Command of check usb.  */
    public static final String DEVICE_TO_DONGLE_CHECK_USB_RESULT
            = "org.deviceconnect.dongle.usb.check.result";
    /** Command of close usb.  */
    public static final String DEVICE_TO_DONGLE_CLOSE_USB
            = "org.deviceconnect.dongle.usb.close";
    /** Result of usb connection.  */
    public static final String DEVICE_TO_DONGLE_OPEN_USB_RESULT
            = "org.deviceconnect.dongle.usb.open.result";

    /** USBが未接続. */
    public static final int CAN_NOT_FIND_USB = 1;
    /** USBに接続失敗. */
    public static final int FAILED_OPEN_USB = 2;
    /** Dongleに接続成功. */
    public static final int SUCCESS_CONNECT_DONGLE = 3;
    /** Dongleに接続失敗. */
    public static final int FAILED_CONNECT_DONGLE = 4;
    /** 停止中. */
    public static final int STATUS_DONGLE_NOCONNECT = 101;
    /** 初期化中. */
    public static final int STATUS_DONGLE_INIT = 102;
    /** 起動中. */
    public static final int STATUS_DONGLE_RUNNING = 103;
    /** ACTIVITY非表示. */
    public static final int STATUS_ACTIVITY_PAUSE = 201;
    /** ACTIVITY表示. */
    public static final int STATUS_ACTIVITY_DISPLAY = 202;
}
