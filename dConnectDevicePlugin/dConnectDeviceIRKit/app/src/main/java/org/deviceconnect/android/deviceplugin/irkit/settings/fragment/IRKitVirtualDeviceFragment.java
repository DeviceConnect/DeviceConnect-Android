/*
 IRKitVirtualDeviceFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.deviceplugin.irkit.R;
import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualDeviceData;
import org.deviceconnect.android.deviceplugin.irkit.settings.activity.IRKitVirtualDeviceListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * IRKit Virtual Device List fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class IRKitVirtualDeviceFragment extends Fragment
        implements IRKitCreateVirtualDeviceDialogFragment.IRKitVirtualDeviceCreateEventListener {

    /** Adapter. */
    private VirtualDeviceAdapter mVirtualDeviceAdapter;
    /** Devices. */
    private List<VirtualDeviceData> mVirtuals;
    /** サービスID. */
    private String mServiceId;
    /** DB Helper. */
    private IRKitDBHelper mDBHelper;
    /** 削除モードフラグ. */
    private boolean mIsRemoved;
    /** 削除フラグリスト. */
    private List<Boolean> mIsRemoves;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreated() {
        updateVirtualDeviceList();
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsRemoved = false;
        updateVirtualDeviceList();
    }

    /**
     * サービスID を受け取る.
     * @param serviceId サービスID
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    /**
     * IRKitのVirtual Device データを保持する。
     * @param device IRKitのVirtual Device データ
     * @return DeviceContainer
     */
    private VirtualDeviceContainer createContainer(final VirtualDeviceData device) {
        VirtualDeviceContainer container = new VirtualDeviceContainer();
        if (device.getCategoryName().equals("ライト")) {
            container.setIcon(getResources().getDrawable(R.drawable.light));
        } else {
            container.setIcon(getResources().getDrawable(R.drawable.tv));
        }
        container.setLabel(device.getDeviceName());
        container.setIsRemove(false);
        return container;
    }

    /**
     * IRKitデバイスのリストの取得.
     * @return IRKitデバイスのリスト.
     */
    private List<VirtualDeviceContainer> createDeviceContainers() {
        List<VirtualDeviceContainer> containers = new ArrayList<VirtualDeviceContainer>();
        mVirtuals = mDBHelper.getVirtualDevicesByServiceId(mServiceId);
        if (mVirtuals != null) {
            mIsRemoves = new ArrayList<Boolean>();
            for (int i = 0; i < mVirtuals.size(); i++) {
                mIsRemoves.add(false);
            }
            for (VirtualDeviceData device : mVirtuals) {
                containers.add(createContainer(device));
            }
        }
        return containers;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MenuItem menuItem = menu.add(getString(R.string.menu_close));
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                if (item.getTitle().equals(menuItem.getTitle())) {
                    getActivity().finish();
                }
                return true;
            }
        });
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        IRKitCreateVirtualDeviceDialogFragment.setEventListner(this);

        mDBHelper = new IRKitDBHelper(getActivity());
        mVirtualDeviceAdapter = new VirtualDeviceAdapter(getActivity(), createDeviceContainers());

        View rootView = inflater.inflate(R.layout.fragment_virtual_device_list, container, false);

        final View addLayout = rootView.findViewById(R.id.add_btn);
        final View deleteLayout = rootView.findViewById(R.id.remove_btn);
        addLayout.setVisibility(View.VISIBLE);
        deleteLayout.setVisibility(View.GONE);

        Button cancelDeviceBtn = (Button) rootView.findViewById(R.id.cancel_virtual_device);
        cancelDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                addLayout.setVisibility(View.VISIBLE);
                deleteLayout.setVisibility(View.GONE);
                mIsRemoved = false;
                updateVirtualDeviceList();
            }
        });

        Button deleteDeviceBtn = (Button) rootView.findViewById(R.id.remove_virtual_device2);
        deleteDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRemove()) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                    alertBuilder.setTitle(getString(R.string.remove_virtual_device_title));
                    alertBuilder.setMessage(getString(R.string.remove_virtual_device_message));
                    alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeCheckVirtualDevices();
                            addLayout.setVisibility(View.VISIBLE);
                            deleteLayout.setVisibility(View.GONE);
                            mIsRemoved = false;
                            updateVirtualDeviceList();
                        }
                    });
                    alertBuilder.setNegativeButton("Cancel", null);
                    alertBuilder.create().show();
                } else {
                    IRKitCreateVirtualDeviceDialogFragment.showAlert(getActivity(),
                            getString(R.string.remove_virtual_device_title),
                            getString(R.string.remove_virtual_select_device));
                    updateVirtualDeviceList();
                }
            }
        });

        Button addDeviceBtn = (Button) rootView.findViewById(R.id.add_virtual_device);
        addDeviceBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                IRKitCategorySelectDialogFragment irkitDialog =
                        IRKitCategorySelectDialogFragment.newInstance(mServiceId);
                irkitDialog.show(getActivity().getFragmentManager(),
                        "fragment_dialog");
                updateVirtualDeviceList();
            }
        });

        Button selectDeleteDeviceBtn = (Button) rootView.findViewById(R.id.remove_virtual_device);
        selectDeleteDeviceBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                addLayout.setVisibility(View.GONE);
                deleteLayout.setVisibility(View.VISIBLE);
                mIsRemoved = true;
                updateVirtualDeviceList();
            }
        });

        ListView listView = (ListView) rootView.findViewById(R.id.listview_devicelist);
        listView.setItemsCanFocus(true);
        listView.setAdapter(mVirtualDeviceAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                if (mIsRemoved) {
                    CheckBox removeCheck = (CheckBox) view.findViewById(R.id.delete_check);
                    removeCheck.setChecked(!removeCheck.isChecked());
                } else {
                    IRKitVirtualDeviceListActivity activity = (IRKitVirtualDeviceListActivity) getActivity();
                    activity.startApp(IRKitVirtualDeviceListActivity.MANAGE_VIRTUAL_PROFILE_PAGE,
                            mVirtuals.get(position).getServiceId());
                }
            }
        });
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Update device list.
     */
    public void updateVirtualDeviceList() {
        if (mVirtualDeviceAdapter == null) {
            return;
        }
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVirtualDeviceAdapter.clear();
                mVirtualDeviceAdapter.addAll(createDeviceContainers());
                mVirtualDeviceAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Virtual Device を削除する.
     */
    private void removeCheckVirtualDevices() {
        boolean isRemoved = false;
        for (int i = 0; i < mIsRemoves.size(); i++) {
            if (mIsRemoves.get(i).booleanValue()) {
                VirtualDeviceData device = mVirtuals.get(i);
                isRemoved = mDBHelper.removeVirtualDevice(device.getServiceId());
                if (isRemoved) {
                    sendEventOnRemoved(device);
                }
            }
        }
        if (isRemoved) {
            IRKitCreateVirtualDeviceDialogFragment.showAlert(getActivity(),
                    getString(R.string.remove_virtual_device_title),
                    getString(R.string.remove_virtual_device_success));
        } else {
            IRKitCreateVirtualDeviceDialogFragment.showAlert(getActivity(),
                    getString(R.string.remove_virtual_device_title),
                    getString(R.string.remove_virtual_device_failure));
        }
    }

    private void sendEventOnRemoved(final VirtualDeviceData device) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(IRKitDeviceService.ACTION_VIRTUAL_DEVICE_REMOVED);
        intent.putExtra(IRKitDeviceService.EXTRA_VIRTUAL_DEVICE_ID, device.getServiceId());
        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
    }

    /**
     * チェックされているかどうかを確認する.
     */
    private boolean isRemove() {
        boolean isRemoved = false;
        for (int i = 0; i < mIsRemoves.size(); i++) {
            if (mIsRemoves.get(i).booleanValue()) {
                isRemoved = true;
                break;
            }
        }
        return isRemoved;
    }


    /**
     * VirtualDeviceContainer.
     */
    static class VirtualDeviceContainer {
        /** ラベル. */
        private String mLabel;
        /** アイコン. */
        private Drawable mIcon;
        /** チェック状態. */
        private boolean mIsRemove;

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

        /**
         * デバイスを削除するかどうかを設定する.
         * @param isRemove true:削除, false: 削除しない
         */
        public void setIsRemove(final boolean isRemove) {
            mIsRemove = isRemove;
        }

        /**
         * デバイスを削除するかどうかを指定する.
         * @return 削除するかどうか
         */
        public boolean isRemove() {
            return mIsRemove;
        }

    }

    /**
     * VirtualDeviceAdapter.
     */
    private class VirtualDeviceAdapter extends ArrayAdapter<VirtualDeviceContainer> {
        /** LayoutInflater. */
        private LayoutInflater mInflater;

        /**
         * コンストラクタ.
         *
         * @param context Context.
         * @param objects DeviceList.
         */
        public VirtualDeviceAdapter(final Context context, final List<VirtualDeviceContainer> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View cv = convertView;
            if (cv == null) {
                cv = mInflater.inflate(R.layout.item_irkitdevice_list, parent, false);
            } else {
                cv = convertView;
            }

            final VirtualDeviceContainer device = getItem(position);

            String name = device.getLabel();

            TextView nameView = (TextView) cv.findViewById(R.id.devicelist_package_name);
            nameView.setText(name);
            Drawable icon = device.getIcon();
            if (icon != null) {
                ImageView iconView = (ImageView) cv.findViewById(R.id.devicelist_icon);
                iconView.setImageDrawable(icon);
            }

            CheckBox removeCheck = (CheckBox) cv.findViewById(R.id.delete_check);
            if (mIsRemoved) {
                removeCheck.setVisibility(View.VISIBLE);
                removeCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        mIsRemoves.set(position, b);
                    }
                });
                removeCheck.setChecked(device.isRemove());
                removeCheck.setFocusable(false);
            } else {
                removeCheck.setVisibility(View.GONE);
                removeCheck.setOnCheckedChangeListener(null);
            }
            return cv;
        }
    }
}


