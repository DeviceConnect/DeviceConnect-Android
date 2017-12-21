/*
 AtmosphericPressureProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.utils.RFC3339DateUtils;

public class AtmosphericPressureProfile extends DConnectProfile implements AtmosphericPressureProfileConstants {
    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    public static void setAtmosphericPressure(final Intent response, final float atmosphericPressure) {
        response.putExtra(PARAM_ATMOSPHERIC_PRESSURE, atmosphericPressure);
    }

    public static void setTimeStamp(final Intent response, final long timeStamp) {
        response.putExtra(PARAM_TIME_STAMP, timeStamp);
        response.putExtra(PARAM_TIME_STAMP_STRING, RFC3339DateUtils.toString(timeStamp));
    }
}
