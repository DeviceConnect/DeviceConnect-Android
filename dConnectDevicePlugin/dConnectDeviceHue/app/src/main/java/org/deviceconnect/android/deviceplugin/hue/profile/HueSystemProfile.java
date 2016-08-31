/*
HueSystemProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.hue.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.hue.activity.HueServiceListActivity;
import org.deviceconnect.android.profile.SystemProfile;


/**
 * Hueデバイスプラグイン, System プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class HueSystemProfile extends SystemProfile {

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return HueServiceListActivity.class;
    }

}
