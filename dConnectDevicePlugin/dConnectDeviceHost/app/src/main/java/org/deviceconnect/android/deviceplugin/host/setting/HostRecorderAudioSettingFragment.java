package org.deviceconnect.android.deviceplugin.host.setting;

import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.R;

import androidx.preference.PreferenceFragmentCompat;

/**
 * プレビューの音声設定を行うための Fragment.
 */
public class HostRecorderAudioSettingFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_host_preview_audio);
    }
}
