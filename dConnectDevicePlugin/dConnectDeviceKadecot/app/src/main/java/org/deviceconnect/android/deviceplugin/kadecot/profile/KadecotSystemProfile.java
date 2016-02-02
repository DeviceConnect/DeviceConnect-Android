/*
 KadecotSystemProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.kadecot.activity.KadecotDeviceSettingsActivity;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * Kadecot System Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotSystemProfile extends SystemProfile {

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return KadecotDeviceSettingsActivity.class;
    }

}
