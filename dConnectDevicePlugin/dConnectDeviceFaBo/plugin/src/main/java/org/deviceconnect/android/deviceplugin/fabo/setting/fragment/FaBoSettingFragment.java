package org.deviceconnect.android.deviceplugin.fabo.setting.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.deviceconnect.android.deviceplugin.fabo.FaBoArduinoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.R;

/**
 * FaBoの設定画面用のフラグメント.
 */
public class FaBoSettingFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_fabo);

        CheckBoxPreference oauthCheckBox = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(getString(R.string.key_settings_security_local_oauth));
        oauthCheckBox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                final String key = preference.getKey();
                if (preference instanceof CheckBoxPreference) {
                    if (getString(R.string.key_settings_security_local_oauth).equals(key)) {
                        notifyChangeLocalOAuth();
                    }
                }
                return true;
            }
        });
    }

    /**
     * Local OAuthの設定が切り替わったことを通知します.
     */
    private void notifyChangeLocalOAuth() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), FaBoArduinoDeviceService.class);
        intent.setAction(FaBoArduinoDeviceService.ACTION_SET_LOCAL_OAUTH);
        getActivity().startService(intent);
    }
}
