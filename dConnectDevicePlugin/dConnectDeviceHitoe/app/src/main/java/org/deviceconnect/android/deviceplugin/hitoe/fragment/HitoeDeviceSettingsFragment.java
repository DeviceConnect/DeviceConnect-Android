/*
 HitoeDeviceSettingsFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.R;
import org.deviceconnect.android.deviceplugin.hitoe.activity.HitoeDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.hitoe.util.BleUtils;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog.ErrorDialogFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog.PinCodeDialogFragment;
import org.deviceconnect.android.deviceplugin.hitoe.fragment.dialog.ProgressDialogFragment;

import java.util.List;


/**
 * This fragment do setting of the connection to the ble device.
 *
 * @author NTT DOCOMO, INC.
 */
public class HitoeDeviceSettingsFragment extends Fragment implements HitoeManager.OnHitoeConnectionListener {
    /**
     * Adapter.
     */
    private DeviceAdapter mDeviceAdapter;

    /**
     * Error Dialog.
     */
    private ErrorDialogFragment mErrorDialogFragment;

    /**
     * Progress Dialog.
     */
    private ProgressDialogFragment mProgressDialogFragment;

    /**
     * Handler.
     */
    private Handler mHandler = new Handler();

    /**
     * Bluetooth device list view.
     */
    private ListView mListView;

    /**
     * footer view.
     */
    private View mFooterView;

    private boolean mCheckDialog;

    private HitoeDevice mConnectingDevice;

