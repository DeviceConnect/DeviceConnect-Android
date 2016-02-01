/*
 WakeupFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.setting.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.deviceconnect.android.deviceplugin.webrtc.R;
import org.deviceconnect.android.deviceplugin.webrtc.setting.SettingUtil;

/**
 * WebRTC起動説明画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class WakeupFragment extends Fragment {


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.setting_wakeup, null);
        setEditText(root);
        return root;
    }

    private void setEditText(final View view) {
        EditText editText = (EditText) view.findViewById(R.id.deviceName);
        String deviceName = SettingUtil.getDeviceName(getActivity());
        editText.setText(deviceName);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                SettingUtil.setDeviceName(getActivity(), s.toString());
            }
        });
    }
}
