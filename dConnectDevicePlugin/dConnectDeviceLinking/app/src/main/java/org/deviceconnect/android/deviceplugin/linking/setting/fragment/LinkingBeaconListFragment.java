/*
 org.deviceconnect.android.deviceplugin.linking
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.setting.LinkingBeaconActivity;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.dialog.NoConnectLinkingBeaconDialogFragment;

public class LinkingBeaconListFragment extends Fragment implements NoConnectLinkingBeaconDialogFragment.OnDialogEventListener,
        LinkingBeaconManager.OnConnectListener {
    private ListAdapter mAdapter;

    public static LinkingBeaconListFragment newInstance() {
        return new LinkingBeaconListFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mAdapter = new ListAdapter(getActivity(), -1);

        final View root = inflater.inflate(R.layout.fragment_linking_beacon_list, container, false);

        ListView listView = (ListView) root.findViewById(R.id.fragment_beacon_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DeviceItem item = (DeviceItem) view.getTag();
                if (item != null) {
                    transitionBeaconControl(item);
                }
            }
        });
        listView.setAdapter(mAdapter);

        Button startScanBtn = (Button) root.findViewById(R.id.fragment_beacon_scan_start);
        startScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });

        Button stopScanBtn = (Button) root.findViewById(R.id.fragment_beacon_scan_stop);
        stopScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScan();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
        LinkingApplication app = (LinkingApplication) getActivity().getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.addOnConnectListener(this);
    }


    @Override
    public void onPause() {
        LinkingApplication app = (LinkingApplication) getActivity().getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.removeOnConnectListener(this);
        super.onPause();
    }

    @Override
    public void onPositiveClick() {

    }

    @Override
    public void onNegativeClick() {

    }

    @Override
    public void onConnected(LinkingBeacon beacon) {
        refresh();
    }

    @Override
    public void onDisconnected(LinkingBeacon beacon) {
        refresh();
    }

    private void refresh() {
        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();

                LinkingApplication app = (LinkingApplication) getActivity().getApplication();
                LinkingBeaconManager mgr = app.getLinkingBeaconManager();

                ListView listView = (ListView) getView().findViewById(R.id.fragment_beacon_list_view);
                for (LinkingBeacon device : mgr.getLinkingBeacons()) {
                    DeviceItem item = new DeviceItem();
                    item.mDevice = device;
                    mAdapter.add(item);
                }
                listView.setAdapter(mAdapter);
            }
        });
    }

    private void transitionBeaconControl(DeviceItem item) {
        if (item.mDevice.isOnline()) {
            Intent intent = new Intent();
            intent.putExtra(LinkingBeaconActivity.EXTRA_ID, item.mDevice.getExtraId());
            intent.putExtra(LinkingBeaconActivity.VENDOR_ID, item.mDevice.getVendorId());
            intent.setClass(getActivity(), LinkingBeaconActivity.class);
            getActivity().startActivity(intent);
        } else {
            NoConnectLinkingBeaconDialogFragment dialog = NoConnectLinkingBeaconDialogFragment.newInstance(this);
            dialog.show(getFragmentManager(), "error");
        }
    }

    private void startScan() {
        LinkingApplication app = (LinkingApplication) getActivity().getApplication();
        app.getLinkingBeaconManager().startBeaconScan();
    }

    private void stopScan() {
        LinkingApplication app = (LinkingApplication) getActivity().getApplication();
        app.getLinkingBeaconManager().stopBeaconScan();
    }

    private class ListAdapter extends ArrayAdapter<DeviceItem> {

        public ListAdapter(Context context, int textViewId) {
            super(context, textViewId);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_linking_device, null);
            }

            DeviceItem item = getItem(position);
            String deviceName = item.mDevice.getDisplayName();
            TextView textView = (TextView) convertView.findViewById(R.id.item_device_name);
            textView.setText(deviceName);
            TextView statusView = (TextView) convertView.findViewById(R.id.item_device_status);
            if (item.mDevice.isOnline()) {
                textView.setTextColor(Color.BLACK);
                statusView.setText(getString(R.string.fragment_device_status_online));
                statusView.setTextColor(Color.BLACK);
            } else {
                textView.setTextColor(Color.GRAY);
                statusView.setText(getString(R.string.fragment_device_status_offline));
                statusView.setTextColor(Color.GRAY);
            }
            convertView.setTag(item);
            return convertView;
        }
    }

    private class DeviceItem {
        LinkingBeacon mDevice;
    }
}
