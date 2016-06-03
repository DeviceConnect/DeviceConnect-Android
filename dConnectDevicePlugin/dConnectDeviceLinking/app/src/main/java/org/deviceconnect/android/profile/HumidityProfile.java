package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class HumidityProfile extends DConnectProfile implements HumidityProfileConstants {

    private static final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy/MM/ddHHMMSS.sss", Locale.JAPAN);

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(Intent request, Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (attribute == null) {
            result = onGetHumidity(request, response, getServiceID(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    protected boolean onGetHumidity(Intent request, Intent response, String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    public static void setHumidity(Intent response, float humidity) {
        response.putExtra(PARAM_HUMIDITY, humidity);
    }

    public static void setTimeStamp(Intent response, long timeStamp) {
        response.putExtra(PARAM_TIME_STAMP, timeStamp);
        response.putExtra(PARAM_TIME_STAMP_STRING, Util.timeStampToText(timeStamp));
    }
}
