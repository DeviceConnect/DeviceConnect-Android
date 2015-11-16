/*
 SummaryFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceModel;

/**
 * The page which summarize the settings window of THETA device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class SummaryFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private ThetaDeviceModel mSelectedModel = ThetaDeviceModel.THETA_S;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_summary, null);

        selectRadioButton(root, R.id.settings_theta_s);

        RadioGroup group = (RadioGroup) root.findViewById(R.id.settings_theta);
        group.setOnCheckedChangeListener(this);
        return root;
    }

    @Override
    public void onCheckedChanged(final RadioGroup radioGroup, final int id) {
        switch (id) {
            case R.id.settings_theta_s:
                mSelectedModel = ThetaDeviceModel.THETA_S;
                break;
            case R.id.settings_theta_m15:
                mSelectedModel = ThetaDeviceModel.THETA_M15;
                break;
            default:
                break;
        }

        ThetaDeviceSettingsActivity activity = (ThetaDeviceSettingsActivity) getActivity();
        if (activity != null) {
            activity.onModelChanged();
        }
    }

    private void selectRadioButton(final View root, final int id) {
        RadioButton radioButton = (RadioButton) root.findViewById(id);
        radioButton.setChecked(true);
    }

    public ThetaDeviceModel getSelectedModel() {
        return mSelectedModel;
    }



}
