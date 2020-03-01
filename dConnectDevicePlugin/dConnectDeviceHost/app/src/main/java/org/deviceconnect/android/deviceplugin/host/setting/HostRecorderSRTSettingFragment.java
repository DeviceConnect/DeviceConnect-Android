package org.deviceconnect.android.deviceplugin.host.setting;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import org.deviceconnect.android.deviceplugin.host.R;

/**
 * SRTソケットのオプション設定を行うための Fragment.
 */
public class HostRecorderSRTSettingFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_host_preview_srt);
    }
}
