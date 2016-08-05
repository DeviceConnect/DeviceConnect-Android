/*
 PairingFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.setting.fragment;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.sphero.R;
import org.deviceconnect.android.deviceplugin.sphero.util.BleUtils;

/**
 * Spheroペアリング説明画面.
 * @author NTT DOCOMO, INC.
 */
public class PairingFragment extends Fragment {
    private final Handler mHandler = new Handler();
    private Button mBlePermissionBtn;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, 
                final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.setting_pairing, null);
        
        final ImageView image = (ImageView) root.findViewById(R.id.animView);
        image.setBackgroundResource(R.anim.sphero_light);
        
        Button button = (Button) root.findViewById(R.id.btnSetting);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
            }
        });
        View permission = root.findViewById(R.id.ble_permission);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permission.setVisibility(View.GONE);
        } else {
            permission.setVisibility(View.VISIBLE);
        }

        mBlePermissionBtn = (Button) root.findViewById(R.id.button_permission);
        mBlePermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BleUtils.isBLEPermission(getActivity())) {
                    openAndroidSettings();
                } else {
                    requestPermissions();
                }
            }
        });
        return root;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root != null) {
            ImageView image = (ImageView) root.findViewById(R.id.animView);
            AnimationDrawable anim = (AnimationDrawable) image.getBackground();
            anim.start();
        }
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

    @Override
    public void onPause() {
        super.onPause();
        View root = getView();
        if (root != null) {
            ImageView image = (ImageView) root.findViewById(R.id.animView);
            AnimationDrawable anim = (AnimationDrawable) image.getBackground();
            anim.stop();
        }
    }
    private void openAndroidSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
        startActivity(intent);
    }

    private void requestPermissions() {
        PermissionUtility.requestPermissions(getActivity(), mHandler,
                BleUtils.BLE_PERMISSIONS,
                new PermissionUtility.PermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @NonNull
                    @Override
                    public void onFail(final String deniedPermission) {
                    }
                });
    }
}
