/*
 TemperatureProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

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
        response.putExtra(PARAM_TIME_STAMP_STRING, Util.timeStampToText(timeStamp));
    }

    public static void setTemperatureType(final Intent response, final TemperatureType type) {
        response.putExtra(PARAM_TYPE, type.getValue());
    }
}
