/*
 IRKitDeviceListFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.irkit.IRKitApplication;
import org.deviceconnect.android.deviceplugin.irkit.IRKitDevice;
import org.deviceconnect.android.deviceplugin.irkit.R;
import org.deviceconnect.android.deviceplugin.irkit.settings.activity.IRKitDeviceListActivity;
import org.deviceconnect.android.deviceplugin.irkit.settings.activity.IRKitSettingActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * IRKit デバイスリスト fragment.
 * 
 * @author NTT DOCOMO, INC.
 */
public class IRKitDeviceListFragment extends Fragment  {

    /** Adapter. */
    private DeviceAdapter mDeviceAdapter;
    /** Devices. */
    private List<IRKitDevice> mDevices;
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDeviceList();
    }

    /**
     * IRKitデバイスリストの取得
     * @return IRKitデバイスリスト
     */
    private List<IRKitDevice> getIRKitDevices() {
        IRKitDeviceListActivity activity =
                (IRKitDeviceListActivity) getActivity();
        IRKitApplication application =
                (IRKitApplication) activity.getApplication();
        return application.getIRKitDevices();
    }
    /**
     * IRKitのデータを保持する。
     * @param device IRKitデバイス
     * @return DeviceContainer
     */
    private DeviceContainer createContainer(final IRKitDevice device) {
        DeviceContainer container = new DeviceContainer();
        container.setIcon(getResources().getDrawable(R.drawable.irkit_icon));
        container.setLabel(device.getName());
        return container;
    }

    /**
     *　IRKitデバイスのリストの取得.
     * @return IRKitデバイスのリスト.
     */
    private List<DeviceContainer> createDeviceContainers() {
        List<DeviceContainer> containers = new ArrayList<DeviceContainer>();

        mDevices = getIRKitDevices();
        if (mDevices != null) {
            for (IRKitDevice device : mDevices) {
                containers.add(createContainer(device));
            }
        }
        return containers;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        mDeviceAdapter = new DeviceAdapter(getActivity(), createDeviceContainers());
        View rootView = inflater.inflate(R.layout.fragment_devicelist, container, false);
        Button registerButton = (Button) rootView.findViewById(R.id.open_irkit_register);
        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                openDeviceSetting();
            }
        });
        ListView listView = (ListView) rootView.findViewById(R.id.listview_devicelist);
        listView.setItemsCanFocus(true);
        listView.setAdapter(mDeviceAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                IRKitDeviceListActivity activity = (IRKitDeviceListActivity) getActivity();
                activity.startApp(IRKitDeviceListActivity.MANAGE_VIRTUAL_DEVICE_PAGE,
                        mDevices.get(position).getName());
            }
        });
        return rootView;
    }



    /**
     * Update device list.
     */
    public void updateDeviceList() {
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
                mDeviceAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * IRKitの接続設定を行う画面を開く.
     *
     */
    private void openDeviceSetting() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), IRKitSettingActivity.class);
        startActivity(intent);
    }

    /**
     * DeviceContainer.
     */
    static class DeviceContainer {
        /** ラベル. */
        private String mLabel;
        /** アイコン. */
        private Drawable mIcon;

        /**
         * デバイスラベルの取得.
         * 
         * @return デバイスラベル.
         */
        public String getLabel() {
            return mLabel;
        }

        /**
         * デバイスラベルの設定.
         * 
         * @param label デバイスラベル.
         */
        public void setLabel(final String label) {
            if (label == null) {
                mLabel = "Unknown";
            } else {
                mLabel = label;
            }
        }

        /**
         * デバイスアイコンの取得.
         * @return デバイスアイコン
         */
        public Drawable getIcon() {
            return mIcon;
        }

        /**
         * デバイスアイコンの設定.
         * @param icon デバイスアイコン
         */
        public void setIcon(final Drawable icon) {
            mIcon = icon;
        }
    }

    /**
     * DeviceAdapter.
     */
    private class DeviceAdapter extends ArrayAdapter<DeviceContainer> {
        /** LayoutInflater. */
        private LayoutInflater mInflater;

        /**
         * コンストラクタ.
         * 
         * @param context Context.
         * @param objects DeviceList.
         */
        public DeviceAdapter(final Context context, final List<DeviceContainer> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View cv = convertView;
            if (convertView == null) {
                cv = mInflater.inflate(R.layout.item_irkitdevice_list, parent, false);
            } else {
                cv = convertView;
            }

            final DeviceContainer device = getItem(position);

            String name = device.getLabel();

            TextView nameView = (TextView) cv.findViewById(R.id.devicelist_package_name);
            nameView.setText(name);

            Drawable icon = device.getIcon();
            if (icon != null) {
                ImageView iconView = (ImageView) cv.findViewById(R.id.devicelist_icon);
                iconView.setImageDrawable(icon);
            }

            return cv;
        }
    }
}


