/*
 UVCDeviceConnectionFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.uvc.R;

public class UVCDeviceInstructionFragment extends UVCDevicePluginBindFragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTitle(getString(R.string.uvc_settings_title_uvc_device_instruction));
        return inflater.inflate(R.layout.fragment_uvc_device_instruction, null);
    }
}
