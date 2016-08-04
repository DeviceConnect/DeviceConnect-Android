/*
 DeviceSelectionPageFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.setting.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.deviceconnect.android.deviceplugin.sphero.R;
import org.deviceconnect.android.deviceplugin.sphero.data.SpheroParcelable;
import org.deviceconnect.android.deviceplugin.sphero.setting.SettingActivity;
import org.deviceconnect.android.deviceplugin.sphero.setting.SettingActivity.DeviceControlListener;
import org.deviceconnect.android.deviceplugin.sphero.setting.widget.DeviceListAdapter;
import org.deviceconnect.android.deviceplugin.sphero.setting.widget.DeviceListAdapter.OnConnectButtonClickListener;

import java.util.List;

/**
 * デバイス一覧画面.
 * @author NTT DOCOMO, INC.
 */
public class DeviceSelectionPageFragment extends Fragment implements DeviceControlListener,
        OnConnectButtonClickListener {

    /**
     * プログレス領域の可視フラグ.
     */
    private static final String KEY_PROGRESS_VISIBILITY = "visible";

    /**
     * アダプター.
     */
    private DeviceListAdapter mAdapter;

    /**
     * インジケーター.
     */
    private ProgressDialog mIndView;

    /**
     * 検索ダイアログの表示状態.
     */
    private int mSearchingVisibility = -1;
    /**
     * Is Thread Running.
     */
    private boolean mIsThreadRunning = false;
    /**
     * Thread Handler.
     */
    private Handler mThreadHandler = new Handler();

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        ((SettingActivity) activity).setDeviceControlListener(this);
        mAdapter = new DeviceListAdapter(getActivity());
        mAdapter.setOnConnectButtonClickListener(this);
        ((SettingActivity) activity).sendGetConnectedDevicesBroadcast();
    }

    @SuppressLint("InflateParams")
	@Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, 
            final Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.setting_device_list, null);
        View emptyView = root.findViewById(R.id.device_list_empty);
        ListView listView = (ListView) root.findViewById(R.id.device_list_view);
        listView.setAdapter(mAdapter);
        listView.setEmptyView(emptyView);
        listView.setItemsCanFocus(true);

        View progressZone = root.findViewById(R.id.progress_zone);
        if (savedInstanceState != null) {
            mSearchingVisibility = savedInstanceState.getInt(KEY_PROGRESS_VISIBILITY);
        }

        if (mSearchingVisibility == -1 || mAdapter.getCount() != 0) {
            progressZone.setVisibility(View.GONE);
        }
        
        return root;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        View root = getView();
        if (root != null) {
            View progressZone = root.findViewById(R.id.progress_zone);
            outState.putInt(KEY_PROGRESS_VISIBILITY, progressZone.getVisibility());
        }
    }
    
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startDiscoveryTimer();
    }


    @Override
    public void onConnectedDevices(final List<Parcelable> devices) {
        if (devices.size() == 0) {
            startDiscoveryTimer();
        } else {
            for (Parcelable device : devices) {
                if (device instanceof SpheroParcelable) {
                    mAdapter.add((SpheroParcelable) device);
                }
            }
        }
    }

    @Override
    public void onDeviceFound(final SpheroParcelable device) {

        View root = getView();
        if (root != null) {
            View progressZone = root.findViewById(R.id.progress_zone);
            progressZone.setVisibility(View.GONE);
        }

        boolean isExist = false;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            SpheroParcelable s = mAdapter.getItem(i);
            if (s.getSpheroId().equals(device.getSpheroId())) {
                isExist = true;
            }
        }
        if (!isExist) {
            mAdapter.add(device);
        }
        startDiscoveryTimer();
    }

    @Override
    public void onDeviceLost(final SpheroParcelable device) {
        Log.d("TEST", "device lost:" + device.getSpheroName());
        mAdapter.remove(device);
        startDiscoveryTimer();

        if (mIndView != null && mIndView.isShowing()) {
            mIndView.dismiss();
        }
    }

    @Override
    public void onDeviceLostAll() {
        mAdapter.clear();
        View root = getView();
        if (root != null) {
            View progressZone = root.findViewById(R.id.progress_zone);
            progressZone.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeviceConnected(final SpheroParcelable device) {
       if (mIndView != null) {
            mIndView.dismiss();
        }

        if (device == null) {
            AlertDialog.Builder builder = new Builder(getActivity());
            builder.setTitle(R.string.title_error);
            builder.setMessage(R.string.message_conn_error);
            builder.setPositiveButton(R.string.btn_close, new OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            return;
        }
        device.setConnected(true);
        mAdapter.changeConnectionState(device);
        startDiscoveryTimer();


    }

    @Override
    public void onDeviceDisconnected(SpheroParcelable device) {
        if (device == null) {
            AlertDialog.Builder builder = new Builder(getActivity());
            builder.setTitle(R.string.title_error);
            builder.setMessage(R.string.message_disconn_error);
            builder.setPositiveButton(R.string.btn_close, new OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            return;
        }
        device.setConnected(false);
        mAdapter.changeConnectionState(device);
        startDiscoveryTimer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        View root = getView();
        if (root != null) {
            View progressZone = root.findViewById(R.id.progress_zone);
            mSearchingVisibility = progressZone.getVisibility();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsThreadRunning = false;
        stopDiscovery();
    }

    @Override
    public void onClicked(final int position, final SpheroParcelable device) {

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        mIsThreadRunning = false;
        stopDiscovery();
        if (device.isConnected()) {
            ((SettingActivity) activity).sendDisonnectBroadcast(device.getSpheroId());
            if (mAdapter.getCount() == 0) {
                // 現在検知している場合は一旦検知をやめ、新たに検知を開始する.
                startDiscoveryTimer();
            }
        } else {
            mIndView = new ProgressDialog(activity);
            mIndView.setMessage(activity.getString(R.string.connecting));
            mIndView.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mIndView.setCancelable(false);
            mIndView.show();
            ((SettingActivity) activity).sendConnectBroadcast(device.getSpheroId());
        }
    }

    /**
     * 検知開始.
     */
    private void startDiscovery() {
        Activity activity = getActivity();
        if (activity != null) {
            ((SettingActivity) activity).sendStartDiscoveryBroadcast();
            View root = getView();
            if (root != null) {
                View progressZone = root.findViewById(R.id.progress_zone);
                progressZone.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 検知終了.
     */
    private void stopDiscovery() {
        Activity activity = getActivity();
        if (activity != null) {
            ((SettingActivity) activity).sendStopDiscoveryBroadcast();
        }
    }

    private void startDiscoveryTimer() {
        if (mIsThreadRunning) {
            return;
        }
        mIsThreadRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    return;
                }
                while (mIsThreadRunning) {
                    if (BluetoothAdapter.STATE_ON == mBluetoothAdapter.getState()) {
                        mThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                stopDiscovery();
                                startDiscovery();
                            }
                        });
                    }

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
