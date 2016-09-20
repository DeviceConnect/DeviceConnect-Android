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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.LinkingBeaconListFragment;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.LinkingDeviceListFragment;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.dialog.ConfirmationDialogFragment;

/**
 * Activity for setting.
 *
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends AppCompatActivity implements ConfirmationDialogFragment.OnDialogEventListener {

    public static final String TAG = "Linking-Plugin";

    private Fragment[] mFragments = new Fragment[2];
    private String[] pageTitle = new String[2];

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linking_setting);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
        }

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

        checkArguments(getIntent());

        if (LinkingUtil.getVersionCode(this) < LinkingUtil.LINKING_APP_VERSION) {
            openUpdateDialog();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkArguments(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_linking_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_setting) {
            transitionLinkingApp();
        } else if (item.getItemId() == R.id.menu_information) {
            transitionAppInform();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveClick(final DialogFragment fragment) {
        LinkingUtil.startGooglePlay(this);
    }

    @Override
    public void onNegativeClick(final DialogFragment fragment) {
    }

    private void checkArguments(final Intent intent) {
        if (intent != null) {
            String appName = intent.getStringExtra(getPackageName() + LinkingUtil.EXTRA_APP_NAME);
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "appName=" + appName);
            }
            int deviceId = intent.getIntExtra(getPackageName() + LinkingUtil.EXTRA_DEVICE_ID, -1);
            int deviceUid = intent.getIntExtra(getPackageName() + LinkingUtil.EXTRA_DEVICE_UID, -1);
            LinkingDevice device = getLinkingDeviceId(deviceId, deviceUid);
            if (device != null) {
                transitionDeviceControl(device);
            }
        }
    }

    private void transitionLinkingApp() {
        Intent intent = new Intent();
        intent.setClass(this, LinkingInductionActivity.class);
        startActivity(intent);
    }

    private void transitionAppInform() {
        Intent intent = new Intent();
        intent.setClass(this, AppInformationActivity.class);
        startActivity(intent);
    }

    private void transitionDeviceControl(final LinkingDevice device) {
        if (device.isConnected()) {
            Intent intent = new Intent();
            intent.putExtra(LinkingDeviceActivity.EXTRA_ADDRESS, device.getBdAddress());
            intent.setClass(this, LinkingDeviceActivity.class);
            startActivity(intent);
        }
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = (LinkingApplication) getApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingDevice getLinkingDeviceId(final int deviceId, final int deviceUid) {
        LinkingDeviceManager mgr = getLinkingDeviceManager();
        return mgr.findDeviceByDeviceId(deviceId, deviceUid);
    }

    private void openUpdateDialog() {
        String title = getString(R.string.activity_setting_update_dialog_title);
        String message = getString(R.string.activity_setting_update_dialog_message);
        String positive = getString(R.string.activity_setting_update_dialog_positive);
        String negative = getString(R.string.activity_setting_update_dialog_negative);

        ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance(title, message, positive, negative);
        dialog.show(getSupportFragmentManager(), "");
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        public MyFragmentPagerAdapter(final FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(final int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            return pageTitle[position];
        }
    }
}
