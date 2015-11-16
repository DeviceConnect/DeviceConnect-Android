/*
 ConfirmationFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;

import java.util.logging.Logger;

/**
 * The page for confirmation of the connection between THETA and Android device.
 *
 * @author NTT DOCOMO, INC.
 */
public class ConfirmationFragment extends Fragment {

    private final Logger mLogger = Logger.getLogger("theta.plugin");

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_confirmation, null);
        Button btnCameraSearch = (Button) rootView.findViewById(R.id.btn_camera_search);
        btnCameraSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ThetaDevice device = getConnectedDevice();
                String message;
                if (device != null) {
                    message = getString(R.string.camera_search_message_found);
                    message = message.replace("$NAME$", device.getName());
                } else {
                    message = getString(R.string.camera_search_message_not_found);
                }
                TextView messageView = (TextView) rootView.findViewById(R.id.camera_search_message);
                messageView.setText(message);
            }
        });
        return rootView;
    }

    private ThetaDevice getConnectedDevice() {
        Activity activity = getActivity();
        if (activity != null) {
            ThetaDeviceApplication app = (ThetaDeviceApplication) activity.getApplication();
            ThetaDeviceManager deviceManager = app.getDeviceManager();
            return deviceManager.getConnectedDevice();
        } else {
            return null;
        }
    }
}
