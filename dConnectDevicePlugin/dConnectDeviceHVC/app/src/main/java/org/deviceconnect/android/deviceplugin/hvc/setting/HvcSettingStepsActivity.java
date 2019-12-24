/*
 HvcSettingStepsActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.setting;

import android.content.Intent;
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
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.hvc.R;
import org.deviceconnect.android.deviceplugin.hvc.ble.BleUtils;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * HVC setting activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class HvcSettingStepsActivity extends DConnectSettingPageFragmentActivity {
    
    /**
     * Page count.
     */
    private static final int TUTORIAL_PAGE_NUMBER = 3;
    
    /**
     * Logger name.
     */
    private static final String LOGGER_NAME = "HvcSettingStepsActivity";
    
    /** フラグメント一覧. */
    private List<Fragment> mFragments = new ArrayList<Fragment>();

    @Override
    public int getPageCount() {
        return TUTORIAL_PAGE_NUMBER;
    }

    @Override
    public Fragment createPage(final int position) {
        if (mFragments.size() == 0) {
            BaseFragment f1 = new AboutFragment();
            f1.setActivity(this);
            BaseFragment f2 = new HvcConnectFragment();
            f2.setActivity(this);
            BaseFragment f3 = new BluetoothSettingPromptFragment();
            f3.setActivity(this);
            mFragments.add(f1);
            mFragments.add(f2);
            mFragments.add(f3);
        }
        return mFragments.get(position);
    }

    /**
     * Base Fragment.
     *
     */
    public static class BaseFragment extends Fragment {
        
        /** ロガー. */
        protected Logger mLogger = Logger.getLogger(LOGGER_NAME);
        /**
         * チュートリアルページアクティビティ.
         */
        HvcSettingStepsActivity mActivity;
        /**
         * アクティビティを設定する.
         * @param activity activity
         */
        public void setActivity(final HvcSettingStepsActivity activity) {
            mActivity = activity;
        }
    }

    /**
     * step1. About HVC Device Plugin.
     */
    public static class AboutFragment extends BaseFragment {
        @Override
        public View onCreateView(final LayoutInflater inflater, 
                final ViewGroup container, final Bundle savedInstanceState) {
            final int layoutId = R.layout.hvc_setting_0;
            View root = inflater.inflate(layoutId, container, false);
            
            return root;
        }
        @Override
        public void onResume() {
            super.onResume();
        }
    }

    /**
     * 手順2 HVC-Cの準備.
     */
    public static class HvcConnectFragment extends BaseFragment {
        @Override
        public View onCreateView(final LayoutInflater inflater, 
                final ViewGroup container, final Bundle savedInstanceState) {
            final int layoutId = R.layout.hvc_setting_1;
            View root = inflater.inflate(layoutId, container, false);
            return root;
        }
    }

    /**
     * 手順3 Bluetooth設定.
     */
    public static class BluetoothSettingPromptFragment extends BaseFragment {
        private final Handler mHandler = new Handler();
        private Button mBlePermissionBtn;

        @Override
        public View onCreateView(final LayoutInflater inflater, 
                final ViewGroup container, final Bundle savedInstanceState) {
            final int layoutId = R.layout.hvc_setting_2;
            View root = inflater.inflate(layoutId, container, false);
            Button button = root.findViewById(R.id.button_launch_bluetooth_setting);
            button.setOnClickListener((v) -> {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            });

            View permission = root.findViewById(R.id.ble_permission);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                permission.setVisibility(View.GONE);
            } else {
                permission.setVisibility(View.VISIBLE);
            }

            mBlePermissionBtn = root.findViewById(R.id.button_permission);
            mBlePermissionBtn.setOnClickListener((v) -> {
                if (BleUtils.isBLEPermission(getActivity())) {
                    openAndroidSettings();
                } else {
                    requestPermissions();
                }
            });
            return root;
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mBlePermissionBtn != null) {
                if (BleUtils.isBLEPermission(getActivity())) {
                    mBlePermissionBtn.setText(getString(R.string.setting_step3_ble_permission_on));
                    mBlePermissionBtn.setBackgroundResource(R.drawable.button_red);
                } else {
                    mBlePermissionBtn.setText(getString(R.string.setting_step3_ble_permission_off));
                    mBlePermissionBtn.setBackgroundResource(R.drawable.button_blue);
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
}
