/*
 SlackMessageHookSystemProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.deviceconnect.android.deviceplugin.slackmessagehook.setting.SlackMessageHookSettingActivity;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * デバイスプラグイン, System プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SlackMessageHookSystemProfile extends SystemProfile {

    @Override
    protected Class<? extends Activity> getSettingPageActivity(Intent request, Bundle param) {
        return SlackMessageHookSettingActivity.class;
    }

}