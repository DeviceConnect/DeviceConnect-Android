/*
 HVCC2WWakeupFragment
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvcp.setting.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.hvcp.R;

/**
 * HVC-P Settings Fragment Page 1.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCPWakeupFragment extends Fragment {
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        return inflater.inflate(R.layout.setting_wakeup, null);
    }
}