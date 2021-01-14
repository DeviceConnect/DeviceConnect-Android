/*
 BluetoothManageActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Bluetooth 管理アクティビティ.
 * 
 * <p>
 * Bluetooth接続をONにするダイアログを表示するシステムActivityを表示するActivityである。 HostDeviceProvider
 * から呼び出されるActivityのため、UIレイヤーから呼び出してはならない。
 *
 * @author NTT DOCOMO, INC.
 */
public class BluetoothManageActivity extends AppCompatActivity {
    public static final String EXTRA_CALLBACK = "callback";

    private static final int REQUEST_CODE = 32421;

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        try {
            startActivityForResult(enableIntent, REQUEST_CODE);
        } catch (Exception e) {
            // Bluetooth 有効化の Activity が見つからない場合には、
            // BluetoothAdapter#enable() を使用して設定します。
            int result = RESULT_CANCELED;
            try {
                boolean r = BluetoothAdapter.getDefaultAdapter().enable();
                if (r) {
                    result = RESULT_OK;
                }
            } catch (Exception exception) {
                // ignore.
            }

            Bundle response = new Bundle();
            ResultReceiver callback = getIntent().getParcelableExtra(EXTRA_CALLBACK);
            if (callback != null) {
                callback.send(result, response);
            }
            finish();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_CODE) {
            return;
        }

        Bundle response = new Bundle();
        ResultReceiver callback = getIntent().getParcelableExtra(EXTRA_CALLBACK);
        if (callback != null) {
            callback.send(resultCode, response);
        }
        finish();
    }
}
