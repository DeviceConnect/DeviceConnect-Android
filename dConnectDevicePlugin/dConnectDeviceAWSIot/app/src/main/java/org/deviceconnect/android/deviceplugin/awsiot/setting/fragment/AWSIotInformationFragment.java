/*
 AWSIotInformationFragment.java
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
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotController;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotDeviceApplication;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.AWSIotPrefUtil;
import org.deviceconnect.android.deviceplugin.awsiot.cores.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.local.AWSIotLocalDeviceService;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;
import org.deviceconnect.android.deviceplugin.awsiot.setting.AWSIotSettingActivity;

/**
 * AWS Iot Information Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class AWSIotInformationFragment extends Fragment {

    private AWSIotPrefUtil mPrefUtil;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        AWSIotSettingActivity activity = (AWSIotSettingActivity) getActivity();

        AWSIotController AWSIotController = activity.getAWSIotController();
        mPrefUtil = new AWSIotPrefUtil(activity);
        RemoteDeviceConnectManager myManager = new RemoteDeviceConnectManager(
                mPrefUtil.getManagerName(), mPrefUtil.getManagerUuid());

        View rootView = inflater.inflate(R.layout.settings_awsiot_info, null);

        Switch sw = (Switch) rootView.findViewById(R.id.manager_switch);
        sw.setText(myManager.getName());
        sw.setChecked(mPrefUtil.getManagerRegister());
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startAWSIotLocal();
                } else {
                    stopAWSIotLocal();
                }
                AWSIotPrefUtil pref = new AWSIotPrefUtil(getActivity());
                pref.setManagerRegister(isChecked);
                AWSIotDeviceApplication.getInstance().updateMyManagerShadow(isChecked);
            }
        });

        EditText et = (EditText) rootView.findViewById(R.id.input_sync_time);
        et.setText(String.valueOf(mPrefUtil.getSyncTime()));
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.length() != 0) {
                    try {
                        long syncTime = Long.valueOf(input);
                        if (syncTime >= 0) {
                            mPrefUtil.setSyncTime(syncTime);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "正しい数値を入力して下さい。", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        TextView tv = (TextView) rootView.findViewById(R.id.display_awsiot_mqtt_endpoint);
        tv.setText(AWSIotController.getAWSIotEndPoint());

        tv = (TextView) rootView.findViewById(R.id.display_awsiot_request_topic);
        tv.setText(myManager.getRequestTopic());

        tv = (TextView) rootView.findViewById(R.id.display_awsiot_response_topic);
        tv.setText(myManager.getResponseTopic());

        tv = (TextView) rootView.findViewById(R.id.display_awsiot_event_topic);
        tv.setText(myManager.getEventTopic());

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
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
