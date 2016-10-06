/*
 TemperatureProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.profile;

import android.content.Intent;

/**
 * Temperature profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class TemperatureProfile extends DConnectProfile implements TemperatureProfileConstants {

    /**
     * Type of temperature.
     */
    public enum TemperatureType {
        Celsius(1),
        CelsiusFahrenheit(2);

        /**
         * type.
         */
        private int mValue;

        /**
         * Generate enum as specified value.
         *
         * @param value type
         */
        TemperatureType(final int value) {
            mValue = value;
        }

        /**
         * Get type
         *
         * @return type
         */
        public int getValue() {
            return mValue;
        }

    }

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    public static void setTemperature(Intent response, double temperature) {
        response.putExtra(PARAM_TEMPERATURE, temperature);
    }

    public static void setType(Intent response, int type) {
        response.putExtra(PARAM_TYPE, type);
    }
}
