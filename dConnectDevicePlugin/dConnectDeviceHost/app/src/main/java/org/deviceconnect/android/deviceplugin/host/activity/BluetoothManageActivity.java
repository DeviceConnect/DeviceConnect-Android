/*
 BluetoothManageActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ConnectionProfile;
import org.deviceconnect.message.DConnectMessage;

/**
 * Bluetooth 管理アクティビティ.
 * 
 * <p>
 * Bluetooth接続をONにするダイアログを表示するシステムActivityを表示するActivityである。 HostDeviceProvider
 * から呼び出されるActivityのため、UIレイヤーから呼び出してはならない。
 * @author NTT DOCOMO, INC.
 */
public class BluetoothManageActivity extends FragmentActivity {
    /**
     * リクエストパラメータ.
     */
    private Bundle mRequestParam;

    @Override
    protected void onStart() {
        super.onStart();

        mRequestParam = new Bundle(getIntent().getExtras());

        if (MessageUtils.getAttribute(getIntent()).equals(ConnectionProfile.ATTRIBUTE_BLUETOOTH)) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                startActivityForResult(enableIntent, 0);
            } catch(ActivityNotFoundException e) {
                BluetoothAdapter.getDefaultAdapter().enable();
            }
        } else if (MessageUtils.getAttribute(getIntent()).equals(ConnectionProfile.ATTRIBUTE_BLE)) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                startActivityForResult(enableIntent, 0);
            } catch(ActivityNotFoundException e) {
                BluetoothAdapter.getDefaultAdapter().enable();
            }
        } else {
            // finish if attribute is unknown
            finish();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // create response data
        Bundle response = new Bundle();
        if (resultCode == RESULT_OK) {
            response.putInt(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        } else {
            response.putInt(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
        }

        Intent intent = MessageUtils.createResponseIntent(mRequestParam, response);
        if (intent != null) {
            sendBroadcast(intent);
        }

        finish();
    }
}
