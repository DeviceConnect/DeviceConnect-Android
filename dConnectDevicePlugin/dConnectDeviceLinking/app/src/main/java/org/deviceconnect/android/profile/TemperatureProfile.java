/*
 TemperatureProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;

public class TemperatureProfile extends DConnectProfile implements TemperatureProfileConstants {
    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(Intent request, Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (attribute == null) {
            result = onGetTemperature(request, response, getServiceID(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    protected boolean onGetTemperature(Intent request, Intent response, String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    public static void setTemperature(Intent response, float temperature) {
        response.putExtra(PARAM_TEMPERATURE, temperature);
    }

    public static void setTimeStamp(Intent response, long timeStamp) {
        response.putExtra(PARAM_TIME_STAMP, timeStamp);
        response.putExtra(PARAM_TIME_STAMP_STRING, Util.timeStampToText(timeStamp));
    }

    public static void setTemperatureType(Intent response, TemperatureType type) {
        response.putExtra(PARAM_TYPE, type.getValue());
    }
}
