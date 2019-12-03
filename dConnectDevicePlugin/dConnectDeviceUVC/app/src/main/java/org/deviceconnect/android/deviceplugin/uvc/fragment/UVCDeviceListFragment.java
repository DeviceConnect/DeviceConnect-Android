/*
 UVCDeviceListFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.uvc.R;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceApplication;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.fragment.dialog.ErrorDialogFragment;
import org.deviceconnect.android.deviceplugin.uvc.fragment.dialog.ProgressDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager.ConnectionListener;
import static org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager.DiscoveryListener;

public class UVCDeviceListFragment extends Fragment {
    /**
     * Adapter.
     */
    private DeviceAdapter mDeviceAdapter;

    /**
     * Error Dialog.
     */
    private ErrorDialogFragment mErrorDialogFragment;

    /**
     * Progress Dialog.
     */
    private ProgressDialogFragment mProgressDialogFragment;

    /**
     * UVC device list view.
     */
    private ListView mListView;

    /**
     * Footer view.
     */
    private View mFooterView;

    /**
     * Logger.
     */
    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    /**
     * Executor.
     */
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        mDeviceAdapter = new DeviceAdapter(getActivity(), createDeviceContainers());

        mFooterView = inflater.inflate(R.layout.item_uvc_error, null);

        View rootView = inflater.inflate(R.layout.fragment_uvc_device_list, null);
        mListView = rootView.findViewById(R.id.device_list_view);
        mListView.setAdapter(mDeviceAdapter);
        mListView.setItemsCanFocus(true);
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        addFooterView();
        getManager().startScan();
        getManager().addConnectionListener(mConnectionListener);
        getManager().addDiscoveryListener(mDiscoverListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getManager().removeConnectionListener(mConnectionListener);
        getManager().removeDiscoveryListener(mDiscoverListener);
        getManager().stopScan();
        dismissProgressDialog();
        dismissErrorDialog();
    }

    /**
     * Added the view at ListView.
     */
    private void addFooterView() {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(() -> {
            UVCDeviceManager mgr = getManager();
            if (mgr == null) {
                return;
            }
            LayoutInflater inflater = activity.getLayoutInflater();
            if (mFooterView != null) {
                mListView.removeFooterView(mFooterView);
            }
            if (mgr.getDeviceList().size() == 0) {
                mFooterView = inflater.inflate(R.layout.item_uvc_error, null);
                mListView.addFooterView(mFooterView);
            }
        });
    }

    /**
     * Connect to the UVC device that have heart rate service.
     *
     * @param device UVC device that have heart rate service.
     */
    private void connectDevice(final DeviceContainer device) {
        showProgressDialog(device.getName());
        mExecutor.execute(() -> {
            getManager().connectDevice(device.getId());
        });
    }

    /**
     * Disconnect to the UVC device that have heart rate service.
     *
     * @param device UVC device that have heart rate service.
     */
    private void disconnectDevice(final DeviceContainer device) {
        mExecutor.execute(() -> {
            getManager().disconnectDevice(device.getId());
        });
    }

    /**
     * Display the dialog of connecting a ble device.
     *
     * @param name device name
     */
    private void showProgressDialog(final String name) {
        dismissProgressDialog();

        Resources res = getActivity().getResources();
        String title = res.getString(R.string.uvc_settings_connecting_title);
        String message = res.getString(R.string.uvc_settings_connecting_message, name);
        mProgressDialogFragment = ProgressDialogFragment.newInstance(title, message);
        mProgressDialogFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Dismiss the dialog of connecting a ble device.
     */
    private void dismissProgressDialog() {
        if (mProgressDialogFragment != null) {
            mProgressDialogFragment.dismiss();
            mProgressDialogFragment = null;
        }
    }

    /**
     * Display the error dialog of not connect device.
     *
     * @param name device name
     */
    private void showErrorDialogNotConnect(final String name) {
        Resources res = getActivity().getResources();
        String message;
        if (name == null) {
            message = res.getString(R.string.uvc_settings_dialog_error_message,
                    getString(R.string.uvc_settings_default_name));
        } else {
            message = res.getString(R.string.uvc_settings_dialog_error_message, name);
        }
        showErrorDialog(message);
    }

    /**
     * Display the error dialog.
     *
     * @param message error message
     */
    public void showErrorDialog(final String message) {
        dismissErrorDialog();

        Resources res = getActivity().getResources();
        String title = res.getString(R.string.uvc_settings_dialog_error_title);
        mErrorDialogFragment = ErrorDialogFragment.newInstance(title, message);
        mErrorDialogFragment.show(getFragmentManager(), "error_dialog");
        mErrorDialogFragment.setOnDismissListener((dialog) -> {
            mErrorDialogFragment = null;
        });
    }

    /**
     * Dismiss the error dialog.
     */
    private void dismissErrorDialog() {
        if (mErrorDialogFragment != null) {
            mErrorDialogFragment.dismiss();
            mErrorDialogFragment = null;
        }
    }

    /**
     * Gets a instance of UVCDeviceManager.
     *
     * @return UVCDeviceManager
     */
    private UVCDeviceManager getManager() {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        UVCDeviceApplication application =
                (UVCDeviceApplication) activity.getApplication();
        return application.getDeviceManager();
    }

    private ConnectionListener mConnectionListener = new ConnectionListener() {

        @Override
        public void onConnect(final UVCDevice device) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(() -> {
                DeviceContainer container = findDeviceContainerById(device.getId());
                if (container != null) {
                    container.setRegisterFlag(true);
                    mDeviceAdapter.notifyDataSetChanged();
                }
                dismissProgressDialog();
            });
        }

        @Override
        public void onConnectionFailed(final UVCDevice device) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(() -> {
                dismissProgressDialog();
                showErrorDialogNotConnect(device.getName());
            });
        }

        @Override
        public void onDisconnect(final UVCDevice device) {
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(() -> {
                DeviceContainer container = findDeviceContainerById(device.getId());
                if (container != null) {
                    container.setRegisterFlag(false);
                    mDeviceAdapter.notifyDataSetChanged();
                }
                dismissProgressDialog();
            });
        }
    };

    private final DiscoveryListener mDiscoverListener = new DiscoveryListener() {

        @Override
        public void onDiscovery(final List<UVCDevice> devices) {
            mLogger.info("Discovered devices: " + devices.size());
            if (mDeviceAdapter == null) {
                return;
            }
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(() -> {
                mDeviceAdapter.clear();
                mDeviceAdapter.addAll(createDeviceContainers());
                mDeviceAdapter.notifyDataSetChanged();
                addFooterView();
            });
        }

    };

    /**
     * Create a list of device.
     *
     * @return list of device
     */
    private List<DeviceContainer> createDeviceContainers() {
        List<DeviceContainer> containers = new ArrayList<>();
        List<UVCDevice> devices = getManager().getDeviceList();
        for (UVCDevice device : devices) {
            containers.add(createContainer(device, device.isInitialized()));
        }
        return containers;
    }

    /**
     * Look for a DeviceContainer with the given id.
     *
     * @param id id of device
     * @return The DeviceContainer that has the given id or null
     */
    private DeviceContainer findDeviceContainerById(final String id) {
        int size = mDeviceAdapter.getCount();
        for (int i = 0; i < size; i++) {
            DeviceContainer container = mDeviceAdapter.getItem(i);
            if (container.getId().equalsIgnoreCase(id)) {
                return container;
            }
        }
        return null;
    }

    /**
     * Create a DeviceContainer from UVCDevice.
     *
     * @param device   Instance of UVCDevice
     * @param register Registration flag
     * @return Instance of DeviceContainer
     */
    private DeviceContainer createContainer(final UVCDevice device, final boolean register) {
        DeviceContainer container = new DeviceContainer();
        container.setName(device.getName());
        container.setId(device.getId());
        container.setRegisterFlag(register);
        return container;
    }

    private class DeviceContainer {
        private String mName;
        private String mId;
        private boolean mRegisterFlag;

        public String getName() {
            return mName;
        }

        public void setName(final String name) {
            if (name == null) {
                mName = getActivity().getResources().getString(
                        R.string.uvc_settings_default_name);
            } else {
                mName = name;
            }
        }

        public String getId() {
            return mId;
        }

        public void setId(final String id) {
            mId = id;
        }

        public boolean isRegisterFlag() {
            return mRegisterFlag;
        }

        public void setRegisterFlag(boolean registerFlag) {
            mRegisterFlag = registerFlag;
        }
    }

    private class DeviceAdapter extends ArrayAdapter<DeviceContainer> {
        private LayoutInflater mInflater;

        public DeviceAdapter(final Context context, final List<DeviceContainer> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_uvc_device, null);
            }

            final DeviceContainer device = getItem(position);

            String name = device.getName();
            TextView nameView = convertView.findViewById(R.id.device_name);
            nameView.setText(name);

            Button btn = convertView.findViewById(R.id.btn_connect_device);
            if (device.isRegisterFlag()) {
                btn.setBackgroundResource(R.drawable.button_red);
                btn.setText(R.string.uvc_settings_disconnect);
            } else {
                btn.setBackgroundResource(R.drawable.button_blue);
                btn.setText(R.string.uvc_settings_connect);
            }
            btn.setOnClickListener((v) -> {
                if (device.isRegisterFlag()) {
                    disconnectDevice(device);
                } else {
                    connectDevice(device);
                }
            });

            return convertView;
        }
    }
}
