/*
 SettingActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.setting;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.R;
import org.deviceconnect.android.deviceplugin.webrtc.setting.fragment.AudioSettingsFragment;
import org.deviceconnect.android.deviceplugin.webrtc.setting.fragment.CameraSettingsFragment;
import org.deviceconnect.android.deviceplugin.webrtc.setting.fragment.PeerSettingsFragment;
import org.deviceconnect.android.deviceplugin.webrtc.setting.fragment.WakeupFragment;
import org.deviceconnect.android.deviceplugin.webrtc.util.CapabilityUtil;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

import java.util.List;

/**
 * Activity for setting.
 *
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends DConnectSettingPageFragmentActivity {

    @SuppressWarnings("rawtypes")
    private static final Class[] PAGES = {
            WakeupFragment.class,
            PeerSettingsFragment.class,
            CameraSettingsFragment.class,
            AudioSettingsFragment.class,
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CapabilityUtil.requestPermissions(this, new Handler(), new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                getViewPager().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
                    }
                    @Override
                    public void onPageSelected(final int position) {
                        if (position == 1) {
                            List<Fragment> fragments = getSupportFragmentManager().getFragments();
                            for (Fragment f : fragments) {
                                if (f instanceof PeerSettingsFragment) {
                                    ((PeerSettingsFragment) f).reload();
                                }
                            }
                        }
                    }
                    @Override
                    public void onPageScrollStateChanged(final int state) {
                    }
                });
            }

            @Override
            public void onFail(@NonNull String s) {
                Toast.makeText(SettingActivity.this, R.string.error_failed_to_connect_permission_not_allow, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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
