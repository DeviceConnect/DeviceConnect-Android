/*
 LinkingDeviceListFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManagerFactory;
import org.deviceconnect.android.deviceplugin.linking.setting.SettingActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment for show Linking Devices.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingDeviceListFragment extends Fragment {

    private ListAdapter mAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mAdapter = new ListAdapter(getActivity(), -1);

        final View root = inflater.inflate(R.layout.device_list, container, false);

        root.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverDevices(root);
            }
        });

        setupClickListener((ListView) root.findViewById(R.id.devicelist));
        return root;
    }

    private void setupClickListener(ListView listview) {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final DeviceItem device = (DeviceItem) parent.getItemAtPosition(position);
                if (device.isConnected) {
                    ((SettingActivity) getActivity()).showControllerPage(device.mDevice);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.device_not_connected), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void discoverDevices(final View root) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
        ExecutorService s = Executors.newSingleThreadExecutor();
        s.submit(new Runnable() {
            @Override
            public void run() {
                LinkingManager manager = LinkingManagerFactory.createManager(getActivity());
                final List<LinkingDevice> devices = manager.getDevices();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (devices == null || devices.size() == 0) {
                            Toast.makeText(getActivity(), getString(R.string.device_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            ListView listview = (ListView) root.findViewById(R.id.devicelist);
                            for (LinkingDevice device : devices) {
                                DeviceItem item = new DeviceItem();
                                item.mDevice = device;
                                item.isConnected = device.isConnected();
                                mAdapter.add(item);
                            }
                            listview.setAdapter(mAdapter);
                        }
                    }
                });
            }
        });
    }

    private class ListAdapter extends ArrayAdapter<DeviceItem> {

        public ListAdapter(Context context, int textViewId) {
            super(context, textViewId);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.text, null);
            }
            DeviceItem item = getItem(position);
            String text = item.mDevice.getDisplayName();
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(text);
            if (item.isConnected) {
                textView.setTextColor(Color.BLUE);
            } else {
                textView.setTextColor(Color.RED);
            }
            convertView.setTag(item);
            return convertView;
        }
    }

    private class DeviceItem {
        LinkingDevice mDevice;
        boolean isConnected = false;
    }

}
