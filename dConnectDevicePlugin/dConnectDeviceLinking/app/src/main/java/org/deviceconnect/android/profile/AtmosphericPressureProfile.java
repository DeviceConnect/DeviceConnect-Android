/*
 AtmosphericPressureProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;

public class AtmosphericPressureProfile extends DConnectProfile implements AtmosphericPressureProfileConstants {
    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }
    @Override
    protected boolean onGetRequest(Intent request, Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (attribute == null) {
            result = onGetAtmosphericPressure(request, response, getServiceID(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    protected boolean onGetAtmosphericPressure(Intent request, Intent response, String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    public static void setAtmosphericPressure(Intent response, float humidity) {
        response.putExtra(PARAM_ATMOSPHERIC_PRESSURE, humidity);
    }

    public static void setTimeStamp(Intent response, long timeStamp) {
        response.putExtra(PARAM_TIME_STAMP, timeStamp);
        response.putExtra(PARAM_TIME_STAMP_STRING, Util.timeStampToText(timeStamp));
    }
}
