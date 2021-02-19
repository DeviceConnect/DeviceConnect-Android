/*
 SecuritySettingDialogActivity.java
 Copyright (c) 2021 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

/**
 * セキュリティの設定画面を出すダイアログ表示用Activity.
 * @author NTT DOCOMO, INC.
 */
public class SecuritySettingDialogActivity extends FragmentActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        byte[] rootCert = intent.getByteArrayExtra(SecuritySettingDialogFragment.EXTRA_ROOT_CERT);
        if (rootCert == null) {
            finish();
            return;
        }

        SecuritySettingDialogFragment fragment = new SecuritySettingDialogFragment();
        Bundle args = new Bundle();
        args.putByteArray(SecuritySettingDialogFragment.EXTRA_ROOT_CERT, rootCert);
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), "security_setting_dialog");
    }
}
