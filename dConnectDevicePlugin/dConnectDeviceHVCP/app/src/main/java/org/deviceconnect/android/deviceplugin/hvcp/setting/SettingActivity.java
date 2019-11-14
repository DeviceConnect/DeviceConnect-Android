/*
 SettingActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcp.setting;


import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.hvcp.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvcp.setting.fragment.HVCPAcceptDialogFragment;
import org.deviceconnect.android.deviceplugin.hvcp.setting.fragment.HVCPWakeupFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;
/**
 * HVC-P Setting Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends DConnectSettingPageFragmentActivity {


    @SuppressWarnings("rawtypes")
    private static final Class[] PAGES = {
            HVCPWakeupFragment.class,
            HVCPAcceptDialogFragment.class,
    };


    @Override
    public int getPageCount() {
        return PAGES.length;
    }

    @Override
    public Fragment createPage(int position) {
        Fragment page;
        try {
            page = (Fragment) PAGES[position].newInstance();
        } catch (InstantiationException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            page = null;
        } catch (IllegalAccessException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            page = null;
        }
        return page;
    }
}
