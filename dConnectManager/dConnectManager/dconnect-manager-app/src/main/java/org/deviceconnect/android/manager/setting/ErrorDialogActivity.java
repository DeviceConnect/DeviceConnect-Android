/*
 ErrorDialogActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * エラーダイアログ表示用Activity.
 * @author NTT DOCOMO, INC.
 */
public class ErrorDialogActivity extends FragmentActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        String title = intent.getStringExtra(ErrorDialogFragment.EXTRA_TITLE);
        String message = intent.getStringExtra(ErrorDialogFragment.EXTRA_MESSAGE);
        if (message == null) {
            finish();
            return;
        }

        ErrorDialogFragment fragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putString(ErrorDialogFragment.EXTRA_TITLE, title);
        args.putString(ErrorDialogFragment.EXTRA_MESSAGE, message);
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), "error_dialog");
    }
}
