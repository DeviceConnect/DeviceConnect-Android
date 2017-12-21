/*
 RawDataParseUtils
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.util;



import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;
import org.deviceconnect.android.deviceplugin.heartrate.data.health.HeartData;
import org.deviceconnect.android.deviceplugin.heartrate.data.health.TargetDeviceData;
import org.deviceconnect.utils.RFC3339DateUtils;

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
    private RawDataParseUtils() {
    }


    /**
     * Parse HeartRate data.
     * @param raw raw data
     * @return HeartRate object
     */
    public static HeartData parseHeartRate(final float raw) {
        return parseHeartRate(raw, HeartData.HeartRateType.Rate, "heart rate",
                                                    147842, "beat per min",  264864);
    }

    /**
     * Parse RRI data.
     * @param raw raw data
     * @return RRI object
     */
    public static HeartData parseRRI(final float raw) {
        return parseHeartRate(raw, HeartData.HeartRateType.RRI, "RR interval",
                147240, "ms",  264338);
    }

    /**
     * Parse EnergyExpended data.
     * @param raw raw data
     * @return EnergyExpended object
     */
    public static HeartData parseEnergyExpended(final float raw) {
        return parseHeartRate(raw, HeartData.HeartRateType.EnergyExpended, "energy expended",
                119, "Calories",  6784);
    }


    /**
     * Parse heartrate Device info data.
     * @param hearRateDevice heartrate device
     * @param batteryLevel battery level
     * @return heartrate device info object
     */
    public static TargetDeviceData parseDeviceData(final HeartRateDevice hearRateDevice, final float batteryLevel) {
        TargetDeviceData device = new TargetDeviceData();
        device.setProductName(hearRateDevice.getName());

        if (batteryLevel > -1.0) {
            device.setBatteryLevel(batteryLevel);
        }
        return device;
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
    private static HeartData parseHeartRate(final float raw, final HeartData.HeartRateType heartRateType,
                                            final String type, final int typeCode,
                                            final String unit, final int unitCode) {
        HeartData heart = new HeartData();
        heart.setHeartRateType(heartRateType);
        heart.setValue(raw);
        heart.setMderFloat(MDERFloatConvreterUtils.convertMDERFloatToFloat(raw));
        heart.setType(type);
        heart.setTypeCode(typeCode);
        heart.setUnit(unit);
        heart.setUnitCode(unitCode);
        long timestamp = System.currentTimeMillis();
        heart.setTimeStamp(timestamp);
        heart.setTimeStampString(RFC3339DateUtils.toString(timestamp));
        return heart;

    }

}
