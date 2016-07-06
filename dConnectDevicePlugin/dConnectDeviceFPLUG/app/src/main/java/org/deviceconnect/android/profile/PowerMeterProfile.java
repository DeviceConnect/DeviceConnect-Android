/*
 PowerMeterProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.profile;

/**
 * PowerMeter Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class PowerMeterProfile extends DConnectProfile implements PowerMeterProfileConstants {

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

}
