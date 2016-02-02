/*
 SettingActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.DConnectWebService;
import org.deviceconnect.android.manager.R;

/**
 * Device Connect Manager設定管理用Activity.
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends Activity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_dconnect_settings);

        Intent i1 = new Intent();
        i1.setClass(this, DConnectService.class);
        startService(i1);

        Intent i2 = new Intent();
        i2.setClass(this, DConnectWebService.class);
        startService(i2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
