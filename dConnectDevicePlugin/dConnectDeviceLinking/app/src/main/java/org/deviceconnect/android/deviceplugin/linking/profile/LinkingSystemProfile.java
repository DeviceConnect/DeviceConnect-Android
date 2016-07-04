/*
 LinkingSystemProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.profile.BeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.setting.SettingActivity;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.ProximityProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * System Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingSystemProfile extends SystemProfile {

    public static final String TAG = "LinkingPlugIn";

    public LinkingSystemProfile() {
        addApi(mDeleteEvents);
    }

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return SettingActivity.class;
    }

    private final DConnectApi mDeleteEvents = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_EVENTS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            List<Event> events = EventManager.INSTANCE.getEventList(getSessionKey(request));
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
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

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
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }
}
