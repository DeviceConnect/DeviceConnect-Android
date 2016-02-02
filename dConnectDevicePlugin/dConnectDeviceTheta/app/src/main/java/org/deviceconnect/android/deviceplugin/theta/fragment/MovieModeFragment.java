/*
 MovieModeFragment
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
 * The page which explains how to boot THETA with movie mode.
 *
 * @author NTT DOCOMO, INC.
 */
public class MovieModeFragment extends SettingsFragment {

    private View mRoot;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        if (mRoot == null) {
            mRoot = inflater.inflate(R.layout.fragment_movie_mode, null);
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
        if (mRoot != null) {
            int imageId;
            int textId;
            switch (model) {
                case THETA_S:
                    imageId = R.drawable.theta_s_movie;
                    textId = R.string.movie_mode_body_theta_s;
                    break;
                case THETA_M15:
                    imageId = R.drawable.theta_movie;
                    textId = R.string.movie_mode_body_theta_m15;
                    break;
                default:
                    return;
            }
            ImageView imageView = (ImageView) mRoot.findViewById(R.id.image_theta_device_movie_mode);
            imageView.setImageResource(imageId);
            TextView textView = (TextView) mRoot.findViewById(R.id.text_movie_mode);
            textView.setText(textId);
        }
    }

}
