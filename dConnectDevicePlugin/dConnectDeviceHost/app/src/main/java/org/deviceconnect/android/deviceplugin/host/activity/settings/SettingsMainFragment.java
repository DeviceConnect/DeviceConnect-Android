package org.deviceconnect.android.deviceplugin.host.activity.settings;

import android.os.Bundle;

import androidx.preference.Preference;

import org.deviceconnect.android.deviceplugin.host.R;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class SettingsMainFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_host_recorder_main, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        startManager();
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        if ("recorder_settings_video".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_main_to_video);
        } else if ("recorder_settings_audio".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_main_to_audio);
        } else if ("recorder_settings_srt".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_main_to_srt);
        } else if ("recorder_settings_broadcast".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_main_to_broadcast);
        } else if ("recorder_settings_port".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_main_to_port);
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void startManager() {
        SettingsActivity a = (SettingsActivity) getActivity();
        if (a != null && !a.isManagerStarted()) {
            a.startManager();
        }
    }
}
