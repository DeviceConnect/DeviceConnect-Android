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
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.activity.UVCDeviceSettingsActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


public class UVCDeviceConnectionFragment extends Fragment {

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private ConnectionButton mConnectionButton;

    private TextView mCurrentDeviceTextView;

    private TextureView mPreviewTextureView;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_uvc_device_connection, null);

        mConnectionButton
            = new ConnectionButton((Button) root.findViewById(R.id.button_uvc_device_connection));
        mCurrentDeviceTextView = (TextView) root.findViewById(R.id.text_current_uvc_device);
        mPreviewTextureView = (TextureView) root.findViewById(R.id.view_preview);

        UVCDevice device = getDevice();
        if (device != null) {
            updateViews(device);
        }
        return root;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
    }

    private void updateViews(final UVCDevice device) {
        final UVCDeviceSettingsActivity activity = (UVCDeviceSettingsActivity) getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConnectionButton.update(device);
                    showCurrentDeviceNames(device);
                }
            });
        }
    }

    private void showCurrentDeviceNames(final UVCDevice device) {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.current_uvc_device));
        sb.append(": ");
        if (device == null || !device.isOpen()) {
            sb.append(getString(R.string.no_uvc_device));
        } else {
            sb.append(device.getName());
        }
        mCurrentDeviceTextView.setText(sb.toString());
    }

    private UVCDeviceManager getDeviceManager() {
        UVCDeviceSettingsActivity activity = (UVCDeviceSettingsActivity) getActivity();
        if (activity == null) {
            return null;
        }
        return activity.getDeviceManager();
    }

    private UVCDevice getDevice() {
        UVCDeviceManager deviceMgr = getDeviceManager();
        if (deviceMgr == null) {
            return null;
        }
        List<UVCDevice> devices = deviceMgr.getDeviceList();
        if (devices.size() == 0) {
            return null;
        }
        return devices.get(0);
    }

    private class ConnectionButton implements View.OnClickListener, UVCPreviewDialogFragment.OnSelectListener  {

        private final Button mButton;

        private UVCDevice mOpenedDevice;

        public ConnectionButton(final Button button) {
            mButton = button;
            mButton.setOnClickListener(this);
        }

        public void update(final UVCDevice device) {
            if (device == null || !device.isOpen()) {
                mOpenedDevice = null;
            } else {
                mOpenedDevice = device;
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
                    UVCPreviewDialogFragment.show(activity, this);
                } else {
                    mOpenedDevice.stopPreview();
                    mOpenedDevice.clearPreviewDisplay();
                    UVCDeviceManager deviceMgr = activity.getDeviceManager();
                    deviceMgr.closeDevice(mOpenedDevice);
                }
            }
        }

        @Override
        public void onSelect(final UVCDevice device) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    UVCDeviceManager deviceMgr = getDeviceManager();
                    if (!device.isOpen()) {
                        if (!deviceMgr.openDevice(device)) {
                            mLogger.severe("Failed to open UVC device for preview: " + device.getId());
                            return;
                        }
                    }
                    device.setPreviewDisplay(mPreviewTextureView);
                    device.startPreview();
                }
            });
        }
    }

}
