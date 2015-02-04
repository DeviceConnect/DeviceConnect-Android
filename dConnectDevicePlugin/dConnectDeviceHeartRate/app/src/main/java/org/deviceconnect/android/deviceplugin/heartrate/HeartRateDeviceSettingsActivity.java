/*
 HeartRateDeviceSettingsActivity
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate;

import android.support.v4.app.Fragment;

import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 *
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDeviceSettingsActivity extends DConnectSettingPageFragmentActivity {

    @Override
    public int getPageCount() {
        return 0;
    }

    @Override
    public Fragment createPage(int position) {
        return null;
    }
}
