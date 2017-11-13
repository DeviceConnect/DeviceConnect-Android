/*
SmartMeterSettingActivity
Copyright (c) 2017 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.smartmeter.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.deviceconnect.android.deviceplugin.smartmeter.BuildConfig;
import org.deviceconnect.android.deviceplugin.smartmeter.setting.fragment.SmartMeterSettingFragment;
import org.deviceconnect.android.deviceplugin.smartmeter.util.PrefUtil;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * 設定用Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class SmartMeterSettingActivity extends DConnectSettingPageFragmentActivity {
    /** PrefUtil Instance. */
    private PrefUtil mPrefUtil;
    /** サービスID. */
    private String mServiceId;
    /** ページ数. */
    private static final int PAGE_COUNT = 1;

    /**
     * ページのクラスリスト.
     */
    @SuppressWarnings("rawtypes")
    private static final Class[] PAGES = {
            SmartMeterSettingFragment.class,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefUtil = new PrefUtil(this);
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

    /**
     * Get PrefUtil Instance.
     * @return PrefUtil Instance.
     */
    public PrefUtil getPrefUtil() {
        return mPrefUtil;
    }
}
