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
import android.content.DialogInterface;
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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
public class ConfirmationFragment extends SettingsFragment implements ThetaDeviceEventListener {
    /** Interval. */
    private static final int INTERVAL = 1000;

    /** View to display the service ID. */
    private TextView mServiceIdView;
    /** Wifi management class. */
    private WifiManager mWifiMgr;
    /** Class that holds the configuration. */
    private UserSettings mSettings;
    /** Search in dialog. */
    private ThetaDialogFragment mDialog;
    /** Logger. */
    private final Logger mLogger = Logger.getLogger("theta.dplugin");
    /** Wi-Fi State Receiver. */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            mLogger.info("ConfirmationFragment: action = " + action
                + ", isWaitingWiFiEnabled = " + mIsWaitingWifiEnabled);
            if (!mIsWaitingWifiEnabled) {
                return;
            }
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                switch (state) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        mIsWaitingWifiEnabled = false;
                        connectTheta();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private boolean mIsWaitingWifiEnabled;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_confirmation, null);
        mServiceIdView = (TextView) rootView.findViewById(R.id.camera_search_message);
        mSettings = new UserSettings(getActivity());
        mWifiMgr = getWifiManager();

        Button btnCameraSearch = (Button) rootView.findViewById(R.id.btn_camera_search);
        btnCameraSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String ssId = mWifiMgr.getConnectionInfo().getSSID();
                mLogger.info("Current Wi-Fi SSID: " + ssId);
                if (WiFiUtil.checkSSID(ssId)) {
                    mServiceIdView.setText(getThetaName());
                } else {
                    connectTheta();
                }
            }
        });

        return rootView;
    }

    private WifiManager getWifiManager() {
        return (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
    public void onPause() {
        super.onPause();
        Activity activity = getActivity();
        if (activity != null) {
            ThetaDeviceApplication app = (ThetaDeviceApplication) activity.getApplication();
            ThetaDeviceManager deviceManager = app.getDeviceManager();
            deviceManager.unregisterDeviceEventListener(this);

            activity.unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity != null) {
            ThetaDeviceApplication app = (ThetaDeviceApplication) activity.getApplication();
            ThetaDeviceManager deviceManager = app.getDeviceManager();
            deviceManager.registerDeviceEventListener(this);

            activity.registerReceiver(mReceiver,
                new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
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
     * Connection to the Theta device.
     */
    private void connectTheta() {
        if (!mWifiMgr.isWifiEnabled()) {
            ThetaDialogFragment.showConfirmAlert(getActivity(),
                getString(R.string.theta_confirm_wifi),
                getString(R.string.theta_confirm_wifi_enable),
                getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        turnOnWifi();
                    }
                });
        } else {
            WifiInfo wifiInfo = mWifiMgr.getConnectionInfo();
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mServiceIdView.setText(R.string.theta_connecting);
                    }
                });
            }
            if (WiFiUtil.checkSSID(wifiInfo.getSSID())) {
                searchTheta();
            } else {
                searchThetaWifi();
            }
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

    /** Get Theta AP List. */
    private void getThetaAPList() {
        final List<ScanResult> scanList = new ArrayList<ScanResult>();
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
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int whichButton) {
                                    pos[0] = whichButton;
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int whichButton) {
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
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int whichButton) {
                                    mServiceIdView.setText(R.string.theta_no_device);
                                }
                            });
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
        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (unregistered) {
                    if (!unregistered.get()) {
                        getContext().unregisterReceiver(receiver);
                        unregistered.set(true);
                    }
                }
            }
        }, 30, TimeUnit.SECONDS);

        getContext().registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }


    /**
     * Dialog to enter a password.
     *
     * @param password password
     * @param listener listener
     */
    private void showPasswordDialog(final String password, final PasswordListener listener) {
        final EditText editView = new EditText(getActivity());
        if (password != null) {
            String ps = password.replace("\"", "");
            editView.setText(ps);
        }

        ThetaDialogFragment.showPasswordDialog(getActivity(), editView,
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        String password = "\"" + editView.getText() + "\"";
                        if (listener != null) {
                            listener.onInputPassword(password);
                        }
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        if (listener != null) {
                            listener.onCancel();
                        }
                    }
                }
        );
    }

    /**
     * Connect to WiFi the specified SSID.
     *
     * @param result Destination SSID
     */
    private void connectWifi(final ScanResult result) {
        final WifiConfiguration wc = new WifiConfiguration();
        String capabilities = result.capabilities;
        String ssid = "\"" + result.SSID + "\"";

        if (capabilities.contains("WPA")) {
            String password = mSettings.getSSIDPassword(ssid);
            showPasswordDialog(password, new PasswordListener() {
                @Override
                public void onInputPassword(final String password) {
                    wc.SSID = "\"" + result.SSID + "\"";
                    wc.preSharedKey = password;
                    wc.hiddenSSID = true;
                    wc.status = WifiConfiguration.Status.ENABLED;
                    wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                    testConnectWifi(wc, password);
                }

                @Override
                public void onCancel() {
                    mServiceIdView.setText(R.string.camera_search_message_not_found);
                }
            });
        } else if (capabilities.contains("WEP")) {
            String password = mSettings.getSSIDPassword(ssid);
            showPasswordDialog(password, new PasswordListener() {
                @Override
                public void onInputPassword(final String password) {
                    wc.SSID = "\"" + result.SSID + "\"";
                    wc.wepKeys[0] = password;
                    wc.wepTxKeyIndex = 0;
                    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    testConnectWifi(wc, password);
                }

                @Override
                public void onCancel() {
                    mServiceIdView.setText(R.string.camera_search_message_not_found);
                }
            });
        } else {
            wc.SSID = "\"" + result.SSID + "\"";
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            testConnectWifi(wc, null);
        }
    }

    /**
     * Check whether the specified network are reflected, to make a connection.
     *
     * @param networkId Network ID
     * @param targetSSID Connect SSID
     * @return True if the connection is successful, otherwise false
     */
    private boolean connectWifi(final int networkId, final String targetSSID) {
        String ssid = targetSSID.replace("\"", "");
        mWifiMgr.startScan();
        for (ScanResult result : mWifiMgr.getScanResults()) {
            if (result.SSID.replace("\"", "").equals(ssid)) {
                WifiInfo info = mWifiMgr.getConnectionInfo();
                if (info != null) {
                    mWifiMgr.disableNetwork(info.getNetworkId());
                }
                return mWifiMgr.enableNetwork(networkId, true);
            }
        }
        return false;
    }

    /**
     * Connection confirmation to the specified Wifi.
     *
     * @param wifiConfig wifi settings
     * @param password pasword
     */
    private void testConnectWifi(final WifiConfiguration wifiConfig, final String password) {
        showConnectionProgress();

        final int networkId = mWifiMgr.addNetwork(wifiConfig);
        mLogger.info("addNetwork: networkId = " + networkId);
        if (networkId != -1) {
            mWifiMgr.saveConfiguration();
            mWifiMgr.updateNetwork(wifiConfig);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Since the addNetwork and does not wait a little from the set is not reflected, to wait a little
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        return;
                    }
                    if (!connectWifi(networkId, wifiConfig.SSID)) {
                        connectTheta();
                    } else {
                        if (password != null) {
                            mSettings.setSSIDPassword(wifiConfig.SSID, password);
                        }
                    }
                }
            }).start();
        } else {
            ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_confirm_wifi),
                    getString(R.string.camera_search_message_not_found), null);
            mServiceIdView.setText(R.string.camera_search_message_not_found);

        }
    }

    private void showConnectionProgress() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mDialog == null) {
                        mDialog = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(R.string.connecting));
                        mDialog.show(getActivity().getFragmentManager(),
                            "fragment_dialog");
                    }
                    new Handler().postDelayed(new Runnable() {  //Timeout Handler
                        @Override
                        public void run() {
                            if (mDialog != null) {
                                mDialog.dismiss();
                                mDialog = null;
                                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_confirm_wifi),
                                    getString(R.string.theta_error_wrong_password), null);
                                mServiceIdView.setText(R.string.camera_search_message_not_found);

                            }
                        }
                    }, 30000);
                }
            });
        }
    }

    /**
     * Enable the Wifi function.
     */
    private void turnOnWifi() {
        mIsWaitingWifiEnabled = true;
        mWifiMgr.setWifiEnabled(true);
    }

    @Override
    public void onConnected(final ThetaDevice device) {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mServiceIdView.setText(getThetaName());
                }
            });
        }

    }

    @Override
    public void onDisconnected(final ThetaDevice device) {
        this.onConnected(device);
    }

    /**
     * Enter Password completion listener.
     */
    private interface PasswordListener {
        /**
         * Notify the entered password.
         *
         * @param password Entered password
         */
        void onInputPassword(final String password);

        /**
         * Notify the input has been canceled.
         */
        void onCancel();
    }
}
