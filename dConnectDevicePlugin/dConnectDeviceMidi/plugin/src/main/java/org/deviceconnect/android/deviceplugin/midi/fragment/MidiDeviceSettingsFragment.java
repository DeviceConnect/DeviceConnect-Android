/*
 MidiDeviceSettingsFragment.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import org.deviceconnect.android.deviceplugin.midi.MidiMessageService;
import org.deviceconnect.android.deviceplugin.midi.R;
import org.deviceconnect.android.deviceplugin.midi.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.midi.MidiDeviceManager;
import org.deviceconnect.android.deviceplugin.midi.fragment.dialog.ErrorDialogFragment;
import org.deviceconnect.android.deviceplugin.midi.fragment.dialog.ProgressDialogFragment;
import org.deviceconnect.android.message.DConnectMessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.deviceconnect.android.deviceplugin.midi.MidiDeviceManager.OnDeviceDiscoveryListener;

/**
 * This fragment do setting of the connection to the ble device.
 *
 * @author NTT DOCOMO, INC.
 */
public class MidiDeviceSettingsFragment extends Fragment {
    private MidiDeviceManager mMidiDeviceManager;
    private boolean mBound;

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

    /**
     * Received a event that Bluetooth has been changed.
     */
    private final BroadcastReceiver mSensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_ON){
                    addFooterView();
                    getManager().startScanBle();
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    addFooterView();
                    getManager().stopScanBle();
                }
            }
        }
    };



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mDeviceAdapter = new DeviceAdapter(getActivity(), createDeviceContainers());

        mFooterView = inflater.inflate(R.layout.item_midi_ble_searching, null);

        View rootView = inflater.inflate(R.layout.fragment_midi_ble_device_settings, null);
        mListView = rootView.findViewById(R.id.device_list_view);
        mListView.setAdapter(mDeviceAdapter);
        mListView.setItemsCanFocus(true);
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        bindMessageService();
        registerBluetoothFilter();
    }

    @Override
    public void onPause() {
        dismissProgressDialog();
        dismissErrorDialog();

        unregisterBluetoothFilter();

        MidiDeviceManager mgr = getManager();
        if (mgr != null) {
            mgr.removeOnDeviceDiscoveryListener(mEvtListener);
            mgr.stopScanBle();
        }
        unbindMessageService();
        super.onPause();
    }

    private void bindMessageService() {
        if (!mBound) {
            Intent intent = new Intent(getActivity().getApplicationContext(), MidiMessageService.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindMessageService() {
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            DConnectMessageService.LocalBinder binder = (DConnectMessageService.LocalBinder) service;
            MidiMessageService midiMessageService = (MidiMessageService) binder.getMessageService();
            mMidiDeviceManager = midiMessageService.getMidiDeviceManager();
            startScanBle();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mBound = false;
        }
    };

    private void startScanBle() {
        MidiDeviceManager mgr = getManager();
        if (mgr != null) {
            updateView(null);

            mgr.addOnDeviceDiscoveryListener(mEvtListener);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                mgr.startScanBle();
            } else {
                if (BleUtils.isBLEPermission(getActivity())) {
                    mgr.startScanBle();
                }
            }
            addFooterView();
        }
    }

    /**
     * Added the view at ListView.
     */
    private void addFooterView() {
        getActivity().runOnUiThread(() -> {
            LayoutInflater inflater = getActivity().getLayoutInflater();

            if (mFooterView != null) {
                mListView.removeFooterView(mFooterView);
            }

            if (!BleUtils.isBLEPermission(getActivity())) {
                mFooterView = inflater.inflate(R.layout.item_midi_ble_error, null);
                TextView textView = (TextView) mFooterView.findViewById(R.id.error_message);
                textView.setText(getString(R.string.midi_ble_setting_dialog_error_permission));
            } else if (getManager().isEnabledBle()) {
                mFooterView = inflater.inflate(R.layout.item_midi_ble_searching, null);
            } else {
                mFooterView = inflater.inflate(R.layout.item_midi_ble_error, null);
                TextView textView = (TextView) mFooterView.findViewById(R.id.error_message);
                textView.setText(getString(R.string.midi_ble_setting_dialog_disable_bluetooth));

                mDeviceAdapter.clear();
                mDeviceAdapter.addAll(createDeviceContainers());
                mDeviceAdapter.notifyDataSetChanged();
            }

            mListView.addFooterView(mFooterView);
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
    private void connectDevice(final DeviceContainer device) {
        if (getManager().isEnabledBle() && mProgressDialogFragment == null) {
            getManager().connectBleDevice(device.getAddress());
            showProgressDialog(device.getName());
        }
    }

    /**
     * Disconnect to the BLE device that have heart rate service.
     *
     * @param device BLE device that have heart rate service.
     */
    private void disconnectDevice(final DeviceContainer device) {
        getManager().disconnectBleDevice(device.getAddress());
        getActivity().runOnUiThread(() -> {
            DeviceContainer container = findDeviceContainerByAddress(device.getAddress());
            if (container != null) {
                container.setRegisterFlag(false);
                mDeviceAdapter.notifyDataSetChanged();
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
        String title = res.getString(R.string.midi_ble_setting_connecting_title);
        String message = res.getString(R.string.midi_ble_setting_connecting_message, name);
        mProgressDialogFragment = ProgressDialogFragment.newInstance(title, message);
        mProgressDialogFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Dismiss the dialog of connecting a ble device.
     */
    private void dismissProgressDialog() {
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
            message = res.getString(R.string.midi_ble_setting_dialog_error_message,
                    getString(R.string.midi_ble_setting_default_name));
        } else {
            message = res.getString(R.string.midi_ble_setting_dialog_error_message, name);
        }
        showErrorDialog(message);
    }

    /**
     * Display the error dialog for no permissions.
     */
    private void showErrorDialogNoPermissions() {
        Resources res = getActivity().getResources();
        String message = res.getString(R.string.midi_ble_setting_dialog_error_permission);
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
        String title = res.getString(R.string.midi_ble_setting_dialog_error_title);
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
     * Gets a instance of MidiDeviceManager.
     *
     * @return MidiDeviceManager
     */
    private MidiDeviceManager getManager() {
        return mMidiDeviceManager;
    }

    private void runOnUiThread(final Runnable run) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(run);
        }
    }

    private OnDeviceDiscoveryListener mEvtListener = new OnDeviceDiscoveryListener() {
        @Override
        public void onConnected(final MidiDevice midiDevice) {
            Bundle props = midiDevice.getInfo().getProperties();
            if (props == null) {
                return;
            }
            BluetoothDevice device = props.getParcelable(MidiDeviceInfo.PROPERTY_BLUETOOTH_DEVICE);
            if (device == null) {
                return;
            }

            runOnUiThread(() -> {
                DeviceContainer container = findDeviceContainerByAddress(device.getAddress());
                if (container != null) {
                    container.setRegisterFlag(true);
                    mDeviceAdapter.notifyDataSetChanged();
                }
                dismissProgressDialog();
            });
        }

        @Override
        public void onConnectFailed(final BluetoothDevice device) {
            runOnUiThread(() -> {
                dismissProgressDialog();
                showErrorDialogNotConnect(device.getName());
            });
        }

        @Override
        public void onDisconnected(final MidiDeviceInfo deviceInfo) {
        }

        @Override
        public void onDiscovery(final BluetoothDevice[] devices) {
            updateView(devices);
        }

        @Override
        public void onDiscovery(final MidiDeviceInfo[] devices) {
        }
    };

    private void updateView(final BluetoothDevice[] devices) {
        if (mDeviceAdapter == null) {
            return;
        }
        runOnUiThread(() -> {
            mDeviceAdapter.clear();
            mDeviceAdapter.addAll(createDeviceContainers());
            if (devices != null) {
                for (BluetoothDevice device : devices) {
                    if (!containAddressForAdapter(device.getAddress())) {
                        mDeviceAdapter.add(createContainer(device));
                    }
                }
            }
            mDeviceAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Create a list of device.
     *
     * @return list of device
     */
    private List<DeviceContainer> createDeviceContainers() {
        List<DeviceContainer> containers = new ArrayList<>();
        MidiDeviceManager mgr = getManager();
        if (mgr != null) {

//            List<HeartRateDevice> devices = mgr.getRegisterDevices();
//            if (devices != null) {
//                for (HeartRateDevice device : devices) {
//                    containers.add(createContainer(device, true));
//                }
//            }

            // MEMO: add of device that are paired to smart phone.
            Set<BluetoothDevice> pairing = mgr.getBondedDevices();
            if (pairing != null) {
                for (BluetoothDevice device : pairing) {
                    String name = device.getName();
                    if (name != null && !containAddressForList(containers, device.getAddress())) {
                        containers.add(createContainer(device));
                    }
                }
            }
        }
        return containers;
    }

    /**
     * Returns true if this address contains the list of device.
     *
     * @param containers list of device
     * @param address    address of device
     * @return true if address is an element of this List, false otherwise
     */
    private boolean containAddressForList(final List<DeviceContainer> containers, final String address) {
        for (DeviceContainer container : containers) {
            if (container.getAddress().equalsIgnoreCase(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Look for a DeviceContainer with the given address.
     *
     * @param address address of device
     * @return The DeviceContainer that has the given address or null
     */
    private DeviceContainer findDeviceContainerByAddress(final String address) {
        int size = mDeviceAdapter.getCount();
        for (int i = 0; i < size; i++) {
            DeviceContainer container = mDeviceAdapter.getItem(i);
            if (container.getAddress().equalsIgnoreCase(address)) {
                return container;
            }
        }
        return null;
    }

    /**
     * Create a DeviceContainer from BluetoothDevice.
     *
     * @param device Instance of BluetoothDevice
     * @return Instance of DeviceContainer
     */
    private DeviceContainer createContainer(final BluetoothDevice device) {
        DeviceContainer container = new DeviceContainer();
        container.setName(device.getName(), device.getAddress());
        return container;
    }

    /**
     * Create a DeviceContainer from HeartRateDevice.
     *
     * @param device   Instance of HeartRateDevice
     * @param register Registration flag
     * @return Instance of DeviceContainer
     */
    private DeviceContainer createContainer(final BluetoothDevice device, final boolean register) {
        DeviceContainer container = new DeviceContainer();
        container.setName(device.getName(), device.getAddress());
        container.setRegisterFlag(register);
        return container;
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
            DeviceContainer container = mDeviceAdapter.getItem(i);
            if (container.getAddress().equalsIgnoreCase(address)) {
                return true;
            }
        }
        return false;
    }

    private class DeviceContainer {
        private String mName;
        private String mAddress;
        private boolean mRegisterFlag;

        public String getName() {
            return mName;
        }

        public void setName(final String name, final String address) {
            if (name == null) {
                mName = address;
            } else {
                mName = name;
            }
            mAddress = address;
        }

        public String getAddress() {
            return mAddress;
        }

        public boolean isRegisterFlag() {
            return mRegisterFlag;
        }

        public void setRegisterFlag(boolean registerFlag) {
            mRegisterFlag = registerFlag;
        }
    }

    private class DeviceAdapter extends ArrayAdapter<DeviceContainer> {
        private LayoutInflater mInflater;

        public DeviceAdapter(final Context context, final List<DeviceContainer> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_midi_ble_device, null);
            }

            final DeviceContainer device = getItem(position);

            String name = device.getName();
            if (device.isRegisterFlag()) {
                if (getManager() != null && getManager().containsConnectedBleDevice(device.getAddress())) {
                    name += " " + getResources().getString(R.string.midi_ble_setting_online);
                } else {
                    name += " " + getResources().getString(R.string.midi_ble_setting_offline);
                }
            }

            TextView nameView = convertView.findViewById(R.id.device_name);
            nameView.setText(name);

            TextView addressView = convertView.findViewById(R.id.device_address);
            addressView.setText(device.getAddress());

            Button btn = convertView.findViewById(R.id.btn_connect_device);
            if (device.isRegisterFlag()) {
                btn.setBackgroundResource(R.drawable.button_red);
                btn.setText(R.string.midi_ble_setting_disconnect);
            } else {
                btn.setBackgroundResource(R.drawable.button_blue);
                btn.setText(R.string.midi_ble_setting_connect);
            }
            btn.setOnClickListener((v) -> {
                if (device.isRegisterFlag()) {
                    disconnectDevice(device);
                } else {
                    connectDevice(device);
                }
            });

            return convertView;
        }
    }
}
