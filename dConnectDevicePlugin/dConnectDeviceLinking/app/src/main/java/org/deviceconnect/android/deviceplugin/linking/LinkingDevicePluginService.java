/*
 LinkingDeviceService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.profile.BeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.service.LinkingBeaconService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.service.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.linking.profile.LinkingSystemProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.ProximityProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.List;

/**
 * Linking device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingDevicePluginService extends DConnectMessageService {

    private static final String TAG = "LinkingPlugIn";

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());

        addLinkingDevice();
        addLinkingBeacon();

        addProfile(new LinkingServiceDiscoveryProfile(getServiceProvider()));
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT.equals(action) ||
                    LinkingBeaconUtil.ACTION_BEACON_SCAN_STATE.equals(action)) {
                LinkingApplication app = (LinkingApplication)  getApplication();
                LinkingBeaconManager mgr = app.getLinkingBeaconManager();
                try {
                    mgr.onReceivedBeacon(intent);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "", e);
                    }
                }
                return START_STICKY;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new LinkingSystemProfile();
    }

    public void onManagerTerminated() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onManagerTerminated");
        }
        ((LinkingApplication) getApplication()).resetManager();
        removeAllServices();
    }

    public void onManagerEventTransmitDisconnected(final String sessionKey) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onManagerEventTransmitDisconnected: " + sessionKey);
        }
        cleanupSession(sessionKey);
    }

    public void onDevicePluginReset() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDevicePluginReset");
        }
        ((LinkingApplication) getApplication()).resetManager();
        resetService();
    }

    public void onManagerUninstalled() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onManagerUninstalled");
        }
        ((LinkingApplication) getApplication()).resetManager();
        removeAllServices();
    }

    public void cleanupSession(final String sessionKey) {
        if (sessionKey == null) {
            return;
        }

        List<Event> events = EventManager.INSTANCE.getEventList(sessionKey);
        for (Event event : events) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "event=" + event);
            }
            EventManager.INSTANCE.removeEvent(event);
            stopDeviceOrientation(event);
            stopProximity(event);
            stopKeyEvent(event);
        }
        stopBeacon();
    }

    private void removeAllServices() {
        List<DConnectService> services = getServiceProvider().getServiceList();
        for (DConnectService service : services) {
            getServiceProvider().removeService(service);
        }
    }

    private void resetService() {
        removeAllServices();
        addLinkingDevice();
        addLinkingBeacon();
    }

    private void addLinkingDevice() {
        for (LinkingDevice device : getLinkingDeviceManager().getDevices()) {
            getServiceProvider().addService(new LinkingDeviceService(this, device));
        }
    }

    private void addLinkingBeacon() {
        for (LinkingBeacon beacon : getLinkingBeaconManager().getLinkingBeacons()) {
            getServiceProvider().addService(new LinkingBeaconService(this, beacon));
        }
    }

    private void stopDeviceOrientation(final Event event) {
        if (!DeviceOrientationProfile.PROFILE_NAME.equals(event.getProfile()) ||
                !DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION.equals(event.getAttribute())) {
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "stopDeviceOrientation");
        }

        String serviceId = event.getServiceId();

        List<Event> events = EventManager.INSTANCE.getEventList(
                serviceId, DeviceOrientationProfile.PROFILE_NAME, null,
                DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);
        if (events.isEmpty()) {
            List<LinkingDevice> devices = getLinkingDeviceManager().getStartedSensorDevices();
            for (LinkingDevice device : devices) {
                if (device.getBdAddress().equals(serviceId)) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Stop a sensor. name=" + device.getDisplayName());
                    }
                    getLinkingDeviceManager().stopSensor(device);
                }
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "events=" + events.size());
            }
        }
    }

    private void stopProximity(final Event event) {
        if (!ProximityProfile.PROFILE_NAME.equals(event.getProfile()) ||
                !ProximityProfile.ATTRIBUTE_ON_DEVICE_PROXIMITY.equals(event.getAttribute())) {
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "stopProximity");
        }

        String serviceId = event.getServiceId();

        List<Event> events = EventManager.INSTANCE.getEventList(
                serviceId, ProximityProfile.PROFILE_NAME, null,
                ProximityProfile.ATTRIBUTE_ON_DEVICE_PROXIMITY);
        if (events.isEmpty()) {
            List<LinkingDevice> devices = getLinkingDeviceManager().getStartedRangeDevices();
            for (LinkingDevice device : devices) {
                if (device.getBdAddress().equals(serviceId)) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Stop a proximity. name=" + device.getDisplayName());
                    }
                    getLinkingDeviceManager().stopRange(device);
                }
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "events=" + events.size());
            }
        }
    }

    private void stopKeyEvent(final Event event) {
        if (!KeyEventProfile.PROFILE_NAME.equals(event.getProfile()) ||
                !KeyEventProfile.ATTRIBUTE_ON_DOWN.equals(event.getAttribute())) {
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "stopKeyEvent");
        }

        String serviceId = event.getServiceId();

        List<Event> events = EventManager.INSTANCE.getEventList(
                serviceId, KeyEventProfile.PROFILE_NAME, null, KeyEventProfile.ATTRIBUTE_ON_DOWN);
        if (events.isEmpty()) {
            List<LinkingDevice> devices = getLinkingDeviceManager().getStartedKeyEventDevices();
            for (LinkingDevice device : devices) {
                if (device.getBdAddress().equals(serviceId)) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Stop a key event. name=" + device.getDisplayName());
                    }
                    getLinkingDeviceManager().stopKeyEvent(device);
                }
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "events=" + events.size());
            }
        }
    }

    private void stopBeacon() {
        if (BeaconUtil.isEmptyEvent(getLinkingBeaconManager())) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "stop beacon");
            }
            getLinkingBeaconManager().stopBeaconScan();
        }
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
        return (LinkingApplication) getApplication();
    }
}
