/*
 SettingActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.manager.R;

import android.app.Activity;
import android.os.Bundle;

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
        LocalOAuth2Main.initialize(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalOAuth2Main.destroy();
    }
}
