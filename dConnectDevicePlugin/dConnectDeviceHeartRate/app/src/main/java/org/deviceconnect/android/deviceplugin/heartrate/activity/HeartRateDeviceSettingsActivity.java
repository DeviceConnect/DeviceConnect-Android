/*
 HeartRateDeviceSettingsActivity
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.heartrate.HeartRateApplication;
import org.deviceconnect.android.deviceplugin.heartrate.fragment.BluetoothSettingsFragment;
import org.deviceconnect.android.deviceplugin.heartrate.fragment.HeartRateDeviceSettingsFragment;
import org.deviceconnect.android.deviceplugin.heartrate.fragment.MioAlphaInstructionsFragment;
import org.deviceconnect.android.deviceplugin.heartrate.fragment.SummaryFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * This activity is settings screen.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDeviceSettingsActivity extends DConnectSettingPageFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HeartRateApplication app = (HeartRateApplication) getApplication();
        app.initialize();
    }

    @Override
    public int getPageCount() {
        return 4;
    }

    @Override
    public Fragment createPage(int position) {
        if (position == 0) {
            return new SummaryFragment();
        } else if (position == 1) {
            return new MioAlphaInstructionsFragment();
        } else if (position == 2) {
            return new BluetoothSettingsFragment();
        } else {
            return new HeartRateDeviceSettingsFragment();
        }
    }

}
