/*
 AWSIotInformationFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.setting.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotCore;
import org.deviceconnect.android.deviceplugin.awsiot.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.local.AWSIotLocalDeviceService;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.deviceconnect.android.deviceplugin.awsiot.setting.AWSIotSettingActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * AWS IoT Settings Fragment Page 3.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotInformationFragment extends Fragment {

    /** AWSIotRemoteManager. */
    private AWSIotController mAWSIotController;
    private AWSIotPrefUtil mPrefUtil;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        AWSIotSettingActivity activity = (AWSIotSettingActivity) getActivity();

        mAWSIotController = activity.getAWSIotController();
        mPrefUtil = new AWSIotPrefUtil(activity);

        View rootView = inflater.inflate(R.layout.settings_awsiot_info, null);

        String name = mPrefUtil.getManagerName();
        Switch sw = (Switch) rootView.findViewById(R.id.manager_switch);
        sw.setText(name);
        sw.setChecked(mPrefUtil.getManagerRegister());
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startAWSIotLocal();
                } else {
                    stopAWSIotLocal();
                }
                updateShadow(isChecked);
            }
        });

        EditText et = (EditText) rootView.findViewById(R.id.input_sync_time);
        et.setText(String.valueOf(10));
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        TextView tv = (TextView) rootView.findViewById(R.id.display_awsiot_mqtt_endpoint);
        tv.setText(mAWSIotController.getAWSIotEndPoint());

        // TODO: 表示データ作成方法検討
        String uuid = mPrefUtil.getManagerUuid();
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

    private void updateShadow(boolean online) {
        mPrefUtil.setManagerRegister(online);

        try {
            AWSIotPrefUtil prefUtil = new AWSIotPrefUtil(getActivity());

            JSONObject managerData = new JSONObject();
            managerData.put("name", prefUtil.getManagerName());
            managerData.put("online", online);
            managerData.put("timeStamp", System.currentTimeMillis());

            mAWSIotController.updateShadow(AWSIotCore.KEY_DCONNECT_SHADOW_NAME, prefUtil.getManagerUuid(), managerData, new AWSIotController.UpdateShadowCallback() {
                @Override
                public void onUpdateShadow(final String result, final Exception err) {
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startAWSIotLocal() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), AWSIotLocalDeviceService.class);
        intent.setAction(AWSIotLocalDeviceService.ACTION_START);
        getActivity().startService(intent);
    }

    private void stopAWSIotLocal() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), AWSIotLocalDeviceService.class);
        intent.setAction(AWSIotLocalDeviceService.ACTION_STOP);
        getActivity().startService(intent);
    }
}
