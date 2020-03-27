/*
 RegisterActivity.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.switchbot.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;
import org.deviceconnect.android.deviceplugin.switchbot.R;
import org.deviceconnect.android.deviceplugin.switchbot.SwitchBotMessageService;
import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDevice;
import org.deviceconnect.android.message.DConnectMessageService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SwitchBotデバイス登録用Activity
 */
public class RegisterActivity extends BaseSettingActivity implements View.OnClickListener {
    private static final String TAG = "RegisterActivity";
    private static final Boolean DEBUG = BuildConfig.DEBUG;
    private static final int REQUEST_DEVICE_SCAN = 818;
    private static final String REGX_PATTERN = "^([0-9A-Fa-f]{1,2}[:-]){5}[0-9A-Fa-f]{1,2}$";
    private EditText mEditDeviceName;
    private EditText mEditDeviceAddress;
    private Spinner mSpinnerDeviceMode;
    private SwitchBotMessageService mSwitchBotMessageService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (DEBUG) {
                Log.d(TAG, "onServiceConnected()");
            }
            mSwitchBotMessageService = (SwitchBotMessageService) ((DConnectMessageService.LocalBinder) iBinder).getMessageService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (DEBUG) {
                Log.d(TAG, "onServiceDisconnected()");
            }
            mSwitchBotMessageService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("デバイス登録");

        if (DEBUG) {
            Log.d(TAG, "onCreate()");
            Log.d(TAG, "savedInstanceState : " + savedInstanceState);
        }

        Intent intent = new Intent(this, SwitchBotMessageService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mEditDeviceName = findViewById(R.id.edit_device_name);
        mEditDeviceAddress = findViewById(R.id.edit_device_address);
        mSpinnerDeviceMode = findViewById(R.id.spinner_device_mode);
        findViewById(R.id.button_scan).setOnClickListener(this);
        findViewById(R.id.button_register).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Log.d(TAG, "onDestroy()");
        }
        unbindService(mServiceConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(TAG, "onActivityResult()");
            Log.d(TAG, "requestCode : " + requestCode);
            Log.d(TAG, "resultCode : " + resultCode);
            Log.d(TAG, "data : " + data);
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DEVICE_SCAN) {
            if (resultCode == RESULT_OK) {
                mEditDeviceAddress.setText(data.getStringExtra(ScanActivity.KEY_DEVICE_ADDRESS));
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (DEBUG) {
            Log.d(TAG, "onClick()");
            Log.d(TAG, "view : " + view);
            Log.d(TAG, "id : " + id);
        }
        switch (id) {
            case R.id.button_register:
                register();
                break;
            case R.id.button_scan:
                Intent scanActivity = new Intent(this, ScanActivity.class);
                startActivityForResult(scanActivity, REQUEST_DEVICE_SCAN);
                break;
            default:
                break;
        }
    }

    /**
     * デバイス登録処理
     */
    private void register() {
        final String deviceName = mEditDeviceName.getText().toString();
        final String deviceAddress = mEditDeviceAddress.getText().toString();
        final SwitchBotDevice.Mode deviceMode = SwitchBotDevice.Mode.getInstance(mSpinnerDeviceMode.getSelectedItemPosition());
        if (DEBUG) {
            Log.d(TAG, "register");
            Log.d(TAG, "device name : " + deviceName);
            Log.d(TAG, "device address : " + deviceAddress);
            Log.d(TAG, "device mode : " + deviceMode);
        }
        if (mEditDeviceName.getText().toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_register_error_device_name_empty), Toast.LENGTH_LONG).show();
            return;
        }
        if (mEditDeviceAddress.getText().toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_register_error_device_address_empty), Toast.LENGTH_LONG).show();
            return;
        }
        if (!checkFormat(mEditDeviceAddress.getText().toString())) {
            Toast.makeText(this, getString(R.string.toast_register_error_device_address_unrecognized), Toast.LENGTH_LONG).show();
            return;
        }
        SwitchBotDevice switchBotDevice = new SwitchBotDevice(mSwitchBotMessageService, deviceName, deviceAddress, deviceMode, mSwitchBotMessageService);
        if (mSwitchBotMessageService.registerDevice(switchBotDevice)) {
            Toast.makeText(this, getString(R.string.toast_register_success), Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.toast_register_error), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * MACアドレス形式チェック
     * @param macAddress チェック対象MAC Address
     * @return true(OK), false(NG)
     */
    private boolean checkFormat(String macAddress) {
        Pattern pattern = Pattern.compile(REGX_PATTERN);
        Matcher matcher = pattern.matcher(macAddress);
        return matcher.find();
    }
}
