package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;

import androidx.preference.EditTextPreference;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.util.NetworkUtil;

public class SettingsRTSPFragment extends SettingsEncoderFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getEncoderId());
        setPreferencesFromResource(R.xml.settings_host_recorder_rtsp, rootKey);
    }

    @Override
    protected String getServerUrl(int port) {
        String ipAddress = NetworkUtil.getIPAddress(requireContext());
        return "rtsp://" + ipAddress + ":" + port;
    }
}
