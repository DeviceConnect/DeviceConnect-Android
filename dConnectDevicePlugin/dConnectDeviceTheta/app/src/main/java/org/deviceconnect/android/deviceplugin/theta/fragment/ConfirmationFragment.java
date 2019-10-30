/*
 ConfirmationFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.EditText;

import org.deviceconnect.android.deviceplugin.theta.R;

/**
 * The page for confirmation of the connection between THETA and Android device.
 *
 * @author NTT DOCOMO, INC.
 */
public class ConfirmationFragment extends BaseConfirmationFragment {

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
                if (state == WifiManager.WIFI_STATE_ENABLED) {
                    mIsWaitingWifiEnabled = false;
                    connectTheta();
                }
            }
        }
    };

    protected boolean mIsWaitingWifiEnabled;

    @Override
    public void onPause() {
        super.onPause();
        Activity activity = getActivity();
        if (activity != null) {
            activity.unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity != null) {
            activity.registerReceiver(mReceiver,
                    new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        }
    }

    @Override
    protected void onWifiDisabled() {
        ThetaDialogFragment.showConfirmAlert(getActivity(),
                getString(R.string.theta_confirm_wifi),
                getString(R.string.theta_confirm_wifi_enable),
                getString(R.string.ok),
                (dialog, which) -> turnOnWifi());
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
                (dialog, which) -> {
                    if (listener != null) {
                        listener.onInputPassword("\"" + editView.getText() + "\"");
                    }
                },
                (dialog, which) -> {
                    if (listener != null) {
                        listener.onCancel();
                    }
                }
        );
    }

    /**
     * Connect to WiFi the specified SSID.
     *
     * @param result Destination SSID
     */
    @Override
    protected void connectWifi(final ScanResult result) {
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

            new Thread(() -> {
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
        mIsWaitingWifiEnabled = true;
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
        void onInputPassword(final String password);

        /**
         * Notify the input has been canceled.
         */
        void onCancel();
    }
}
