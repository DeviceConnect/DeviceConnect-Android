/*
 AWSIotInformationFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.setting.fragment;

import org.deviceconnect.android.deviceplugin.awsiot.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.AWSIotDeviceService;
import org.deviceconnect.android.deviceplugin.awsiot.AWSIotRemoteManager;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.deviceconnect.android.deviceplugin.awsiot.setting.AWSIotSettingActivity;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

/**
 * AWS IoT Settings Fragment Page 3.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotInformationFragment extends Fragment {
    /** Application Instance. */
    private AWSIotDeviceApplication mApp;
    /** AWSIotRemoteManager. */
    private AWSIotRemoteManager mIot;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        AWSIotSettingActivity activity = (AWSIotSettingActivity) getActivity();
        mApp = (AWSIotDeviceApplication) activity.getApplication();
        final AWSIotPrefUtil prefUtil = ((AWSIotSettingActivity) getContext()).getPrefUtil();
        mIot = ((AWSIotSettingActivity) getContext()).getAWSIotRemoteManager();

        mIot.setOnEventListener(null);

        View rootView = inflater.inflate(R.layout.settings_awsiot_info, null);

        String name = prefUtil.getManagerName();
        Switch sw = (Switch) rootView.findViewById(R.id.manager_switch);
        sw.setText(name);
        sw.setChecked(prefUtil.getManagerRegister());
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject jsonObject = new JSONObject();
                JSONObject managerData = new JSONObject();
                if (isChecked) {
                    prefUtil.setManagerRegister(true);

                    // TODO: Subscribe登録
//                    mIot.subscribe("dconnect/"+mApp.getMyManagerName()+"/request");

                    // Manager ListへOnline登録
                    try {
                        managerData.put("name", prefUtil.getManagerName());
                        managerData.put("online", true);
                        managerData.put("timeStamp", System.currentTimeMillis());
                        mIot.updateShadow("DeviceConnect", prefUtil.getManagerUuid(), managerData);

                        jsonObject.put(prefUtil.getManagerUuid(), managerData);
                        Log.d("ABC", "Manager List Data: " + jsonObject);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("ABC", "Checked.");
                } else {
                    mApp.setMyManagerOnlineFlag(false);
                    // TODO: Subscribe解除

                    // Manager ListへOffline登録
                    try {
                        managerData.put("name", prefUtil.getManagerName());
                        managerData.put("online", false);
                        managerData.put("timeStamp", System.currentTimeMillis());
                        mIot.updateShadow("DeviceConnect", prefUtil.getManagerUuid(), managerData);

                        jsonObject.put(prefUtil.getManagerUuid(), managerData);
                        Log.d("ABC", "Manager List Data: " + jsonObject);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("ABC", "Unchecked.");
                }
            }
        });

        EditText et = (EditText) rootView.findViewById(R.id.input_sync_time);
        et.setText(String.valueOf(mApp.getAWSIotSyncTime()));
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mApp.setAWSIotSyncTime(Long.valueOf(s.toString()));
            }
        });

        TextView tv = (TextView) rootView.findViewById(R.id.display_awsiot_mqtt_endpoint);
        tv.setText(mIot.getAWSIotEndPoint());

        // TODO: 表示データ作成方法検討
        String uuid = prefUtil.getManagerUuid();
        String request = "deviceconnect/" + uuid + "/request";
        String response = "deviceconnect/" + uuid + "/response";
        String event = "deviceconnect/" + uuid + "/event";

        tv = (TextView) rootView.findViewById(R.id.display_awsiot_request_topic);
        tv.setText(request);

        tv = (TextView) rootView.findViewById(R.id.display_awsiot_response_topic);
        tv.setText(response);

        tv = (TextView) rootView.findViewById(R.id.display_awsiot_event_topic);
        tv.setText(event);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }
}
