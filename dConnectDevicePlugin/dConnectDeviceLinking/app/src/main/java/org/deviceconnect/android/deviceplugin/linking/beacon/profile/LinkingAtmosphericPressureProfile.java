/*
 LinkingAtmosphericPressureProfile.java
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
import org.deviceconnect.android.deviceplugin.linking.beacon.data.AtmosphericPressureData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.service.LinkingBeaconService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.AtmosphericPressureProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;

public class LinkingAtmosphericPressureProfile extends AtmosphericPressureProfile {

    private static final String TAG = "LinkingPlugin";
    private static final int TIMEOUT = 30;

    public LinkingAtmosphericPressureProfile() {
        addApi(mGetAtmosphericPressure);
    }

    private final DConnectApi mGetAtmosphericPressure = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingBeacon beacon = ((LinkingBeaconService) getService()).getLinkingBeacon();

            AtmosphericPressureData apd = beacon.getAtmosphericPressureData();
            if (apd != null && System.currentTimeMillis() - apd.getTimeStamp() < TIMEOUT) {
                setAtmosphericPressureToResponse(response, apd);
                return true;
            }

            final LinkingBeaconManager mgr = getLinkingBeaconManager();
            mgr.addOnBeaconAtmosphericPressureEventListener(new OnBeaconAtmosphericPressureEventListenerImpl(beacon) {
                @Override
                public void onCleanup() {
                    mgr.removeOnBeaconAtmosphericPressureEventListener(this);
                }

                @Override
                public synchronized void onTimeout() {
                    if (mCleanupFlag) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onAtmosphericPressure: timeout");
                    }

                    MessageUtils.setTimeoutError(response);
                    sendResponse(response);
                }

                @Override
                public synchronized void onAtmosphericPressure(final LinkingBeacon beacon,
                                                               final AtmosphericPressureData atmosphericPressure) {
                    if (mCleanupFlag || !beacon.equals(mBeacon)) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onAtmosphericPressure: beacon=" + beacon.getDisplayName()
                                + " atmosphericPressure=" + atmosphericPressure.getValue());
                    }

                    setAtmosphericPressureToResponse(response, atmosphericPressure);
                    sendResponse(response);
                    cleanup();
                }
            });
            mgr.startBeaconScan(TIMEOUT);
            return false;
        }
    };

    private void setAtmosphericPressureToResponse(final Intent response, final AtmosphericPressureData atmosphericPressureData) {
        setResult(response, DConnectMessage.RESULT_OK);
        setAtmosphericPressure(response, atmosphericPressureData.getValue());
        setTimeStamp(response, atmosphericPressureData.getTimeStamp());
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private abstract class OnBeaconAtmosphericPressureEventListenerImpl extends TimeoutSchedule implements
            LinkingBeaconManager.OnBeaconAtmosphericPressureEventListener {
        OnBeaconAtmosphericPressureEventListenerImpl(final LinkingBeacon beacon) {
            super(beacon);
        }
    }
}
