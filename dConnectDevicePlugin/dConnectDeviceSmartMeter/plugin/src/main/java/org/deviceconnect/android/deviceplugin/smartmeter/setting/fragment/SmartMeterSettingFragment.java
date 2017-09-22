/*
SmartMeterConnectFragment
Copyright (c) 2017 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.smartmeter.setting.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.smartmeter.R;
import org.deviceconnect.android.deviceplugin.smartmeter.setting.SmartMeterSettingActivity;
import org.deviceconnect.android.deviceplugin.smartmeter.util.PrefUtil;

/**
 * 接続画面用Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class SmartMeterSettingFragment extends Fragment {
    /** PrefUtil Instance. */
    private PrefUtil mPrefUtil;
    /** B Route ID EditText. */
    private EditText mBrouteId;
    /** B Route Password EditText. */
    private EditText mBroutePassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Root view.
        View root = inflater.inflate(R.layout.setting, container, false);

        // PrefUtil Instance.
        mPrefUtil = ((SmartMeterSettingActivity) getContext()).getPrefUtil();

        mBrouteId = (EditText)root.findViewById(R.id.input_b_route_id);
        mBrouteId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeSoftwareKeyboard(v);
                }
            }
        });
        String bRouteId = mPrefUtil.getBRouteId();
        if (bRouteId != null) {
            mBrouteId.setText(bRouteId);
        }

        mBroutePassword = (EditText) root.findViewById(R.id.input_b_route_password);
        mBroutePassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeSoftwareKeyboard(v);
                }
            }
        });
        String bRoutePassword = mPrefUtil.getBRoutePass();
        if (bRoutePassword != null) {
            mBroutePassword.setText(bRoutePassword);
        }

        Button okButton = (Button) root.findViewById(R.id.settings_save);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bRouteId = mBrouteId.getText().toString();
                if (bRouteId.length() == 0) {
                    Toast.makeText(getContext(), R.string.setting_error_b_route_id, Toast.LENGTH_LONG).show();
                    return;
                }
                String bRoutePassword = mBroutePassword.getText().toString();
                if (bRoutePassword.length() == 0) {
                    Toast.makeText(getContext(), R.string.setting_error_b_route_password, Toast.LENGTH_LONG).show();
                    return;
                }

                mPrefUtil.setBRouteId(bRouteId);
                mPrefUtil.setBRoutePass(bRoutePassword);
                Toast.makeText(getContext(), R.string.setting_save_complete, Toast.LENGTH_LONG).show();
            }
        });
        return root;
    }

    /**
     * Close software Keyboard
     */
    private void closeSoftwareKeyboard(final View v) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
