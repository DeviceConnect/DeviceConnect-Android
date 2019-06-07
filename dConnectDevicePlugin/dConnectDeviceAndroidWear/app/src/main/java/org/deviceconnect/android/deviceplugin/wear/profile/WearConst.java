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

    /** When Action is closed. */
    public static final String DEVICE_TO_WEAR_NOTIFICATION_CLOSED
            = "org.deviceconnect.wear.notification.closed";

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
    /** Register Key Event (onkeychange). */
    public static final String DEVICE_TO_WEAR_KEYEVENT_ONKEYCHANGE_REGISTER
            = "org.deviceconnect.wear.keyevent.onkeychange.regist";

    /** Remove Key Event (ondown). */
    public static final String DEVICE_TO_WEAR_KEYEVENT_ONDOWN_UNREGISTER
            = "org.deviceconnect.wear.keyevent.ondown.unregist";

    /** Remove Key Event (onup). */
    public static final String DEVICE_TO_WEAR_KEYEVENT_ONUP_UNREGISTER
            = "org.deviceconnect.wear.keyevent.onup.unregist";
    /** Remove Key Event (onkeychange). */
    public static final String DEVICE_TO_WEAR_KEYEVENT_ONKEYCHANGE_UNREGISTER
            = "org.deviceconnect.wear.keyevent.onkeychange.unregist";
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
     * Timestamp.
     */
    public static final String TIMESTAMP = "timestamp";
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

    /** Wear to Android. */
    public static final String WEAR_TO_DEVICE_CANVAS_RESULT = "org.deviceconnect.wear.canvas.result";

    /** リクエストID. */
    public static final String PARAM_REQUEST_ID = "requestId";

    /** データ変更イベントの送信元(Android端末)を特定するためのID. */
    public static final String PARAM_SOURCE_ID = "sourceId";

    /** メッセージの送信先(Android端末)を特定するためのID. */
    public static final String PARAM_DESTINATION_ID = "destinationId";

    /** 成功. */
    public static final String RESULT_SUCCESS = "success";

    /** エラー: ビットマップサイズが大きすぎる. */
    public static final String RESULT_ERROR_TOO_LARGE_BITMAP = "errorTooLargeBitmap";

    /** エラー: Android端末との接続に失敗. */
    public static final String RESULT_ERROR_CONNECTION_FAILURE = "errorConnectionFailure";

    /** エラー: サポートしていない画像形式. */
    public static final String RESULT_ERROR_NOT_SUPPORTED_FORMAT = "errorNotSupportedFormat";

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
    /** Register Touch (ontouchchange). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCHCHANGE_REGISTER
            = "org.deviceconnect.wear.touch.ontouchchange.regist";

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
    /** Remove Touch (ontouchchange). */
    public static final String DEVICE_TO_WEAR_TOUCH_ONTOUCHCHANGE_UNREGISTER
            = "org.deviceconnect.wear.touch.ontouchchange.unregist";
    /** Wear to Android. */
    public static final String WEAR_TO_DEVICE_TOUCH_DATA = "org.deviceconnect.wear.touch.data";
    /** Set wear id.. */
    public static final String DEVICE_TO_WEAR_SET_ID
            = "org.deviceconnect.wear.id.set";

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
    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_TOUCH_CHANGE = "onTouchChange";
    /** Touch State start. */
    public static final String STATE_START = "start";
    /** Touch State end. */
    public static final String STATE_END = "end";
    /** Touch State double tap. */
    public static final String STATE_DOUBLE_TAP = "doubletap";
    /** Touch State move. */
    public static final String STATE_MOVE = "move";
    /** Touch State cancel. */
    public static final String STATE_CANCEL = "cancel";
    /** KeyEvent State move. */
    public static final String STATE_UP = "up";
    /** KeyEvent State cancel. */
    public static final String STATE_DOWN = "down";
    /**
     * Constructor.
     */
    private WearConst() {
    }
}
