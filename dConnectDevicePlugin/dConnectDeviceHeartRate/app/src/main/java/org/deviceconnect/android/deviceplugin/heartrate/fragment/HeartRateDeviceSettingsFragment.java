/*
 HeartRateDeviceSettingsFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.deviceplugin.heartrate.HeartRateApplication;
import org.deviceconnect.android.deviceplugin.heartrate.HeartRateManager;
import org.deviceconnect.android.deviceplugin.heartrate.R;
import org.deviceconnect.android.deviceplugin.heartrate.activity.HeartRateDeviceSettingsActivity;

import static org.deviceconnect.android.deviceplugin.heartrate.HeartRateManager.*;

/**
 * This fragment do setting of the connection to the ble device.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDeviceSettingsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_heart_rate_device_settings, null);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getManager().setOnBleDeviceEventListener(mEvtListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getManager().setOnBleDeviceEventListener(null);
    }

    /**
     * Gets a instance of HeartRateManager.
     * @return HeartRateManager
     */
    private HeartRateManager getManager() {
        HeartRateDeviceSettingsActivity activity =
                (HeartRateDeviceSettingsActivity) getActivity();
        HeartRateApplication application =
                (HeartRateApplication) activity.getApplication();
        return application.getHeartRateManager();
    }

    private OnHeartRateDiscoveryListener mEvtListener = new OnHeartRateDiscoveryListener() {
        @Override
        public void onDiscovery() {
        }
    };
}
