/*
 PhotoModeFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceModel;

/**
 * The page which explains how to boot THETA with photo mode.
 *
 * @author NTT DOCOMO, INC.
 */
public class PhotoModeFragment extends SettingsFragment {

    private View mRoot;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        if (mRoot == null) {
            mRoot = inflater.inflate(R.layout.fragment_photo_mode, null);
        }
        updateView();
        return mRoot;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public void onModelSelected(final ThetaDeviceModel model) {
        updateView(model);
    }

    private void updateView() {
        ThetaDeviceSettingsActivity activity = (ThetaDeviceSettingsActivity) getActivity();
        if (activity != null) {
            updateView(activity.getSelectedModel());
        }
    }

    public void updateView(final ThetaDeviceModel model) {
        View root = getView();
        if (root != null) {
            int imageId;
            int textId;
            switch (model) {
                case THETA_S:
                    imageId = R.drawable.theta_s_photo;
                    textId = R.string.photo_mode_body_theta_s;
                    break;
                case THETA_M15:
                    imageId = R.drawable.theta_photo;
                    textId = R.string.photo_mode_body_theta_m15;
                    break;
                default:
                    return;
            }
            ImageView imageView = (ImageView) root.findViewById(R.id.image_theta_device_photo_mode);
            imageView.setImageResource(imageId);
            TextView textView = (TextView) root.findViewById(R.id.text_photo_mode);
            textView.setText(textId);
        }
    }

}
