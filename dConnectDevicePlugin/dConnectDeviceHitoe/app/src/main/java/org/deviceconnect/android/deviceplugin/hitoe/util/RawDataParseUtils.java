/*
 RawDataParseUtils
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.util;

import org.deviceconnect.android.deviceplugin.hitoe.ble.HitoeConstants;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.TargetDeviceData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A class containing utility methods parse raw data.
 * @author NTT DOCOMO, INC.
 */
public final class RawDataParseUtils {

    /**
     * Private Constructor.
     */
    private RawDataParseUtils() {}


    /**
     * Parse HeartRate data.
     * @param raw raw data
     * @return HeartRate object
     */
    public static HeartData parseHeartRate(final String raw) {
        return parseHeartRate(raw, HeartData.HeartRateType.Rate, "heart rate",
                                                    147842, "beat per min",  264864);
    }

    /**
     * Parse RRI data.
     * @param raw raw data
     * @return RRI object
     */
    public static HeartData parseRRI(final String raw) {
        return parseHeartRate(raw, HeartData.HeartRateType.RRI, "RR interval",
                147240, "ms",  264338);
    }

    /**
     * Parse EnergyExpended data.
     * @param raw raw data
     * @return EnergyExpended object
     */
    public static HeartData parseEnergyExpended(final String raw) {
        return parseHeartRate(raw, HeartData.HeartRateType.EnergyExpended, "energy expended",
                119, "Calories",  6784);
    }


    public static TargetDeviceData parseDeviceData(final HitoeDevice hitoe, final float batteryLevel) {
        TargetDeviceData device = new TargetDeviceData();
        device.setProductName(hitoe.getName());
        if (batteryLevel > -1.0) {
            device.setBatteryLevel(batteryLevel);
        }
        return device;
    }


    /**
     * Parse ECG Data.
     * @param raw raw data
     * @return ECG data object
     */
    public static HeartData parseECG(final String raw) {
        String[] lineList=raw.split(HitoeConstants.BR);
        HeartData heart = new HeartData();
        for(int i=0; i < lineList.length; i++) {
            String val = lineList[i];
            if(val == null) {
                continue;
            }
            String[] list = val.split(HitoeConstants.COMMA, -1);
            long timestamp = Long.parseLong(list[0]);
            String[] ecgList = list[1].split(HitoeConstants.COLON, -1);
            String date = nowTimeStampString(timestamp);
            heart.setValue(Float.parseFloat(ecgList[0]));
            heart.setTimeStamp(timestamp);
            heart.setTimeStampString(date);
        }
        heart.setHeartRateType(HeartData.HeartRateType.ECG);
        heart.setMderFloat(MDERFloatConvreterUtils.convertMDERFloatToFloat(heart.getValue()));
        heart.setType("ecg beat");
        heart.setTypeCode(663568);
        heart.setUnit("mVolt * miliSecond");
        heart.setUnitCode(3328);

        return heart;

    }


    /**
     * Parse HeartRate data.
     * @param raw raw data
     * @param heartRateType HeartRate type
     * @param type type
     * @param typeCode type code
     * @param unit unit
     * @param unitCode unit Code
     * @return HeartRate data
     */
    private static HeartData parseHeartRate(final String raw, final HeartData.HeartRateType heartRateType,
                                            final String type, final int typeCode,
                                            final String unit, final int unitCode) {
        HeartData heart = new HeartData();
        String[] lineList=raw.split(HitoeConstants.BR);
        String rateString = lineList[lineList.length - 1];
        heart.setHeartRateType(heartRateType);
        if (rateString == null) {
            return null;
        }
        String[] hrValue = splitComma(rateString);
        float rate = Float.parseFloat(hrValue[1]);
        heart.setValue(rate);
        heart.setMderFloat(MDERFloatConvreterUtils.convertMDERFloatToFloat(rate));
        heart.setType(type);
        heart.setTypeCode(typeCode);
        heart.setUnit(unit);
        heart.setUnitCode(unitCode);
        heart.setTimeStamp(Long.parseLong(hrValue[0]));
        heart.setTimeStampString(nowTimeStampString(Long.parseLong(hrValue[0])));
        return heart;

    }

    /**
     * Split Comma.
     * @param val split value
     * @return string array
     */
    private static String[] splitComma(final String val) {
        return val.split(",", -1);
    }

    /**
     * Now TimeStamp String.
     * @param now now timestamp
     * @return timestamp string
     */
    private static String nowTimeStampString(final long now) {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmdss.SSSZZZ");
        df.setTimeZone(TimeZone.getDefault());
        return df.format(new Date(System.currentTimeMillis()));
    }
}
