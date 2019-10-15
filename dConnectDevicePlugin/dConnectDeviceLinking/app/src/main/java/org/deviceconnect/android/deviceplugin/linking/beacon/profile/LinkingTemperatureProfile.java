/*
 LinkingTemperatureProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.profile;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.lib.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.TemperatureData;
import org.deviceconnect.android.deviceplugin.linking.beacon.service.LinkingBeaconService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.TemperatureProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

public class LinkingTemperatureProfile extends TemperatureProfile {

    private static final String TAG = "LinkingPlugin";
    private static final int TIMEOUT = 30 * 1000;

    public LinkingTemperatureProfile() {
        addApi(mGetTemperature);
    }

    private final DConnectApi mGetTemperature = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingBeaconManager mgr = getLinkingBeaconManager();
            LinkingBeacon beacon = ((LinkingBeaconService) getService()).getLinkingBeacon();

            TemperatureData temperature = beacon.getTemperatureData();
            if (temperature != null && System.currentTimeMillis() - temperature.getTimeStamp() < TIMEOUT) {
                setTemperatureToResponse(request, response, temperature);
                mgr.startBeaconScanWithTimeout(TIMEOUT);
                return true;
            }

            mgr.addOnBeaconTemperatureEventListener(new OnBeaconTemperatureEventListenerImpl(mgr, beacon) {
                @Override
                public void onCleanup() {
                    mBeaconManager.removeOnBeaconTemperatureEventListener(this);
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
                public synchronized void onTemperature(final LinkingBeacon beacon, final TemperatureData temperature) {
                    if (mCleanupFlag || !beacon.equals(mBeacon)) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onTemperature: beacon=" + beacon.getDisplayName() + " temperature=" + temperature.getValue());
                    }

                    setTemperatureToResponse(request, response, temperature);
                    sendResponse(response);
                    cleanup();
                }
            });
            mgr.startBeaconScanWithTimeout(TIMEOUT);
            return false;
        }
    };

    private void setTemperatureToResponse(final Intent request, final Intent response, final TemperatureData temperatureData) {
        int type = TemperatureProfile.getType(request);
        TemperatureType tType = TemperatureType.TYPE_CELSIUS;
        float temp = temperatureData.getValue();
        if (type == TemperatureType.TYPE_FAHRENHEIT.getValue()) {
            temp = TemperatureProfile.convertCelsiusToFahrenheit(temp);
            tType = TemperatureType.TYPE_FAHRENHEIT;
        }
        setResult(response, DConnectMessage.RESULT_OK);
        setTemperature(response, temp);
        setTemperatureType(response, tType);
        setTimeStamp(response, System.currentTimeMillis());
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private abstract class OnBeaconTemperatureEventListenerImpl extends TimeoutSchedule implements
            LinkingBeaconManager.OnBeaconTemperatureEventListener {
        OnBeaconTemperatureEventListenerImpl(final LinkingBeaconManager mgr, final LinkingBeacon beacon) {
            super(mgr, beacon);
        }
    }
}
