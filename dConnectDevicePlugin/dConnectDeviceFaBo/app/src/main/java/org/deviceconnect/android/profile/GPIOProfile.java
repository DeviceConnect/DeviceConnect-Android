package org.deviceconnect.android.profile;


import android.content.Intent;

import org.deviceconnect.profile.GPIOProfileConstants;

public class GPIOProfile extends DConnectProfile implements GPIOProfileConstants {

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    public static void setValue(final Intent message, final int value) {
        message.putExtra(PARAM_VALUE, value);
    }

}
