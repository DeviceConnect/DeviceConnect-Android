/*
 UVCDeviceErrorActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;

public class ErrorDialogActivity extends Activity {

    public static final String PARAM_TITLE = "title";
    public static final String PARAM_MESSAGE = "message";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        String title = intent.getStringExtra(PARAM_TITLE);
        String message = intent.getStringExtra(PARAM_MESSAGE);
        if (title == null || message == null) {
            finish();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
            finish();
        });
        builder.setOnCancelListener((dialogInterface) -> {
            finish();
        });
        builder.create().show();
    }

    public static void show(final Context context, final String title, final String message) {
        Intent intent = new Intent(context, ErrorDialogActivity.class);
        intent.putExtra(PARAM_TITLE, title);
        intent.putExtra(PARAM_MESSAGE, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void show(final Context context, final String message) {
        show(context, context.getString(R.string.uvc_error_dialog_default_title), message);
    }

    public static void showNotSupportedError(final Context context, final UVCDevice device) {
        String baseMessage = context.getString(R.string.uvc_error_message_device_not_supported);
        String message = baseMessage.replace("{NAME}", device.getName());
        show(context, message);
    }
}
