/*
 SettingActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;

import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.util.DConnectUtil;

/**
 * Device Connect Manager設定管理用Activity.
 * @author NTT DOCOMO, INC.
 */
public class SettingActivity extends BaseSettingActivity implements AlertDialogFragment.OnAlertDialogListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dconnect_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onPositiveButton(final String tag) {
        SettingsFragment f = (SettingsFragment) getFragmentManager()
                .findFragmentById(R.id.activity_settings_category_fragment);
        f.onPositiveButton(tag);
    }

    @Override
    public void onNegativeButton(final String tag) {
        SettingsFragment f = (SettingsFragment) getFragmentManager()
                .findFragmentById(R.id.activity_settings_category_fragment);
        f.onNegativeButton(tag);
    }

    public void onButtonClick(View v) {
        Intent qr = new Intent(this, QRCodeActivity.class);
        startActivity(qr);
    }

}
