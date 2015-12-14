/*
 DevicePluginInfoActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.manager.R;

/**
 * Device Connect Manager device plug-in Information Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginInfoActivity extends Activity {

    /** デバイスプラグインのパッケージ名のキー. */
    static final String PACKAGE_NAME = "packageName";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.activity_deviceplugin_info_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        String packageName = intent.getStringExtra(PACKAGE_NAME);
        if (packageName == null) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
            Fragment f = new DevicePluginInfoFragment();
            Bundle args = new Bundle();
            args.putString(PACKAGE_NAME, packageName);
            f.setArguments(args);

            FragmentManager fm = getFragmentManager();
            FragmentTransaction t = fm.beginTransaction();
            t.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            t.add(android.R.id.content, f, "container");
            t.commit();
        }
    }
}
