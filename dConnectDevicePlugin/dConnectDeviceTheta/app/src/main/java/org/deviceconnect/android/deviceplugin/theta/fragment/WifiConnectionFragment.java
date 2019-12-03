/*
 WifiConnectionFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.deviceconnect.android.deviceplugin.theta.R;

/**
 * The page which has the button to open WiFi settings window.
 *
 * @author NTT DOCOMO, INC.
 */
public class WifiConnectionFragment extends SettingsFragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi_connection, null);
        Button btnCameraSearch = rootView.findViewById(R.id.btn_wifi_settings);
        btnCameraSearch.setOnClickListener((v) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);
        });
        return rootView;
    }

}
