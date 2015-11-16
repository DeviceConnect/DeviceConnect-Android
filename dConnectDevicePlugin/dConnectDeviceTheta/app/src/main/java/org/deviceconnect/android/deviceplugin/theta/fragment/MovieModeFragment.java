/*
 MovieModeFragment
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
import android.widget.ImageView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaDeviceSettingsActivity;

/**
 * The page which explains how to boot THETA with movie mode.
 *
 * @author NTT DOCOMO, INC.
 */
public class MovieModeFragment extends Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_mode, null);
    }

    public void updateView() {
        ThetaDeviceSettingsActivity activity = (ThetaDeviceSettingsActivity) getActivity();
        View root = getView();
        if (activity != null && root != null) {
            int imageId;
            int textId;
            switch (activity.getSelectedModel()) {
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
            ImageView imageView = (ImageView) root.findViewById(R.id.image_theta_device_movie_mode);
            imageView.setImageResource(imageId);
            TextView textView = (TextView) root.findViewById(R.id.text_movie_mode);
            textView.setText(textId);
        }
    }

    @Override
    public void onResume () {
        super.onResume();
        updateView();
    }

}
