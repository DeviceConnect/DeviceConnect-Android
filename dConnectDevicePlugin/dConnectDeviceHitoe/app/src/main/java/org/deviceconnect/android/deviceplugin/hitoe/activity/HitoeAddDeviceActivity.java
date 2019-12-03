/*
 HitoeAddDeviceActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.hitoe.R;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeConstants;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog.DefaultDialogFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog.PinCodeDialogFragment;
import org.deviceconnect.android.deviceplugin.hitoe.util.BleUtils;
import org.deviceconnect.android.deviceplugin.hitoe.util.HitoeScheduler;

import java.util.List;

/**
 * This activity is Add Device screen.
 * @author NTT DOCOMO, INC.
 */
public class HitoeAddDeviceActivity extends HitoeListActivity  implements HitoeManager.OnHitoeConnectionListener,
                                                                            AdapterView.OnItemClickListener,
                                                                            HitoeScheduler.OnRegularNotify {

    /**
     * Periodic processing object.
     */
    private HitoeScheduler mScheduler;
    /**
     * Received a event that Bluetooth has been changed.
     */
    private final BroadcastReceiver mSensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_ON) {
                    addFooterView();
                    getManager().addHitoeConnectionListener(HitoeAddDeviceActivity.this);
                    mScheduler.scanHitoeDevice(true);
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    addFooterView();
                    getManager().addHitoeConnectionListener(null);
                    mScheduler.scanHitoeDevice(false);
                }
            }
        }
    };


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        mEnableConnectedBtn = false;
        mDeviceAdapter.clear();
        mDeviceAdapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(this);
        mScheduler = new HitoeScheduler(this, -1, -1);
        DefaultDialogFragment.showHitoeONStateDialog(this);
        registerBluetoothFilter();
        getManager().addHitoeConnectionListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mScheduler.scanHitoeDevice(true);
        } else {
            if (BleUtils.isBLEPermission(this)) {
                mScheduler.scanHitoeDevice(true);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getManager().removeHitoeConnectionListener(this);
        mScheduler.scanHitoeDevice(false);
        dismissProgressDialog();
        dismissErrorDialog();
        unregisterBluetoothFilter();
    }

    @Override
    protected void setUI() {
        TextView title = (TextView) findViewById(R.id.view_title);
        title.setText(R.string.add_device_view);
        Button btn = (Button) findViewById(R.id.btn_add_open);
        btn.setText(R.string.action_search);
        btn.setOnClickListener((view) -> {
            getManager().addHitoeConnectionListener(HitoeAddDeviceActivity.this);
            getManager().discoveryHitoeDevices();
        });
    }


    /**
     * Register a BroadcastReceiver of Bluetooth event.
     */
    private void registerBluetoothFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mSensorReceiver, filter, null, mHandler);
    }

    /**
     * Unregister a previously registered BroadcastReceiver.
     */
    private void unregisterBluetoothFilter() {
        unregisterReceiver(mSensorReceiver);
    }



    @Override
    public void onConnected(final HitoeDevice device) {

        runOnUiThread(() -> {
            if (mCheckDialog) {
                DefaultDialogFragment.showHitoeSetShirtDialog(HitoeAddDeviceActivity.this);
            }
            dismissProgressDialog();
            mDeviceAdapter.remove(device);
            mDeviceAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onConnectFailed(final HitoeDevice device) {
        runOnUiThread(() -> {
            dismissProgressDialog();
            if (device == null) {
                if (mConnectingDevice != null) {
                    HitoeDevice container = findDeviceContainerByAddress(mConnectingDevice.getId());
                    if (container != null) {
                        container.setPinCode(null);
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                    Resources res = getResources();
                    showErrorDialog(res.getString(R.string.hitoe_setting_dialog_error_message03));
                }
            } else {
                showErrorDialogNotConnect(device.getName());
            }
        });
    }

    @Override
    public void onDiscovery(final List<HitoeDevice> devices) {
        if (mDeviceAdapter == null) {
            return;
        }
        runOnUiThread(() -> {
            mDeviceAdapter.clear();
            for (HitoeDevice device : devices) {
                if (device.getPinCode() == null) {
                    mDeviceAdapter.add(device);
                }
            }
            mDeviceAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDisconnected(final int res, final HitoeDevice device) {
    }

    @Override
    public void onDeleted(final HitoeDevice device) {

    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view,
                            final int i, final long l) {
        final HitoeDevice hitoe = (HitoeDevice) adapterView.getItemAtPosition(i);
        if (hitoe == null) {
            return;
        }
        mConnectingDevice = hitoe;
        if (hitoe.getPinCode() == null) {
            final Resources res = getResources();
            PinCodeDialogFragment pinDialog = PinCodeDialogFragment.newInstance();
            pinDialog.show(getSupportFragmentManager(), "pin_dialog");
            pinDialog.setOnPinCodeListener((pin) -> {
                if (pin.isEmpty()) {
                    showErrorDialog(res.getString(R.string.hitoe_setting_dialog_error_message02));
                    return;
                }
                hitoe.setPinCode(pin);
                for (HitoeDevice d: getManager().getRegisterDevices()) {
                    if (!d.getName().equals(hitoe.getName()) && d.isRegisterFlag()) {
                        getManager().disconnectHitoeDevice(d);
                    }
                }

                connectDevice(hitoe);
            });
        }
        new Handler().postDelayed(() -> {
            if (mCheckDialog) {
                HitoeDevice containar = findDeviceContainerByAddress(hitoe.getId());
                if (containar != null) {
                    containar.setPinCode(null);
                }

                runOnUiThread(() -> {
                    dismissProgressDialog();
                    Resources res = getResources();
                    showErrorDialog(res.getString(R.string.hitoe_setting_dialog_error_message04));
                });
            }
        }, HitoeConstants.DISCOVERY_CYCLE_TIME);

    }

    @Override
    public void onRegularNotify() {
        if (BleUtils.isEnabled(this)) {
            addFooterView();
            getManager().addHitoeConnectionListener(this);
            getManager().discoveryHitoeDevices();
        } else {
            mScheduler.scanHitoeDevice(false);
        }
    }
}
