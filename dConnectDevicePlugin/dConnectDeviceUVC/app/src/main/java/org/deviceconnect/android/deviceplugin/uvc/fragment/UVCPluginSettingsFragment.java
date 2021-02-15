package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceService;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class UVCPluginSettingsFragment extends UVCDevicePluginBindPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_uvc_plugin, rootKey);
    }

    @Override
    public void onBindService() {
        UVCDeviceService deviceService = getUVCDeviceService();
        if (deviceService != null) {
            setAuth(deviceService);
        }
        setTitle(getString(R.string.uvc_settings_uvc_title));
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        if ("uvc_settings_service_list".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_plugin_to_service);
        } else if ("uvc_settings_service_instruction".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_plugin_to_instruction);
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void setAuth(UVCDeviceService deviceService) {
        SwitchPreferenceCompat pref = findPreference("uvc_settings_auth");
        if (pref != null) {
            pref.setChecked(deviceService.isUseLocalOAuth());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                deviceService.setUseLocalOAuth((Boolean) newValue);
                return true;
            });
        }
    }
}