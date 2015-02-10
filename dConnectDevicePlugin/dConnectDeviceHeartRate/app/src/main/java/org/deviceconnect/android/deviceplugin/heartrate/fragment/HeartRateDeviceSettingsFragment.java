/*
 HeartRateDeviceSettingsFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.fragment;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import static org.deviceconnect.android.deviceplugin.heartrate.HeartRateManager.OnHeartRateDiscoveryListener;

/**
 * This fragment do setting of the connection to the ble device.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDeviceSettingsFragment extends Fragment {
    private DeviceAdapter mDeviceAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        List<HeartRateDevice> list = new ArrayList<>(getManager().getDiscoveryDevices());
        mDeviceAdapter = new DeviceAdapter(getActivity(), list);

        View rootView = inflater.inflate(R.layout.fragment_heart_rate_device_settings, null);
        ListView listView = (ListView) rootView.findViewById(R.id.device_list_view);
        listView.setAdapter(mDeviceAdapter);
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
    }

    /**
     * Connect to Ble device has heart rate service.
     * @param device Ble device has heart rate service.
     */
    private void connectDevice(HeartRateDevice device) {
        getManager().registerHeartRateDevice(device);
    }

    /**
     * Disconnect to Ble device has heart rate service.
     * @param device Ble device has heart rate service.
     */
    private void disconnectDevice(HeartRateDevice device) {
        getManager().unregisterHeartRateDevice(device);
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
        public void onDiscovery(final List<HeartRateDevice> devices) {
           getActivity().runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   mDeviceAdapter.clear();
                   mDeviceAdapter.addAll(devices);
                   mDeviceAdapter.notifyDataSetChanged();
               }
           });
        }
    };

    private class DeviceAdapter extends ArrayAdapter<HeartRateDevice> {
        private LayoutInflater mInflater;
        public DeviceAdapter(final Context context, final List<HeartRateDevice> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_heart_rate_device, null);
            }

            final HeartRateDevice device = getItem(position);

            TextView nameView = (TextView) convertView.findViewById(R.id.device_name);
            nameView.setText(device.getName());

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
