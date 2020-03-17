package org.deviceconnect.android.deviceplugin.host.setting;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.deviceconnect.android.deviceplugin.host.R;

/**
 * SRTソケットのオプション設定を行うための Fragment.
 */
public class HostRecorderSRTSettingFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_host_preview_srt);

        EditTextPreference inputBwPref = findPreference(getString(R.string.pref_key_settings_srt_inputbw));
        if (inputBwPref != null) {
            inputBwPref.setSummaryProvider(summaryOptionAuto);
        }
        EditTextPreference oHeadBwPref = findPreference(getString(R.string.pref_key_settings_srt_oheadbw));
        if (oHeadBwPref != null) {
            oHeadBwPref.setSummaryProvider(summaryOptionAuto);
        }
    }

    private final Preference.SummaryProvider<EditTextPreference> summaryOptionAuto = (preference) -> {
        String value = preference.getText();
        if (TextUtils.isEmpty(value)) {
            return getString(R.string.host_setting_srt_option_auto);
        }
        return value;
    };
}
