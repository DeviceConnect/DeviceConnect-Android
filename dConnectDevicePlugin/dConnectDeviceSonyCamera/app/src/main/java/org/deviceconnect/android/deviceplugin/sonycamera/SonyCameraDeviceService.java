/*
SonyCameraDeviceService
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sonycamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.sonycamera.profile.SonyCameraSystemProfile;
import org.deviceconnect.android.deviceplugin.sonycamera.receiver.WiFiStateReceiver;
import org.deviceconnect.android.deviceplugin.sonycamera.service.SonyCameraService;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.SonyCameraUtil;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;

import java.util.List;
import java.util.logging.Logger;

/**
 * SonyCameraデバイスプラグイン用サービス.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraDeviceService extends DConnectMessageService {

    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("sonycamera.dplugin");

    /**
     * SonyCamera管理クラス.
     */
    private SonyCameraManager mSonyCameraManager;
    private BroadcastReceiver mWiFiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                mLogger.info("Received: WIFI_STATE_CHANGED_ACTION");
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if (state == WifiManager.WIFI_STATE_ENABLED) {
                    WifiManager wifiMgr = getWifiManager();
                    WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                    if (SonyCameraUtil.checkSSID(wifiInfo.getSSID())) {
                        mSonyCameraManager.connectSonyCamera();
                    } else {
                        mSonyCameraManager.disconnectSonyCamera();
                    }
                } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                    mSonyCameraManager.disconnectSonyCamera();
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                mLogger.info("Received: NETWORK_STATE_CHANGED_ACTION");
                NetworkInfo ni = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (ni != null) {
                    NetworkInfo.State state = ni.getState();
                    int type = ni.getType();
                    mLogger.info("Active network: type = " + ni.getTypeName()
                            + ", connected = " + ni.isConnected()
                            + ", available = " + ni.isAvailable()
                            + ", state = " + ni.getDetailedState());
                    if (ni.isConnected() && state == NetworkInfo.State.CONNECTED && type == ConnectivityManager.TYPE_WIFI) {
                        WifiManager wifiMgr = getWifiManager();
                        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                        mLogger.info("Active Wi-Fi: SSID = " + wifiInfo.getSSID()
                                + ", supplicantState = " + wifiInfo.getSupplicantState());
                        if (SonyCameraUtil.checkSSID(wifiInfo.getSSID())) {
                            mSonyCameraManager.connectSonyCamera();
                        } else {
                            mSonyCameraManager.disconnectSonyCamera();
                        }
                    }
                } else {
                    mLogger.info("No active network. ");
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mSonyCameraManager = new SonyCameraManager(this);
        mSonyCameraManager.setOnSonyCameraManagerListener(new SonyCameraManager.OnSonyCameraManagerListener() {
            @Override
            public void onTakePicture(final String postImageUrl) {
                notifyTakePhoto(mSonyCameraManager.getServiceId(), postImageUrl);
            }

            @Override
            public void onAdded(final SonyCameraService service) {
                getServiceProvider().addService(service);
            }

            @Override
            public void onError() {
            }
        });

        for (SonyCameraService service : mSonyCameraManager.getSonyCameraServices()) {
            getServiceProvider().addService(service);
        }

        WifiManager wifiMgr = getWifiManager();
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        if (SonyCameraUtil.checkSSID(wifiInfo.getSSID())) {
            mSonyCameraManager.connectSonyCamera();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWiFiReceiver, filter);
    }

    @Override
    public void onDestroy() {
        mSonyCameraManager.disconnectSonyCamera();
        unregisterReceiver(mWiFiReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null) {
            return START_STICKY;
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerUninstalled");
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onManagerEventTransmitDisconnected(String sessionKey) {
        // ManagerのEvent送信経路切断通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerEventTransmitDisconnected");
        }
        if (sessionKey != null) {
            EventManager.INSTANCE.removeEvents(sessionKey);
        } else {
            EventManager.INSTANCE.removeAll();
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onDevicePluginReset");
        }

        EventManager.INSTANCE.removeAll();
        mSonyCameraManager.resetSonyCamera();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SonyCameraSystemProfile();
    }

    /**
     * SonyCameraManagerのインスタンスを取得します.
     * @return SonyCameraManagerのインスタンス
     */
    public SonyCameraManager getSonyCameraManager() {
        return mSonyCameraManager;
    }

    /**
     * WifiManagerを取得する.
     * @return WifiManagerのインスタンス
     */
    private WifiManager getWifiManager() {
        return (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 写真撮影を通知する.
     *
     * @param serviceId サービスID
     * @param uri 写真へのURI
     */
    private void notifyTakePhoto(final String serviceId, final String uri) {
        if (serviceId == null) {
            return;
        }

        List<Event> eventList = EventManager.INSTANCE.getEventList(serviceId,
                "mediaStreamRecording", null, "onPhoto");

        for (Event evt : eventList) {
            Bundle photo = new Bundle();
            photo.putString("uri", uri);
            photo.putString("mimeType", "image/jpeg");

            Intent intent = EventManager.createEventMessage(evt);
            intent.putExtra("photo", photo);

            sendEvent(intent, evt.getAccessToken());
        }
    }
}
