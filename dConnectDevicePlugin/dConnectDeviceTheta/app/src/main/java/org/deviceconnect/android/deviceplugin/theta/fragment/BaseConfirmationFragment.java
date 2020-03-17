/*
 ConfirmationFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceEventListener;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.utils.UserSettings;
import org.deviceconnect.android.deviceplugin.theta.utils.WiFiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * The page for confirmation of the connection between THETA and Android device.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class BaseConfirmationFragment extends SettingsFragment implements ThetaDeviceEventListener {
    /** Interval. */
    protected static final int INTERVAL = 1000;

    /** View to display the service ID. */
    protected TextView mServiceIdView;
    /** Wifi management class. */
    protected WifiManager mWifiMgr;
    /** Class that holds the configuration. */
    protected UserSettings mSettings;
    /** Search in dialog. */
    protected ThetaDialogFragment mDialog;
    /** Logger. */
    protected final Logger mLogger = Logger.getLogger("theta.dplugin");

    protected abstract void connectWifi(ScanResult result);

    protected abstract void onWifiDisabled();

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_confirmation, null);
        mServiceIdView = rootView.findViewById(R.id.camera_search_message);
        mSettings = new UserSettings(getActivity());
        mWifiMgr = getWifiManager();

        Button btnCameraSearch = rootView.findViewById(R.id.btn_camera_search);
        btnCameraSearch.setOnClickListener(view -> {
            String ssId = mWifiMgr.getConnectionInfo().getSSID();
            mLogger.info("Current Wi-Fi SSID: " + ssId);
            if (WiFiUtil.checkSSID(ssId)) {
                mServiceIdView.setText(getThetaName());
            } else {
                connectTheta();
            }
        });

        Activity activity = getActivity();
        if (activity != null) {
            ThetaDeviceApplication app = (ThetaDeviceApplication) activity.getApplication();
            ThetaDeviceManager deviceManager = app.getDeviceManager();
            deviceManager.registerDeviceEventListener(this);
        }

        return rootView;
    }

    /* Get connected Theta device's info. */
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

    private WifiManager getWifiManager() {
        return (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /* Get Theta Device's Name. */
    private String getThetaName() {
        ThetaDevice device = getConnectedDevice();
        String message;
        if (device != null) {
            message = getString(R.string.camera_search_message_found);
            message = message.replace("$NAME$", device.getName());
        } else {
            message = getString(R.string.camera_search_message_not_found);
        }
        return message;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Activity activity = getActivity();
        if (activity != null) {
            ThetaDeviceApplication app = (ThetaDeviceApplication) activity.getApplication();
            ThetaDeviceManager deviceManager = app.getDeviceManager();
            deviceManager.unregisterDeviceEventListener(this);
        }
    }

    /**
     * Connection to the Theta device.
     */
    protected void connectTheta() {
        if (!mWifiMgr.isWifiEnabled()) {
            onWifiDisabled();
        } else {
            runOnUiThread(() -> mServiceIdView.setText(R.string.theta_connecting));
            WifiInfo wifiInfo = mWifiMgr.getConnectionInfo();
            if (WiFiUtil.checkSSID(wifiInfo.getSSID())) {
                searchTheta();
            } else {
                searchThetaWifi();
            }
        }
    }

    /**
     * Explore the WiFi that exist around.
     * <p>
     * Search results, we want to display to WiFiDeviceListFragment.
     * </p>
     */
    private void searchTheta() {
        WifiInfo info = mWifiMgr.getConnectionInfo();
        if (info != null && WiFiUtil.checkSSID(info.getSSID())) {
            mServiceIdView.setText(getThetaName());
        } else {
            mServiceIdView.setText(R.string.camera_search_message_not_found);
        }
    }

    /**
     * Search to connect the Wifi of Theta device.
     */
    private void searchThetaWifi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getThetaAPList();
        } else {
            checkLocationServiceEnabled();
        }
    }

    /** Check Location Service Permission. */
    private void checkLocationServiceEnabled() {
        checkPermission();
    }

    /** Check Permission. */
    private void checkPermission() {
        // WiFi scan requires location permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && getContext().checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getThetaAPList();
            } else {
                PermissionUtility.requestPermissions(getContext(), new Handler(Looper.getMainLooper()),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                getThetaAPList();
                            }

                            @Override
                            public void onFail(@NonNull String deniedPermission) {
                                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_confirm_wifi),
                                        getString(R.string.theta_error_request_permission), null);
                            }
                        });
            }
        }
    }

    protected void showConnectionProgress() {
        runOnUiThread(() -> {
            if (mDialog == null) {
                mDialog = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(R.string.connecting));
                mDialog.show(getActivity().getFragmentManager(),
                        "fragment_dialog");
            }
            new Handler().postDelayed(() -> { //Timeout Hadler
                if (mDialog != null) {
                    mDialog.dismiss();
                    mDialog = null;
                    ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_confirm_wifi),
                            getString(R.string.theta_error_wrong_password), null);
                    mServiceIdView.setText(R.string.camera_search_message_not_found);

                }
            }, 30000);
        });
    }

    /** Get Theta AP List. */
    protected void getThetaAPList() {
        final List<ScanResult> scanList = new ArrayList<>();
        mWifiMgr.startScan();

        final AtomicBoolean unregistered = new AtomicBoolean(false);
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> results = mWifiMgr.getScanResults();
                for (ScanResult result : results) {
                    if (WiFiUtil.checkSSID(result.SSID)) {
                        scanList.add(result);
                    }
                }

                if (scanList.size() > 0) {
                    String[] wifiList = new String[scanList.size()];
                    for (int i = 0; i < scanList.size(); i++) {
                        wifiList[i] = scanList.get(i).SSID;
                        wifiList[i] = wifiList[i].replace("\"", "");
                    }
                    final int[] pos = new int[1];
                    ThetaDialogFragment.showSelectWifiDialog(getActivity(), getString(R.string.theta_confirm_wifi),
                            wifiList,
                            (dialog, which) -> pos[0] = which,
                            (dialog, which)-> {
                                ScanResult result = scanList.get(pos[0]);
                                List<WifiConfiguration> list = mWifiMgr.getConfiguredNetworks();
                                mLogger.info("Selected Wi-Fi: SSID = " + result.SSID);
                                mLogger.info("Configured Networks: size = " + list.size());
                                boolean isEnabled = false;
                                for (WifiConfiguration i : list) {
                                    mLogger.info("Found Wi-Fi SSID = " + i.SSID);
                                    if (i.SSID != null && i.SSID.indexOf(result.SSID) > 0) {
                                        isEnabled = mWifiMgr.enableNetwork(i.networkId, true);
                                        break;
                                    }
                                }
                                if (!isEnabled) {
                                    mLogger.info("Need to create new Wi-Fi configuration: SSID = " + result.SSID);
                                    connectWifi(result);
                                } else {
                                    mLogger.info("Enable other network: isEnabled = " + isEnabled + ", SSID = " + result.SSID);
                                    showConnectionProgress();
                                }
                            },
                            (dialog, which) -> mServiceIdView.setText(R.string.theta_no_device));
                } else {
                    ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_confirm_wifi),
                            getString(R.string.camera_search_message_not_found), null);
                    mServiceIdView.setText(R.string.camera_search_message_not_found);

                }

                synchronized (unregistered) {
                    if (!unregistered.get()) {
                        getContext().unregisterReceiver(this);
                        unregistered.set(true);
                    }
                }
            }
        };
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            synchronized (unregistered) {
                if (!unregistered.get()) {
                    getContext().unregisterReceiver(receiver);
                    unregistered.set(true);
                }
            }
        }, 30, TimeUnit.SECONDS);

        getContext().registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    public void onConnected(final ThetaDevice device) {
//        mLogger.info("onConnected: device = " + device.getName());

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        runOnUiThread(() -> mServiceIdView.setText(getThetaName()));
    }

    @Override
    public void onDisconnected(final ThetaDevice device) {
        this.onConnected(device);
    }

    private void runOnUiThread(final Runnable r) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(r);
        }
    }
}
