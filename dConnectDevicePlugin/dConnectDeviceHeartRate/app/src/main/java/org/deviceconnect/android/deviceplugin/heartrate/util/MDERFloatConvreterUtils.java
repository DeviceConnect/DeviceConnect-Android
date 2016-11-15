/*
 MDERFloatConvreterUtils
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * A class containing utility methods convert MDER Float.
 * @author NTT DOCOMO, INC.
 */
public final class MDERFloatConvreterUtils {

    /**
     * Private Constructor.
     */
    private MDERFloatConvreterUtils() {
    }

    /**
     * The Plug-Ins and the APIs designed for consumer/ personal use perspective.
     * The following requirements specify the guidelines for all Health Device Plug-Ins.
     * Values, when reported, are reported as Strings or MDER FLOATs.
     * MDER FLOATs are used to report integers or real numbers.
     * The reason for using MDER FLOATs is to capture precision as reported by the device.
     * An MDER FLOAT is a 32 bit integer interpreted as follows:
     * @param target target float value
     * @return mder float value
     */
    public static String convertMDERFloatToFloat(final float target) {
        DecimalFormat df = new DecimalFormat("##########.##########");
        BigDecimal value = new BigDecimal(target);
        int exponent = getExponent(df.format(target));
        int mantissa = value.scaleByPowerOfTen(exponent * -1).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        return String.format("%02X%06X",(exponent & 0xFF), (mantissa & 0xFFFFFF));
    }

    /**
     * Get Exponent.
     * @param value target value
     * @return exponent
     */
    private static int getExponent(final String value) {
        int index = value.indexOf(".");
        if (index != -1) {
            return ((value.length() -1) - index) * -1;
        } else {
            return countZero(value);
        }
    }

    /**
     * Count Zero.
     * @param c target
     * @return Count
     */
    private static int countZero(final String c) {
        int count = 0;
        if (c.charAt(c.length() - 1) != '0') {
            return 0;
        }
        for (int i = 0; i < c.length(); i++) {
            if (c.charAt(i) == '0') {
                count++;
            }
        }
        return count;
    }
}
