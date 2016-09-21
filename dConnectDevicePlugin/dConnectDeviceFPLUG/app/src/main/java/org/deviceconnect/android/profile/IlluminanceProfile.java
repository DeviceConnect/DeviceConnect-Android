/*
 IlluminanceProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.profile;

import android.content.Intent;

/**
 * Illuminance profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class IlluminanceProfile extends DConnectProfile implements IlluminanceProfileConstants {

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    public static void setIlluminance(Intent response, double illuminance) {
        response.putExtra(PARAM_ILLUMINANCE, illuminance);
    }

}
