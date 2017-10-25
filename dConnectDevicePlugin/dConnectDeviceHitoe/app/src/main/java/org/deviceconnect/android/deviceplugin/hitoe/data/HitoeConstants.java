/*
 HitoeConsts
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

/**
 * Hitoe's constant.
 * @author NTT DOCOMO, INC.
 */
public final class HitoeConstants {
    /**
     * Private constructor.
     */
    private HitoeConstants() {
    }

    /**
     * Line seperator.
     */
    public static final String BR = System.getProperty("line.separator");
    /** VB. */
    public static final String VB = "\\|";
    /** Comma. */
    public static final String COMMA = ",";
    /** Colon. */
    public static final String COLON = ":";

    /** Raw data prefix.*/
    public static final String RAW_DATA_PREFFIX = "raw.";
    /** Ba data prefix. */
    public static final String BA_DATA_PREFFIX = "ba.";
    /** Ex data prefix. */
    public static final String EX_DATA_PREFFIX = "ex.";

    /** Raw connection prefix. */
    public static final String RAW_CONNECTION_PREFFIX = "R";
    /** Ba connection prefix. */
    public static final String BA_CONNECTION_PREFFIX = "B";
    /** Ex connection prefix. */
    public static final String EX_CONNECTION_PREFFIX = "E";

    /** Available ex data string. */
    public static final String AVAILABLE_EX_DATA_STR = "ex.stress\nex.posture\nex.walk\nex.lr_balance";
    /** ECG Chart update cycle time. */
    public static final int ECG_CHART_UPDATE_CYCLE_TIME = 40;
    /** Acceleration chart update cycle time. */
    public static final int ACC_CHART_UPDATE_CYCLE_TIME = 100;
    /** HeartRate text update cycle time. */
    public static final int HR_TEXT_UPDATE_CYCLE_TIME = 1000;
    /** Stress Estimation update cycle time. */
    public static final int LFHF_TEXT_UPDATE_CYCLE_TIME = 1000;
    /** Pose Estimation update cycle time. */
    public static final int POSTURE_STATE_UPDATE_CYCLE_TIME = 1000;
    /** Discovery Cycle time. */
    public static final int DISCOVERY_CYCLE_TIME = 30000;

    /** Ex posture unit number. */
    public static final int EX_POSTURE_UNIT_NUM = 25;
    /** Ex walk unit number. */
    public static final int EX_WALK_UNIT_NUM = 100;
    /** Ex Left Right balance unit number. */
    public static final int EX_LR_BALANCE_UNIT_NUM = 250;

    /** getAvailableSensor. */
    public static final int API_ID_GET_AVAILABLE_SENSOR        = 0x1010;
    /** connect. */
    public static final int API_ID_CONNECT                    = 0x1020;
    /** disconnect. */
    public static final int API_ID_DISCONNECT                = 0x1021;
    /** getAvailableData. */
    public static final int API_ID_GET_AVAILABLE_DATA        = 0x1030;
    /** addReciever. */
    public static final int API_ID_ADD_RECIVER                = 0x1040;
    /** removeReciever. */
    public static final int API_ID_REMOVE_RECEIVER            = 0x1041;
    /** getStatus. */
    public static final int API_ID_GET_STATUS                = 0x1090;

    /** Response id for success. */
    public static final int    RES_ID_SUCCESS                    = 0x00;
    /** Response id for failure. */
    public static final int    RES_ID_FAILURE                     = 0x01;
    /** Response id for continue. */
    public static final int    RES_ID_CONTINUE                    = 0x05;
    /** Response id for api busy. */
    public static final int RES_ID_API_BUSY                    = 0x09;
    /** Response id for invalid argument. */
    public static final int    RES_ID_INVALID_ARG                 = 0x10;
    /** Response id for invalid parameter. */
    public static final int    RES_ID_INVALID_PARAM             = 0x30;
    /** Response id for sensor connect. */
    public static final int    RES_ID_SENSOR_CONNECT            = 0x60;
    /** Response id for sensor connect failure. */
    public static final int    RES_ID_SENSOR_CONNECT_FAILURE    = 0x61;
    /** Response id for sensor connect notice. */
    public static final int    RES_ID_SENSOR_CONNECT_NOTICE    = 0x62;
    /** Response id for sensor unauthorized. */
    public static final int RES_ID_SENSOR_UNAUTHORIZED        = 0x63;
    /** Response id for sensor disconnect. */
    public static final int    RES_ID_SENSOR_DISCONECT            = 0x65;
    /** Response id for sensor disconnect notice. */
    public static final int    RES_ID_SENSOR_DISCONECT_NOTICE    = 0x66;

