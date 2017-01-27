/*
 SettingsActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.deviceconnect.android.uiapp.R;

/**
 * 設定画面アクティビティ.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
