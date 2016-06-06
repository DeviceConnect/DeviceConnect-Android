package org.deviceconnect.android.deviceplugin.linking.beacon;

import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;

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

    public static final String PREFIX = "linking_beacon";
    public static final String SEPARATOR = "-";

    public static final int DETAIL_OK = 0;
    public static final int DETAIL_TIMEOUT = 1;
    public static final int DETAIL_META_DATA_NONE = 2;
    public static final int DETAIL_BT_DISABLED = 3;
    public static final int DETAIL_SDA_DISABLED = 4;
    public static final int DETAIL_PERMISSION_DENIED = 5;

    public static final int RESULT_OK = 0;
    public static final int RESULT_NG = 1;

    public enum ScanMode {
        HIGHT(0),
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
            return null;
        }
    }

    private LinkingBeaconUtil() {
    }

    public static String createServiceIdFromLinkingBeacon(LinkingBeacon beacon) {
        return PREFIX + SEPARATOR + beacon.getVendorId() + SEPARATOR + beacon.getExtraId();
    }

    public static boolean isLinkingBeaconByServiceId(String serviceId) {
        if (serviceId != null) {
            String[] split = serviceId.split(SEPARATOR);
            if (split.length == 3) {
                return PREFIX.equals(split[0]);
            }
        }
        return false;
    }

    public static int getVendorIdFromServiceId(String serviceId) throws IllegalArgumentException {
        if (serviceId == null) {
            throw new IllegalArgumentException("serviceId is null");
        }
        String[] split = serviceId.split(SEPARATOR);
        if (split.length == 3) {
            return Integer.parseInt(split[1]);
        }
        throw new IllegalArgumentException("Cannot separate the serviceId.");
    }

    public static int getExtraIdFromServiceId(String serviceId) throws IllegalArgumentException {
        if (serviceId == null) {
            throw new IllegalArgumentException("serviceId is null");
        }
        String[] split = serviceId.split(SEPARATOR);
        if (split.length == 3) {
            return Integer.parseInt(split[2]);
        }
        throw new IllegalArgumentException("Cannot separate the serviceId.");
    }

    public static LinkingBeacon findLinkingBeacon(LinkingBeaconManager mgr, String serviceId) {
        try {
            int vendorId = getVendorIdFromServiceId(serviceId);
            int extraId = getExtraIdFromServiceId(serviceId);
            return mgr.findBeacon(extraId, vendorId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
