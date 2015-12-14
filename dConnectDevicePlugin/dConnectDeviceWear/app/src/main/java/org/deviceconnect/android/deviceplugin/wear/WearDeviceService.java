/*
 WearDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.wear.profile.WearCanvasProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearConst;
import org.deviceconnect.android.deviceplugin.wear.profile.WearDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearKeyEventProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearNotificationProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearSystemProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearTouchProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearVibrationProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

import java.util.List;

/**
 * WearService.
 *
 * @author NTT DOCOMO, INC.
 */
public class WearDeviceService extends DConnectMessageService {

    /**
     * Android Wearとの通信を管理するクラス.
     */
    private WearManager mWearManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mWearManager = new WearManager(this);
        mWearManager.init();

        // initialize of the EventManager
        EventManager.INSTANCE.setController(new MemoryCacheController());

        // add supported profiles
        addProfile(new WearNotificationProfile());
        addProfile(new WearVibrationProfile());
        addProfile(new WearDeviceOrientationProfile(mWearManager));
        addProfile(new WearCanvasProfile());
        addProfile(new WearTouchProfile(mWearManager));
        addProfile(new WearKeyEventProfile(mWearManager));

    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (WearConst.DEVICE_TO_WEAR_NOTIFICATION_OPEN.equals(action)) {
                String serviceId = intent.getStringExtra(WearConst.PARAM_DEVICEID);
                int notificationId = intent.getIntExtra(WearConst.PARAM_NOTIFICATIONID, -1);
                List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                        WearNotificationProfile.PROFILE_NAME, null, WearNotificationProfile.ATTRIBUTE_ON_CLICK);
                synchronized (events) {
                    for (Event event : events) {
                        Intent msg = EventManager.createEventMessage(event);
                        msg.putExtra(WearNotificationProfile.PARAM_NOTIFICATION_ID, notificationId);
                        sendEvent(msg, event.getAccessToken());
                    }
                }
            } else if (WearConst.DEVICE_TO_WEAR_NOTIFICATION_CLOSED.equals(action)) {
                String serviceId = intent.getStringExtra(WearConst.PARAM_DEVICEID);
                int notificationId = intent.getIntExtra(WearConst.PARAM_NOTIFICATIONID, -1);
                List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                        WearNotificationProfile.PROFILE_NAME, null, WearNotificationProfile.ATTRIBUTE_ON_CLOSE);
                synchronized (events) {
                    for (Event event : events) {
                        Intent msg = EventManager.createEventMessage(event);
                        msg.putExtra(WearNotificationProfile.PARAM_NOTIFICATION_ID, notificationId);
                        sendEvent(msg, event.getAccessToken());
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWearManager != null) {
            mWearManager.destory();
            mWearManager = null;
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new WearSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this) { };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new WearServiceDiscoveryProfile(this);
    }

    /**
     * Android Wear管理クラスを取得する.
     * @return WearManagerのインスタンス
     */
    public WearManager getManager() {
        return mWearManager;
    }
}
