package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.R;

public class SettingsPortFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getRecorderId().replaceAll("/", "_"));
        setPreferencesFromResource(R.xml.settings_host_recorder_port, rootKey);
    }

    @Override
    public void onBindService() {
        setInputTypeNumber("mjpeg_port");
        setInputTypeNumber("rtsp_port");
        setInputTypeNumber("srt_port");
    }
}