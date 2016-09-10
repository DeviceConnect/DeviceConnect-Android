/*
 AWSIotDeviceAuthenticationFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.setting.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.awsiot.DConnectLocalHelper;
import org.deviceconnect.android.deviceplugin.awsiot.core.LocalDevice;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * AWS IoT Settings Fragment Page 4.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDeviceAuthenticationFragment extends Fragment {
    /** Device list view. */
    private ListView mListView;

    /** Local Device Information Adapter */
    private LocalDeviceAdapter mDeviceAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.settings_device_authentication, null);

        mListView = (ListView) rootView.findViewById(R.id.device_list_view);
        mDeviceAdapter = new LocalDeviceAdapter(getActivity(), new ArrayList<LocalDevice>());
        mListView.setAdapter(mDeviceAdapter);
        mListView.setItemsCanFocus(true);

        getDeviceList();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private class LocalDeviceAdapter extends ArrayAdapter<LocalDevice> {
        private LayoutInflater mInflater;

        public LocalDeviceAdapter(final Context context, final List<LocalDevice> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_device, null);
            }

            final LocalDevice device = getItem(position);
            String name = device.getDeviceName();

            TextView nameView = (TextView) convertView.findViewById(R.id.device_name);
            nameView.setText(name);

            Button btn = (Button) convertView.findViewById(R.id.btn_auth_device);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    Button btn = (Button) v.findViewById(R.id.btn_auth_device);
                    if (!device.isAuthentication()) {
                        // TODO: Service Information 発行

                        // preference変更処理(flag = true)
                        sp.edit().putBoolean(device.getServiceId(), true).apply();
                        device.setAuthenticationFlag(true);

                        // ボタン変更
                        btn.setBackgroundResource(R.drawable.button_gray);
                        btn.setEnabled(false);
                    }
                }
            });
//            if (device.isAuthentication()) {
//                btn.setBackgroundResource(R.drawable.button_gray);
//                btn.setEnabled(false);
//            }
            return convertView;
        }
    }

    private void getDeviceList() {
        DConnectLocalHelper.INSTANCE.serviceDiscovery(new DConnectLocalHelper.FinishCallback() {
            @Override
            public void onFinish(final String response, final Exception error) {
                if (response == null) {
                    return;
                }

                List<LocalDevice> services = new ArrayList<>();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("result");
                    if (result == 0) {
                        JSONArray array = jsonObject.getJSONArray("services");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject o = array.getJSONObject(i);
                            LocalDevice service = new LocalDevice();
                            service.setServiceId(o.getString("id"));
                            service.setDeviceName(o.getString("name"));
                            services.add(service);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("AWS", "", e);
                }

                mDeviceAdapter.clear();
                mDeviceAdapter.addAll(services);
                mDeviceAdapter.notifyDataSetInvalidated();
            }
        });
    }
}
