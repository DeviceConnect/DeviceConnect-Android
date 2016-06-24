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
import org.deviceconnect.android.deviceplugin.linking.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.setting.SettingActivity;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.ProximityProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * System Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingSystemProfile extends SystemProfile {

    public static final String TAG = "Linking-Plugin";

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return SettingActivity.class;
    }

    @Override
    protected boolean onDeleteEvents(final Intent request, final Intent response, final String sessionKey) {
        EventManager.INSTANCE.removeEvents(sessionKey);
        stopDeviceOrientation();
        stopProximity();
        stopKeyEvent();
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private void stopDeviceOrientation() {
        List<Event> events = EventManager.INSTANCE.getEventList(
                DeviceOrientationProfile.PROFILE_NAME, null,
                DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);
        List<LinkingDevice> devices = getLinkingDeviceManager().getStartedSensorDevices();
        for (LinkingDevice device : devices) {
            boolean stopFlag = true;
            for (Event event : events) {
                if (device.getBdAddress().equals(event.getServiceId())) {
                    stopFlag = false;
                    break;
                }
            }
            if (stopFlag) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Stop a sensor. name=" + device.getDisplayName());
                }
                getLinkingDeviceManager().stopSensor(device);
            }
        }
    }

    private void stopProximity() {
        List<Event> events = EventManager.INSTANCE.getEventList(
                ProximityProfile.PROFILE_NAME, null,
                ProximityProfile.ATTRIBUTE_ON_DEVICE_PROXIMITY);
        List<LinkingDevice> devices = getLinkingDeviceManager().getStartedRangeDevices();
        for (LinkingDevice device : devices) {
            boolean stopFlag = true;
            for (Event event : events) {
                if (device.getBdAddress().equals(event.getServiceId())) {
                    stopFlag = false;
                    break;
                }
            }
            if (stopFlag) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Stop a proximity. name=" + device.getDisplayName());
                }
                getLinkingDeviceManager().stopRange(device);
            }
        }
    }

    private void stopKeyEvent() {
        List<Event> events = EventManager.INSTANCE.getEventList(
                KeyEventProfile.PROFILE_NAME, null, KeyEventProfile.ATTRIBUTE_ON_DOWN);
        List<LinkingDevice> devices = getLinkingDeviceManager().getStartedKeyEventDevices();
        for (LinkingDevice device : devices) {
            boolean stopFlag = true;
            for (Event event : events) {
                if (device.getBdAddress().equals(event.getServiceId())) {
                    stopFlag = false;
                    break;
                }
            }
            if (stopFlag) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Stop a key event. name=" + device.getDisplayName());
                }
                getLinkingDeviceManager().stopKeyEvent(device);
            }
        }
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDeviceService service = (LinkingDeviceService) getContext();
        return (LinkingApplication) service.getApplication();
    }
}
