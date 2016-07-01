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
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.service.LinkingBeaconService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.service.LinkingDeviceService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

public class LinkingServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    private static final String TAG = "LinkingPlugin";
    private static final int TIMEOUT = 20 * 1000;

    public LinkingServiceDiscoveryProfile(final DConnectServiceProvider provider) {
        super(provider);
        addApi(mServiceDiscoveryApi);
    }

    private final DConnectApi mServiceDiscoveryApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            createLinkingDeviceList();
            createLinkingBeaconList();
            cleanupDConnectService();

            List<Bundle> serviceBundles = new ArrayList<>();
            for (DConnectService service : getServiceProvider().getServiceList()) {
                Bundle serviceBundle = new Bundle();
                setId(serviceBundle, service.getId());
                setName(serviceBundle, service.getName());
                setOnline(serviceBundle, service.isOnline());
                if (service.getConfig() != null) {
                    setConfig(serviceBundle, service.getConfig());
                }
                serviceBundles.add(serviceBundle);
            }
            setServices(response, serviceBundles);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private void createLinkingDeviceList() {
        for (LinkingDevice device : getLinkingDeviceManager().getDevices()) {
            DConnectService service = findDConnectService(device.getBdAddress());
            if (service == null) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Added Device: " + device.getDisplayName());
                }
                getServiceProvider().addService(new LinkingDeviceService((DConnectMessageService) getContext(), device));
            } else {
                ((LinkingDeviceService) service).setLinkingDevice(device);
            }
        }
    }

    private void createLinkingBeaconList() {
        LinkingBeaconManager mgr = getLinkingBeaconManager();
        mgr.startBeaconScanWithTimeout(TIMEOUT);

        for (LinkingBeacon beacon : mgr.getLinkingBeacons()) {
            DConnectService service = findDConnectService(beacon.getServiceId());
            if (service == null) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Added Beacon: " + beacon.getDisplayName());
                }
                getServiceProvider().addService(new LinkingBeaconService((DConnectMessageService) getContext(), beacon));
            } else {
                ((LinkingBeaconService) service).setLinkingBeacon(beacon);
            }
        }
    }

    private void cleanupDConnectService() {
        for (DConnectService service : getServiceProvider().getServiceList()) {
            if (!containsLinkingDevices(service.getId()) && !containsLinkingBeacons(service.getId())) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Remove Service: " + service.getName());
                }
                getServiceProvider().removeService(service);
            }
        }
    }

    private boolean containsLinkingDevices(final String id) {
        for (LinkingDevice device : getLinkingDeviceManager().getDevices()) {
            if (id.equals(device.getBdAddress())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsLinkingBeacons(final String id) {
        for (LinkingBeacon beacon : getLinkingBeaconManager().getLinkingBeacons()) {
            if (id.equals(beacon.getServiceId())) {
                return true;
            }
        }
        return false;
    }

    private DConnectService findDConnectService(final String id) {
        for (DConnectService service : getServiceProvider().getServiceList()) {
            if (service.getId().equals(id)) {
                return service;
            }
        }
        return null;
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }
}
