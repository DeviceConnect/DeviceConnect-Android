/*
 HVCC2WPairingFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvcc2w.setting.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.deviceconnect.android.activity.PermissionRequestActivity;
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
    /** SSID Edit Text. */
    private EditText mSSID;
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.setting_pairing, null);
        mSSID = (EditText) root.findViewById(R.id.input_ssid);
        final EditText passwordText = (EditText) root.findViewById(R.id.input_password);
        root.findViewById(R.id.connect_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ssid = mSSID.getText().toString();
                String password = passwordText.getText().toString();
                if (!ssid.isEmpty() && !password.isEmpty()) {
                    HVCManager.INSTANCE.playConnectSound(getContext(), ssid, password);
                } else {
                    HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name), getString(R.string.c2w_setting_error_5_1), null);
                }
            }
        });
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP + 1) {
            searchWifi();
        } else {
            checkPermission();
        }

        return root;
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }


    /** Check Permission. */
    private void checkPermission() {
        // WiFi scan requires location permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP + 1) {
            if (PermissionChecker.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && PermissionChecker.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                searchWifi();
            } else {
                PermissionRequestActivity.requestPermissions(getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION}, new ResultReceiver(new Handler(Looper.getMainLooper())) {
                    @Override
                    protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                        String[] retPermissions = resultData.getStringArray("EXTRA_PERMISSIONS");
                        int[] retGrantResults = resultData.getIntArray("EXTRA_GRANT_RESULTS");
                        if (retPermissions == null || retGrantResults == null) {
                            HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name), "WiFi scan aborted.", null);
                            return;
                        }
                        for (int i = 0; i < retPermissions.length; ++i) {
                            if (retGrantResults[i] == PackageManager.PERMISSION_DENIED) {
                                HVCC2WDialogFragment.showAlert(getActivity(), getString(R.string.hw_name), "WiFi scan aborted.", null);
                                return;
                            }
                        }
                        searchWifi();
                    }
                });
            }
        }
    }

    /**
     * Check Wifi.
     */
    private void searchWifi() {
        final WifiManager wifiManager = getWifiManager();
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    List<ScanResult> results = wifiManager.getScanResults();
                    if (results.size() == 0) {
                        return;
                    }
                    mSSID.setText(results.get(0).SSID);
                }
            };
            getActivity().registerReceiver(mReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        wifiManager.startScan();
    }

    private WifiManager getWifiManager() {
        return (WifiManager) getActivity().getApplicationContext().getSystemService(Activity.WIFI_SERVICE);
    }
}
