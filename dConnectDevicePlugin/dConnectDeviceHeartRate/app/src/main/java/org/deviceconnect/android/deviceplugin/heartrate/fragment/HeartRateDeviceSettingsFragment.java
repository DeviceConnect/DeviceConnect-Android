/*
 HeartRateDeviceSettingsFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.heartrate.HeartRateApplication;
import org.deviceconnect.android.deviceplugin.heartrate.HeartRateManager;
import org.deviceconnect.android.deviceplugin.heartrate.R;
import org.deviceconnect.android.deviceplugin.heartrate.activity.HeartRateDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;
import org.deviceconnect.android.deviceplugin.heartrate.fragment.dialog.ErrorDialogFragment;
import org.deviceconnect.android.deviceplugin.heartrate.fragment.dialog.ProgressDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.deviceconnect.android.deviceplugin.heartrate.HeartRateManager.OnHeartRateDiscoveryListener;

/**
 * This fragment do setting of the connection to the ble device.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDeviceSettingsFragment extends Fragment {
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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        mDeviceAdapter = new DeviceAdapter(getActivity(), createDeviceContainers());

        View rootView = inflater.inflate(R.layout.fragment_heart_rate_device_settings, null);
        ListView listView = (ListView) rootView.findViewById(R.id.device_list_view);
        listView.setAdapter(mDeviceAdapter);
        listView.setItemsCanFocus(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getManager().setOnHeartRateDiscoveryListener(mEvtListener);
        getManager().startScanBle();
    }

    @Override
    public void onPause() {
        super.onPause();
        getManager().setOnHeartRateDiscoveryListener(null);
        getManager().stopScanBle();
        dismissProgressDialog();
        dismissErrorDialog();
    }

    /**
     * Connect to the BLE device that have heart rate service.
     * @param device BLE device that have heart rate service.
     */
    private void connectDevice(final DeviceContainer device) {
        getManager().connectBleDevice(device.getAddress());
        showProgressDialog(device.getName());
    }

    /**
     * Disconnect to the BLE device that have heart rate service.
     * @param device BLE device that have heart rate service.
     */
    private void disconnectDevice(final DeviceContainer device) {
        getManager().disconnectBleDevice(device.getAddress());

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DeviceContainer container = findDeviceContainerByAddress(device.getAddress());
                if (container != null) {
                    container.setRegisterFlag(false);
                    mDeviceAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Display the dialog of connecting a ble device.
     * @param name device name
     */
    private void showProgressDialog(final String name) {
        dismissProgressDialog();

        Resources res = getActivity().getResources();
        String title = res.getString(R.string.heart_rate_setting_connecting_title);
        String message = res.getString(R.string.heart_rate_setting_connecting_message, name);
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
     * Display the error dialog.
     * @param name device name
     */
    private void showErrorDialog(final String name) {
        dismissErrorDialog();

        Resources res = getActivity().getResources();
        String title = res.getString(R.string.heart_rate_setting_dialog_error_title);
        String message;
        if (name == null) {
            message = res.getString(R.string.heart_rate_setting_dialog_error_message,
                    getString(R.string.heart_rate_setting_default_name));
        } else {
            message = res.getString(R.string.heart_rate_setting_dialog_error_message, name);
        }
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
     * Gets a instance of HeartRateManager.
     * @return HeartRateManager
     */
    private HeartRateManager getManager() {
        HeartRateDeviceSettingsActivity activity =
                (HeartRateDeviceSettingsActivity) getActivity();
        HeartRateApplication application =
                (HeartRateApplication) activity.getApplication();
        return application.getHeartRateManager();
    }

    private OnHeartRateDiscoveryListener mEvtListener = new OnHeartRateDiscoveryListener() {
        @Override
        public void onConnected(final BluetoothDevice device) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DeviceContainer container = findDeviceContainerByAddress(device.getAddress());
                    if (container != null) {
                        container.setRegisterFlag(true);
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                    dismissProgressDialog();
                }
            });
        }

        @Override
        public void onConnectFailed(final BluetoothDevice device) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissProgressDialog();
                    showErrorDialog(device.getName());
                }
            });
        }

        @Override
        public void onDiscovery(final List<BluetoothDevice> devices) {
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
                    mDeviceAdapter.addAll(createDeviceContainers());
                    for (BluetoothDevice device : devices) {
                        if (!containAddressForAdapter(device.getAddress())) {
                            mDeviceAdapter.add(createContainer(device));
                        }
                    }
                    mDeviceAdapter.notifyDataSetChanged();
                }
           });
        }
    };

    /**
     * Create a list of device.
     * @return list of device
     */
    private List<DeviceContainer> createDeviceContainers() {
        List<DeviceContainer> containers = new ArrayList<>();
        List<HeartRateDevice> devices = getManager().getRegisterDevices();
        for (HeartRateDevice device : devices) {
            containers.add(createContainer(device, true));
        }

        // MEMO: add of device that are paired to smart phone.
        Set<BluetoothDevice> pairing = getManager().getBondedDevices();
        if (pairing != null) {
            for (BluetoothDevice device : pairing) {
                String name = device.getName();
                if (name != null && name.contains("PS-100")
                        && !containAddressForList(containers, device.getAddress())) {
                    containers.add(createContainer(device));
                }
            }
        }

        return containers;
    }

    /**
     * Returns true if this address contains the list of device.
     * @param containers list of device
     * @param address address of device
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
     * @param device Instance of BluetoothDevice
     * @return Instance of DeviceContainer
     */
    private DeviceContainer createContainer(final BluetoothDevice device) {
        DeviceContainer container = new DeviceContainer();
        container.setName(device.getName());
        container.setAddress(device.getAddress());
        return container;
    }

    /**
     * Create a DeviceContainer from HeartRateDevice.
     * @param device Instance of HeartRateDevice
     * @param register Registration flag
     * @return Instance of DeviceContainer
     */
    private DeviceContainer createContainer(final HeartRateDevice device, final boolean register) {
        DeviceContainer container = new DeviceContainer();
        container.setName(device.getName());
        container.setAddress(device.getAddress());
        container.setRegisterFlag(register);
        return container;
    }

    /**
     * Returns true if this address contains the mDeviceAdapter.
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

        public void setName(final String name) {
            if (name == null) {
                mName = getActivity().getResources().getString(
                    R.string.heart_rate_setting_default_name);
            } else {
                mName = name;
            }
        }

        public String getAddress() {
            return mAddress;
        }

        public void setAddress(final String address) {
            mAddress = address;
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
                convertView = mInflater.inflate(R.layout.item_heart_rate_device, null);
            }

            final DeviceContainer device = getItem(position);

            String name = device.getName();
            if (device.isRegisterFlag()) {
                if (getManager().containConnectedHeartRateDevice(device.getAddress())) {
                    name += " " + getResources().getString(R.string.heart_rate_setting_online);
                } else {
                    name += " " + getResources().getString(R.string.heart_rate_setting_offline);
                }
            }

            TextView nameView = (TextView) convertView.findViewById(R.id.device_name);
            nameView.setText(name);

            TextView addressView = (TextView) convertView.findViewById(R.id.device_address);
            addressView.setText(device.getAddress());

            Button btn = (Button) convertView.findViewById(R.id.btn_connect_device);
            if (device.isRegisterFlag()) {
                btn.setBackgroundResource(R.drawable.button_red);
                btn.setText(R.string.heart_rate_setting_disconnect);
            } else {
                btn.setBackgroundResource(R.drawable.button_blue);
                btn.setText(R.string.heart_rate_setting_connect);
            }
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (device.isRegisterFlag()) {
                        disconnectDevice(device);
                    } else {
                        connectDevice(device);
                    }
                }
            });

            return convertView;
        }
    }
}
