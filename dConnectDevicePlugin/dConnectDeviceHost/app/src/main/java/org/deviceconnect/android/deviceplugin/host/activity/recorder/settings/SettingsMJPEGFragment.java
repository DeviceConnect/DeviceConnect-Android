package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.os.Bundle;

import androidx.preference.EditTextPreference;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.SeekBarDialogPreference;
import org.deviceconnect.android.deviceplugin.host.util.NetworkUtil;

public class SettingsMJPEGFragment extends SettingsEncoderFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getEncoderId());
        setPreferencesFromResource(R.xml.settings_host_recorder_mjpeg, rootKey);
    }

    @Override
    public void onBindService() {
        super.onBindService();
        setPreviewJpegQuality();
    }

    @Override
    protected String getServerUrl(int port) {
        String ipAddress = NetworkUtil.getIPAddress(requireContext());
        return "http://" + ipAddress + ":" + port + "/mjpeg";
    }

    /**
     * JPEG クオリティを設定します.
     */
    private void setPreviewJpegQuality() {
        SeekBarDialogPreference pref = findPreference("preview_jpeg_quality");
        if (pref != null) {
            pref.setMinValue(0);
            pref.setMaxValue(100);
            pref.setEnabled(true);
        }
    }
}
