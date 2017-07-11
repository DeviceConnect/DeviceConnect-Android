package org.deviceconnect.android.deviceplugin.hogp.profiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.hogp.activity.HOGPSettingActivity;
import org.deviceconnect.android.profile.SystemProfile;


public class HOGPSystemProfile extends SystemProfile {
    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        // TODO 設定画面が不要な場合、null を返却してください.
        return HOGPSettingActivity.class;
    }
}