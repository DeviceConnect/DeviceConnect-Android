/*
 LinkingProximityProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.GattData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.service.LinkingBeaconService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.LinkingDestroy;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ProximityProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

public class LinkingProximityProfile extends ProximityProfile implements LinkingDestroy {

    private static final String TAG = "LinkingPlugIn";
    private static final int TIMEOUT = 30 * 1000;

    public LinkingProximityProfile(final DConnectMessageService service) {
        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.addOnBeaconProximityEventListener(mListener);

        addApi(mGetOnDeviceProximity);
        addApi(mPutOnDeviceProximity);
        addApi(mDeleteOnDeviceProximity);
    }

    private final LinkingBeaconManager.OnBeaconProximityEventListener mListener = new LinkingBeaconManager.OnBeaconProximityEventListener() {
        @Override
        public void onProximity(final LinkingBeacon beacon, final GattData gatt) {
            notifyProximityEvent(beacon, gatt);
        }
    };

    private final DConnectApi mGetOnDeviceProximity = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_PROXIMITY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingBeaconManager mgr = getLinkingBeaconManager();
            LinkingBeacon beacon = ((LinkingBeaconService) getService()).getLinkingBeacon();

            GattData gatt = beacon.getGattData();
            if (gatt != null && System.currentTimeMillis() - gatt.getTimeStamp() < TIMEOUT) {
                setResult(response, DConnectMessage.RESULT_OK);
                setProximity(response, createProximity(gatt));
                mgr.startBeaconScanWithTimeout(TIMEOUT);
                return true;
            }

            mgr.addOnBeaconProximityEventListener(new OnBeaconProximityEventListenerImpl(mgr, beacon) {
                @Override
                public void onCleanup() {
                    mBeaconManager.removeOnBeaconProximityEventListener(this);
                }

                @Override
                public void onDisableScan(final String message) {
                    if (mCleanupFlag) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onTemperature: disable scan.");
                    }

                    MessageUtils.setIllegalDeviceStateError(response, message);
                    sendResponse(response);
                }

                @Override
                public void onTimeout() {
                    if (mCleanupFlag) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onTemperature: timeout");
                    }

                    MessageUtils.setTimeoutError(response);
                    sendResponse(response);
                }

                @Override
                public synchronized void onProximity(LinkingBeacon beacon, GattData gatt) {
                    if (mCleanupFlag || !beacon.equals(mBeacon)) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onTemperature: beacon=" + beacon.getDisplayName() + " gatt=" + gatt);
                    }

                    setResult(response, DConnectMessage.RESULT_OK);
                    setProximity(response, createProximity(gatt));
                    sendResponse(response);
                    cleanup();
                }
            });
            mgr.startBeaconScanWithTimeout(TIMEOUT);
            return false;
        }
    };

    private final DConnectApi mPutOnDeviceProximity = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_PROXIMITY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                getLinkingBeaconManager().startBeaconScan();
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnDeviceProximity = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_PROXIMITY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                if (BeaconUtil.isEmptyEvent(getLinkingBeaconManager())) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Linking Beacon Event is empty.");
                    }
                    getLinkingBeaconManager().stopBeaconScan();
                }
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingProximityProfile#destroy: " + getService().getId());
        }
        getLinkingBeaconManager().removeOnBeaconProximityEventListener(mListener);
    }

    private Bundle createProximity(final LinkingDeviceManager.Range range) {
        Bundle proximity = new Bundle();
        setRange(proximity, convertProximityRange(range));
        return proximity;
    }

    private Bundle createProximity(final GattData gatt) {
        Bundle proximity = createProximity(gatt.getRange());
        setValue(proximity, calcDistance(gatt) * 100);
        return proximity;
    }

    private void notifyProximityEvent(final LinkingBeacon beacon, final GattData gatt) {
        if (!beacon.equals(getLinkingBeacon())) {
            return;
        }

        String serviceId = beacon.getServiceId();
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_PROXIMITY);
        if (events != null && events.size() > 0) {
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);
                setProximity(intent, createProximity(gatt));
                sendEvent(intent, event.getAccessToken());
            }
        }
    }

    private Range convertProximityRange(final LinkingDeviceManager.Range range) {
        switch (range) {
            case IMMEDIATE:
                return Range.IMMEDIATE;
            case NEAR:
                return Range.NEAR;
            case FAR:
                return Range.FAR;
            case UNKNOWN:
            default:
                return Range.UNKNOWN;
        }
    }

    private double calcDistance(final GattData gatt) {
      return Math.pow(10.0, (gatt.getTxPower() - gatt.getRssi()) / 20.0);
    }

    private LinkingBeacon getLinkingBeacon() {
        return ((LinkingBeaconService) getService()).getLinkingBeacon();
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private abstract class OnBeaconProximityEventListenerImpl extends TimeoutSchedule implements
            LinkingBeaconManager.OnBeaconProximityEventListener, Runnable {
        OnBeaconProximityEventListenerImpl(final LinkingBeaconManager mgr, final LinkingBeacon beacon) {
            super(mgr, beacon);
        }
    }
}
