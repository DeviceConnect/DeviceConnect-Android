/*
WearConst.java
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

/**
 * Wear Const.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class WearConst {

    /** Vibration start. */
    public static final String DEVICE_TO_WEAR_VIBRATION_RUN
            = "org.deviceconnect.wear.vibration.run";

    /** Vibration stop. */
    public static final String DEVICE_TO_WEAR_VIBRATION_DEL
            = "org.deviceconnect.wear.vibration.del";

    /** When Action is opened. */
    public static final String DEVICE_TO_WEAR_NOTIFICATION_OPEN
            = "org.deviceconnect.wear.notification.open";

    /** When DeviceOrientation register is opened. */
    public static final String DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER
            = "org.deviceconnect.wear.deivceorienatation.regist";

    /** When DeviceOrientation unregister is opened. */
    public static final String DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER
            = "org.deviceconnect.wear.deivceorienatation.unregist";

    /** When Canvas deleted. */
    public static final String DEVICE_TO_WEAR_CANCAS_DELETE_IMAGE
            = "org.deviceconnect.wear.canvas.delete";

    /** Wear to Android. */
    public static final String WEAR_TO_DEVICE_DEIVCEORIENTATION_DATA
            = "org.deviceconnect.wear.deivceorienatation.data";

    /** Register Key Event (ondown). */
    public static final String DEVICE_TO_WEAR_KEYEVENT_ONDOWN_REGISTER
            = "org.deviceconnect.wear.keyevent.ondown.regist";

    /** Register Key Event (onup). */
    public static final String DEVICE_TO_WEAR_KEYEVENT_ONUP_REGISTER = "org.deviceconnect.wear.keyevent.onup.regist";

    /** Remove Key Event (ondown). */
    public static final String DEVICE_TO_WEAR_KEYEVENT_ONDOWN_UNREGISTER
            = "org.deviceconnect.wear.keyevent.ondown.unregist";

    /** Remove Key Event (onup). */
    public static final String DEVICE_TO_WEAR_KEYEVENT_ONUP_UNREGISTER
            = "org.deviceconnect.wear.keyevent.onup.unregist";

    /** Wear to Android. */
    public static final String WEAR_TO_DEVICE_KEYEVENT_DATA = "org.deviceconnect.wear.keyevent.data";

    /** Key Event (down). */
    public static final String PARAM_KEYEVENT_DOWN = "down";

    /** Key Event (up). */
    public static final String PARAM_KEYEVENT_UP = "up";

    /** ServiceId. */
    public static final String PARAM_DEVICEID = "serviceId";

    /** NotificationId. */
    public static final String PARAM_NOTIFICATIONID = "notificationId";

    /**
     * サービスID.
     */
    public static final String SERVICE_ID = "Wear";

    /**
     * デバイス名: {@value}.
     */
    public static final String DEVICE_NAME = "Android Wear";

    /**
     * bitmapを受け渡しするためのキー.
     */
    public static final String PARAM_BITMAP = "bitmap";

    /**
     * x座標を受け渡しするためのキー.
     */
    public static final String PARAM_X = "x";

    /**
     * y座標を受け渡しするためのキー.
     */
    public static final String PARAM_Y = "y";

    /**
     * 描画モードを受け渡しするためのキー.
     */
    public static final String PARAM_MODE = "mode";

    /**
     * 画像を送信するためのパスを定義する.
     */
    public static final String PATH_CANVAS = "/canvas/profile";

    /**
     * 通常の描画モード.
     */
    public static final int MODE_NORMAL = 0;

    /**
     * 拡大の描画モード.
     */
    public static final int MODE_SCALES = 1;

    /**
     * リピート描画モード.
     */
    public static final int MODE_FILLS = 2;

    /**
     * Constructor.
     */
    private WearConst() {
    }
}
