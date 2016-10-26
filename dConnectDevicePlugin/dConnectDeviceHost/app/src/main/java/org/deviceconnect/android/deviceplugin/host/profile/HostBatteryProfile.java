/*
 HostBatteryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
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

import static android.R.attr.action;

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
            int mStatus = getBatteryManager().getBatteryStatus();
            setResult(response, IntentDConnectMessage.RESULT_OK);
            setCharging(response, getBatteryChargingStatus(mStatus));
            return true;
        }
    };

    private final DConnectApi mBatteryAllApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            int mLevel = getBatteryManager().getBatteryLevel();
            int mScale = getBatteryManager().getBatteryScale();
            if (mScale <= 0) {
                MessageUtils.setUnknownError(response, "Scale of battery level is unknown.");
            } else if (mLevel < 0) {
                MessageUtils.setUnknownError(response, "Battery level is unknown.");
            } else {
                setLevel(response, mLevel / (float) mScale);
                int mStatus = getBatteryManager().getBatteryStatus();
                setCharging(response, getBatteryChargingStatus(mStatus));

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

    private HostBatteryManager mHostBatteryManager;

    public HostBatteryProfile(final HostBatteryManager manager) {
        mHostBatteryManager = manager;
        mHostBatteryManager.setBatteryChargingEventListener(mBatteryChargingEventListener);
        mHostBatteryManager.setBatteryStatusEventListener(mBatteryStatusEventListener);

        addApi(mBatteryLevelApi);
        addApi(mBatteryChargingApi);
        addApi(mBatteryAllApi);
        addApi(mPutOnChargingChangeApi);
        addApi(mDeleteOnChargingChangeApi);
        addApi(mPutOnBatteryChangeApi);
        addApi(mDeleteOnBatteryChangeApi);
    }

    /**
     * Get status of charging.
     * 
     * @param mStatus BatteryStatus
     * @return true:charging false:not charging
     */
    private boolean getBatteryChargingStatus(final int mStatus) {
        switch (mStatus) {
        case HostBatteryManager.BATTERY_STATUS_CHARGING:
        case HostBatteryManager.BATTERY_STATUS_FULL:
            return true;
        case HostBatteryManager.BATTERY_STATUS_UNKNOWN:
        case HostBatteryManager.BATTERY_STATUS_DISCHARGING:
        case HostBatteryManager.BATTERY_STATUS_NOT_CHARGING:
        default:
            return false;
        }
    }

    private HostBatteryManager getBatteryManager() {
        return mHostBatteryManager;
    }

    private final HostBatteryManager.BatteryChargingEventListener mBatteryChargingEventListener = new HostBatteryManager.BatteryChargingEventListener() {
        @Override
        public void onChangeCharging() {
            List<Event> events = EventManager.INSTANCE.getEventList(HostDeviceService.SERVICE_ID, HostBatteryProfile.PROFILE_NAME,
                    null, HostBatteryProfile.ATTRIBUTE_ON_BATTERY_CHANGE);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent mIntent = EventManager.createEventMessage(event);
                HostBatteryProfile.setAttribute(mIntent, HostBatteryProfile.ATTRIBUTE_ON_BATTERY_CHANGE);
                Bundle battery = new Bundle();
                double level = ((double) (getBatteryManager().getBatteryLevel())) / ((double) getBatteryManager().getBatteryScale());
                HostBatteryProfile.setLevel(battery, level);
                HostBatteryProfile.setBattery(mIntent, battery);
                sendEvent(mIntent, event.getAccessToken());
            }
        }
    };

    private final HostBatteryManager.BatteryStatusEventListener mBatteryStatusEventListener = new HostBatteryManager.BatteryStatusEventListener() {
        @Override
        public void onChangeStatus() {
            List<Event> events = EventManager.INSTANCE.getEventList(HostDeviceService.SERVICE_ID, HostBatteryProfile.PROFILE_NAME,
                    null, HostBatteryProfile.ATTRIBUTE_ON_CHARGING_CHANGE);

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent mIntent = EventManager.createEventMessage(event);
                HostBatteryProfile.setAttribute(mIntent, HostBatteryProfile.ATTRIBUTE_ON_CHARGING_CHANGE);
                Bundle charging = new Bundle();
                if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                    HostBatteryProfile.setCharging(charging, true);
                } else {
                    HostBatteryProfile.setCharging(charging, false);
                }
                HostBatteryProfile.setBattery(mIntent, charging);
                sendEvent(mIntent, event.getAccessToken());
            }
        }
    };
}
