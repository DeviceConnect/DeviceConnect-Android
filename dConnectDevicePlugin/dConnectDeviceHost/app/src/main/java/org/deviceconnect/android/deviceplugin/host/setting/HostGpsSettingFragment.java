/*
 HostGpsSettingFragment.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.R;

/**
 * This fragment do setting of the GPS.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostGpsSettingFragment extends BaseHostSettingPageFragment {
    private final Handler mHandler = new Handler();
    private Button mGpsPermissionBtn;
    /**
     * Defined the permission of BLE scan.
     */
    public static final String[] GPS_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected String getPageTitle() {
        return getString(R.string.gps_settings_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_host_gps_setting, null);

        Button btn = rootView.findViewById(R.id.btn_settings_open);
        btn.setOnClickListener((v) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });

        View permission = rootView.findViewById(R.id.gps_permission);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permission.setVisibility(View.GONE);
        } else {
            permission.setVisibility(View.VISIBLE);
        }

        mGpsPermissionBtn = rootView.findViewById(R.id.button_permission);
        mGpsPermissionBtn.setOnClickListener((v) -> {
            if (isGPSPermission(getActivity())) {
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
        if (mGpsPermissionBtn != null) {
            if (isGPSPermission(getActivity())) {
                mGpsPermissionBtn.setText(getString(R.string.gps_settings_gps_permission_on));
                mGpsPermissionBtn.setBackgroundResource(R.drawable.button_red);
            } else {
                mGpsPermissionBtn.setText(getString(R.string.gps_settings_gps_permission_off));
                mGpsPermissionBtn.setBackgroundResource(R.drawable.button_blue);
            }
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
                GPS_PERMISSIONS,
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

    public static boolean isGPSPermission(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return context.checkSelfPermission(GPS_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
        }
    }

}
