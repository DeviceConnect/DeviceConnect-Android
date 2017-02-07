/*
 SettingActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.setting;

import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.hvcc2w.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvcc2w.setting.fragment.HVCC2WAccountRegisterFragment;
import org.deviceconnect.android.deviceplugin.hvcc2w.setting.fragment.HVCC2WPairingFragment;
import org.deviceconnect.android.deviceplugin.hvcc2w.setting.fragment.HVCC2WPushButtonFragment;
import org.deviceconnect.android.deviceplugin.hvcc2w.setting.fragment.HVCC2WWakeupFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * HVC-C2W Setting Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends DConnectSettingPageFragmentActivity {


    @SuppressWarnings("rawtypes")
    private static final Class[] PAGES = {
            HVCC2WWakeupFragment.class,
            HVCC2WAccountRegisterFragment.class,
            HVCC2WPushButtonFragment.class,
            HVCC2WPairingFragment.class,
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
