/*
 ThetaDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceEventListener;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.core.wifi.WifiStateEventListener;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaOmnidirectionalImageProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaSystemProfile;
import org.deviceconnect.android.deviceplugin.theta.service.ThetaImageService;
import org.deviceconnect.android.deviceplugin.theta.service.ThetaService;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.OmnidirectionalImageProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.android.service.DConnectService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Theta Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceService extends DConnectMessageService
    implements ThetaDeviceEventListener {

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("theta.dplugin");
    private static final String TYPE_NONE = "none";
    private ThetaDeviceManager mDeviceMgr;
    private ThetaDeviceClient mClient;
    private FileManager mFileMgr;
    private ThetaMediaStreamRecordingProfile mThetaMediaStreamRecording;
    private WifiStateEventListener mListener;
    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                mListener.onNetworkChanged(wifiInfo);
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                switch (state) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        mListener.onWiFiDisabled();
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        mListener.onWiFiEnabled();
                        break;
                    default:
                        break;
                }
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        mListener = ((ThetaDeviceApplication) getApplication()).getDeviceManager();
        ThetaDeviceApplication app = (ThetaDeviceApplication) getApplication();
        mDeviceMgr = app.getDeviceManager();
        mDeviceMgr.registerDeviceEventListener(this);
        mDeviceMgr.checkConnectedDevice();
        mClient = new ThetaDeviceClient(mDeviceMgr);
        mFileMgr = new FileManager(this);

        EventManager.INSTANCE.setController(new MemoryCacheController());

        getServiceProvider().addService(new ThetaImageService(app.getHeadTracker()));

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mWifiReceiver);
        mDeviceMgr.unregisterDeviceEventListener(this);
        try {
            PtpipInitiator.close();
        } catch (ThetaException e) {
            // Nothing to do.
        }
        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new ThetaSystemProfile();
    }

    @Override
    public void onConnected(final ThetaDevice device) {
        DConnectService service = getServiceProvider().getService(device.getId());
        if (service == null) {
            service = new ThetaService(device, mClient, mFileMgr);
            getServiceProvider().addService(service);
            mThetaMediaStreamRecording = (ThetaMediaStreamRecordingProfile)service.getProfile(ThetaMediaStreamRecordingProfile.PROFILE_NAME);
        }
        service.setOnline(true);
    }

    @Override
    public void onDisconnected(final ThetaDevice device) {
        if (getServiceProvider().hasService(device.getId())) {
            DConnectService service = getServiceProvider().getService(device.getId());
            service.setOnline(false);
        }
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
        resetPluginResource();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        /* 全イベント削除. */
        EventManager.INSTANCE.removeAll();

        /* 記録処理・プレビュー停止 */

        if (mThetaMediaStreamRecording != null) {
            mThetaMediaStreamRecording.forcedStopRecording();
        }

        List<ThetaOmnidirectionalImageProfile> omnidirectionalImageProfiles = new ArrayList<>();
        for (DConnectService service : getServiceProvider().getServiceList()) {
            ThetaOmnidirectionalImageProfile profile = (ThetaOmnidirectionalImageProfile) service.getProfile(OmnidirectionalImageProfile.PROFILE_NAME);
            if (profile != null && !omnidirectionalImageProfiles.contains(profile)) {
                omnidirectionalImageProfiles.add(profile);
            }
        }
        for (ThetaOmnidirectionalImageProfile profile : omnidirectionalImageProfiles) {
            profile.forceStopPreview();
        }
    }

}
