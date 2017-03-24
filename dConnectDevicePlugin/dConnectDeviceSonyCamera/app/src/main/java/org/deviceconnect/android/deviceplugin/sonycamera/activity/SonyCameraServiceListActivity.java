/*
SonyCameraServiceListActivity
Copyright (c) 2016 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sonycamera.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.sonycamera.R;
import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraDeviceService;
import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraManager;
import org.deviceconnect.android.deviceplugin.sonycamera.service.SonyCameraService;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.SonyCameraUtil;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.UserSettings;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

import java.util.List;


/**
 * SonyCameraサービス一覧画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraServiceListActivity extends DConnectServiceListActivity {
    private WifiManager mWifiMgr;
    private UserSettings mSettings;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mSettings = new UserSettings(this);
        saveWiFiSSID();
    }

    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return SonyCameraDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return SonyCameraSettingActivity.class;
    }

    @Override
    public void onServiceRemoved(final DConnectService service) {
        super.onServiceRemoved(service);

        SonyCameraDeviceService s = (SonyCameraDeviceService) getMessageService();
        if (s != null) {
            SonyCameraManager manager = s.getSonyCameraManager();
            manager.removeSonyCameraService((SonyCameraService) service);
        }
    }

    @Override
    protected boolean enablesItemClick() {
        return true;
    }

    @Override
    protected void onItemClick(final DConnectService service) {
        if (!mWifiMgr.isWifiEnabled()) {
            confirmEnableWifi();
        } else {
            if (!service.isOnline()) {
                confirmConnectSonyCamera(service);
            } else {
                String ssid = mSettings.getSSID();
                if (ssid != null) {
                    confirmConnectWiFi(ssid);
                }
            }
        }
    }

    /**
     * 元々接続してあるWiFiのSSIDを保存します.
     */
    private void saveWiFiSSID() {
        String ssid = SonyCameraUtil.getSSID(this);
        if (ssid != null && !SonyCameraUtil.checkSSID(ssid)) {
            mSettings.setSSID(ssid);
        }
    }

    /**
     * Wifiの機能を有効にする.
     */
    private void turnOnWifi() {
        mWifiMgr.setWifiEnabled(true);
    }

    /**
     * Wifi機能を入れる確認を行う.
     */
    private void confirmEnableWifi() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.sonycamera_confirm_wifi);
        builder.setMessage(R.string.sonycamera_confirm_wifi_enable);
        builder.setPositiveButton(R.string.sonycamera_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int whichButton) {
                turnOnWifi();
            }
        });
        builder.setNegativeButton(R.string.sonycamera_cancel, null);
        builder.setCancelable(true);
        builder.show();
    }

    /**
     * SonyCamera接続確認.
     */
    private void confirmConnectSonyCamera(final DConnectService service) {
        saveWiFiSSID();
        confirmConnectWiFi(service.getId());
    }

    /**
     * 元のWiFiのアクセスポイントに接続確認.
     */
    private void confirmConnectWiFi(final String ssid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.sonycamera_confirm_wifi);
        builder.setMessage(getString(R.string.sonycamera_confirm_connect_wifi, ssid));
        builder.setPositiveButton(R.string.sonycamera_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int whichButton) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectWifi2(ssid);
                    }
                }).start();
            }
        });
        builder.setNegativeButton(R.string.sonycamera_cancel, null);
        builder.setCancelable(true);
        builder.show();
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
     * 既にWifiConfigurationにネットワーク情報が存在する場合には、既存の情報で接続する.
     * @param id サービスのSSID
     * @return 接続処理が行われた場合にはtrue、それ以外の場合にはfalse
     */
    private boolean connectWifi2(final String id) {
        String ssid = '"' + id + '"';
        List<WifiConfiguration> wifiConfigurations = mWifiMgr.getConfiguredNetworks();
        for (WifiConfiguration configuration : wifiConfigurations) {
            if (configuration.SSID.contains(ssid)) {
                return connectWifi(configuration.networkId, configuration.SSID);
            }
        }
        return false;
    }
}
