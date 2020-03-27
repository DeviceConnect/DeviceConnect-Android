/*
 ModifyAndDeleteActivity.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.switchbot.settings;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;
import org.deviceconnect.android.deviceplugin.switchbot.R;
import org.deviceconnect.android.deviceplugin.switchbot.SwitchBotMessageService;
import org.deviceconnect.android.deviceplugin.switchbot.utility.ListAdapter;
import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDevice;
import org.deviceconnect.android.message.DConnectMessageService;

import java.util.ArrayList;

/**
 * SwitchBot更新・削除用Activity
 */
public class ModifyAndDeleteActivity extends BaseSettingActivity implements ListAdapter.EventListener, View.OnClickListener {
    private static final String TAG = "ModifyAndDeleteActivity";
    private static final Boolean DEBUG = BuildConfig.DEBUG;
    private static final int REQUEST_DEVICE_MODIFY = 328;
    private ListAdapter<SwitchBotDevice> mListAdapter;
    private SwitchBotMessageService mSwitchBotMessageService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mSwitchBotMessageService = (SwitchBotMessageService) ((DConnectMessageService.LocalBinder) iBinder).getMessageService();
            updateDeviceList();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSwitchBotMessageService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_and_delete);
        setTitle("デバイス更新・削除");

        Intent intent = new Intent(this, SwitchBotMessageService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        findViewById(R.id.button_delete).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    public void onItemClick(BluetoothDevice bluetoothDevice) {
        if (DEBUG) {
            Log.d(TAG, "onItemClick()");
            Log.d(TAG, "device address : " + bluetoothDevice.getAddress());
        }
    }

    private void updateDeviceList() {
        mListAdapter = new ListAdapter<>(
                mSwitchBotMessageService.getDeviceList(),
                R.layout.list_modify_and_delete_row,
                this);
        RecyclerView deviceList = findViewById(R.id.list_device);
        deviceList.setHasFixedSize(true);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        deviceList.setAdapter(mListAdapter);
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
        if (requestCode == REQUEST_DEVICE_MODIFY) {
            updateDeviceList();
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (DEBUG) {
            Log.d(TAG, "onClick()");
            Log.d(TAG, "view : " + view);
            Log.d(TAG, "viewId : " + viewId);
        }
        if (viewId == R.id.button_delete) {
            final ArrayList<SwitchBotDevice> checkedList = mListAdapter.getCheckedList();
            if (checkedList != null && checkedList.size() != 0) {
                new android.app.AlertDialog.Builder(this)
                        .setMessage("デバイス削除確認")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            mSwitchBotMessageService.unregisterDevices(checkedList);
                            updateDeviceList();
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
            }
        }
    }

    @Override
    public void onItemClick(SwitchBotDevice switchBotDevice) {
        if (DEBUG) {
            Log.d(TAG, "onItemClick()");
            Log.d(TAG, "device name : " + switchBotDevice.getDeviceName());
            Log.d(TAG, "device address : " + switchBotDevice.getDeviceAddress());
            Log.d(TAG, "device mode : " + switchBotDevice.getDeviceMode());
        }
        Intent intent = new Intent(this, ModifyActivity.class);
        intent.putExtra(ModifyActivity.KEY_DEVICE_NAME, switchBotDevice.getDeviceName());
        startActivityForResult(intent, REQUEST_DEVICE_MODIFY);
    }
}
