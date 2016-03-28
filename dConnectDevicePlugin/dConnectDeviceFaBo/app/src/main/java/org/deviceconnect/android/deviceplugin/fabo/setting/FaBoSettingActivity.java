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

    /** サービスID. */
    private String mServiceId;

    /** ページ数. */
    private static final int PAGE_COUNT = 2;

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


    /**
     * サービスIDを取得する.
     *
     * @return サービスID
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * サービスIDを設定する.
     *
     * @param serviceId サービスID
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    @Override
    public int getPageCount() {
        return PAGE_COUNT;
    }

    public void moveWriteFirmata() {
        ViewPager vp = getViewPager();
        vp.setCurrentItem(1, true);
    }

    public void moveConnectFirmata() {
        ViewPager vp = getViewPager();
        vp.setCurrentItem(0, true);
    }

}