/*
 HeartRateDeviceSettingsFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.deviceconnect.android.deviceplugin.heartrate.R;

/**
 * @author NTT DOCOMO, INC.
 */
public class BluetoothSettingsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bluetooth_settings, null);

        Button btn = (Button) rootView.findViewById(R.id.btn_settings_open);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });
        return rootView;
    }
}
