/*
 UVCDeviceSettingsActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.activity;

import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.fragment.UVCDeviceInstructionFragment;
import org.deviceconnect.android.deviceplugin.uvc.fragment.UVCDeviceListFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;


public class UVCDeviceSettingsActivity extends DConnectSettingPageFragmentActivity {

    @Override
    public int getPageCount() {
        return 2;
    }

    @Override
    public Fragment createPage(int position) {
        if (position == 0) {
            return new UVCDeviceInstructionFragment();
        } else {
            return new UVCDeviceListFragment();
        }
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        if (position == 0) {
            return getString(R.string.uvc_settings_title_uvc_device_instruction);
        } else {
            return getString(R.string.uvc_settings_title_uvc_device_list);
        }
    }
}
