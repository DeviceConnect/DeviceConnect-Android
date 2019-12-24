/*
 LinkingDeviceListFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.lib.R;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.setting.LinkingDeviceActivity;
import org.deviceconnect.android.deviceplugin.linking.setting.LinkingInductionActivity;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.dialog.ConfirmationDialogFragment;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.dialog.DiscoveryDeviceDialogFragment;

import java.util.List;

/**
 * Fragment for show Linking Devices.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingDeviceListFragment extends Fragment implements ConfirmationDialogFragment.OnDialogEventListener {

    private DiscoveryDeviceDialogFragment mDiscoveryDeviceDialogFragment;
    private ListAdapter mAdapter;

    public static LinkingDeviceListFragment newInstance() {
        return new LinkingDeviceListFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mAdapter = new ListAdapter(getActivity(), -1);

        final View root = inflater.inflate(R.layout.fragment_linking_device_list, container, false);

        ListView listView = root.findViewById(R.id.fragment_device_list_view);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            DeviceItem item = (DeviceItem) view.getTag();
            if (item != null) {
                transitionDeviceControl(item);
            }
        });
        listView.setAdapter(mAdapter);

        Button searchBtn = root.findViewById(R.id.fragment_device_search);
        searchBtn.setOnClickListener((v) -> {
            discoverDevices(root);
        });

        Button linkingBtn = root.findViewById(R.id.fragment_device_guidance_btn);
        linkingBtn.setOnClickListener((v) -> {
            transitionLinkingApp();
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        discoverDevices(getView());

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (mgr != null) {
            mgr.addConnectListener(mOnConnectListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (mgr != null) {
            mgr.removeConnectListener(mOnConnectListener);
        }
    }

    @Override
    public void onPositiveClick(final DialogFragment fragment) {
        transitionLinkingApp();
    }

    @Override
    public void onNegativeClick(final DialogFragment fragment) {
        // do nothing
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = (LinkingApplication) getActivity().getApplication();
        return app.getLinkingDeviceManager();
    }

    private void transitionDeviceControl(final DeviceItem item) {
        if (item.isConnected) {
            Intent intent = new Intent();
            intent.putExtra(LinkingDeviceActivity.EXTRA_ADDRESS, item.mDevice.getBdAddress());
            intent.setClass(getContext(), LinkingDeviceActivity.class);
            getActivity().startActivity(intent);
        } else {
            String title = getString(R.string.fragment_device_error_title);
            String message = getString(R.string.fragment_device_error_message, item.mDevice.getDisplayName());
            String positive = getString(R.string.fragment_device_error_positive);
            String negative = getString(R.string.fragment_device_error_negative);
            ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance(title, message, positive, negative, this);
            dialog.show(getFragmentManager(), "error");
        }
    }

    private void transitionLinkingApp() {
        Intent intent = new Intent();
        intent.setClass(getContext(), LinkingInductionActivity.class);
        getActivity().startActivity(intent);
    }

    private void showGuidance() {
        if (getView() == null) {
            return;
        }

        View view = getView().findViewById(R.id.fragment_device_guidance);
        if (view != null) {
            if (mAdapter.getCount() == 0) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    private void refreshDeviceList() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            discoverDevices(getView());
        });
    }

    private void discoverDevices(final View root) {

        if (mDiscoveryDeviceDialogFragment != null) {
            return;
        }

        if (root == null) {
            return;
        }

        AsyncTask<Void, Void, List<LinkingDevice>> task = new AsyncTask<Void, Void, List<LinkingDevice>>() {
            @Override
            protected void onPreExecute() {
                mDiscoveryDeviceDialogFragment = DiscoveryDeviceDialogFragment.newInstance(getString(R.string.fragment_device_discovery));
                mDiscoveryDeviceDialogFragment.show(getFragmentManager(), "progress");
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            protected List<LinkingDevice> doInBackground(final Void... params) {
                try {
                    LinkingApplication app = (LinkingApplication) getActivity().getApplicationContext();
                    LinkingDeviceManager manager = app.getLinkingDeviceManager();
                    return manager.getDevices();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final List<LinkingDevice> devices) {
                mDiscoveryDeviceDialogFragment.dismiss();
                mDiscoveryDeviceDialogFragment = null;

                if (devices != null) {
                    ListView listView = root.findViewById(R.id.fragment_device_list_view);
                    for (LinkingDevice device : devices) {
                        DeviceItem item = new DeviceItem();
                        item.mDevice = device;
                        item.isConnected = device.isConnected();
                        mAdapter.add(item);
                    }
                    listView.setAdapter(mAdapter);
                }
                showGuidance();
            }
        };
        task.execute();
    }

    private class ListAdapter extends ArrayAdapter<DeviceItem> {

        public ListAdapter(final Context context, final int textViewId) {
            super(context, textViewId);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_linking_device, null);
            }

            DeviceItem item = getItem(position);
            String deviceName = item.mDevice.getDisplayName();
            TextView textView = convertView.findViewById(R.id.item_device_name);
            textView.setText(deviceName);
            TextView statusView = convertView.findViewById(R.id.item_device_status);
            if (item.isConnected) {
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
        LinkingDevice mDevice;
        boolean isConnected = false;
    }

    private LinkingDeviceManager.OnConnectListener mOnConnectListener = new LinkingDeviceManager.OnConnectListener() {
        @Override
        public void onConnect(final LinkingDevice device) {
            refreshDeviceList();
        }

        @Override
        public void onDisconnect(final LinkingDevice device) {
            refreshDeviceList();
        }
    };
}
