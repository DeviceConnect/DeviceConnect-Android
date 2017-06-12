/*
FaBoSettingActivity
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import org.deviceconnect.android.deviceplugin.fabo.BuildConfig;
import org.deviceconnect.android.deviceplugin.fabo.setting.fragment.FaBoConnectFragment;
import org.deviceconnect.android.deviceplugin.fabo.setting.fragment.FaBoFirmwareFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * 設定用Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class FaBoSettingActivity extends DConnectSettingPageFragmentActivity {

    /**
     * ページのクラスリスト.
     */
    @SuppressWarnings("rawtypes")
    private static final Class[] PAGES = {
            FaBoConnectFragment.class,
            FaBoFirmwareFragment.class,
    };


    @Override
    public Fragment createPage(final int position) {
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


    @Override
    public int getPageCount() {
        return PAGES.length;
    }

    public void moveConnectFirmata() {
        ViewPager vp = getViewPager();
        vp.setCurrentItem(0, true);
    }
}