    /**
     * Received a event that Bluetooth has been changed.
     */
    private final BroadcastReceiver mSensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_ON ){
                    addFooterView();
                    mDeviceAdapter.clear();
                    mDeviceAdapter.notifyDataSetChanged();
                    getManager().addHitoeConnectionListener(HitoeDeviceSettingsFragment.this);
                    getManager().discoveryHitoeDevices();
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    addFooterView();
                    getManager().addHitoeConnectionListener(null);
//                    getManager().stopScanBle();
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mDeviceAdapter = new DeviceAdapter(getActivity(), createDeviceContainers());

        mFooterView = inflater.inflate(R.layout.item_hitoe_searching, null);

        View rootView = inflater.inflate(R.layout.fragment_device_list, null);
        rootView.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                addFooterView();
                mDeviceAdapter.clear();
                mDeviceAdapter.notifyDataSetChanged();

                getManager().addHitoeConnectionListener(HitoeDeviceSettingsFragment.this);
                getManager().discoveryHitoeDevices();
            }
        });
        mListView = (ListView) rootView.findViewById(R.id.device_list);
        mListView.setAdapter(mDeviceAdapter);
        mListView.setItemsCanFocus(true);
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        registerBluetoothFilter();

        getManager().addHitoeConnectionListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getManager().discoveryHitoeDevices();
        } else {
            if (BleUtils.isBLEPermission(getActivity())) {
                getManager().discoveryHitoeDevices();
                addFooterView();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getManager().addHitoeConnectionListener(null);
//        getManager().stopScanBle();
        dismissProgressDialog();
        dismissErrorDialog();
        unregisterBluetoothFilter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Added the view at ListView.
     */
    private void addFooterView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = getActivity().getLayoutInflater();

                if (mFooterView != null) {
                    mListView.removeFooterView(mFooterView);
                }

                if (!BleUtils.isBLEPermission(getActivity())) {
                    mFooterView = inflater.inflate(R.layout.item_hitoe_error, null);
                    TextView textView = (TextView) mFooterView.findViewById(R.id.error_message);
                    textView.setText(getString(R.string.hitoe_setting_dialog_error_permission));
                } else if (BleUtils.isEnabled(getActivity())) {
                    mFooterView = inflater.inflate(R.layout.item_hitoe_searching, null);
                } else {
                    mFooterView = inflater.inflate(R.layout.item_hitoe_error, null);
                    TextView textView = (TextView) mFooterView.findViewById(R.id.error_message);
                    textView.setText(getString(R.string.hitoe_setting_dialog_disable_bluetooth));

                    mDeviceAdapter.clear();
                    mDeviceAdapter.addAll(createDeviceContainers());
                    mDeviceAdapter.notifyDataSetChanged();
                }

                mListView.addFooterView(mFooterView);
            }
        });
    }

    /**
     * Register a BroadcastReceiver of Bluetooth event.
     */
    private void registerBluetoothFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(mSensorReceiver, filter, null, mHandler);
    }

    /**
     * Unregister a previously registered BroadcastReceiver.
     */
    private void unregisterBluetoothFilter() {
        getActivity().unregisterReceiver(mSensorReceiver);
    }

    /**
     * Connect to the BLE device that have heart rate service.
     *
     * @param device BLE device that have heart rate service.
     */
    private void connectDevice(final HitoeDevice device) {
        if (BleUtils.isEnabled(getContext())) {
            mConnectingDevice = device;
            getManager().connectHitoeDevice(device);
            showProgressDialog(device.getName());
        }
    }

    /**
     * Disconnect to the BLE device that have heart rate service.
     *
     * @param device BLE device that have heart rate service.
     */
    private void disconnectDevice(final HitoeDevice device) {
        getManager().disconnectHitoeDevice(device);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HitoeDevice container = findDeviceContainerByAddress(device.getId());
                if (container != null) {
                    container.setRegisterFlag(false);
                    mDeviceAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Display the dialog of connecting a ble device.
     *
     * @param name device name
     */
    private void showProgressDialog(final String name) {
        dismissProgressDialog();

        Resources res = getActivity().getResources();
        String title = res.getString(R.string.hitoe_setting_connecting_title);
        String message = res.getString(R.string.hitoe_setting_connecting_message, name);
        mProgressDialogFragment = ProgressDialogFragment.newInstance(title, message);
        mProgressDialogFragment.show(getFragmentManager(), "dialog");
        mCheckDialog = true;
    }

    /**
     * Dismiss the dialog of connecting a ble device.
     */
    private void dismissProgressDialog() {
        mCheckDialog = false;
        if (mProgressDialogFragment != null) {
            mProgressDialogFragment.dismiss();
            mProgressDialogFragment = null;
        }
    }

    /**
     * Display the error dialog of not connect device.
     *
     * @param name device name
     */
    private void showErrorDialogNotConnect(final String name) {
        Resources res = getActivity().getResources();
        String message;
        if (name == null) {
            message = res.getString(R.string.hitoe_setting_dialog_error_message,
                    getString(R.string.hitoe_setting_default_name));
        } else {
            message = res.getString(R.string.hitoe_setting_dialog_error_message, name);
        }
        showErrorDialog(message);
    }

    /**
     * Display the error dialog for no permissions.
     */
    private void showErrorDialogNoPermissions() {
        Resources res = getActivity().getResources();
        String message = res.getString(R.string.hitoe_setting_dialog_error_permission);
        showErrorDialog(message);
    }

    /**
     * Display the error dialog.
     *
     * @param message error message
     */
    public void showErrorDialog(final String message) {
        dismissErrorDialog();

        Resources res = getActivity().getResources();
        String title = res.getString(R.string.hitoe_setting_dialog_error_title);
        mErrorDialogFragment = ErrorDialogFragment.newInstance(title, message);
        mErrorDialogFragment.show(getFragmentManager(), "error_dialog");
        mErrorDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mErrorDialogFragment = null;
            }
        });
    }

    /**
     * Dismiss the error dialog.
     */
    private void dismissErrorDialog() {
        if (mErrorDialogFragment != null) {
            mErrorDialogFragment.dismiss();
            mErrorDialogFragment = null;
        }
    }

    /**
     * Gets a instance of HitoeManager.
     *
     * @return HitoeManager
     */
    private HitoeManager getManager() {
        HitoeDeviceSettingsActivity activity =
                (HitoeDeviceSettingsActivity) getActivity();
        HitoeApplication application =
                (HitoeApplication) activity.getApplication();
        return application.getHitoeManager();
    }



    /**
     * Create a list of device.
     *
     * @return list of device
     */
    private List<HitoeDevice> createDeviceContainers() {
        return getManager().getRegisterDevices();
    }


    /**
     * Look for a DeviceContainer with the given address.
     *
     * @param address address of device
     * @return The DeviceContainer that has the given address or null
     */
    private HitoeDevice findDeviceContainerByAddress(final String address) {
        int size = mDeviceAdapter.getCount();
        for (int i = 0; i < size; i++) {
            HitoeDevice container = mDeviceAdapter.getItem(i);
            if (container.getId().equalsIgnoreCase(address)) {
                return container;
            }
        }
        return null;
    }


    /**
     * Returns true if this address contains the mDeviceAdapter.
     *
     * @param address address of device
     * @return true if address is an element of mDeviceAdapter, false otherwise
     */
    private boolean containAddressForAdapter(final String address) {
        int size = mDeviceAdapter.getCount();
        for (int i = 0; i < size; i++) {
            HitoeDevice container = mDeviceAdapter.getItem(i);
            if (container.getId().equalsIgnoreCase(address)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConnected(final HitoeDevice device) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HitoeDevice container = findDeviceContainerByAddress(device.getId());
                if (container != null) {
//                    container.setRegisterFlag(true);
                    mDeviceAdapter.notifyDataSetChanged();
                }
                dismissProgressDialog();
            }
        });
    }

    @Override
    public void onConnectFailed(final HitoeDevice device) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                if (device == null) {
                    HitoeDevice container = findDeviceContainerByAddress(mConnectingDevice.getId());
                    if (container != null) {
                        container.setPinCode(null);
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                    Resources res = getActivity().getResources();
                    showErrorDialog(res.getString(R.string.hitoe_setting_dialog_error_message03));
                } else {
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
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceAdapter.clear();
//                mDeviceAdapter.addAll(getManager().getRegisterDevices());
                for (HitoeDevice existDevice: getManager().getRegisterDevices()) {
                    if (existDevice.isRegisterFlag()) {
                        mDeviceAdapter.add(existDevice);
                    }
                }
                for (HitoeDevice device : devices) {
                    if (!containAddressForAdapter(device.getId())) {
                        mDeviceAdapter.add(device);
                    }
                }
                mDeviceAdapter.notifyDataSetChanged();
                dismissProgressDialog();
            }
        });
    }

    @Override
    public void onDisconnected(final HitoeDevice device) {
    }

    private class DeviceAdapter extends ArrayAdapter<HitoeDevice> {
        private LayoutInflater mInflater;

        public DeviceAdapter(final Context context, final List<HitoeDevice> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_hitoe_device, null);
            }

            final HitoeDevice device = getItem(position);

            String name = device.getName();
            if (device.isRegisterFlag()) {
                if (getManager().containConnectedHitoeDevice(device.getId())) {
                    name += " " + getResources().getString(R.string.hitoe_setting_online);
                } else {
                    name += " " + getResources().getString(R.string.hitoe_setting_offline);
                }
            }

            TextView nameView = (TextView) convertView.findViewById(R.id.device_name);
            nameView.setText(name);

            TextView addressView = (TextView) convertView.findViewById(R.id.device_address);
            addressView.setText(device.getId());

            final Button btn = (Button) convertView.findViewById(R.id.btn_connect_device);
            if (device.isRegisterFlag()) {
                btn.setBackgroundResource(R.drawable.button_red);
                btn.setText(R.string.hitoe_setting_disconnect);
            } else {
                btn.setBackgroundResource(R.drawable.button_blue);
                btn.setText(R.string.hitoe_setting_connect);
            }
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (device.isRegisterFlag()) {
                        btn.setBackgroundResource(R.drawable.button_blue);
                        btn.setText(R.string.hitoe_setting_connect);
                        disconnectDevice(device);
                    } else {
                        if (device.getPinCode() == null) {
                            final Resources res = getActivity().getResources();
                            String title = res.getString(R.string.hitoe_setting_dialog_pin_title);
                            PinCodeDialogFragment pinDialog = PinCodeDialogFragment.newInstance(title);
                            pinDialog.show(getFragmentManager(), "pin_dialog");
                            pinDialog.setOnPinCodeListener(new PinCodeDialogFragment.OnPinCodeListener() {
                                @Override
                                public void onPinCode(String pin) {
                                    if (pin.isEmpty()) {
                                        showErrorDialog(res.getString(R.string.hitoe_setting_dialog_error_message02));
                                        return;
                                    }
                                    device.setPinCode(pin);
                                    connectDevice(device);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mCheckDialog) {
                                                device.setPinCode(null);
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        dismissProgressDialog();
                                                        Resources res = getActivity().getResources();
                                                        showErrorDialog(res.getString(R.string.hitoe_setting_dialog_error_message04));

                                                    }
                                                });
                                            }
                                        }
                                    },  10000);
                                }
                            });
                        } else {
                            connectDevice(device);
                        }

                    }
                }
            });

            return convertView;
        }
    }
}
