/*
 WearDeviceService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear;

import android.content.Intent;

import com.google.android.gms.wearable.Node;

import org.deviceconnect.android.deviceplugin.wear.profile.WearConst;
import org.deviceconnect.android.deviceplugin.wear.profile.WearNotificationProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearSystemProfile;
import org.deviceconnect.android.deviceplugin.wear.profile.WearUtils;
import org.deviceconnect.android.deviceplugin.wear.service.WearService;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.List;

/**
 * WearService.
 *
 * @author NTT DOCOMO, INC.
 */
public class WearDeviceService extends DConnectMessageService implements WearManager.NodeEventListener {

    /**
     * Android Wearとの通信を管理するクラス.
     */
    private WearManager mWearManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mWearManager = new WearManager(this);
        mWearManager.addNodeListener(this);
        mWearManager.init();

        addProfile(new WearServiceDiscoveryProfile(mWearManager, getServiceProvider()));
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
            mWearManager.destroy();
            mWearManager = null;
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new WearSystemProfile();
    }

    @Override
    public void onNodeConnected(final Node node) {
        DConnectService service = WearService.getInstance(node, mWearManager);
        service.setOnline(node.isNearby());
        getServiceProvider().addService(service);
        getManager().sendWearData();
    }

    @Override
    public void onNodeDisconnected(final Node node) {
        String serviceId = WearUtils.createServiceId(node.getId());
        DConnectService service = getServiceProvider().getService(serviceId);
        if (service != null) {
            service.setOnline(false);
        } else {
            DConnectService addService = WearService.getInstance(node, mWearManager);
            addService.setOnline(node.isNearby());
            getServiceProvider().addService(addService);
        }
    }

    /**
     * Android Wear管理クラスを取得する.
     * @return WearManagerのインスタンス
     */
    public WearManager getManager() {
        return mWearManager;
    }
}
