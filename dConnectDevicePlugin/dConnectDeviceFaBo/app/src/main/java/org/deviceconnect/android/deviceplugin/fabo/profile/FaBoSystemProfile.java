/*
FaBoSystemProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fabo.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fabo.setting.FaBoServiceListActivity;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * FaBoデバイスプラグイン, System プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class FaBoSystemProfile extends SystemProfile {

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return FaBoServiceListActivity.class;
    }

}