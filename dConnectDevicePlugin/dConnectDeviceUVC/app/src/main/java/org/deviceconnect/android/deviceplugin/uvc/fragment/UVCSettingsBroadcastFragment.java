package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.uvc.R;

public class UVCSettingsBroadcastFragment extends UVCSettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(getSettingsName());
        setPreferencesFromResource(R.xml.settings_uvc_recorder_broadcast, rootKey);
    }
}
