/*
 SummaryFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceModel;

/**
 * The page which summarize the settings window of THETA device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class SummaryFragment extends SettingsFragment implements RadioGroup.OnCheckedChangeListener {

    private View mRoot;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        if (mRoot == null) {
            mRoot = inflater.inflate(R.layout.fragment_summary, null);
            RadioGroup group = (RadioGroup) mRoot.findViewById(R.id.settings_theta);
            group.setOnCheckedChangeListener(this);
        }
        return mRoot;
    }

    @Override
    public void onCheckedChanged(final RadioGroup radioGroup, final int id) {
        ThetaDeviceModel model;
        switch (id) {
            case R.id.settings_theta_s:
                model = ThetaDeviceModel.THETA_S;
                break;
            case R.id.settings_theta_m15:
                model = ThetaDeviceModel.THETA_M15;
                break;
            default:
                return;
        }

        ThetaDeviceSettingsActivity activity = (ThetaDeviceSettingsActivity) getActivity();
        if (activity != null) {
            activity.setSelectedModel(model);
        }
    }

}
