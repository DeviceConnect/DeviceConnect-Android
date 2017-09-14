/*
 SmartMeterSystemProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter.profiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.smartmeter.setting.SmartMeterSettingActivity;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * SmartMeter System Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class SmartMeterSystemProfile extends SystemProfile {
    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return SmartMeterSettingActivity.class;
    }
}
