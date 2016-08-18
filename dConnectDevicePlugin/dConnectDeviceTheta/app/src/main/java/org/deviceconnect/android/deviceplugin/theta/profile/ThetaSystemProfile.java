/*
 ThetaSystemProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.theta.activity.ThetaServiceListActivity;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * Theta System Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaSystemProfile extends SystemProfile {

    @Override
    protected Class<? extends Activity> getSettingPageActivity(Intent request, Bundle param) {
        return ThetaServiceListActivity.class;
    }

}
