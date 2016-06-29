/*
 LinkingDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.service.LinkingBeaconService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.service.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingSystemProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;

/**
 * Linking device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingDevicePluginService extends DConnectMessageService {

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());

        addLinkingDevice();
        addLinkingBeacon();

        addProfile(new LinkingServiceDiscoveryProfile(getServiceProvider()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT.equals(action) ||
                    LinkingBeaconUtil.ACTION_BEACON_SCAN_STATE.equals(action)) {
                LinkingApplication app = (LinkingApplication)  getApplication();
                LinkingBeaconManager mgr = app.getLinkingBeaconManager();
                mgr.onReceivedBeacon(intent);
                return START_STICKY;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new LinkingSystemProfile();
    }

    private void addLinkingDevice() {
        LinkingDeviceManager mgr = ((LinkingApplication) getApplication()).getLinkingDeviceManager();
        for (LinkingDevice device : mgr.getDevices()) {
            getServiceProvider().addService(new LinkingDeviceService(this, device));
        }
    }

    private void addLinkingBeacon() {
        LinkingBeaconManager mgr = ((LinkingApplication) getApplication()).getLinkingBeaconManager();
        for (LinkingBeacon beacon : mgr.getLinkingBeacons()) {
            getServiceProvider().addService(new LinkingBeaconService(this, beacon));
        }
    }
}
