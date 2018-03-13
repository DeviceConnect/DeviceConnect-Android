package org.deviceconnect.android.deviceplugin.ruleengine.profiles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.profile.SystemProfile;


public class RuleEngineSystemProfile extends SystemProfile {
    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return null;
    }
}