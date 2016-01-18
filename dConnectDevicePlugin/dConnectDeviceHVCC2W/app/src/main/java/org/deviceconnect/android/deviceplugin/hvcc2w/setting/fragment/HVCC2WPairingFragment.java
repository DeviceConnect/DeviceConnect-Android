/*
 HVCC2WPairingFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvcc2w.setting.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.hvcc2w.R;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCManager;

import java.util.List;

/**
 * HVC-C2W Settings Fragment Page 4.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WPairingFragment extends Fragment {
    /** WiFi AP Scan. */
    private BroadcastReceiver mReceiver;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.setting_pairing, null);
        final EditText ssidText = (EditText) root.findViewById(R.id.input_ssid);
        final EditText passwordText = (EditText) root.findViewById(R.id.input_password);
        root.findViewById(R.id.connect_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ssid = ssidText.getText().toString();
                String password = passwordText.getText().toString();
                if (!ssid.isEmpty() && !password.isEmpty()) {
                    HVCManager.INSTANCE.playConnectSound(getContext(), ssid, password);
                } else {
                    Toast.makeText(getContext(), getString(R.string.c2w_setting_error_5_1), Toast.LENGTH_LONG).show();
                }
            }
        });
        final WifiManager wifiManager = (WifiManager)getActivity().getSystemService(Activity.WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    List<ScanResult> results = wifiManager.getScanResults();
                    ssidText.setText(results.get(0).SSID);
                }
            };
            getActivity().registerReceiver(mReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        wifiManager.startScan();

        return root;
    }

    @Override
    public void onDestroy() {
        // レシーバーの解除
        getActivity().unregisterReceiver(mReceiver);

        super.onDestroy();
    }

}
