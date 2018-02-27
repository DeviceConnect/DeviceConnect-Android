/*
 IRKitAccessPointSettingFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.irkit.IRKitManager;
import org.deviceconnect.android.deviceplugin.irkit.IRKitManager.WiFiSecurityType;
import org.deviceconnect.android.deviceplugin.irkit.R;
import org.deviceconnect.android.deviceplugin.irkit.network.WiFiUtil;
import org.deviceconnect.android.deviceplugin.irkit.settings.activity.IRKitSettingActivity;

/**
 * IRKit が接続するアクセスポイントの SSID、セキュリティー、パスワード等を入力するフラグメント.
 * @author NTT DOCOMO, INC.
 */
public class IRKitAccessPointSettingFragment extends IRKitBaseFragment {
    
    /** 
     * SSID.
     */
    private static final String KEY_SSID = "ssid";
    
    /** 
     * PASS.
     */
    private static final String KEY_PASS = "pass";
    
    /** 
     * タイプ.
     */
    private static final String KEY_TYPE = "type";
   
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, 
            final Bundle savedInstanceState) {
       
        final View root = inflater.inflate(R.layout.irkit_settings_step_2, null);
        WiFiUtil.checkLocationPermission(getActivity(), new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                setWifiStatus(savedInstanceState, root);
            }

            @Override
            public void onFail(@NonNull String s) {
                getActivity().finish();
                // Wifi情報が取得許可がおりない場合は設定を終了する
                Toast.makeText(getActivity(), getString(R.string.alert_message_wifi_permission), Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }

    private void setWifiStatus(Bundle savedInstanceState, View root) {
        EditText ssidText = (EditText) root.findViewById(R.id.inputSSID);
        EditText passwordText = (EditText) root.findViewById(R.id.inputPassword);
        RadioGroup radioGroup = (RadioGroup) root.findViewById(R.id.radioGroupSecurity);
        
        int typeId = -1;
        if (savedInstanceState != null) {
            ssidText.setText(savedInstanceState.getString(KEY_SSID));
            passwordText.setText(savedInstanceState.getString(KEY_PASS));
            typeId = savedInstanceState.getInt(KEY_TYPE);
        } else {
            
            IRKitSettingActivity a = (IRKitSettingActivity) getActivity();
            String ssid = a.getSSID();
            if (ssid == null) {
                ssid = WiFiUtil.getCurrentSSID(a);
            }
            
            ssidText.setText(ssid);
            passwordText.setText(a.getPassword());
            WiFiSecurityType type = a.getSecType();
            switch (type) {
            case NONE:
                typeId = R.id.radioButton1;
                break;
            case WEP:
                typeId = R.id.radioButton2;
                break;
            case WPA2:
            default:
                typeId = R.id.radioButton3;
                break;
            }
        }
        radioGroup.check(typeId);
    }

    @Override
    public void onDisapper() {
        super.onDisapper();
        IRKitSettingActivity a = (IRKitSettingActivity) getActivity();
        if (a == null) {
            return;
        }
        saveInfo(null, a);
    }
    
    /**
     * 情報を保存する.
     * 
     * @param outState バンドル
     * @param activity アクティビティ
     */
    private void saveInfo(final Bundle outState, final IRKitSettingActivity activity) {

        View root = getView();
        if (root == null) {
            return;
        }

        EditText ssidText = (EditText) root.findViewById(R.id.inputSSID);
        EditText passwordText = (EditText) root.findViewById(R.id.inputPassword);
        RadioGroup radioGroup = (RadioGroup) root.findViewById(R.id.radioGroupSecurity);

        if (activity != null) {
            activity.setSSID(ssidText.getText().toString().trim());
            activity.setPassword(passwordText.getText().toString());
            WiFiSecurityType type = null;
            int typeId = radioGroup.getCheckedRadioButtonId();
            switch (typeId) {
                case R.id.radioButton1:
                    type = WiFiSecurityType.NONE;
                    break;
                case R.id.radioButton2:
                    type = WiFiSecurityType.WEP;
                    break;
                case R.id.radioButton3:
                default:
                    type = WiFiSecurityType.WPA2;
                    break;
            }
            activity.setSecType(type);
        } else {
            outState.putString(KEY_SSID, ssidText.getText().toString().trim());
            outState.putString(KEY_TYPE, passwordText.getText().toString());
            outState.putInt(KEY_TYPE, radioGroup.getCheckedRadioButtonId());
        }

    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        saveInfo(outState, null);
    }

}
