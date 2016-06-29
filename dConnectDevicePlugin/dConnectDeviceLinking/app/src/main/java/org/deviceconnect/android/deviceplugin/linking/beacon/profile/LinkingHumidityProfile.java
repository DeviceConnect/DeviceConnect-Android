/*
 LinkingHumidityProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.profile;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.HumidityData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.service.LinkingBeaconService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HumidityProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

public class LinkingHumidityProfile extends HumidityProfile {
    private static final String TAG = "LinkingPlugIn";
    private static final int TIMEOUT = 30 * 1000;

    public LinkingHumidityProfile() {
        addApi(mGetHumidity);
    }

    private final DConnectApi mGetHumidity = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingBeaconManager mgr = getLinkingBeaconManager();
            LinkingBeacon beacon = ((LinkingBeaconService) getService()).getLinkingBeacon();

            HumidityData humidity = beacon.getHumidityData();
            if (humidity != null && System.currentTimeMillis() - humidity.getTimeStamp() < TIMEOUT) {
                setHumidityToResponse(response, humidity);
                mgr.startBeaconScan(TIMEOUT);
                return true;
            }

            mgr.addOnBeaconHumidityEventListener(new OnBeaconHumidityEventListenerImpl(mgr, beacon) {
                @Override
                public void onCleanup() {
                    mBeaconManager.removeOnBeaconHumidityEventListener(this);
                }

                @Override
                public void onDisableScan(String message) {
                    if (mCleanupFlag) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onHumidity: disable scan.");
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
                        Log.i(TAG, "onHumidity: timeout");
                    }

                    MessageUtils.setTimeoutError(response);
                    sendResponse(response);
                }

                @Override
                public synchronized void onHumidity(final LinkingBeacon beacon, final HumidityData humidity) {
                    if (mCleanupFlag || !beacon.equals(mBeacon)) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onHumidity: beacon=" + beacon.getDisplayName() + " humidity=" + humidity.getValue());
                    }

                    setHumidityToResponse(response, humidity);
                    sendResponse(response);
                    cleanup();
                }
            });
            mgr.startBeaconScan(TIMEOUT);
            return false;
        }
    };

    private void setHumidityToResponse(final Intent response, final HumidityData humidityData) {
        setResult(response, DConnectMessage.RESULT_OK);
        setHumidity(response, humidityData.getValue() / 100.0f);
        setTimeStamp(response, humidityData.getTimeStamp());
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private abstract class OnBeaconHumidityEventListenerImpl extends TimeoutSchedule implements
            LinkingBeaconManager.OnBeaconHumidityEventListener, Runnable {
        OnBeaconHumidityEventListenerImpl(final LinkingBeaconManager mgr, final LinkingBeacon beacon) {
            super(mgr, beacon);
        }
    }
}
