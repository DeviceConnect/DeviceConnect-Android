/*
 FPLUGConnectFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.setting.fragment;

import android.bluetooth.BluetoothDevice;
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

import org.deviceconnect.android.deviceplugin.fplug.FPLUGApplication;
import org.deviceconnect.android.deviceplugin.fplug.R;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGController;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGDiscover;
import org.deviceconnect.android.deviceplugin.fplug.setting.SettingActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment for connect to F-PLUG.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGConnectFragment extends Fragment {

    private ListAdapter mAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mAdapter = new ListAdapter(getActivity(), -1);

        final View root = inflater.inflate(R.layout.connect_fplug, container, false);

        root.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverFPlug(root);
            }
        });

        setupInitListener((ListView) root.findViewById(R.id.pluglist));
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((FPLUGApplication) getActivity().getApplication()).removeConnectionListener(mConnectionListener);
    }

    private void setupInitListener(ListView listview) {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FPlugItem fplug = (FPlugItem) parent.getItemAtPosition(position);
                if (fplug.isConnected) {
                    ((SettingActivity) getActivity()).showControllerPage(fplug.mDevice.getAddress());
                } else {
                    Toast.makeText(getActivity(), getString(R.string.setting_connect_not_connected), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void discoverFPlug(final View root) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
        ExecutorService s = Executors.newSingleThreadExecutor();
        s.submit(new Runnable() {
            @Override
            public void run() {
                final List<BluetoothDevice> fplugs = FPLUGDiscover.getAll();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (fplugs == null || fplugs.size() == 0) {
                            Toast.makeText(getActivity(), getString(R.string.setting_connect_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            ListView listview = (ListView) root.findViewById(R.id.pluglist);
                            for (BluetoothDevice fplug : fplugs) {
                                FPlugItem item = new FPlugItem();
                                item.mDevice = fplug;
                                item.isConnected = false;
                                mAdapter.add(item);
                            }
                            listview.setAdapter(mAdapter);
                            for (BluetoothDevice fplug : fplugs) {
                                ((FPLUGApplication) getActivity().getApplication()).connectFPlug(fplug, mConnectionListener);
                            }
                        }
                    }
                });
            }
        });
    }

    private class ListAdapter extends ArrayAdapter<FPlugItem> {

        public ListAdapter(Context context, int textViewId) {
            super(context, textViewId);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.text, null);
            }
            FPlugItem item = getItem(position);
            String text = getString(R.string.setting_connect_fplug_list, item.mDevice.getAddress());
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

    private class FPlugItem {
        BluetoothDevice mDevice;
        boolean isConnected = false;
    }

    private FPLUGController.FPLUGConnectionListener mConnectionListener = new FPLUGController.FPLUGConnectionListener() {
        @Override
        public void onConnected(String address) {
            updateStatus(address, true);
        }

        @Override
        public void onDisconnected(String address) {
            updateStatus(address, false);
        }

        @Override
        public void onConnectionError(String address, String message) {
            updateStatus(address, false);
        }

        private void updateStatus(String address, boolean isConnected) {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                FPlugItem item = mAdapter.getItem(i);
                if (item.mDevice.getAddress().equals(address)) {
                    item.isConnected = isConnected;
                }
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    };

}
