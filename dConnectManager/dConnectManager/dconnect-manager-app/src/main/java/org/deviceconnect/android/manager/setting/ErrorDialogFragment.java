/*
 ErrorDialogFragment.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.deviceconnect.android.manager.R;

/**
 * エラーダイアログ.
 *
 * @author NTT DOCOMO, INC.
 */
public class ErrorDialogFragment extends DialogFragment {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_MESSAGE = "message";

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) {
            dismiss();
        }

        String title = args.getString(EXTRA_TITLE);
        if (title == null) {
            title = getString(R.string.dconnect_error_default_title);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(args.getString(EXTRA_MESSAGE));
        builder.setPositiveButton(R.string.activity_settings_close, (dialog, which) -> {
            dialog.dismiss();
        });
        return builder.create();
    }

    @Override
    public void onStop() {
        super.onStop();

        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }
}
