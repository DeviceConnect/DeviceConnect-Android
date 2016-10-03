/*
 LinkingBeaconUtil.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon;

import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;

public final class LinkingBeaconUtil {
    public static final String LINKING_PACKAGE_NAME = "com.nttdocomo.android.smartdeviceagent";

    public static final String BEACON_SERVICE_NAME = "com.nttdocomo.android.smartdeviceagent.beacon.BeaconService";
    public static final String ACTION_START_BEACON_SCAN = ".sda.action.START_BEACON_SCAN";
    public static final String ACTION_STOP_BEACON_SCAN = ".sda.action.STOP_BEACON_SCAN";
    public static final String EXTRA_SERVICE_ID = ".sda.extra.SERVICE_ID";
    public static final String EXTRA_SCAN_MODE = ".sda.extra.SCAN_MODE";

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

    public static final String PREFIX = "linking_beacon";
    public static final String SEPARATOR = "-";

    public enum ScanState {
        RESULT_OK(0),
        RESULT_NG(1);

        private int mValue;

        ScanState(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static ScanState valueOf(int value) {
            for (ScanState state : values()) {
                if (state.getValue() == value) {
                    return state;
                }
            }
            return RESULT_NG;
        }
    }

    public enum ScanDetail {
        DETAIL_OK(0),
        DETAIL_TIMEOUT(1),
        DETAIL_META_DATA_NONE(2),
        DETAIL_BT_DISABLED(3),
        DETAIL_SDA_DISABLED(4),
        DETAIL_PERMISSION_DENIED(5),
        DETAIL_UNKNOWN(6);

        private int mValue;

        ScanDetail(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static ScanDetail valueOf(int value) {
            for (ScanDetail detail : values()) {
                if (detail.getValue() == value) {
                    return detail;
                }
            }
            return DETAIL_UNKNOWN;
        }
    }

    public enum ScanMode {
        HIGH(0),
        NORMAL(1),
        LOW(2);

        private int mValue;

        ScanMode(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static ScanMode valueOf(int value) {
            for (ScanMode mode : values()) {
                if (mode.getValue() == value) {
                    return mode;
                }
            }
            return NORMAL;
        }
    }

    private LinkingBeaconUtil() {
    }

    public static String createServiceIdFromLinkingBeacon(final LinkingBeacon beacon) {
        return PREFIX + SEPARATOR + beacon.getVendorId() + SEPARATOR + beacon.getExtraId();
    }
}
