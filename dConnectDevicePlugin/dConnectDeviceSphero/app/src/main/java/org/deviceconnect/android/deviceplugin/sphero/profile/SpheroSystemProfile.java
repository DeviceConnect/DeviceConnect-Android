/*
 SpheroSystemProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.sphero.setting.SpheroServiceListActivity;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * System プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SpheroSystemProfile extends SystemProfile {

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return SpheroServiceListActivity.class;
    }
}
