package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

import java.util.List;

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
        } else if ("recorder_settings_mjpeg".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_main_to_mjpeg);
        } else if ("recorder_settings_rtsp".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_main_to_rtsp);
        } else if ("recorder_settings_srt".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_main_to_srt);
        } else if ("recorder_settings_broadcast".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_main_to_broadcast);
        } else {
            Bundle params = new Bundle();
            params.putString("setting_name", preference.getKey());

            HostMediaRecorder recorder = getRecorder();
            HostMediaRecorder.Settings settings = recorder.getSettings();
            HostMediaRecorder.StreamingSettings s = settings.getPreviewServer(preference.getKey());
            if (s != null) {
                String mimeType = s.getMimeType();
                if ("video/x-mjpeg".equalsIgnoreCase(mimeType)) {
                    findNavController(this).navigate(R.id.action_main_to_mjpeg, params);
                } else if ("video/x-rtp".equalsIgnoreCase(mimeType)) {
                    findNavController(this).navigate(R.id.action_main_to_rtsp, params);
                } else if ("video/MP2T".equalsIgnoreCase(mimeType)) {
                    findNavController(this).navigate(R.id.action_main_to_srt, params);
                }
            } else {
                s = settings.getBroadcaster(preference.getKey());
                if (s != null) {
                    findNavController(this).navigate(R.id.action_main_to_broadcast, params);
                }
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onBindService() {
        addPreviewServerList();
        addBroadcasterList();
    }

    private boolean isNotExistPreference(String name) {
        return findPreference(name) == null;
    }

    private void addPreviewServerList() {
        HostMediaRecorder recorder = getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();
        List<String> previewServerList = settings.getPreviewServerList();

        PreferenceCategory preferenceCategory = findPreference("recorder_settings_preview_server");
        for (String name : previewServerList) {
            HostMediaRecorder.StreamingSettings s = settings.getPreviewServer(name);
            if (isNotExistPreference(name)) {
                Preference preference = new Preference(requireContext());
                preference.setTitle(s.getMimeType());
                preference.setKey(name);
                preference.setIconSpaceReserved(false);
                preferenceCategory.addPreference(preference);
            }
        }
    }

    private void addBroadcasterList() {
        HostMediaRecorder recorder = getRecorder();
        HostMediaRecorder.Settings settings = recorder.getSettings();
        List<String> broadcasterList = settings.getBroadcasterList();

        PreferenceCategory preferenceCategory = findPreference("recorder_settings_broadcaster");
        for (String name : broadcasterList) {
            HostMediaRecorder.StreamingSettings s = settings.getBroadcaster(name);
            if (isNotExistPreference(name)) {
                Preference preference = new Preference(requireContext());
                preference.setTitle(s.getMimeType());
                preference.setKey(name);
                preference.setIconSpaceReserved(false);
                preferenceCategory.addPreference(preference);
            }
        }
    }

    private void startManager() {
        SettingsActivity a = (SettingsActivity) getActivity();
        if (a != null && !a.isManagerStarted()) {
            a.startManager();
        }
    }
}
