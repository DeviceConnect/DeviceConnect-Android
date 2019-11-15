/*
 HitoeDeviceListActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.hitoe.R;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog.DefaultDialogFragment;

import java.util.List;

/**
 * This activity is device list screen.
 * @author NTT DOCOMO, INC.
 */
public class HitoeDeviceListActivity extends HitoeListActivity implements
                                                HitoeManager.OnHitoeConnectionListener,
                                                 AdapterView.OnItemClickListener,
                                                 AdapterView.OnItemLongClickListener {

    /**
     * Received a event that Bluetooth has been changed.
     */
    private final BroadcastReceiver mSensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                addFooterView();
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
        mDeviceAdapter.clear();
        mEnableConnectedBtn = true;
        mDeviceAdapter.addAll(createDeviceContainers());
        mDeviceAdapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        getManager().addHitoeConnectionListener(this);
        addFooterView();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mSensorReceiver, filter, null, mHandler);
        DefaultDialogFragment.showHitoeWarningMessageDialog(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getManager().removeHitoeConnectionListener(this);
        unregisterReceiver(mSensorReceiver);
    }


    @Override
    protected void setUI() {
        TextView title = (TextView) findViewById(R.id.view_title);
        title.setText(R.string.device_list_view);
        Button btn = (Button) findViewById(R.id.btn_add_open);
        btn.setText(R.string.add_device_button);
        btn.setOnClickListener((view) -> {
            Intent intent = new Intent();
            intent.setClass(HitoeDeviceListActivity.this, HitoeAddDeviceActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view,
                                                            final int i, final long l) {
        final HitoeDevice hitoe = (HitoeDevice) adapterView.getItemAtPosition(i);
        if (hitoe == null || !hitoe.isRegisterFlag()) {
            Toast.makeText(this, R.string.error_disconnected_hitoe, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(HitoeDeviceControlActivity.FEATURE_SERVICE_ID, hitoe.getId());
        intent.setClass(HitoeDeviceListActivity.this, HitoeDeviceControlActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> adapterView, final View view,
                                                    final int i, final long l) {
        final HitoeDevice hitoe = (HitoeDevice) adapterView.getItemAtPosition(i);
        if (hitoe == null) {
            return false;
        }
        mConnectingDevice = hitoe;
        DefaultDialogFragment.showConfirmAlert(this, hitoe.getName(), getString(R.string.confirm_delete_device),
                getString(R.string.ok), (dialogInterface, ii) -> {
                    runOnUiThread(() -> {
                            disconnectDevice(hitoe);
                            getManager().deleteHitoeDevice(hitoe);
                            mDeviceAdapter.remove(hitoe);
                            mDeviceAdapter.notifyDataSetChanged();
                            addFooterView();
                    });
                });
        return true;
    }

    @Override
    public void onConnected(final HitoeDevice device) {

        runOnUiThread(() -> {
            if (!mCheckDialog) {
                return;
            }
            HitoeDevice container = findDeviceContainerByAddress(device.getId());
            if (container != null) {
                container.setRegisterFlag(true);
                container.setSessionId(device.getSessionId());
                mDeviceAdapter.notifyDataSetChanged();
            }

            dismissProgressDialog();
        });
    }

    @Override
    public void onConnectFailed(final HitoeDevice device) {
        runOnUiThread(() -> {
            if (!mCheckDialog) {
                return;
            }
            if (device == null && mConnectingDevice != null) {
                HitoeDevice container = findDeviceContainerByAddress(mConnectingDevice.getId());
                if (container != null) {
                    container.setPinCode(null);
                    mDeviceAdapter.notifyDataSetChanged();
                }
                Resources res = getResources();
                showErrorDialog(res.getString(R.string.hitoe_setting_dialog_error_message03));

            } else if (device != null) {
                showErrorDialogNotConnect(device.getName());
            }
            dismissProgressDialog();
        });
    }

    @Override
    public void onDiscovery(final List<HitoeDevice> devices) {
        if (mDeviceAdapter == null) {
            return;
        }
        runOnUiThread(() -> {
            for (HitoeDevice device : devices) {
                if (!containAddressForAdapter(device.getId())
                        && !device.isRegisterFlag()) {
                    mDeviceAdapter.add(device);
                }
            }
            mDeviceAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDisconnected(final int res, final HitoeDevice device) {
        HitoeDevice container = findDeviceContainerByAddress(device.getId());
        if (container != null) {
            if (res != 0) {
                container.setRegisterFlag(true);
            }
            runOnUiThread(() -> {
                mDeviceAdapter.notifyDataSetChanged();
            });
        }

    }

    @Override
    public void onDeleted(final HitoeDevice device) {

    }
}
