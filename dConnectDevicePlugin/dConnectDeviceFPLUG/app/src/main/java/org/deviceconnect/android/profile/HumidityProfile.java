/*
 HumidityProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.profile;

import android.content.Intent;

/**
 * Humidity profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HumidityProfile extends DConnectProfile implements HumidityProfileConstants {

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    public static void setHumidity(Intent response, double humidity) {
        if (humidity < 0 || humidity > 1) {
            throw new IllegalArgumentException("humidity out of range");
        }
        response.putExtra(PARAM_HUMIDITY, humidity);
    }

}
