/*
 UVCDeviceSettingsActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.activity;


import androidx.fragment.app.Fragment;

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

}
