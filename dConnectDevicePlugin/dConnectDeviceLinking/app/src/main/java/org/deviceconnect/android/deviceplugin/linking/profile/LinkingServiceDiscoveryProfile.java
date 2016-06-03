/*
 LinkingServiceDiscoveryProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManagerFactory;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Discovery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    public LinkingServiceDiscoveryProfile(final DConnectMessageService service) {
        super(service);

        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.addOnBeaconConnectListener(new LinkingBeaconManager.OnBeaconConnectListener() {
            @Override
            public void onConnected(LinkingBeacon beacon) {
                notifyConnectEvent(beacon);
            }
            @Override
            public void onDisconnected(LinkingBeacon beacon) {
                notifyDisconnectEvent(beacon);
            }
        });
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        if (BuildConfig.DEBUG) {
            Log.i("LinkingPlugIn", "ServiceDiscovery:onGetServices");
        }
        List<Bundle> services = new ArrayList<>();
        getServiceFromLinkingDevice(services);
        getServiceFromLinkingBeacon(services);
        getServiceFromLinkingApp(services);
        setServices(response, services);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onPutOnServiceChange(Intent request, Intent response, String serviceId, String sessionKey) {
        return super.onPutOnServiceChange(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onDeleteOnServiceChange(Intent request, Intent response, String serviceId, String sessionKey) {
        return super.onDeleteOnServiceChange(request, response, serviceId, sessionKey);
    }

    private void getServiceFromLinkingDevice(List<Bundle> services) {
        LinkingManager mgr = LinkingManagerFactory.createManager(getContext().getApplicationContext());
        List<LinkingDevice> list = mgr.getDevices();
        for (LinkingDevice device : list) {
            services.add(createServiceFromLinkingDevice(device));
        }
    }

    private Bundle createServiceFromLinkingDevice(LinkingDevice device) {
        Bundle service = new Bundle();
        setId(service, device.getBdAddress());
        setName(service, device.getDisplayName());
        setType(service, NetworkType.BLE);
        setOnline(service, device.isConnected());
        service.putStringArray(PARAM_SCOPES, Util.createLinkingDeviceScopes(device));
        return service;
    }

    private void getServiceFromLinkingBeacon(List<Bundle> services) {
        List<LinkingBeacon> beacons = getLinkingBeaconManager().getLinkingBeacons();
        synchronized (beacons) {
            for (LinkingBeacon beacon : beacons) {
                services.add(createServiceFromLinkingBeacon(beacon));
            }
        }
    }

    private Bundle createServiceFromLinkingBeacon(LinkingBeacon beacon) {
        Bundle service = new Bundle();
        setId(service, LinkingBeaconUtil.createServiceIdFromLinkingBeacon(beacon));
        setName(service, beacon.getDisplayName());
        setType(service, NetworkType.BLE);
        setOnline(service, beacon.isOnline());
        service.putStringArray(PARAM_SCOPES, Util.createLinkingBeaconScopes());
        return service;
    }

    private void getServiceFromLinkingApp(List<Bundle> services) {
        if (LinkingUtil.isApplicationInstalled(getContext())) {
            Bundle service = new Bundle();
            setId(service, Util.LINKING_APP_ID);
            setName(service, "LinkingApp");
            setOnline(service, true);
            service.putStringArray(PARAM_SCOPES, Util.createLinkingAppScopes());
            services.add(service);
        }
    }


    private void notifyConnectEvent(LinkingBeacon beacon) {
    }

    private void notifyDisconnectEvent(LinkingBeacon beacon) {
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDeviceService service = (LinkingDeviceService) getContext();
        return (LinkingApplication) service.getApplication();
    }
}
