package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.os.Bundle;

import androidx.preference.Preference;

import org.deviceconnect.android.deviceplugin.uvc.R;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class UVCSettingsMainFragment extends UVCSettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_uvc_recorder_main, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        Bundle args = getArguments();
        if (args != null) {
            Bundle bundle = new Bundle();
            bundle.putString("service_id", getServiceId());
            bundle.putString("recorder_id", getRecorderId());
            bundle.putString("settings_name", getSettingsName());

            if ("recorder_settings_video".equals(preference.getKey())) {
                findNavController(this).navigate(R.id.action_main_to_video, bundle);
            } else if ("recorder_settings_srt".equals(preference.getKey())) {
                findNavController(this).navigate(R.id.action_main_to_srt, bundle);
            } else if ("recorder_settings_broadcast".equals(preference.getKey())) {
                findNavController(this).navigate(R.id.action_main_to_broadcast, bundle);
            } else if ("recorder_settings_port".equals(preference.getKey())) {
                findNavController(this).navigate(R.id.action_main_to_port, bundle);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }
}