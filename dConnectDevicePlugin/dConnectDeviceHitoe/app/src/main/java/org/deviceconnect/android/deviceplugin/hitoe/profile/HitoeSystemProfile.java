/*
 HitoeSystemProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.hitoe.activity.HitoeDeviceListActivity;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * Implement SystemProfile.
 * @author NTT DOCOMO, INC.
 */
public class HitoeSystemProfile extends SystemProfile {
    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return HitoeDeviceListActivity.class;
    }
}
