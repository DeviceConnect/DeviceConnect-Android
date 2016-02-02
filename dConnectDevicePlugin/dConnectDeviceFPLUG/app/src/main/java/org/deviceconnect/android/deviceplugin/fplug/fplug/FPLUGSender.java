/*
 FPLUGSender.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.fplug;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.fplug.BuildConfig;

import java.io.IOException;
import java.util.Calendar;

/**
 * This class provides functions of sending requests to F-PLUG.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGSender {

    private final static String TAG = "FPLUGSender";

    private int mTid = 0;//NOTE:unused in response handling. But it has no problem in practical use.
    private BluetoothSocket mSocket;

    public FPLUGSender(BluetoothSocket socket) {
        mSocket = socket;
    }

    public void executeRequest(FPLUGRequest request) {
        byte[] command;
        switch (request.getType()) {
            case INIT:
                command = createInitPlugCommand();
                break;
            case CANCEL_PAIRING:
                command = createCancelPairing();
                break;
            case WATT_HOUR:
                command = createWattHourCommand();
                break;
            case TEMPERATURE:
                command = createTemperatureCommand();
                break;
            case HUMIDITY:
                command = createHumidityCommand();
                break;
            case ILLUMINANCE:
                command = createIlluminanceCommand();
                break;
            case REALTIME_WATT:
                command = createRealtimeWattCommand();
                break;
            case PAST_WATT:
                command = createPastWattHourCommand((Calendar) request.getValue());
                break;
            case PAST_VALUES:
                command = createPastValuesCommand((Calendar) request.getValue());
                break;
            case SET_DATE:
                command = createSetDateCommand((Calendar) request.getValue());
                break;
            case LED_ON:
                command = createLEDControlCommand(true);
                break;
            case LED_OFF:
                command = createLEDControlCommand(false);
                break;
            default:
                request.getCallback().onError("unknown request type");
                return;
        }
        try {
            sendRequest(command);
        } catch (IOException e) {
            request.getCallback().onError("request error");
        } catch (IllegalArgumentException e) {
            request.getCallback().onError("invalid request command");
        }
    }

    private void sendRequest(byte[] command) throws IOException, IllegalArgumentException {
        if (!isValidCommand(command)) {
            throw new IllegalArgumentException("invalid command");
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "request:" + toHexString(command));
        }
        mSocket.getOutputStream().write(command);
    }

    private boolean isValidCommand(byte[] command) {
        return !(command == null || command.length == 0);
    }

    private byte[] createInitPlugCommand() {
        Calendar cal = Calendar.getInstance();
        int iYear = cal.get(Calendar.YEAR);
        int iMonth = cal.get(Calendar.MONTH) + 1;
        int iDate = cal.get(Calendar.DATE);
        int iHour = cal.get(Calendar.HOUR_OF_DAY);
        int iMinute = cal.get(Calendar.MINUTE);

        mTid++;
        byte tid1 = (byte) (mTid & 0xff);
        byte tid2 = (byte) ((mTid & 0xff00) >> 8);
        return new byte[]{
                (byte) 0x10,
                (byte) 0x81,
                tid1, tid2,
                (byte) 0x0E, (byte) 0xF0, (byte) 0x00,
                (byte) 0x00, (byte) 0x22, (byte) 0x00,
                (byte) 0x61,
                (byte) 0x02,
                (byte) 0x97,
                (byte) 0x02,
                (byte) (iHour & 0xff),
                (byte) (iMinute & 0xff),
                (byte) 0x98,
                (byte) 0x04,
                (byte) (iYear & 0xff), (byte) ((iYear & 0xff00) >> 8),
                (byte) (iMonth & 0xff),
                (byte) (iDate & 0xff)
        };
    }

    private byte[] createCancelPairing() {
        return new byte[]{
                (byte) 0x06
        };
    }

    private byte[] createWattHourCommand() {
        Calendar cal = Calendar.getInstance();
        int iYear = cal.get(Calendar.YEAR);
        int iMonth = cal.get(Calendar.MONTH) + 1;
        int iDate = cal.get(Calendar.DATE);
        int iHour = cal.get(Calendar.HOUR_OF_DAY);
        int iMinute = cal.get(Calendar.MINUTE);

        mTid++;
        byte tid1 = (byte) (mTid & 0xff);
        byte tid2 = (byte) ((mTid & 0xff00) >> 8);
        return new byte[]{
                (byte) 0x10,
                (byte) 0x82,
                tid1, tid2,
                (byte) 0x11,
                (byte) (iHour & 0xff),
                (byte) (iMinute & 0xff),
                (byte) (iYear & 0xff), (byte) ((iYear & 0xff00) >> 8),
                (byte) (iMonth & 0xff),
                (byte) (iDate & 0xff)
        };
    }

    private byte[] createTemperatureCommand() {
        mTid++;
        byte tid1 = (byte) (mTid & 0xff);
        byte tid2 = (byte) ((mTid & 0xff00) >> 8);
        return new byte[]{
                (byte) 0x10,
                (byte) 0x81,
                tid1, tid2,
                (byte) 0x0E, (byte) 0xF0, (byte) 0x00,
                (byte) 0x00, (byte) 0x11, (byte) 0x00,
                (byte) 0x62,
                (byte) 0x01,
                (byte) 0xE0,
                (byte) 0x00
        };
    }

    private byte[] createHumidityCommand() {
        mTid++;
        byte tid1 = (byte) (mTid & 0xff);
        byte tid2 = (byte) ((mTid & 0xff00) >> 8);
        return new byte[]{
                (byte) 0x10,
                (byte) 0x81,
                tid1, tid2,
                (byte) 0x0E, (byte) 0xF0, (byte) 0x00,
                (byte) 0x00, (byte) 0x12, (byte) 0x00,
                (byte) 0x62,
                (byte) 0x01,
                (byte) 0xE0,
                (byte) 0x00
        };
    }

    private byte[] createIlluminanceCommand() {
        mTid++;
        byte tid1 = (byte) (mTid & 0xff);
        byte tid2 = (byte) ((mTid & 0xff00) >> 8);
        return new byte[]{
                (byte) 0x10,
                (byte) 0x81,
                tid1, tid2,
                (byte) 0x0E, (byte) 0xF0, (byte) 0x00,
                (byte) 0x00, (byte) 0x0D, (byte) 0x00,
                (byte) 0x62,
                (byte) 0x01,
                (byte) 0xE0,
                (byte) 0x00
        };
    }

    private byte[] createRealtimeWattCommand() {
        mTid++;
        byte tid1 = (byte) (mTid & 0xff);
        byte tid2 = (byte) ((mTid & 0xff00) >> 8);
        return new byte[]{
                (byte) 0x10,
                (byte) 0x81,
                tid1, tid2,
                (byte) 0x0E, (byte) 0xF0, (byte) 0x00,
                (byte) 0x00, (byte) 0x22, (byte) 0x00,
                (byte) 0x62,
                (byte) 0x01,
                (byte) 0xE2,
                (byte) 0x00
        };
    }

    private byte[] createPastWattHourCommand(Calendar date) {
        int iYear = date.get(Calendar.YEAR);
        int iMonth = date.get(Calendar.MONTH) + 1;
        int iDate = date.get(Calendar.DATE);
        int iHour = date.get(Calendar.HOUR_OF_DAY);
        int iMinute = date.get(Calendar.MINUTE);

        mTid++;
        byte tid1 = (byte) (mTid & 0xff);
        byte tid2 = (byte) ((mTid & 0xff00) >> 8);
        return new byte[]{
                (byte) 0x10,
                (byte) 0x82,
                tid1, tid2,
                (byte) 0x16,
                (byte) (iHour & 0xff),
                (byte) (iMinute & 0xff),
                (byte) (iYear & 0xff), (byte) ((iYear & 0xff00) >> 8),
                (byte) (iMonth & 0xff),
                (byte) (iDate & 0xff)
        };
    }

    private byte[] createPastValuesCommand(Calendar date) {
        int iYear = date.get(Calendar.YEAR);
        int iMonth = date.get(Calendar.MONTH) + 1;
        int iDate = date.get(Calendar.DATE);
        int iHour = date.get(Calendar.HOUR_OF_DAY);
        int iMinute = date.get(Calendar.MINUTE);

        mTid++;
        byte tid1 = (byte) (mTid & 0xff);
        byte tid2 = (byte) ((mTid & 0xff00) >> 8);
        return new byte[]{
                (byte) 0x10,
                (byte) 0x82,
                tid1, tid2,
                (byte) 0x17,
                (byte) (iHour & 0xff),
                (byte) (iMinute & 0xff),
                (byte) (iYear & 0xff), (byte) ((iYear & 0xff00) >> 8),
                (byte) (iMonth & 0xff),
                (byte) (iDate & 0xff)
        };
    }

    private byte[] createSetDateCommand(Calendar date) {
        int iYear = date.get(Calendar.YEAR);
        int iMonth = date.get(Calendar.MONTH) + 1;
        int iDate = date.get(Calendar.DATE);
        int iHour = date.get(Calendar.HOUR_OF_DAY);
        int iMinute = date.get(Calendar.MINUTE);
        return new byte[]{
                (byte) 0x07,
                (byte) (iHour & 0xff),
                (byte) (iMinute & 0xff),
                (byte) (iYear & 0xff), (byte) ((iYear & 0xff00) >> 8),
                (byte) (iMonth & 0xff),
                (byte) (iDate & 0xff)
        };
    }

    private byte[] createLEDControlCommand(boolean on) {
        byte type = on ? (byte) 0x01 : (byte) 0x00;
        return new byte[]{(byte) 0x05, type};
    }

    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String s = String.format("%02x", b);
            sb.append(s).append(" ");
        }
        return sb.toString();
    }

}
