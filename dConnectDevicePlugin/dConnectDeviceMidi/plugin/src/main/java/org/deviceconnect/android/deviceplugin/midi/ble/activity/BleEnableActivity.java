/*
 BleEnableActivity.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.ble.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * BLE デバイス接続画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class BleEnableActivity extends Activity {
    private static final String EXTRA_CALLBACK = "EXTRA_CALLBACK";
    private static final int REQUEST_CODE = 123456789;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            test(resultCode, data);
        }

        finish();
    }

    private void test(final int resultCode, final Intent data) {
        Intent callingIntent = getIntent();
        if (callingIntent == null) {
            return;
        }

        Bundle extras = callingIntent.getExtras();
        if (extras == null) {
            return;
        }

        ResultReceiver callback = extras.getParcelable(EXTRA_CALLBACK);
        if (callback == null) {
            return;
        }

        callback.send(resultCode, null);

    }

    public static void requestEnableBluetooth(final Context context, final ResultReceiver resultReceiver) {
        Intent callIntent = new Intent(context, BleEnableActivity.class);
        callIntent.putExtra(EXTRA_CALLBACK, resultReceiver);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(callIntent);
    }
}
