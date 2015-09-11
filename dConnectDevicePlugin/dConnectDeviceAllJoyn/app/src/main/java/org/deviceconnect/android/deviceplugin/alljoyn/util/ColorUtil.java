package org.deviceconnect.android.deviceplugin.alljoyn.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

/**
 * Color utility.
 *
 * @author NTT DOCOMO, INC.
 */
public class ColorUtil {
    private ColorUtil() {

    }

    /**
     * NOTE: Arithmetic operations in primitive types may lead to arithmetic overflow. To retain
     * precision, BigDecimal objects are used.
     *
     * @param rgb
     * @return
     */
    public static int[] convertRGB_8_8_8_To_HSB_32_32_32(int[] rgb) {
        int[] hsb = new int[3];
        int maxChroma = Math.max(Math.max(rgb[0], rgb[1]), rgb[2]);
        int minChroma = Math.min(Math.min(rgb[0], rgb[1]), rgb[2]);
        int diff = maxChroma - minChroma;

        // Hue
        BigDecimal hue;
        if (diff == 0) {
            hue = BigDecimal.ZERO;
        } else if (maxChroma == rgb[0]) {
            float tmp = (rgb[1] - rgb[2]) / (float) diff;
            if (tmp < 0) {
                tmp += 6 * Math.ceil(-tmp / 6.0);
            } else {
                tmp -= 6 * Math.floor(tmp / 6.0);
            }
            hue = BigDecimal.valueOf(tmp);
        } else if (maxChroma == rgb[1]) {
            hue = BigDecimal.valueOf((rgb[2] - rgb[0]) / (float) diff + 2);
        } else {
            hue = BigDecimal.valueOf((rgb[0] - rgb[1]) / (float) diff + 4);
        }
        // [0, 360] -> [0, 0xffffffff]
        hue = hue.multiply(BigDecimal.valueOf(0xffffffffL));
        hue = hue.divide(BigDecimal.valueOf(6), RoundingMode.FLOOR);
        hsb[0] = ByteBuffer.allocate(8).putLong(hue.longValue()).getInt(4);

        // Saturation
        if (maxChroma == 0) {
            hsb[1] = 0;
        } else {
            // [0, 1] -> [0, 0xffffffff]
            BigDecimal sat = BigDecimal.valueOf(diff);
            sat = sat.multiply(BigDecimal.valueOf(0xffffffffL));
            sat = sat.divide(BigDecimal.valueOf(maxChroma), RoundingMode.FLOOR);
            hsb[1] = ByteBuffer.allocate(8).putLong(sat.longValue()).getInt(4);
        }

        // Brightness
        // [0, 255] -> [0, 0xffffffff]
        BigDecimal brightness = BigDecimal.valueOf(maxChroma);
        brightness = brightness.multiply(BigDecimal.valueOf(0xffffffffL));
        brightness = brightness.divide(BigDecimal.valueOf(0xffL), RoundingMode.FLOOR);
        hsb[2] = ByteBuffer.allocate(8).putLong(brightness.longValue()).getInt(4);

        return hsb;
    }
}
