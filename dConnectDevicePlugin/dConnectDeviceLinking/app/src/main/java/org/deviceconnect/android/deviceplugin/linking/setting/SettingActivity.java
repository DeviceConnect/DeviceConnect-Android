/*
 SettingActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.LinkingControllerFragment;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.LinkingDeviceListFragment;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.LinkingImageFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * Activity for setting.
 *
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends DConnectSettingPageFragmentActivity {

    private LinkingControllerFragment mControllFragment;

    @SuppressWarnings("rawtypes")
    private static final Class[] PAGES = {
            LinkingImageFragment.class,
            LinkingDeviceListFragment.class,
            LinkingControllerFragment.class
    };

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
            if (page instanceof LinkingControllerFragment) {
                mControllFragment = (LinkingControllerFragment) page;
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

    public void showControllerPage(LinkingDevice device) {
        if (mControllFragment == null) {
            return;
        }
        mControllFragment.setTargetDevice(device);
        getViewPager().setCurrentItem(3, true);
    }

}
