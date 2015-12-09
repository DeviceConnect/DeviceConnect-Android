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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.utils.DConnectMessageHandler;
import org.deviceconnect.android.deviceplugin.theta.utils.UserSettings;
import org.deviceconnect.android.deviceplugin.theta.utils.WiFiUtil;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The page for confirmation of the connection between THETA and Android device.
 *
 * @author NTT DOCOMO, INC.
 */
public class ConfirmationFragment extends SettingsFragment {
    /** Interval. */
    private static final int INTERVAL = 1000;

    /** View to display the service ID. */
    private TextView mServiceIdView;
    /** Theta's service Id. */
    private String mServiceId;
    /** Wifi management class. */
    private WifiManager mWifiMgr;
    /** Class that holds the configuration. */
    private UserSettings mSettings;
    /** Search in dialog. */
    private ThetaDialogFragment mDialog;

    /** Receiver to receive the state notification of Wifi. */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null) {
                    NetworkInfo.State state = ni.getState();
                    int type = ni.getType();
                    if (state == NetworkInfo.State.CONNECTED
                            && type == ConnectivityManager.TYPE_WIFI) {
                        WifiInfo wifiInfo = mWifiMgr.getConnectionInfo();
                        if (WiFiUtil.checkSSID(wifiInfo.getSSID())) {
                            mServiceIdView.setText(getThetaName());
                        }
                    }
                }
            }
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_confirmation, null);
        mServiceIdView = (TextView) rootView.findViewById(R.id.camera_search_message);
        mSettings = new UserSettings(getActivity());
        mWifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        Button btnCameraSearch = (Button) rootView.findViewById(R.id.btn_camera_search);
        btnCameraSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (WiFiUtil.checkSSID(mWifiMgr.getConnectionInfo().getSSID())) {
                    mServiceIdView.setText(getThetaName());
                } else {
                    connectTheta();
                }
            }
        });

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
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(mReceiver, filter);
    }


    /**
     * Explore the WiFi that exist around.
     * <p>
     * Search results, we want to display to WiFiDeviceListFragment.
     * </p>
     */
    private void searchTheta() {

        if (mDialog != null) {
            return;
        }

        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDialog = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(R.string.loading));
                    mDialog.show(getActivity().getFragmentManager(),
                            "fragment_dialog");

                }
            });
        }

        WiFiUtil.asyncSearchDevice(new DConnectMessageHandler() {
            @Override
            public void handleMessage(final DConnectMessage message) {

                if (message == null) {
                    return;
                }

                int result = message.getInt(DConnectMessage.EXTRA_RESULT);
                if (result == DConnectMessage.RESULT_OK) {
                    List<Object> services = message.getList(ServiceDiscoveryProfile.PARAM_SERVICES);
                    if (services.size() == 0) {
                        searchTheta();
                    } else {
                        if (mDialog != null) {
                            mDialog.dismiss();
                            mDialog = null;
                        }
                        for (int i = 0; i < services.size(); i++) {
                            HashMap<?, ?> service = (HashMap<?, ?>) services.get(i);
                            String name = (String) service.get(ServiceDiscoveryProfile.PARAM_NAME);
                            if (name != null && name.indexOf("THETA") > 0) {
                                String id = (String) service.get(ServiceDiscoveryProfile.PARAM_ID);
                                if (mServiceIdView != null) {
                                    mServiceIdView.setText(getThetaName());
                                    mServiceId = id;
                                }
                            }
                        }
                    }
                } else {
                    mServiceIdView.setText(R.string.camera_search_message_not_found);
                }
            }
        });
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
                                Toast.makeText(getContext(), "WiFi scan aborted.", Toast.LENGTH_LONG).show();
                            }
                        });
                Toast.makeText(getContext(), "WiFi scan requires Location permission.", Toast.LENGTH_LONG).show();
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
                                    connectWifi(scanList.get(pos[0]));
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
                            listener.onIntputPassword(password);
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
                public void onIntputPassword(final String password) {
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
                public void onIntputPassword(final String password) {
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
        List<WifiConfiguration> list = mWifiMgr.getConfiguredNetworks();
        int connectedNetworkId = -1;
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals(wifiConfig.SSID)) {
                mWifiMgr.disconnect();
                mWifiMgr.enableNetwork(i.networkId, true);
                connectedNetworkId = i.networkId;
                mWifiMgr.reconnect();

                break;
            }
        }
        if (connectedNetworkId == -1) {
            connectedNetworkId = mWifiMgr.addNetwork(wifiConfig);
        }
        final int networkId = connectedNetworkId;
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
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String message = getString(R.string.camera_search_message_found);
                                    message = message.replace("$NAME$", wifiConfig.SSID);
                                    message = message.replaceAll("\"", "");

                                    mServiceIdView.setText(message);
                                }
                            });
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

    /**
     * Enable the Wifi function.
     */
    private void turnOnWifi() {
        mWifiMgr.setWifiEnabled(true);
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
        void onIntputPassword(final String password);

        /**
         * Notify the input has been canceled.
         */
        void onCancel();
    }
}
