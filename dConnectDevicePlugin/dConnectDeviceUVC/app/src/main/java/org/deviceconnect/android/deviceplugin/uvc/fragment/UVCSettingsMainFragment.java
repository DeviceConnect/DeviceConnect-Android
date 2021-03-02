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
    public void onBindService() {
        super.onBindService();

        if (!isOnlineUVCService()) {
            // UVC が接続されていない場合には前の画面に戻ります
            popBackFragment();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        Bundle arguments = createArguments();
        if (arguments != null) {
            if ("recorder_settings_video".equals(preference.getKey())) {
                findNavController(this).navigate(R.id.action_main_to_video, arguments);
            } else if ("recorder_settings_srt".equals(preference.getKey())) {
                findNavController(this).navigate(R.id.action_main_to_srt, arguments);
            } else if ("recorder_settings_broadcast".equals(preference.getKey())) {
                findNavController(this).navigate(R.id.action_main_to_broadcast, arguments);
            } else if ("recorder_settings_port".equals(preference.getKey())) {
                findNavController(this).navigate(R.id.action_main_to_port, arguments);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private Bundle createArguments() {
        String serviceId = getServiceId();
        String recorderId = getRecorderId();
        String settingsName = getSettingsName();
        if (serviceId != null && recorderId != null && settingsName != null) {
            Bundle bundle = new Bundle();
            bundle.putString("service_id", serviceId);
            bundle.putString("recorder_id", recorderId);
            bundle.putString("settings_name", settingsName);
            return bundle;
        }
        return null;
    }
}