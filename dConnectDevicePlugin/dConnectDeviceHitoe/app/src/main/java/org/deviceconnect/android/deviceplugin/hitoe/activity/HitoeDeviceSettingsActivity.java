/*
 HitoeDeviceSettingsActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.BluetoothSettingsFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeDeviceSettingsFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.HitoeInstructionsFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * This activity is settings screen.
 * @author NTT DOCOMO, INC.
 */
public class HitoeDeviceSettingsActivity extends DConnectSettingPageFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HitoeApplication app = (HitoeApplication) getApplication();
        app.initialize();
    }

    @Override
    public int getPageCount() {
        return 3;
    }

    @Override
    public Fragment createPage(int position) {
        if (position == 0) {
            return new HitoeInstructionsFragment();
        } else if (position == 1) {
            return new BluetoothSettingsFragment();
        } else {
            return new HitoeDeviceSettingsFragment();
        }
    }
}
