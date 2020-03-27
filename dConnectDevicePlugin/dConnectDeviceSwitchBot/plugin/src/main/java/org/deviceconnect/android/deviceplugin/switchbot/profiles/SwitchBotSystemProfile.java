/*
 SwitchBotSystemProfile.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.switchbot.profiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.switchbot.settings.SettingActivity;
import org.deviceconnect.android.profile.SystemProfile;

public class SwitchBotSystemProfile extends SystemProfile {
    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        // TODO 設定画面が不要な場合、null を返却してください.
        return SettingActivity.class;
    }
}