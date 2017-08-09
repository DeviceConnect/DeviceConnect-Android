/*
 IRKitVirtualProfileListFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.irkit.IRKitApplication;
import org.deviceconnect.android.deviceplugin.irkit.R;
import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualProfileData;
import org.deviceconnect.android.deviceplugin.irkit.settings.activity.IRKitVirtualDeviceListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * IRKit Profileリスト fragment.
 * 
 * @author NTT DOCOMO, INC.
 */
public class IRKitVirtualProfileListFragment extends Fragment {

    /** Adapter. */
    private VirtualProfileAdapter mVirtualProfileAdapter;
    /** Profiles. */
    private List<VirtualProfileData> mProfiles;
    /** サービスID. */
    private String mServiceId;

    /** DB Helper. */
    private IRKitDBHelper mDBHelper;

    /** ListView. */
    private ListView mListView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        IRKitVirtualDeviceListActivity activity = (IRKitVirtualDeviceListActivity) getActivity();
        IRKitApplication application = activity.getIRKitApplication();
        IRKitApplication.ListViewPosition p = application.getListViewPosition(
                IRKitVirtualDeviceListActivity.MANAGE_VIRTUAL_PROFILE_PAGE);
        if (mListView != null && p != null) {
            mListView.requestFocusFromTouch();
            mListView.setSelectionFromTop(p.getPosition(), p.getOffset());
        }
        updateProfileList();
    }


    /**
     * サービスID を受け取る.
     * @param serviceId サービスID
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    /**
     * VirtualDevice のProfile を保持する。
     * @param profile IRKitデバイス
     * @return VirtualProfileContainer
     */
    private VirtualProfileContainer createContainer(final VirtualProfileData profile) {
        VirtualProfileContainer container = new VirtualProfileContainer();
        container.setLabel(profile.getName());
        container.setRegister((profile.getIr() != null));
        return container;
    }

    /**
     *　IRKitデバイスのリストの取得.
     * @return IRKitデバイスのリスト.
     */
    private List<VirtualProfileContainer> createDeviceContainers() {
        List<VirtualProfileContainer> containers = new ArrayList<VirtualProfileContainer>();

        mProfiles = mDBHelper.getVirtualProfiles(mServiceId, null);
        if (mProfiles != null) {
            for (VirtualProfileData device : mProfiles) {
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

        mDBHelper = new IRKitDBHelper(getActivity());
        mVirtualProfileAdapter = new VirtualProfileAdapter(getActivity(), createDeviceContainers());
        View rootView = inflater.inflate(R.layout.fragment_profilelist, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listview_devicelist);
        mListView.setItemsCanFocus(true);
        mListView.setAdapter(mVirtualProfileAdapter);
        return rootView;
    }



    /**
     * Update profile list.
     */
    public void updateProfileList() {
        if (mVirtualProfileAdapter == null) {
            return;
        }
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVirtualProfileAdapter.clear();
                mVirtualProfileAdapter.addAll(createDeviceContainers());
                mVirtualProfileAdapter.notifyDataSetChanged();
            }
        });
    }


    /**
     * DeviceContainer.
     */
    static class VirtualProfileContainer {
        /** ラベル. */
        private String mLabel;
        /** 更新・登録判定. */
        private boolean mIsRegister;

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
         * 赤外線が登録されているかどうか.
         *
         * @return true:登録済み false:登録されていない.
         */
        public boolean isRegister() {
            return mIsRegister;
        }

        /**
         * 赤外線が登録されているかの設定.
         *
         * @param isRegister 登録されているか.
         */
        public void setRegister(final boolean isRegister) {
           mIsRegister = isRegister;
        }
    }

    /**
     * DeviceAdapter.
     */
    private class VirtualProfileAdapter extends ArrayAdapter<VirtualProfileContainer> {
        /** LayoutInflater. */
        private LayoutInflater mInflater;

        /**
         * コンストラクタ.
         * 
         * @param context Context.
         * @param objects DeviceList.
         */
        public VirtualProfileAdapter(final Context context, final List<VirtualProfileContainer> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View cv = convertView;
            if (cv == null) {
                cv = mInflater.inflate(R.layout.item_irkit_profile_list, parent, false);
            } else {
                cv = convertView;
            }

            final VirtualProfileContainer device = getItem(position);

            String name = device.getLabel();

            TextView nameView = (TextView) cv.findViewById(R.id.profile_name);
            nameView.setText(name);

            Button registerBtn = (Button) cv.findViewById(R.id.btn_register_ir);
            registerBtn.setTag(position);
            if (device.isRegister()) {
                registerBtn.setBackgroundResource(R.drawable.button_orange);
                registerBtn.setText(getString(R.string.virtual_device_update));
            } else {
                registerBtn.setBackgroundResource(R.drawable.button_blue);
                registerBtn.setText(getString(R.string.virtual_device_register));
            }
            registerBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int pos = ((Integer) view.getTag());
                    VirtualProfileData profile = mProfiles.get(pos);
                    IRKitVirtualDeviceListActivity activity = (IRKitVirtualDeviceListActivity) getActivity();
                    activity.startRegisterPageApp(profile);
                    IRKitApplication application = activity.getIRKitApplication();
                    int yOffset = mListView.getChildAt(0).getTop();
                    application.setListViewPosition(
                            IRKitVirtualDeviceListActivity.MANAGE_VIRTUAL_PROFILE_PAGE,
                            pos,
                            yOffset);

                }
            });
            return cv;
        }
    }
}


