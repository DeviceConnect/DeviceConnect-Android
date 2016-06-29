/*
 HitoeDeviceListActivity
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.activity;

import android.content.DialogInterface;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEnableConnectedBtn = true;
        mDeviceAdapter.clear();
        mDeviceAdapter.addAll(createDeviceContainers());
        mDeviceAdapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        getManager().addHitoeConnectionListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        addFooterView();
    }


    @Override
    protected void setUI() {
        TextView title = (TextView) findViewById(R.id.view_title);
        title.setText(R.string.device_list_view);
        Button btn = (Button) findViewById(R.id.btn_add_open);
        btn.setText(R.string.add_device_button);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(HitoeDeviceListActivity.this, HitoeAddDeviceActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        final HitoeDevice hitoe = (HitoeDevice) adapterView.getItemAtPosition(i);
        if (hitoe == null) {
            return false;
        }
        mConnectingDevice = hitoe;
        DefaultDialogFragment.showConfirmAlert(this, hitoe.getName(), getString(R.string.confirm_delete_device),
                getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                disconnectDevice(hitoe);
                                getManager().deleteHitoeDevice(hitoe);
                                mDeviceAdapter.remove(hitoe);
                                mDeviceAdapter.notifyDataSetChanged();
                                addFooterView();

                            }
                        });
                    }
                });
        return true;
    }

    @Override
    public void onConnected(final HitoeDevice device) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCheckDialog = false;
                HitoeDevice container = findDeviceContainerByAddress(device.getId());
                if (container != null) {
                    container.setRegisterFlag(true);
                    mDeviceAdapter.notifyDataSetChanged();
                }

                dismissProgressDialog();
            }
        });
    }

    @Override
    public void onConnectFailed(final HitoeDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
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
            }
        });
    }

    @Override
    public void onDiscovery(final List<HitoeDevice> devices) {
        if (mDeviceAdapter == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (HitoeDevice device : devices) {
                    if (!containAddressForAdapter(device.getId())
                            && !device.isRegisterFlag()) {
                        mDeviceAdapter.add(device);
                    }
                }
                mDeviceAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDisconnected(HitoeDevice device) {

    }
}
