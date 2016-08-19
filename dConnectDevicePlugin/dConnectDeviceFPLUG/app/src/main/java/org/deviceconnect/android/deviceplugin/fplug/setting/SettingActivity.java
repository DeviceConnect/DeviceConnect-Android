/*
 SettingActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.fplug.BuildConfig;
import org.deviceconnect.android.deviceplugin.fplug.FPLUGApplication;
import org.deviceconnect.android.deviceplugin.fplug.setting.fragment.FPLUGConnectFragment;
import org.deviceconnect.android.deviceplugin.fplug.setting.fragment.FPLUGControllerFragment;
import org.deviceconnect.android.deviceplugin.fplug.setting.fragment.FPLUGImageFragment;
import org.deviceconnect.android.deviceplugin.fplug.setting.fragment.FPLUGPairingFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * Activity for setting.
 *
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends DConnectSettingPageFragmentActivity {

    @SuppressWarnings("rawtypes")
    private static final Class[] PAGES = {
        FPLUGImageFragment.class,
        FPLUGPairingFragment.class,
        FPLUGConnectFragment.class,
        FPLUGControllerFragment.class,
    };

    private FPLUGControllerFragment mControllFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getPageCount() {
        return PAGES.length;
    }

    @Override
    public Fragment createPage(final int position) {
        Fragment page;
        try {
            page = (Fragment) PAGES[position].newInstance();
            if (page instanceof FPLUGControllerFragment) {
                mControllFragment = (FPLUGControllerFragment) page;
            }
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

    public void showControllerPage(String selectedFPlugAddress) {
        if (mControllFragment == null) {
            return;
        }
        mControllFragment.setTargetFPlugAddress(((FPLUGApplication) getApplication()).getFPLUGController(selectedFPlugAddress));
        getViewPager().setCurrentItem(3, true);
    }

}
