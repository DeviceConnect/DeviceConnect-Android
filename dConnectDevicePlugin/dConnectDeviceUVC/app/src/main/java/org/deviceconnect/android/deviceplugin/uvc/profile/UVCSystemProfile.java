/*
 UVCSystemProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.uvc.activity.UVCSettingsActivity;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * UVC System Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCSystemProfile extends SystemProfile {
    @Override
    protected Class<? extends Activity> getSettingPageActivity(Intent request, Bundle param) {
        return UVCSettingsActivity.class;
    }
}
