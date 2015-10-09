/*
 KadecotHomeAirConditioner
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice;

import java.util.ArrayList;

/**
 * Kadecot Home Air Conditioner.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotHomeAirConditioner {
    /** JSON string list. */
    ArrayList<KadecotJsonString> mJsonStringList = new ArrayList<>();

    /** Define : Get power status. */
    public static final int POWERSTATE_GET = 0x80800000;
    /** Define : Set power status on. */
    public static final int POWERSTATE_ON = 0x00800030;
    /** Define : Set power status off. */
    public static final int POWERSTATE_OFF = 0x00800031;
    /** Define : Get power saving status. */
    public static final int POWERSAVING_GET = 0x808F0000;
    /** Define : Set power saving status on. */
    public static final int POWERSAVING_ON = 0x008F0041;
    /** Define : Set power saving status off. */
    public static final int POWERSAVING_OFF = 0x008F0042;
    /** Define : Get air flow status. */
    public static final int AIRFLOW_GET = 0x80A00000;
    /** Define : Set air flow level1. */
    public static final int AIRFLOW_LV1 = 0x00A00031;
    /** Define : Set air flow level2. */
    public static final int AIRFLOW_LV2 = 0x00A00032;
    /** Define : Set air flow level3. */
    public static final int AIRFLOW_LV3 = 0x00A00033;
    /** Define : Set air flow level4. */
    public static final int AIRFLOW_LV4 = 0x00A00034;
    /** Define : Set air flow level5. */
    public static final int AIRFLOW_LV5 = 0x00A00035;
    /** Define : Set air flow level6. */
    public static final int AIRFLOW_LV6 = 0x00A00036;
    /** Define : Set air flow level7. */
    public static final int AIRFLOW_LV7 = 0x00A00037;
    /** Define : Set air flow level8. */
    public static final int AIRFLOW_LV8 = 0x00A00038;
    /** Define : Set air flow auto. */
    public static final int AIRFLOW_AUTO = 0x00A00041;
    /** Define : Get operation mode. */
    public static final int OPERATIONMODE_GET = 0x80B00000;
    /** Define : Set operation mode other. */
    public static final int OPERATIONMODE_OTHER = 0x00B00040;
    /** Define : Set operation mode automatic. */
    public static final int OPERATIONMODE_AUTO = 0x00B00041;
    /** Define : Set operation mode cooling. */
    public static final int OPERATIONMODE_COOL = 0x00B00042;
    /** Define : Set operation mode heating. */
    public static final int OPERATIONMODE_HEAT = 0x00B00043;
    /** Define : Set operation mode dry. */
    public static final int OPERATIONMODE_DRY = 0x00B00044;
    /** Define : Set operation mode wind. */
    public static final int OPERATIONMODE_WIND = 0x00B00045;
    /** Define : Get temperature value. */
    public static final int TEMPERATUREVALUE_GET = 0x80B30000;
    /** Define : Set temperature value. */
    public static final int TEMPERATUREVALUE_SET = 0x00B30000;
    /** Define : Get air flow value. */
    public static final int AIRFLOWVALUE_GET = 0x80A00000;
    /** Define : Set air flow value. */
    public static final int AIRFLOWVALUE_SET = 0x00A00000;
    /** Define : Get room temperature. */
    public static final int ROOMTEMPERATURE_GET = 0x80BB0000;

    /** Define : Get procedure. */
    public static final String PROC_GET = "get";
    /** Define : Set procedure. */
    public static final String PROC_SET = "set";

    /** Define : Operation status property. */
    public static final String PROP_OPERATIONSTATUS = "OperationStatus";
    /** Define : Power saving operation setting property. */
    public static final String PROP_POWERSAVINGOPERATIONSETTING = "PowerSavingOperationSetting";
    /** Define : Air flow rate setting property. */
    public static final String PROP_AIRFLOWRATESETTING = "AirFlowRateSetting";
    /** Define : Operation mode setting property. */
    public static final String PROP_OPERATIONMODESETTING = "OperationModeSetting";
    /** Define : Set temperature value property. */
    public static final String PROP_SETTEMPERATUREVALUE = "SetTemperatureValue";
    /** Define : Measured value of room temperature property. */
    public static final String PROP_MEASUREDVALUEOFROOMTEMPERATURE = "MeasuredValueOfRoomTemperature";

    /** Constructor. */
    public KadecotHomeAirConditioner() {
        mJsonStringList.add(new KadecotJsonString(POWERSTATE_GET, PROC_GET, PROP_OPERATIONSTATUS));
        mJsonStringList.add(new KadecotJsonString(POWERSTATE_ON, PROC_SET, PROP_OPERATIONSTATUS, 0x30));
        mJsonStringList.add(new KadecotJsonString(POWERSTATE_OFF, PROC_SET, PROP_OPERATIONSTATUS, 0x31));
        mJsonStringList.add(new KadecotJsonString(POWERSAVING_GET, PROC_GET, PROP_POWERSAVINGOPERATIONSETTING));
        mJsonStringList.add(new KadecotJsonString(POWERSAVING_ON, PROC_SET, PROP_POWERSAVINGOPERATIONSETTING, 0x41));
        mJsonStringList.add(new KadecotJsonString(POWERSAVING_OFF, PROC_SET, PROP_POWERSAVINGOPERATIONSETTING, 0x42));
        mJsonStringList.add(new KadecotJsonString(AIRFLOW_GET, PROC_GET, PROP_AIRFLOWRATESETTING));
        mJsonStringList.add(new KadecotJsonString(AIRFLOW_LV1, PROC_SET, PROP_AIRFLOWRATESETTING, 0x31));
        mJsonStringList.add(new KadecotJsonString(AIRFLOW_LV2, PROC_SET, PROP_AIRFLOWRATESETTING, 0x32));
        mJsonStringList.add(new KadecotJsonString(AIRFLOW_LV3, PROC_SET, PROP_AIRFLOWRATESETTING, 0x33));
        mJsonStringList.add(new KadecotJsonString(AIRFLOW_LV4, PROC_SET, PROP_AIRFLOWRATESETTING, 0x34));
        mJsonStringList.add(new KadecotJsonString(AIRFLOW_LV5, PROC_SET, PROP_AIRFLOWRATESETTING, 0x35));
        mJsonStringList.add(new KadecotJsonString(AIRFLOW_LV6, PROC_SET, PROP_AIRFLOWRATESETTING, 0x36));
        mJsonStringList.add(new KadecotJsonString(AIRFLOW_LV7, PROC_SET, PROP_AIRFLOWRATESETTING, 0x37));
        mJsonStringList.add(new KadecotJsonString(AIRFLOW_LV8, PROC_SET, PROP_AIRFLOWRATESETTING, 0x38));
        mJsonStringList.add(new KadecotJsonString(AIRFLOW_AUTO, PROC_SET, PROP_AIRFLOWRATESETTING, 0x41));
        mJsonStringList.add(new KadecotJsonString(OPERATIONMODE_GET, PROC_GET, PROP_OPERATIONMODESETTING));
        mJsonStringList.add(new KadecotJsonString(OPERATIONMODE_OTHER, PROC_SET, PROP_OPERATIONMODESETTING, 0x40));
        mJsonStringList.add(new KadecotJsonString(OPERATIONMODE_AUTO, PROC_SET, PROP_OPERATIONMODESETTING, 0x41));
        mJsonStringList.add(new KadecotJsonString(OPERATIONMODE_COOL, PROC_SET, PROP_OPERATIONMODESETTING, 0x42));
        mJsonStringList.add(new KadecotJsonString(OPERATIONMODE_HEAT, PROC_SET, PROP_OPERATIONMODESETTING, 0x43));
        mJsonStringList.add(new KadecotJsonString(OPERATIONMODE_DRY, PROC_SET, PROP_OPERATIONMODESETTING, 0x44));
        mJsonStringList.add(new KadecotJsonString(OPERATIONMODE_WIND, PROC_SET, PROP_OPERATIONMODESETTING, 0x45));
        mJsonStringList.add(new KadecotJsonString(TEMPERATUREVALUE_GET, PROC_GET, PROP_SETTEMPERATUREVALUE));
        mJsonStringList.add(new KadecotJsonString(TEMPERATUREVALUE_SET, PROC_SET, PROP_SETTEMPERATUREVALUE, 0x1C));
        mJsonStringList.add(new KadecotJsonString(ROOMTEMPERATURE_GET, PROC_GET, PROP_MEASUREDVALUEOFROOMTEMPERATURE));
    }

    /**
     * Get Object count.
     *
     * @return Object count.
     */
    public int getObjectCount() {
        if (mJsonStringList != null) {
            return mJsonStringList.size();
        } else {
            return 0;
        }
    }

    /**
     * Exchange JSON string.
     *
     * @param deviceId DeviceId.
     * @param index Index.
     * @return JSON string.
     */
    public String exchangeJsonString(final String deviceId, final int index) {
        for (int i = 0; i < mJsonStringList.size(); i++) {
            KadecotJsonString jsonStr = mJsonStringList.get(i);
            if (jsonStr.getIndex() == index) {
                return jsonStr.getJsonString(deviceId);
            }
        }
        return null;
    }

    /**
     * Exchange JSON string.
     *
     * @param deviceId DeviceId.
     * @param index Index.
     * @param value Value.
     * @return JSON string.
     */
    public String exchangeJsonString(final String deviceId, final int index, final int value) {
        for (int i = 0; i < mJsonStringList.size(); i++) {
            KadecotJsonString jsonStr = mJsonStringList.get(i);
            if (jsonStr.getIndex() == index) {
                return jsonStr.getJsonString(deviceId, value);
            }
        }
        return null;
    }

}
