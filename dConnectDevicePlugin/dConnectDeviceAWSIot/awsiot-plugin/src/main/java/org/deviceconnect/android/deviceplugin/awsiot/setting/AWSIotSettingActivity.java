/*
 AWSIotSettingActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import org.deviceconnect.android.deviceplugin.awsiot.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.AWSIotRemoteManager;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.deviceconnect.android.deviceplugin.awsiot.setting.fragment.AWSIotLoginFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageActivity;

/**
 * 設定用Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotSettingActivity extends FragmentActivity {
    private AWSIotRemoteManager mAWSIotRemoteManager;
    private AWSIotPrefUtil mPrefUtil;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        mPrefUtil = new AWSIotPrefUtil(this);
        mAWSIotRemoteManager = new AWSIotRemoteManager(this);

        AWSIotDeviceApplication app = (AWSIotDeviceApplication) getApplication();
        app.initialize();
        setContentView(R.layout.activity_main);
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
        ab.setTitle("CLOSE");

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

    public AWSIotRemoteManager getAWSIotRemoteManager() {
        return mAWSIotRemoteManager;
    }

    public AWSIotPrefUtil getPrefUtil() {
        return mPrefUtil;
    }
}