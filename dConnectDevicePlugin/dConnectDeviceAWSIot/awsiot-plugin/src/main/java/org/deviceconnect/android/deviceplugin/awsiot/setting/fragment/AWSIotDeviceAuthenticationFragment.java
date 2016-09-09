/*
 AWSIotDeviceAuthenticationFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.setting.fragment;


import org.deviceconnect.android.deviceplugin.awsiot.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.DConnectLocalHelper;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.core.LocalDevice;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.deviceconnect.android.deviceplugin.awsiot.setting.AWSIotSettingActivity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * AWS IoT Settings Fragment Page 4.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotDeviceAuthenticationFragment extends Fragment {
    /** Application Instance. */
    private AWSIotDeviceApplication mApp;
    /** AWSIoTController. */
    private AWSIotController mIot;
    /** Device list view. */
    private ListView mListView;
    /** Local Device Infomation Adapter */
    private LocalDeviceAdapter mDeviceAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        serviceDiscovery(new DConnectLocalHelper.FinishCallback() {
            @Override
            public void onFinish(final String response, final Exception error) {
                if (response == null) {
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("result");
                    Log.d("ABC", "result : " + result);
                    if (result == 0) {
                        Log.d("ABC", "json : " + jsonObject);

                    } else {
                        // TODO Managerが起動していない場合の処理
                    }
                } catch (JSONException e) {
//                    Log.e(TAG, "", e);
                }
            }
        });


        AWSIotSettingActivity activity = (AWSIotSettingActivity) getActivity();
        mApp = (AWSIotDeviceApplication) activity.getApplication();

        View rootView = inflater.inflate(R.layout.settings_device_authentication, null);

        // TODO:Adapter登録
        mListView = (ListView) rootView.findViewById(R.id.device_list_view);
        List<LocalDevice> informations = new ArrayList<>();
        // for test
        {
            String dummy = "dummy";
            informations.addAll(createLocalDeviceInfomations(dummy));
        }
        mDeviceAdapter = new LocalDeviceAdapter(getActivity(), informations);
        mListView.setAdapter(mDeviceAdapter);
        mListView.setItemsCanFocus(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private List<LocalDevice> createLocalDeviceInfomations(final String data) {
        List<LocalDevice> informations = new ArrayList<>();
        // TODO:JSON解析、Id, Device名取得。
        //for test
        informations.add(createLocalDeviceInformation("1", "HOST", false));
        informations.add(createLocalDeviceInformation("2", "HUE A", false));
        informations.add(createLocalDeviceInformation("3", "HUE B", true));
        informations.add(createLocalDeviceInformation("4", "HUE C", false));

        return informations;
    }

    private LocalDevice createLocalDeviceInformation(final String id, final String name, final Boolean authentication) {
        LocalDevice info = new LocalDevice();
        info.setServiceId(id);
        info.setDeviceName(name);
        info.setAuthenticationFlag(authentication);
        return info;
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

    private void serviceDiscovery(final DConnectLocalHelper.FinishCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DConnectLocalHelper.INSTANCE.sendRequest("GET", "http://localhost:4035/gotapi/servicediscovery", callback);
            }
        }).start();
    }

}
