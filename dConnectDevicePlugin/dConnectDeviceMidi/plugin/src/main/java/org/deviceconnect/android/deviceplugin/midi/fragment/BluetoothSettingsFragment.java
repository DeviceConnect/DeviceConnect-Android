/*
 HeartRateDeviceSettingsFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.midi.R;
import org.deviceconnect.android.deviceplugin.midi.ble.BleUtils;

/**
 * This fragment do setting of the Bluetooth.
 *
 * @author NTT DOCOMO, INC.
 */
public class BluetoothSettingsFragment extends Fragment {
    private Button mBlePermissionBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bluetooth_settings, null);

        Button btn = rootView.findViewById(R.id.btn_settings_open);
        btn.setOnClickListener((v) -> {
            openBluetoothSettings();
        });

        View permission = rootView.findViewById(R.id.ble_permission);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permission.setVisibility(View.GONE);
        } else {
            permission.setVisibility(View.VISIBLE);
        }

        mBlePermissionBtn = rootView.findViewById(R.id.button_permission);
        mBlePermissionBtn.setOnClickListener((v) -> {
            if (BleUtils.isBLEPermission(getActivity())) {
                openAndroidSettings();
            } else {
                requestPermissions();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBlePermissionBtn != null) {
            if (BleUtils.isBLEPermission(getActivity())) {
                mBlePermissionBtn.setText(getString(R.string.bluetooth_settings_ble_permission_on));
                mBlePermissionBtn.setBackgroundResource(R.drawable.button_red);
            } else {
                mBlePermissionBtn.setText(getString(R.string.bluetooth_settings_ble_permission_off));
                mBlePermissionBtn.setBackgroundResource(R.drawable.button_blue);
            }
        }
    }

    private void openBluetoothSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
    }

    private void openAndroidSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
        startActivity(intent);
    }

    private void requestPermissions() {
        BleUtils.requestBLEPermission(getActivity(), new BleUtils.BleRequestCallback() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onFail(final String deniedPermission) {
            }
        });
    }
}
