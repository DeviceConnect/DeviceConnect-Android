/*
 AWSIotSettingActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.setting;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.deviceconnect.android.deviceplugin.awsiot.setting.fragment.AWSIotLoginFragment;

/**
 * 設定用Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotSettingActivity extends FragmentActivity {
    private AWSIotPrefUtil mPrefUtil;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        mPrefUtil = new AWSIotPrefUtil(this);

        AWSIotDeviceApplication app = (AWSIotDeviceApplication) getApplication();
        setContentView(R.layout.activity_main);
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
            ab.setTitle("CLOSE");
        }

        FragmentManager manager = getSupportFragmentManager();
        AWSIotLoginFragment mFragment = new AWSIotLoginFragment();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, mFragment, "AWSIotLoginFragment");
        transaction.commit();
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