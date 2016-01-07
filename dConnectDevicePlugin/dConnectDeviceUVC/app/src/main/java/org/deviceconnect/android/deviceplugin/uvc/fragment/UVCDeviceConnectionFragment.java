/*
 UVCDeviceConnectionFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.serenegiant.usb.CameraDialog;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.activity.UVCDeviceSettingsActivity;

import java.util.Iterator;
import java.util.List;


public class UVCDeviceConnectionFragment extends Fragment implements UVCDeviceManager.DeviceListener {

    private TextView mCurrentDeviceTextView;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_uvc_device_connection, null);

        Button connectionButton = (Button) root.findViewById(R.id.button_uvc_device_connection);
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Activity activity = getActivity();
                if (activity != null) {
                    CameraDialog.showDialog(activity);
                }
            }
        });

        mCurrentDeviceTextView = (TextView) root.findViewById(R.id.text_current_uvc_device);
        showCurrentDeviceNames();
        return root;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
    }

    private void showCurrentDeviceNames() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showCurrentDeviceNames(activity);
                }
            });
        }
    }

    private void showCurrentDeviceNames(final Context context) {
        UVCDeviceManager deviceMgr = getDeviceManager();
        if (deviceMgr != null) {
            List<UVCDevice> devices = deviceMgr.getDeviceList();
            StringBuilder sb = new StringBuilder();
            sb.append(context.getString(R.string.current_uvc_device));
            sb.append(": ");
            for (Iterator<UVCDevice> it = devices.iterator(); it.hasNext(); ) {
                UVCDevice device = it.next();
                sb.append(device.getName());
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
            mCurrentDeviceTextView.setText(sb.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        UVCDeviceManager deviceMgr = getDeviceManager();
        if (deviceMgr != null) {
            deviceMgr.start();
            deviceMgr.addDeviceListener(this);
        }
    }

    @Override
    public void onPause() {
        UVCDeviceManager deviceMgr = getDeviceManager();
        if (deviceMgr != null) {
            deviceMgr.removeDeviceListener(this);
        }

        super.onPause();
    }

    private UVCDeviceManager getDeviceManager() {
        UVCDeviceSettingsActivity activity = (UVCDeviceSettingsActivity) getActivity();
        if (activity == null) {
            return null;
        }
        return activity.getDeviceManager();
    }

    @Override
    public void onOpen(final UVCDevice device) {
        showCurrentDeviceNames();
    }

    @Override
    public void onClose(final UVCDevice device) {
        showCurrentDeviceNames();
    }

}
