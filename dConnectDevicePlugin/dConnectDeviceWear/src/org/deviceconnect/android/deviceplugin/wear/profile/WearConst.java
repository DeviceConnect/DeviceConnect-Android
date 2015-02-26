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
    public static final String DEVICE_TO_WEAR_VIBRATION_RUN = "org.deviceconnect.wear.vibration.run";

    /** Vibration stop. */
    public static final String DEVICE_TO_WEAR_VIBRATION_DEL = "org.deviceconnect.wear.vibration.del";

    /** When Action is opened. */
    public static final String DEVICE_TO_WEAR_NOTIFICATION_OPEN = "org.deviceconnect.wear.notification.open";

    /** When DeviceOrientation register is opened. */
    public static final String DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER
            = "org.deviceconnect.wear.deivceorienatation.regist";

    /** When DeviceOrientation unregister is opened. */
    public static final String DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER
            = "org.deviceconnect.wear.deivceorienatation.unregist";

    /** Wear to Android. */
    public static final String WEAR_TO_DEVICE_DEIVCEORIENTATION_DATA = "org.deviceconnect.wear.deivceorienatation.data";

    /** Register Touch (ontouch). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCH_REGISTER
            = "org.deviceconnect.wear.touch.ontouch.regist";

    /** Register Touch (ontouchstart). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_REGISTER
            = "org.deviceconnect.wear.touch.ontouchstart.regist";

    /** Register Touch (ontouchend). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_REGISTER
            = "org.deviceconnect.wear.touch.ontouchend.regist";

    /** Register Touch (ondoubletap). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_REGISTER
            = "org.deviceconnect.wear.touch.ondoubletap.regist";

    /** Register Touch (ontouchmove). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_REGISTER
            = "org.deviceconnect.wear.touch.ontouchmove.regist";

    /** Register Touch (ontouchcancel). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_REGISTER
            = "org.deviceconnect.wear.touch.ontouchcancel.regist";

    /** Remove Touch (ontouch). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCH_UNREGISTER
            = "org.deviceconnect.wear.touch.ontouch.unregist";

    /** Remove Touch (ontouchstart). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_UNREGISTER
            = "org.deviceconnect.wear.touch.ontouchstart.unregist";

    /** Remove Touch (ontouchend). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_UNREGISTER
            = "org.deviceconnect.wear.touch.ontouchend.unregist";

    /** Remove Touch (ondoubletap). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_UNREGISTER
            = "org.deviceconnect.wear.touch.ondoubletap.unregist";

    /** Remove Touch (ontouchmove). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_UNREGISTER
            = "org.deviceconnect.wear.touch.ontouchmove.unregist";

    /** Remove Touch (ontouchcancel). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_UNREGISTER
            = "org.deviceconnect.wear.touch.ontouchcancel.unregist";

    /** Wear to Android. */
    public static final String WEAR_TO_DEVICE_TOUCH_DATA = "org.deviceconnect.wear.touch.data";

    /** Touch (touch). */
    public static final String PARAM_TOUCH_TOUCH = "touch";

    /** Touch (touchstart). */
    public static final String PARAM_TOUCH_TOUCHSTART = "touchstart";

    /** Touch (touchend). */
    public static final String PARAM_TOUCH_TOUCHEND = "touchend";

    /** Touch (doubletap). */
    public static final String PARAM_TOUCH_DOUBLETAP = "doubletap";

    /** Touch (touchmove). */
    public static final String PARAM_TOUCH_TOUCHMOVE = "touchmove";

    /** Touch (touchcancel). */
    public static final String PARAM_TOUCH_TOUCHCANCEL = "touchcancel";

    /** ServiceId. */
    public static final String PARAM_DEVICEID = "serviceId";

    /** NotificationId. */
    public static final String PARAM_NOTIFICATIONID = "notificationId";

    /**
     * Constructor.
     */
    private WearConst() {
    }
}
