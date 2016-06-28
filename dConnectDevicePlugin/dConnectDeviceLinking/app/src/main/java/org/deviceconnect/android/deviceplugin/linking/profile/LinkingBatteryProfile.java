/*
 LinkingBatteryProfile.java
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
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.BatteryData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LinkingBatteryProfile extends BatteryProfile {

    private static final String TAG = "LinkingPlugin";
    private static final int TIMEOUT = 30;

    public LinkingBatteryProfile(final DConnectMessageService service) {
        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.addOnBeaconBatteryEventListener(new LinkingBeaconManager.OnBeaconBatteryEventListener() {
            @Override
            public void onBattery(LinkingBeacon beacon, BatteryData battery) {
                notifyBatteryEvent(beacon, battery);
            }
        });
    }

    @Override
    protected boolean onGetAll(final Intent request, final Intent response, final String serviceId) {
        final LinkingBeaconManager mgr = getLinkingBeaconManager();
        LinkingBeacon beacon = LinkingBeaconUtil.findLinkingBeacon(mgr, serviceId);
        if (beacon == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        mgr.addOnBeaconBatteryEventListener(new OnBeaconBatteryEventListenerImpl(beacon) {
            public void cleanup() {
                if (mCleanupFlag) {
                    return;
                }
                mCleanupFlag = true;

                mgr.removeOnBeaconBatteryEventListener(this);

                mScheduledFuture.cancel(false);
                mExecutorService.shutdown();
            }

            @Override
            public void onTimeout() {
                if (mCleanupFlag) {
                    return;
                }

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onBattery: timeout");
                }

                MessageUtils.setTimeoutError(response);
                sendResponse(response);
                cleanup();
            }

            @Override
            public void onBattery(final LinkingBeacon beacon, final BatteryData battery) {
                if (mCleanupFlag && !beacon.equals(mBeacon)) {
                    return;
                }

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onBattery: beacon=" + beacon.getDisplayName() + " battery=" + battery);
                }

                setResult(response, DConnectMessage.RESULT_OK);
                setLevel(response, battery.getLevel() / 100.0f);
                sendResponse(response);
                cleanup();
            }
        });
        mgr.startBeaconScan(TIMEOUT);

        return false;
    }

    @Override
    protected boolean onGetLevel(final Intent request, final Intent response, final String serviceId) {
        LinkingBeacon beacon = getLinkingBeacon(response, serviceId);
        if (beacon == null) {
            return true;
        }

        BatteryData batteryData = beacon.getBatteryData();
        if (batteryData == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        setResult(response, DConnectMessage.RESULT_OK);
        setLevel(response, batteryData.getLevel() / 100.0f);

        return true;
    }

    @Override
    protected boolean onPutOnBatteryChange(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {
        if (getLinkingBeacon(response, serviceId) == null) {
            return true;
        }

        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnBatteryChange(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {
        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    private void notifyBatteryEvent(final LinkingBeacon beacon, final BatteryData batteryData) {
        String serviceId = LinkingBeaconUtil.createServiceIdFromLinkingBeacon(beacon);
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_BATTERY_CHANGE);
        if (events != null && events.size() > 0) {
            synchronized (events) {
                for (Event event : events) {
                    Intent intent = EventManager.createEventMessage(event);
                    setBattery(intent, createBattery(batteryData));
                    sendEvent(intent, event.getAccessToken());
                }
            }
        }
    }

    private Bundle createBattery(final BatteryData batteryData) {
        Bundle battery = new Bundle();
        setLevel(battery, batteryData.getLevel() / 100.0f);
        return battery;
    }

    private LinkingBeacon getLinkingBeacon(final Intent response, final String serviceId) {
        LinkingBeaconManager mgr = getLinkingBeaconManager();
        LinkingBeacon beacon = LinkingBeaconUtil.findLinkingBeacon(mgr, serviceId);
        if (beacon == null) {
            MessageUtils.setNotSupportProfileError(response);
            return null;
        }

        if (!beacon.isOnline()) {
            MessageUtils.setIllegalDeviceStateError(response, beacon.getDisplayName() + " is offline.");
            return null;
        }
        return beacon;
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }


    private abstract class OnBeaconBatteryEventListenerImpl implements LinkingBeaconManager.OnBeaconBatteryEventListener, Runnable {
        protected LinkingBeacon mBeacon;
        protected ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
        protected ScheduledFuture<?> mScheduledFuture;
        protected boolean mCleanupFlag;
        OnBeaconBatteryEventListenerImpl(final LinkingBeacon beacon) {
            mBeacon = beacon;
            mScheduledFuture = mExecutorService.schedule(this, TIMEOUT, TimeUnit.SECONDS);
        }

        @Override
        public void run() {
            onTimeout();
        }
        public abstract void onTimeout();
    }
}
