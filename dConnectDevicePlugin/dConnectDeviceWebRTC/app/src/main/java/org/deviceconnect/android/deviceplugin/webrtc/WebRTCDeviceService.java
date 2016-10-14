/*
 WebRTCDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc;

import org.deviceconnect.android.deviceplugin.webrtc.profile.WebRTCSystemProfile;
import org.deviceconnect.android.deviceplugin.webrtc.service.WebRTCService;
import org.deviceconnect.android.deviceplugin.webrtc.util.WebRTCManager;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;

import java.util.logging.Logger;

/**
 * WebRTC device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebRTCDeviceService extends DConnectMessageService {

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("webrtc.dplugin");

    private WebRTCManager mWebRTCManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mWebRTCManager = new WebRTCManager((WebRTCApplication) getApplication());
        getServiceProvider().addService(new WebRTCService());
    }

    @Override
    public void onDestroy() {
        if (mWebRTCManager != null) {
            mWebRTCManager.destroy();
            mWebRTCManager = null;
        }
        super.onDestroy();
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
        /** 全イベント削除. */
        EventManager.INSTANCE.removeAll();

        /** Peer切断処理. */
        WebRTCApplication app = (WebRTCApplication) ((DConnectMessageService) getContext()).getApplication();
        if (app != null) {
            app.destroyPeer();
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new WebRTCSystemProfile();
    }

    public WebRTCManager getWebRTCManager() {
        return mWebRTCManager;
    }
}
