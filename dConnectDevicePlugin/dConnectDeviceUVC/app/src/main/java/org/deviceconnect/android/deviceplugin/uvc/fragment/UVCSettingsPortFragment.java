package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.uvc.R;

public class UVCSettingsPortFragment extends UVCSettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getSettingsName());
        setPreferencesFromResource(R.xml.settings_uvc_recorder_port, rootKey);
    }

    @Override
    public void onBindService() {
        super.onBindService();

        setInputTypeNumber("mjpeg_port");
        setInputTypeNumber("rtsp_port");
        setInputTypeNumber("srt_port");
    }
}
