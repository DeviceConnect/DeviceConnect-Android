/*
 SettingActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.LinkingBeaconListFragment;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.LinkingDeviceListFragment;

/**
 * Activity for setting.
 *
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends AppCompatActivity {
    private Fragment[] mFragments = new Fragment[2];
    private String[] pageTitle = new String[2];

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        pageTitle[0] = getString(R.string.activity_setting_tab_paring);
        pageTitle[1] = getString(R.string.activity_setting_tab_beacon);

        mFragments[0] = LinkingDeviceListFragment.newInstance();
        mFragments[1] = LinkingBeaconListFragment.newInstance();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_setting_tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_setting_view_pager);

        if (viewPager != null) {
            viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
            if (tabLayout != null) {
                tabLayout.setupWithViewPager(viewPager);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_setting:
                transitionLinkingApp();
                break;
            case R.id.menu_information:
                transitionAppInform();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void transitionLinkingApp() {
        Intent intent = new Intent();
        intent.setClass(this, LinkingSettingActivity.class);
        startActivity(intent);
    }

    private void transitionAppInform() {
        Intent intent = new Intent();
        intent.setClass(this, LinkingDetailActivity.class);
        startActivity(intent);
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        public MyFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitle[position];
        }
    }
}