    /** Get Available sensor device type. */
    public static final String GET_AVAILABLE_SENSOR_DEVICE_TYPE = "hitoe D01";
    /** Get Available sensor parameter search time. */
    public static final int GET_AVAILABLE_SENSOR_PARAM_SEARCH_TIME = 5000;

    /** Connect disconnect retry time. */
    public static final int CONNECT_DISCONNECT_RETRY_TIME = 1000;
    /** Connect disconnect retry count. */
    public static final int CONNECT_DISCONNECT_RETRY_COUNT = 3;
    /** Connect no packet retry time. */
    public static final int CONNECT_NOPACKET_RETRY_TIME = 5000;

    /** Add receiver parameter ECG sampling interval. */
    public static final int ADD_RECEIVER_PARAM_ECG_SAMPLING_INTERVAL = 40;
    /** Add receiver parameter Acceleration sampling interval. */
    public static final int ADD_RECEIVER_PARAM_ACC_SAMPLING_INTERVAL = 40;
    /** Add receiver patameter RRI sampling interval. */
    public static final int ADD_RECEIVER_PARAM_RRI_SAMPLING_INTERVAL = 1000;
    /** Add receiver parameter heartrate sampling interval. */
    public static final int ADD_RECEIVER_PARAM_HR_SAMPLING_INTERVAL = 1000;
    /** Add receiver parameter battery sampling interval. */
    public static final int ADD_RECEIVER_PARAM_BAT_SAMPLING_INTERVAL = 10000;

    /** Add receiver parameter Ba samling interval. */
    public static final int ADD_RECEIVER_PARAM_BA_SAMPLING_INTERVAL = 4000;
    /** Add receiver parameter Ba ECG Threshold. */
    public static final int ADD_RECEIVER_PARAM_BA_ECG_THRESHHOLD = 250;
    /** Add receiver parameter Ba skip count. */
    public static final int ADD_RECEIVER_PARAM_BA_SKIP_COUNT = 50;
    /** Add receiver parameter Ba RRI min. */
    public static final int ADD_RECEIVER_PARAM_BA_RRI_MIN = 240;
    /** Add receiver parameter Ba RRI max. */
    public static final int ADD_RECEIVER_PARAM_BA_RRI_MAX = 3999;
    /** Add receiver parameter Ba sample count. */
    public static final int ADD_RECEIVER_PARAM_BA_SAMPLE_COUNT = 20;
    /** Add receiver parameter Ba RRI input. */
    public static final String ADD_RECEIVER_PARAM_BA_RRI_INPUT = "extracted_rri";
    /** Add receiver parameter Ba Frequency sampling interval. */
    public static final int ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_INTERVAL = 4000;
    /** Add receiver parameter Ba frequency sampling window. */
    public static final int ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_WINDOW = 60;
    /** Add receiver parameter Ba RRI sampling rate. */
    public static final int ADD_RECEIVER_PARAM_BA_RRI_SAMPLING_RATE = 8;
    /** Add receiver parameter Ba time sampling interval. */
    public static final int ADD_RECEIVER_PARAM_BA_TIME_SAMPLING_INTERVAL = 4000;
    /** Add receiver parameter Ba time sampling window. */
    public static final int ADD_RECEIVER_PARAM_BA_TIME_SAMPLING_WINDOW = 60;

    /** Add receiver parameter ex accleration axis xyz. */
    public static final String ADD_RECEIVER_PARAM_EX_ACC_AXIS_XYZ = "XYZ";
    /** Add receiver parameter ex psture window. */
    public static final int ADD_RECEIVER_PARAM_EX_POSTURE_WINDOW = 1;
    /** Add receiver parameter ex walk stride. */
    public static final double ADD_RECEIVER_PARAM_EX_WALK_STRIDE = 0.81;
    /** Add receiver parameter ex run stride cof. */
    public static final double ADD_RECEIVER_PARAM_EX_RUN_STRIDE_COF = 0.0091;
    /** Add receiver parameter ex run stride int. */
    public static final double ADD_RECEIVER_PARAM_EX_RUN_STRIDE_INT = 0.1806;

    /** Back forward threshold. */
    public static final int BACK_FORWARD_THRESHOLD = 30;
    /** Left right threshold. */
    public static final int LEFT_RIGHT_THRESHOLD = 20;

}
