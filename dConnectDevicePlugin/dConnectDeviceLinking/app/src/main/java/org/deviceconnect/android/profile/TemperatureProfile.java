/*
 TemperatureProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.utils.RFC3339DateUtils;

public class TemperatureProfile extends DConnectProfile implements TemperatureProfileConstants {
    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    public static void setTemperature(final Intent response, final float temperature) {
        response.putExtra(PARAM_TEMPERATURE, temperature);
    }

    public static void setTimeStamp(final Intent response, final long timeStamp) {
        response.putExtra(PARAM_TIME_STAMP, timeStamp);
        response.putExtra(PARAM_TIME_STAMP_STRING, RFC3339DateUtils.toString(timeStamp));
    }

    public static void setTemperatureType(final Intent response, final TemperatureType type) {
        response.putExtra(PARAM_TYPE, type.getValue());
    }
    // Convert Celsius to Fahrenheit.
    public static float convertCelsiusToFahrenheit(final float celsius) {
        return (float) (1.8 * celsius + 32);
    }

    // Convert Fahrenheit to Celsius.
    public static float convertFahrenheitToCelsius(final float fahrenheit) {
        return (float) ((0.56) * (fahrenheit - 32));
    }
    public static int getType(final Intent request) {
        String typeString = request.getStringExtra("type");
        int type;
        try {
            type = Integer.valueOf(typeString);
        } catch(NumberFormatException e) {
            type = TemperatureType.TYPE_CELSIUS.getValue();
        }
        return type;
    }
}
