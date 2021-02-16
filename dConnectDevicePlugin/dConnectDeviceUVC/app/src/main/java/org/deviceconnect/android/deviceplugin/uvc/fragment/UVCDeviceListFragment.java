/*
 UVCDeviceListFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceService;
import org.deviceconnect.android.deviceplugin.uvc.databinding.FragmentUvcDeviceListBinding;
import org.deviceconnect.android.deviceplugin.uvc.databinding.ItemUvcDeviceBinding;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;
import org.deviceconnect.android.service.DConnectService;

import java.util.ArrayList;
import java.util.List;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class UVCDeviceListFragment extends UVCDevicePluginBindFragment {
    private DeviceAdapter mDeviceAdapter;

    private UVCDeviceService mUVCDeviceService;

    private final UVCDeviceService.OnEventListener mOnEventListener = new UVCDeviceService.OnEventListener() {
        @Override
        public void onConnected(UVCService service) {
            if (mDeviceAdapter != null) {
                mDeviceAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onDisconnected(UVCService service) {
            if (mDeviceAdapter != null) {
                mDeviceAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentUvcDeviceListBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_uvc_device_list, container, false);
        binding.setPresenter(this);

        mDeviceAdapter = new DeviceAdapter(getContext(), new ArrayList<>());

        setTitle(getString(R.string.uvc_settings_title_uvc_device_list));

        View rootView = binding.getRoot();
        ListView listView = rootView.findViewById(R.id.device_list_view);
        listView.setAdapter(mDeviceAdapter);
        listView.setItemsCanFocus(true);
        return rootView;
    }

    @Override
    public void onBindService() {
        List<DeviceContainer> containers = new ArrayList<>();

        mUVCDeviceService = getUVCDeviceService();
        if (mUVCDeviceService != null) {
            for (DConnectService service : mUVCDeviceService.getServiceProvider().getServiceList()) {
                containers.add(new DeviceContainer(service));
            }
            mUVCDeviceService.addOnEventListener(mOnEventListener);
        }

        mDeviceAdapter.setContainers(containers);
    }

    @Override
    public void onUnbindService() {
        if (mUVCDeviceService != null) {
            mUVCDeviceService.removeOnEventListener(mOnEventListener);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceContainer container = mDeviceAdapter.getItem(position);
        if (container.isOnline()) {
            Bundle bundle = new Bundle();
            bundle.putString("service_id", container.getId());
            findNavController(this).navigate(R.id.action_service_to_recorder, bundle);
        }
    }

    public class DeviceContainer {
        private final DConnectService mService;

        DeviceContainer(DConnectService service) {
            mService = service;
        }

        public String getName() {
            return mService.getName();
        }

        public String getId() {
            return mService.getId();
        }

        public String getStatus() {
            return mService.isOnline() ? getString(R.string.uvc_settings_online) : getString(R.string.uvc_settings_offline);
        }

        public boolean isOnline() {
            return mService.isOnline();
        }

        public int getBackgroundColor() {
            return mService.isOnline() ? Color.WHITE : Color.GRAY;
        }
    }

    private static class DeviceAdapter extends ArrayAdapter<DeviceContainer> {
        DeviceAdapter(final Context context, final List<DeviceContainer> objects) {
            super(context, 0, objects);
        }

        void setContainers(List<DeviceContainer> containers) {
            clear();
            addAll(containers);
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ItemUvcDeviceBinding binding;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                binding = DataBindingUtil.inflate(inflater, R.layout.item_uvc_device, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ItemUvcDeviceBinding) convertView.getTag();
            }
            binding.setDeviceContainer(getItem(position));
            return convertView;
        }
    }
}
