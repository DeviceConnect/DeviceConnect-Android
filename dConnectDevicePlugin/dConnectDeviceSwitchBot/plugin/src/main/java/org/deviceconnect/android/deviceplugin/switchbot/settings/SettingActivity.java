package org.deviceconnect.android.deviceplugin.switchbot.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.deviceconnect.android.deviceplugin.switchbot.R;


public class SettingActivity extends PreferenceActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.switchbot_setting);

        // TODO デバイスとの接続等で手動操作が必要な場合は、設定画面を実装してください.
        // TODO 不要な場合は削除してください.
    }

}