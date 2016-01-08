/*
 UVCDeviceConnectionFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.fragment;

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

import java.util.List;


public class UVCDeviceConnectionFragment extends Fragment implements UVCDeviceManager.DeviceListener {

    private ConnectionButton mConnectionButton;

    private TextView mCurrentDeviceTextView;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_uvc_device_connection, null);

        mConnectionButton
            = new ConnectionButton((Button) root.findViewById(R.id.button_uvc_device_connection));
        mCurrentDeviceTextView = (TextView) root.findViewById(R.id.text_current_uvc_device);
        updateViews();
        return root;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
    }

    private void updateViews() {
        final UVCDeviceSettingsActivity activity = (UVCDeviceSettingsActivity) getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UVCDeviceManager deviceMgr = activity.getDeviceManager();
                    List<UVCDevice> devices = deviceMgr.getDeviceList();
                    mConnectionButton.update(devices);
                    showCurrentDeviceNames(devices);
                }
            });
        }
    }

    private void showCurrentDeviceNames(final List<UVCDevice> devices) {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.current_uvc_device));
        sb.append(": ");
        if (devices.size() == 0 || !devices.get(0).isOpen()) {
            sb.append(getString(R.string.no_uvc_device));
        } else {
            UVCDevice device = devices.get(0);
            sb.append(device.getName());
        }
        mCurrentDeviceTextView.setText(sb.toString());
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
        updateViews();
    }

    @Override
    public void onClose(final UVCDevice device) {
        updateViews();
    }

    private class ConnectionButton implements View.OnClickListener  {

        private final Button mButton;

        private UVCDevice mOpenedDevice;

        public ConnectionButton(final Button button) {
            mButton = button;
            mButton.setOnClickListener(this);
        }

        public void update(final List<UVCDevice> devices) {
            if (devices.size() == 0 || !devices.get(0).isOpen()) {
                mOpenedDevice = null;
            } else {
                mOpenedDevice = devices.get(0);
            }

            int messageId;
            if (mOpenedDevice == null) {
                messageId = R.string.uvc_device_connection;
            } else {
                messageId = R.string.uvc_device_disconnection;
            }
            mButton.setText(getString(messageId));
        }

        @Override
        public void onClick(final View view) {
            UVCDeviceSettingsActivity activity = (UVCDeviceSettingsActivity) getActivity();
            if (activity != null) {
                if (mOpenedDevice == null) {
                    CameraDialog.showDialog(activity);
                } else {
                    UVCDeviceManager deviceMgr = activity.getDeviceManager();
                    deviceMgr.closeDevice(mOpenedDevice);
                }
            }
        }
    }

}
