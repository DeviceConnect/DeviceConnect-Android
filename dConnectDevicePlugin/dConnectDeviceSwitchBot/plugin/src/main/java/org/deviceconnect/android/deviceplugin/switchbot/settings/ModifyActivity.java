/*
 ModifyActivity.java
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;
import org.deviceconnect.android.deviceplugin.switchbot.R;
import org.deviceconnect.android.deviceplugin.switchbot.SwitchBotMessageService;
import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDevice;
import org.deviceconnect.android.message.DConnectMessageService;

/**
 * SwitchBotデバイス情報更新用Activity
 */
public class ModifyActivity extends BaseSettingActivity implements View.OnClickListener {
    private static final String TAG = "ModifyActivity";
    private static final Boolean DEBUG = BuildConfig.DEBUG;
    public static final String KEY_DEVICE_NAME = "key_device_name";
    private SwitchBotDevice mSwitchBotDevice;
    private SwitchBotMessageService mSwitchBotMessageService = null;
    private EditText mEditDeviceName;
    private EditText mEditDeviceAddress;
    private Spinner mSpinnerDeviceMode;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (DEBUG) {
                Log.d(TAG, "onServiceConnected()");
            }
            mSwitchBotMessageService = (SwitchBotMessageService) ((DConnectMessageService.LocalBinder) iBinder).getMessageService();
            mSwitchBotDevice = mSwitchBotMessageService.getSwitchBotDeviceFromDeviceName(getIntent().getStringExtra(KEY_DEVICE_NAME));
            if (mSwitchBotDevice != null) {
                if (DEBUG) {
                    Log.d(TAG, "device name : " + mSwitchBotDevice.getDeviceName());
                    Log.d(TAG, "device address : " + mSwitchBotDevice.getDeviceAddress());
                    Log.d(TAG, "device mode : " + mSwitchBotDevice.getDeviceMode());
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    mEditDeviceName.setText(mSwitchBotDevice.getDeviceName());
                    mEditDeviceAddress.setText(mSwitchBotDevice.getDeviceAddress());
                    mSpinnerDeviceMode.setSelection(mSwitchBotDevice.getDeviceMode().getValue());
                });
            }
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
        setContentView(R.layout.activity_modify);

        if (DEBUG) {
            Log.d(TAG, "onCreate()");
            Log.d(TAG, "savedInstanceState : " + savedInstanceState);
        }

        Intent intent = new Intent(this, SwitchBotMessageService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mEditDeviceName = findViewById(R.id.edit_device_name);
        mEditDeviceAddress = findViewById(R.id.edit_device_address);
        mSpinnerDeviceMode = findViewById(R.id.spinner_device_mode);
        Button button = findViewById(R.id.button_modify);
        button.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (DEBUG) {
            Log.d(TAG, "onClick()");
            Log.d(TAG, "view : " + view);
            Log.d(TAG, "view.getId() : " + viewId);
        }
        if (viewId == R.id.button_modify) {
            if (mEditDeviceName.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_modify_error_device_name_empty), Toast.LENGTH_LONG).show();
                return;
            }
            if (!mSwitchBotMessageService.modifyDevice(
                    mSwitchBotDevice,
                    new SwitchBotDevice(
                            mSwitchBotMessageService,
                            mEditDeviceName.getText().toString(),
                            mEditDeviceAddress.getText().toString(),
                            SwitchBotDevice.Mode.getInstance(mSpinnerDeviceMode.getSelectedItemPosition()),
                            mSwitchBotMessageService
                    )
            )) {
                Toast.makeText(this, getString(R.string.toast_modify_error), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_modify_success), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
