/*
 HostBatteryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.battery.HostBatteryManager;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.List;

/**
 * Battery Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostBatteryProfile extends BatteryProfile {

    private final DConnectApi mBatteryLevelApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_LEVEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            getBatteryManager().getBatteryInfo();
            int mLevel = getBatteryManager().getBatteryLevel();
            int mScale = getBatteryManager().getBatteryScale();
            if (mScale <= 0) {
                MessageUtils.setUnknownError(response, "Scale of battery level is unknown.");
            } else if (mLevel < 0) {
                MessageUtils.setUnknownError(response, "Battery level is unknown.");
            } else {
                setResult(response, IntentDConnectMessage.RESULT_OK);
                setLevel(response, mLevel / (float) mScale);
            }
            return true;
        }
    };

    private final DConnectApi mBatteryChargingApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_CHARGING;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            getBatteryManager().getBatteryInfo();
            setResult(response, IntentDConnectMessage.RESULT_OK);
            setCharging(response, getBatteryManager().isChargingFlag());
            return true;
        }
    };

    private final DConnectApi mBatteryTemperatureApi = new GetApi() {

        @Override
        public String getAttribute() {
            return "temperature";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            getBatteryManager().getBatteryInfo();
            setResult(response, IntentDConnectMessage.RESULT_OK);
            response.putExtra("temperature", getBatteryManager().getTemperature());
            return true;
        }
    };

    private final DConnectApi mBatteryAllApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            getBatteryManager().getBatteryInfo();
            int mLevel = getBatteryManager().getBatteryLevel();
            int mScale = getBatteryManager().getBatteryScale();
            if (mScale <= 0) {
                MessageUtils.setUnknownError(response, "Scale of battery level is unknown.");
            } else if (mLevel < 0) {
                MessageUtils.setUnknownError(response, "Battery level is unknown.");
            } else {
                setLevel(response, mLevel / (float) mScale);
                setCharging(response, getBatteryManager().isChargingFlag());
                response.putExtra("temperature", getBatteryManager().getTemperature());
                setResult(response, IntentDConnectMessage.RESULT_OK);
            }
            return true;
        }
    };

    private final DConnectApi mPutOnChargingChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CHARGING_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                getBatteryManager().registerBatteryConnectBroadcastReceiver();
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnChargingChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CHARGING_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            getBatteryManager().unregisterBatteryConnectBroadcastReceiver();
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownError(response, "Can not unregister event.");
            }
            return true;
        }
    };

    private final DConnectApi mPutOnBatteryChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BATTERY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                getBatteryManager().registerBatteryChargeBroadcastReceiver();
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnBatteryChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BATTERY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            getBatteryManager().unregisterBatteryChargeBroadcastReceiver();
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownError(response, "Can not unregister event.");
            }
            return true;
        }
    };

    private final HostBatteryManager mHostBatteryManager;

    public HostBatteryProfile(final HostBatteryManager manager) {
        mHostBatteryManager = manager;
        mHostBatteryManager.setBatteryChargingEventListener(mBatteryChargingEventListener);
        mHostBatteryManager.setBatteryStatusEventListener(mBatteryStatusEventListener);

        addApi(mBatteryLevelApi);
        addApi(mBatteryChargingApi);
        addApi(mBatteryTemperatureApi);
        addApi(mBatteryAllApi);
        addApi(mPutOnChargingChangeApi);
        addApi(mDeleteOnChargingChangeApi);
        addApi(mPutOnBatteryChangeApi);
        addApi(mDeleteOnBatteryChangeApi);
    }

    private HostBatteryManager getBatteryManager() {
        return mHostBatteryManager;
    }

    private double getLevel() {
        return ((double) (getBatteryManager().getBatteryLevel())) / ((double) getBatteryManager().getBatteryScale());
    }

    private final HostBatteryManager.BatteryChargingEventListener mBatteryChargingEventListener = () -> {
        List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID, HostBatteryProfile.PROFILE_NAME,
                null, HostBatteryProfile.ATTRIBUTE_ON_BATTERY_CHANGE);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent intent = EventManager.createEventMessage(event);
            Bundle battery = new Bundle();
            HostBatteryProfile.setCharging(battery, getBatteryManager().isChargingFlag());
            HostBatteryProfile.setLevel(battery, getLevel());
            battery.putFloat("temperature", getBatteryManager().getTemperature());
            HostBatteryProfile.setBattery(intent, battery);
            sendEvent(intent, event.getAccessToken());
        }
    };

    private final HostBatteryManager.BatteryStatusEventListener mBatteryStatusEventListener = () -> {
        List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID, HostBatteryProfile.PROFILE_NAME,
                null, HostBatteryProfile.ATTRIBUTE_ON_CHARGING_CHANGE);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent intent = EventManager.createEventMessage(event);
            Bundle charging = new Bundle();
            HostBatteryProfile.setCharging(charging, getBatteryManager().isChargingFlag());
            HostBatteryProfile.setBattery(intent, charging);
            sendEvent(intent, event.getAccessToken());
        }
    };
}
