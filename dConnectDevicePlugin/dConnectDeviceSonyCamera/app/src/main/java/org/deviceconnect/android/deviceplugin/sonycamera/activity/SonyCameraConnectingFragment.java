/*
SonyCameraSettingFragment
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sonycamera.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.deviceconnect.android.activity.IntentHandlerActivity;
import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.sonycamera.R;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.DConnectMessageHandler;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.DConnectUtil;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.UserSettings;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
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
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Sony Camera 接続処理用フラグメント.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraConnectingFragment extends SonyCameraBaseFragment {

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("sonycamera.dplugin");
    /** インターバル. */
    private static final int INTERVAL = 1000;

    /** サービスIDを表示するためのView. */
    private TextView mServiceIdView;

    /** Wifi管理クラス. */
    private WifiManager mWifiMgr;
    /** 設定を保持するクラス. */
    private UserSettings mSettings;

    /** Wifiの状態通知を受け取るReceiver. */
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
                        if (DConnectUtil.checkSSID(wifiInfo.getSSID())) {
                            mServiceIdView.setText(R.string.sonycamera_connect);
                        }
                    }
                }
            }
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, 
            final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connecting_camera, container, false);

        mSettings = new UserSettings(getActivity());
        mWifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        mServiceIdView = (TextView) view.findViewById(R.id.camera_id);

        final Button searchBtn = (Button) view.findViewById(R.id.search_and_connect_button);
        searchBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (DConnectUtil.checkSSID(mWifiMgr.getConnectionInfo().getSSID())) {
                    mServiceIdView.setText(R.string.sonycamera_already_connect);
                } else {
                    connectSonyCamera();
                }
            }
        });

        return view;
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
     * 周りに存在するWiFiを探索する.
     * <p>
     * 探索結果は、WiFiDeviceListFragmentに表示する。
     * </p>
     */
    private void searchSonyCamera() {
        mLogger.entering(this.getClass().getName(), "searchSonyCamera");

        if (isShowProgressDialog()) {
            mLogger.exiting(this.getClass().getName(), "searchSonyCamera", null);
            return;
        }

        showProgressDialog();

        DConnectUtil.asyncSearchDevice(new DConnectMessageHandler() {
            @Override
            public void handleMessage(final DConnectMessage message) {

                if (message == null) {
                    return;
                }

                int result = message.getInt(DConnectMessage.EXTRA_RESULT);
                if (result == DConnectMessage.RESULT_OK) {
                    List<Object> services = message.getList(ServiceDiscoveryProfile.PARAM_SERVICES);
                    if (services.size() == 0) {
                        searchSonyCamera();
                    } else {
                        dismissProgressDialog();
                        for (int i = 0; i < services.size(); i++) {
                            HashMap<?, ?> service = (HashMap<?, ?>) services.get(i);
                            String name = (String) service.get(ServiceDiscoveryProfile.PARAM_NAME);
                            if (name != null && name.equals("Sony Camera")) {
                                String id = (String) service.get(ServiceDiscoveryProfile.PARAM_ID);
                                if (mServiceIdView != null) {
                                    mServiceIdView.setText(R.string.sonycamera_connect);
                                    setServiceId(id);
                                }
                            }
                        }
                    }
                } else {
                    mLogger.warning("error: result=" + result);
                    mServiceIdView.setText(R.string.sonycamera_not_found);
                }
            }
        });

        mLogger.exiting(this.getClass().getName(), "searchSonyCamera");
    }

    /**
     * SonyCameraデバイスに接続を行います.
     */
    private void connectSonyCamera() {
        if (!mWifiMgr.isWifiEnabled()) {
            confirmConnectWifi();
        } else {
            WifiInfo wifiInfo = mWifiMgr.getConnectionInfo();
            mServiceIdView.setText(R.string.sonycamera_connecting);
            if (DConnectUtil.checkSSID(wifiInfo.getSSID())) {
                searchSonyCamera();
            } else {
                searchSonyCameraWifi();
            }
        }
    }

    /**
     * SonyCameraデバイスのWifiを探索してに接続を行います.
     */
    private void searchSonyCameraWifi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getSonyCameraAPList();
        } else {
            checkLocationServiceEnabled();
        }
    }
    
    private void checkLocationServiceEnabled() {
        // WiFi scan in SDK 23 requires location service to be enabled.
        final LocationManager manager = getContext().getSystemService(LocationManager.class);
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            IntentHandlerActivity.startActivityForResult(getContext(),
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    new ResultReceiver(new Handler(Looper.getMainLooper())) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);

                            if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
                                permissionCheck();
                            } else {
                                Toast.makeText(getContext(), "WiFi scan aborted.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            Toast.makeText(getContext(), "WiFi scan requires Location Service.", Toast.LENGTH_LONG).show();
        } else {
            permissionCheck();
        }
    }

    private void permissionCheck() {
        // WiFi scan requires location permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && getContext().checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getSonyCameraAPList();
            } else {
                PermissionUtility.requestPermissions(getContext(), new Handler(Looper.getMainLooper()),
                        new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION },
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                getSonyCameraAPList();
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

    private void getSonyCameraAPList() {
        final List<ScanResult> scanList = new ArrayList<ScanResult>();
        mWifiMgr.startScan();

        final AtomicBoolean unregistered = new AtomicBoolean(false);
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> results = mWifiMgr.getScanResults();
                for (ScanResult result : results) {
                    if (DConnectUtil.checkSSID(result.SSID)) {
                        scanList.add(result);
                        mLogger.fine("Found SonyCamera Wifi. SSID=" + result.SSID);
                    }
                }

                if (scanList.size() > 0) {
                    confirmConnectSonyCameraWifi(scanList);
                } else {
                    showErrorDialog(getString(R.string.sonycamera_not_found_wifi));
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
     * SonyCameraデバイスのWifiへの接続確認を行う.
     * 
     * @param configs Wifiの設定一覧
     */
    private void confirmConnectSonyCameraWifi(final List<ScanResult> configs) {
        String[] wifiList = new String[configs.size()];
        for (int i = 0; i < configs.size(); i++) {
            wifiList[i] = configs.get(i).SSID;
            wifiList[i] = wifiList[i].replace("\"", "");
        }

        final int[] pos = new int[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.sonycamera_confirm_wifi);
        builder.setSingleChoiceItems(wifiList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int whichButton) {
                pos[0] = whichButton;
            }
        });
        builder.setPositiveButton(R.string.sonycamera_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int whichButton) {
                connectWifi(configs.get(pos[0]));
            }
        });
        builder.setNegativeButton(R.string.sonycamera_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int whichButton) {
                mServiceIdView.setText(R.string.sonycamera_no_device);
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    /**
     * Wifi機能を入れる確認を行う.
     */
    private void confirmConnectWifi() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.sonycamera_confirm_wifi);
        builder.setMessage(R.string.sonycamera_confirm_wifi_enable);
        builder.setPositiveButton(R.string.sonycamera_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int whichButton) {
                turnOnWifi();
            }
        });
        builder.setNegativeButton(R.string.sonycamera_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int whichButton) {
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    /**
     * エラーダイアログを表示する.
     * 
     * @param message エラーメッセージ
     */
    private void showErrorDialog(final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.sonycamera_confirm_wifi);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.sonycamera_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int whichButton) {
            }
        });
        builder.setCancelable(true);
        builder.show();
        mServiceIdView.setText(R.string.sonycamera_no_device);
    }

    /**
     * パスワードを入力するダイアログを表示する.
     * 
     * @param password パスワード
     * @param listener リスナー
     */
    private void showPasswordDialog(final String password, final PasswordListener listener) {
        final EditText editView = new EditText(getActivity());
        if (password != null) {
            String ps = password.replace("\"", "");
            editView.setText(ps);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.sonycamera_password_dialog);
        builder.setView(editView).setPositiveButton(R.string.sonycamera_ok, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int whichButton) {
                String password = "\"" + editView.getText() + "\"";
                if (listener != null) {
                    listener.onIntputPassword(password);
                }
            }
        });
        builder.setNegativeButton(R.string.sonycamera_cancel, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int whichButton) {
                if (listener != null) {
                    listener.onCancel();
                }
            }
        });
        builder.show();
    }

    /**
     * 指定されたSSIDのWiFiに接続を行う.
     * 
     * @param result 接続先のSSID
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
                    mServiceIdView.setText(R.string.sonycamera_no_device);
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
                    mServiceIdView.setText(R.string.sonycamera_no_device);
                }
            });
        } else {
            wc.SSID = "\"" + result.SSID + "\"";
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            testConnectWifi(wc, null);
        }
    }

    /**
     * 指定されたネットワークが反映されているかをチェックして、接続を行う.
     * 
     * @param networkId ネットワークID
     * @param targetSSID 接続するSSID
     * @return 接続に成功した場合はtrue、それ以外はfalse
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
     * 指定されたWifiに接続確認を行う.
     * 
     * @param wifiConfig wifi設定
     * @param password パスワード
     */
    private void testConnectWifi(final WifiConfiguration wifiConfig, final String password) {
        final int networkId = mWifiMgr.addNetwork(wifiConfig);
        if (networkId != -1) {
            mWifiMgr.saveConfiguration();
            mWifiMgr.updateNetwork(wifiConfig);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // addNetworkしてから少し待たないと設定が反映されないので、少し待つ
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        return;
                    }
                    if (!connectWifi(networkId, wifiConfig.SSID)) {
                        connectSonyCamera();
                    } else {
                        if (password != null) {
                            mSettings.setSSIDPassword(wifiConfig.SSID, password);
                        }
                    }
                }
            }).start();
        } else {
            showErrorDialog(getString(R.string.sonycamera_not_connected));
        }
    }

    /**
     * Wifiの機能を有効にする.
     */
    private void turnOnWifi() {
        mWifiMgr.setWifiEnabled(true);
    }

    /**
     * パスワードの入力完了リスナー.
     */
    private interface PasswordListener {
        /**
         * 入力されたパスワードを通知する.
         * 
         * @param password 入力されたパスワード
         */
        void onIntputPassword(String password);

        /**
         * 入力がキャンセルされたことを通知する.
         */
        void onCancel();
    }
}
