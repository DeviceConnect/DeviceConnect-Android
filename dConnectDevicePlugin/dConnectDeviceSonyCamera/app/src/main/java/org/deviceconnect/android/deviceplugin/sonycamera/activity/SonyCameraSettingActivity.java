/*
SonyCameraSettingActivity
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.sonycamera.activity;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

/**
 * Sony Cameraデバイスプラグイン設定画面用Activity.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraSettingActivity extends DConnectSettingPageFragmentActivity {
    /** QX10のサービスID. */
    private String mServiceId;
    /** 全Fragmentページ数. */
    private static final int PAGE_COUNTER = 3;

    /** フラグメント一覧. */
    private SonyCameraBaseFragment[] mFragments = {
        new SonyCameraPreparationFragment(),
        new SonyCameraTurnOnFragment(),
        new SonyCameraConnectingFragment()
    };

    @Override
    protected void onResume() {
        super.onResume();

        ViewPager vp = getViewPager();
        vp.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(final int state) {
            }

            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
            }
        });
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
        return PAGE_COUNTER;
    }

    @Override
    public Fragment createPage(final int position) {
        return mFragments[position];
    }

}
