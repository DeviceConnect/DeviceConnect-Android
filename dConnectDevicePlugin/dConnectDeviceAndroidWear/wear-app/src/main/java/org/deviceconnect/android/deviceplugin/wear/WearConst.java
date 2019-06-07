/*
WearConst.java
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear;

/**
 * Wear Const.
 *
 * @author NTT DOCOMO, INC.
 */
public final class WearConst {

    /** Broadcast Intent Key. */
    public static final String ACTION_WEAR_PING_SERVICE = "org.deviceconnect.wear.ping.service";

    /** Vibration start. */
    public static final String DEVICE_TO_WEAR_VIBRATION_RUN = "org.deviceconnect.wear.vibration.run";

    /** Vibration stop. */
    public static final String DEVICE_TO_WEAR_VIBRATION_DEL = "org.deviceconnect.wear.vibration.del";

    /** when DeviceOrientation register is opened. */
    public static final String DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER
            = "org.deviceconnect.wear.deivceorienatation.regist";

    /** When Canvas deleted. */
    public static final String DEVICE_TO_WEAR_CANCAS_DELETE_IMAGE
            = "org.deviceconnect.wear.canvas.delete";

    /** when DeviceOrientation unregister is opened. */
    public static final String DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER
            = "org.deviceconnect.wear.deivceorienatation.unregist";

    /** Wear to Android. */
    public static final String WEAR_TO_DEVICE_DEIVCEORIENTATION_DATA = "org.deviceconnect.wear.deivceorienatation.data";

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
    /** Set wear id.. */
    public static final String DEVICE_TO_WEAR_SET_ID
            = "org.deviceconnect.wear.id.set";

    /** Wear to Android. */
    public static final String WEAR_TO_DEVICE_KEYEVENT_DATA = "org.deviceconnect.wear.keyevent.data";

    /** Broadcast Intent Key. */
    public static final String PARAM_DC_WEAR_KEYEVENT_SVC_TO_ACT = "DC_WEAR_KEYEVENT_SVC_TO_ACT";

    /** Broadcast Intent Key. */
    public static final String PARAM_DC_WEAR_KEYEVENT_ACT_TO_SVC = "DC_WEAR_KEYEVENT_ACT_TO_SVC";

    /** Key Event (down). */
    public static final String PARAM_KEYEVENT_DOWN = "down";

    /** Key Event (up). */
    public static final String PARAM_KEYEVENT_UP = "up";

    /** Broadcast Data Key (Regist). */
    public static final String PARAM_KEYEVENT_REGIST = "keyeventRegist";

    /** Broadcast Data Key (Regist). */
    public static final String PARAM_KEYEVENT_ID = "keyeventId";

    /** Broadcast Data Key (Data). */
    public static final String PARAM_KEYEVENT_DATA = "data";

    /** Key Type (Standard Keyboard). */
    public static final int KEYTYPE_STD_KEY = 0x00000000;

    /** Key Type (Media Control). */
    public static final int KEYTYPE_MEDIA_CTRL = 0x00000200;

    /** Key Type (Directional Pad / Button). */
    public static final int KEYTYPE_DPAD_BUTTON = 0x00000400;

    /** Key Type (User Define). */
    public static final int KEYTYPE_USER = 0x00000800;

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

    /** Broadcast Data Key (Regist). */
    public static final String PARAM_TOUCH_REGIST = "touchRegist";

    public static final String PARAM_TOUCH_ID = "touchId";

    /** Broadcast Data Key (Data). */
    public static final String PARAM_TOUCH_DATA = "data";

    /** Broadcast Intent Key. */
    public static final String PARAM_DC_WEAR_TOUCH_SVC_TO_ACT = "DC_WEAR_TOUCH_SVC_TO_ACT";

    /** Broadcast Intent Key. */
    public static final String PARAM_DC_WEAR_TOUCH_ACT_TO_SVC = "DC_WEAR_TOUCH_ACT_TO_SVC";

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

    /**
     * Canvas Profile Action.
     */
    public static final String ACTION_DELETE_CANVAS = "org.devcieconnect.wear.canvas.delete";

    /** Broadcast Intent Key. */
    public static final String PARAM_DC_WEAR_CANVAS_ACT_TO_SVC = "DC_WEAR_CANVAS_ACT_TO_SVC";

    /** Wear to Android. */
    public static final String WEAR_TO_DEVICE_CANVAS_RESULT = "org.deviceconnect.wear.canvas.result";

    /** リクエストID. */
    public static final String PARAM_REQUEST_ID = "requestId";

    /** データ変更イベントの送信元(Android端末)を特定するためのID. */
    public static final String PARAM_SOURCE_ID = "sourceId";

    /** メッセージの送信先(Android端末)を特定するためのID. */
    public static final String PARAM_DESTINATION_ID = "destinationId";

    public static final String PARAM_SENSOR_ID = "sensorId";

    /**
     * bitmapを受け渡しするためのキー.
     */
    public static final String PARAM_RESULT = "result";

    /** 成功. */
    public static final String RESULT_SUCCESS = "success";

    /** エラー: ビットマップサイズが大きすぎる. */
    public static final String RESULT_ERROR_TOO_LARGE_BITMAP = "errorTooLargeBitmap";

    /** エラー: Android端末との接続に失敗. */
    public static final String RESULT_ERROR_CONNECTION_FAILURE = "errorConnectionFailure";

    /** エラー: サポートしていない画像形式. */
    public static final String RESULT_ERROR_NOT_SUPPORTED_FORMAT = "errorNotSupportedFormat";

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
