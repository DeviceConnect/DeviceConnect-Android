/*
 HitoeConsts
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.ble;

/**
 * Hitoe's constant.
 * @author NTT DOCOMO, INC.
 */
public class HitoeConstants {

	public static final String BR = System.getProperty("line.separator");
	public static final String VB = "\\|";
	public static final String COMMA = ",";
	public static final String COLON = ":";

	public static final String RAW_DATA_PREFFIX = "raw.";
	public static final String BA_DATA_PREFFIX = "ba.";
	public static final String EX_DATA_PREFFIX = "ex.";

	public static final String RAW_CONNECTION_PREFFIX = "R";
	public static final String BA_CONNECTION_PREFFIX = "B";
	public static final String EX_CONNECTION_PREFFIX = "E";

	public static final String AVAILABLE_EX_DATA_STR = "ex.stress\nex.posture\nex.walk\nex.lr_balance";

	public static final int ECG_CHART_UPDATE_CYCLE_TIME = 40;
	public static final int ACC_CHART_UPDATE_CYCLE_TIME = 40;
	public static final int HR_TEXT_UPDATE_CYCLE_TIME = 1000;
	public static final int LFHF_TEXT_UPDATE_CYCLE_TIME = 1000;
	public static final int POSTURE_STATE_UPDATE_CYCLE_TIME = 1000;

	public static final int EX_POSTURE_UNIT_NUM = 25;
	public static final int EX_WALK_UNIT_NUM = 100;
	public static final int EX_LR_BALANCE_UNIT_NUM = 250;

	public static final int API_ID_GET_AVAILABLE_SENSOR		= 0x1010;	// getAvailableSensor
	public static final int API_ID_CONNECT					= 0x1020;	// connect
	public static final int API_ID_DISCONNECT				= 0x1021;	// disconnect
	public static final int API_ID_GET_AVAILABLE_DATA		= 0x1030;	// getAvailableData
	public static final int API_ID_ADD_RECIVER				= 0x1040;	// addReciever
	public static final int API_ID_REMOVE_RECEIVER			= 0x1041;	// removeReciever
	public static final int API_ID_GET_STATUS				= 0x1090;	// getStatus

	public static final int	RES_ID_SUCCESS					= 0x00;
	public static final int	RES_ID_FAILURE 					= 0x01;
	public static final int	RES_ID_CONTINUE					= 0x05;
	public static final int RES_ID_API_BUSY					= 0x09;
	public static final int	RES_ID_INVALID_ARG 				= 0x10;
	public static final int	RES_ID_INVALID_PARAM 			= 0x30;
	public static final int	RES_ID_SENSOR_CONNECT			= 0x60;
	public static final int	RES_ID_SENSOR_CONNECT_FAILURE	= 0x61;
	public static final int	RES_ID_SENSOR_CONNECT_NOTICE	= 0x62;
	public static final int RES_ID_SENSOR_UNAUTHORIZED		= 0x63;
	public static final int	RES_ID_SENSOR_DISCONECT			= 0x65;
	public static final int	RES_ID_SENSOR_DISCONECT_NOTICE	= 0x66;

	public static final String GET_AVAILABLE_SENSOR_DEVICE_TYPE = "hitoe D01";
	public static final int GET_AVAILABLE_SENSOR_PARAM_SEARCH_TIME = 5000;

	public static final int CONNECT_DISCONNECT_RETRY_TIME = 1000;
	public static final int CONNECT_DISCONNECT_RETRY_COUNT= 3;
	public static final int CONNECT_NOPACKET_RETRY_TIME = 5000;

	public static final int ADD_RECEIVER_PARAM_ECG_SAMPLING_INTERVAL = 40;
	public static final int ADD_RECEIVER_PARAM_ACC_SAMPLING_INTERVAL = 40;
	public static final int ADD_RECEIVER_PARAM_RRI_SAMPLING_INTERVAL = 1000;
	public static final int ADD_RECEIVER_PARAM_HR_SAMPLING_INTERVAL = 1000;
	public static final int ADD_RECEIVER_PARAM_BAT_SAMPLING_INTERVAL = 10000;

	public static final int ADD_RECEIVER_PARAM_BA_SAMPLING_INTERVAL = 4000;
	public static final int ADD_RECEIVER_PARAM_BA_ECG_THRESHHOLD = 250;
	public static final int ADD_RECEIVER_PARAM_BA_SKIP_COUNT = 50;
	public static final int ADD_RECEIVER_PARAM_BA_RRI_MIN = 240;
	public static final int ADD_RECEIVER_PARAM_BA_RRI_MAX = 3999;
	public static final int ADD_RECEIVER_PARAM_BA_SAMPLE_COUNT = 20;
	public static final String ADD_RECEIVER_PARAM_BA_RRI_INPUT = "extracted_rri";
	public static final int ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_INTERVAL = 4000;
	public static final int ADD_RECEIVER_PARAM_BA_FREQ_SAMPLING_WINDOW = 60;
	public static final int ADD_RECEIVER_PARAM_BA_RRI_SAMPLING_RATE = 8;
	public static final int ADD_RECEIVER_PARAM_BA_TIME_SAMPLING_INTERVAL = 4000;
	public static final int ADD_RECEIVER_PARAM_BA_TIME_SAMPLING_WINDOW = 60;

	public static final String ADD_RECEIVER_PARAM_EX_ACC_AXIS_XYZ = "XYZ";
	public static final int ADD_RECEIVER_PARAM_EX_POSTURE_WINDOW = 1;
	public static final double ADD_RECEIVER_PARAM_EX_WALK_STRIDE = 0.81;
	public static final double ADD_RECEIVER_PARAM_EX_RUN_STRIDE_COF = 0.0091;
	public static final double ADD_RECEIVER_PARAM_EX_RUN_STRIDE_INT = 0.1806;

	public static final int BACK_FORWARD_THRESHOLD = 30;
	public static final int LEFT_RIGHT_THRESHOLD = 20;

}
