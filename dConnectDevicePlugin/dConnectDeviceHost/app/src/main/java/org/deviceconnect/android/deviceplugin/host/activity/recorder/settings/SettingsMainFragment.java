package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.AlertDialogFragment;
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
        HostMediaRecorder recorder = getRecorder();
        if (recorder == null) {
            showErrorDialog();
            return false;
        }

        if ("recorder_settings_video".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_main_to_video);
        } else if ("recorder_settings_audio".equals(preference.getKey())) {
            findNavController(this).navigate(R.id.action_main_to_audio);
        } else {
            Bundle params = new Bundle();
            params.putString("encoder_id", preference.getKey());
            HostMediaRecorder.Settings settings = recorder.getSettings();
            HostMediaRecorder.EncoderSettings s = settings.getEncoderSetting(preference.getKey());
            if (s != null) {
                switch (s.getMimeType()) {
                    case MJPEG:
                        findNavController(this).navigate(R.id.action_main_to_mjpeg, params);
                        break;
                    case RTSP:
                        findNavController(this).navigate(R.id.action_main_to_rtsp, params);
                        break;
                    case SRT:
                        findNavController(this).navigate(R.id.action_main_to_srt, params);
                        break;
                    case RTMP:
                        findNavController(this).navigate(R.id.action_main_to_broadcast, params);
                        break;
                }
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onBindService() {
        addEncoderList();
    }

    private void showErrorDialog() {
        Bundle args = AlertDialogFragment.createParam("error-dialog",
                getString(R.string.host_recorder_settings_error_not_found_camera_title),
                getString(R.string.host_recorder_settings_error_not_found_camera_message),
                getString(R.string.host_recorder_settings_error_not_found_camera_btn),
                null);
        findNavController(this).navigate(R.id.action_open_error_dialog, args);
    }

    private boolean isNotExistPreference(String key) {
        return findPreference(key) == null;
    }

    private void addEncoderList() {
        HostMediaRecorder recorder = getRecorder();
        if (recorder == null) {
            return;
        }

        HostMediaRecorder.Settings settings = recorder.getSettings();
        List<String> encoderList = settings.getEncoderIdList();

        PreferenceCategory previewCategory = findPreference("recorder_settings_preview_server");
        PreferenceCategory broadcasterCategory = findPreference("recorder_settings_broadcaster");
        for (String encoderId : encoderList) {
            HostMediaRecorder.EncoderSettings encoderSetting = settings.getEncoderSetting(encoderId);
            if (isNotExistPreference(encoderId)) {
                Preference preference = new Preference(requireContext());
                preference.setTitle(encoderSetting.getName());
                preference.setKey(encoderId);
                preference.setIconSpaceReserved(false);
                if (encoderSetting.getMimeType() == HostMediaRecorder.MimeType.RTMP) {
                    broadcasterCategory.addPreference(preference);
                } else {
                    previewCategory.addPreference(preference);
                }
            }
        }

        previewCategory.setVisible(true);
        broadcasterCategory.setVisible(true);
    }

    private void startManager() {
        SettingsActivity a = (SettingsActivity) getActivity();
        if (a != null && !a.isManagerStarted()) {
            a.startManager();
        }
    }
}
