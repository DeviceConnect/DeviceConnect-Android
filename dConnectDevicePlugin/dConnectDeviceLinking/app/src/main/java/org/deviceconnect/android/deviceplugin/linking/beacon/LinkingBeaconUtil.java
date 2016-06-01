package org.deviceconnect.android.deviceplugin.linking.beacon;

public final class LinkingBeaconUtil {
    public static final String ACTION_BEACON_SCAN_RESULT = "com.nttdocomo.android.smartdeviceagent.action.BEACON_SCAN_RESULT";
    public static final String ACTION_BEACON_SCAN_STATE = "com.nttdocomo.android.smartdeviceagent.action.BEACON_SCAN_STATE";

    public static final String SCAN_STATE = "com.nttdocomo.android.smartdeviceagent.extra.SCAN_STATE";
    public static final String DETAIL = "com.nttdocomo.android.smartdeviceagent.extra.DETAIL";

    public static final String TIME_STAMP = "com.nttdocomo.android.smartdeviceagent.extra.TIMESTAMP";

    public static final String VENDOR_ID = "com.nttdocomo.android.smartdeviceagent.extra.VENDOR_ID";
    public static final String EXTRA_ID = "com.nttdocomo.android.smartdeviceagent.extra.EXTRA_ID";
    public static final String VERSION = "com.nttdocomo.android.smartdeviceagent.extra.VERSION";

    public static final String RSSI = "com.nttdocomo.android.smartdeviceagent.extra.RSSI";
    public static final String TX_POWER = "com.nttdocomo.android.smartdeviceagent.extra.TX_POWER";
    public static final String DISTANCE = "com.nttdocomo.android.smartdeviceagent.extra.DISTANCE";

    public static final String TEMPERATURE = "com.nttdocomo.android.smartdeviceagent.extra.SERVICE_ID_1";

    public static final String HUMIDITY = "com.nttdocomo.android.smartdeviceagent.extra.SERVICE_ID_2";

    public static final String ATMOSPHERIC_PRESSURE = "com.nttdocomo.android.smartdeviceagent.extra.SERVICE_ID_3";

    public static final String LOW_BATTERY = "com.nttdocomo.android.smartdeviceagent.extra.SERVICE_ID_4_1";
    public static final String BATTERY_LEVEL = "com.nttdocomo.android.smartdeviceagent.extra.SERVICE_ID_4_2";

    public static final String BUTTON_ID = "com.nttdocomo.android.smartdeviceagent.extra.SERVICE_ID_5";

    public static final String RAW_DATA = "com.nttdocomo.android.smartdeviceagent.extra.SERVICE_ID_15";


    public static final int DETAIL_OK = 0;
    public static final int DETAIL_TIMEOUT = 1;
    public static final int DETAIL_META_DATA_NONE = 2;
    public static final int DETAIL_BT_DISABLED = 3;
    public static final int DETAIL_SDA_DISABLED = 4;
    public static final int DETAIL_PERMISSION_DENIED = 5;

    public static final int RESULT_OK = 0;
    public static final int RESULT_NG = 1;

    private LinkingBeaconUtil() {

    }
}
