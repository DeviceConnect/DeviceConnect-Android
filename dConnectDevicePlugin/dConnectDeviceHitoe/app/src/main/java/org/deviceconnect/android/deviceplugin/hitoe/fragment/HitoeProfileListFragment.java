/*
 HitoeProfileListFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.R;
import org.deviceconnect.android.deviceplugin.hitoe.activity.HitoeDeviceControlActivity;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;


/**
 * This fragment do setting of the control hitoe device.
 *
 * @author NTT DOCOMO, INC.
 */
public class HitoeProfileListFragment extends Fragment implements AdapterView.OnItemClickListener {

    /**
     * Bluetooth device list view.
     */
    private ListView mProfileListView;


    private HitoeDevice mCurrentDevice;

    private TextView mTitle;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_hitoe_device_list, null);
        Button underBtn = (Button) rootView.findViewById(R.id.btn_add_open);
        underBtn.setVisibility(View.GONE);
        mTitle = (TextView) rootView.findViewById(R.id.view_title);
        mProfileListView = (ListView) rootView.findViewById(R.id.device_list_view);
        String[] profiles = getResources().getStringArray(R.array.support_profiles);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, profiles);
        mProfileListView.setAdapter(mAdapter);
        mProfileListView.setOnItemClickListener(this);

        Bundle args = getArguments();
        if (args != null) {

            String serviceId = args.getString(HitoeDeviceControlActivity.FEATURE_SERVICE_ID);
            HitoeApplication app = (HitoeApplication) getActivity().getApplication();
            HitoeManager manager = app.getHitoeManager();

            mCurrentDevice = manager.getHitoeDeviceForServiceId(serviceId);
            if (mCurrentDevice != null) {
                mTitle.setText(mCurrentDevice.getName() + " " + getString(R.string.title_control));
            }
        }

        return rootView;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        HitoeDeviceControlActivity control = (HitoeDeviceControlActivity) getActivity();
        control.movePage(i + 1);
    }
}
