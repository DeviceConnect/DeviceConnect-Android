/*
 AWSIotSettingActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.setting;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.deviceconnect.android.deviceplugin.awsiot.setting.fragment.AWSIotLoginFragment;
import org.deviceconnect.android.deviceplugin.awsiot.setting.fragment.AWSIotManagerListFragment;

/**
 * 設定用Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotSettingActivity extends AppCompatActivity {
    private AWSIotPrefUtil mPrefUtil;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        mPrefUtil = new AWSIotPrefUtil(this);

        setContentView(R.layout.activity_main);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            ab.setTitle("CLOSE");
        }

        AWSIotController controller = getAWSIotController();
        if (controller.isConnected()) {
            FragmentManager manager = getSupportFragmentManager();
            AWSIotManagerListFragment fragment = new AWSIotManagerListFragment();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.container, fragment, "AWSIotLoginFragment");
            transaction.commit();
        } else {
            FragmentManager manager = getSupportFragmentManager();
            AWSIotLoginFragment fragment = new AWSIotLoginFragment();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.container, fragment, "AWSIotLoginFragment");
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public AWSIotController getAWSIotController() {
        return ((AWSIotDeviceApplication) getApplication()).getAWSIotController();
    }

    public AWSIotPrefUtil getPrefUtil() {
        return mPrefUtil;
    }
}