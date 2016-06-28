/*
 LinkingAtmosphericPressureProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.AtmosphericPressureData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.AtmosphericPressureProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LinkingAtmosphericPressureProfile extends AtmosphericPressureProfile {

    private static final String TAG = "LinkingPlugin";
    private static final int TIMEOUT = 30;

    @Override
    protected boolean onGetAtmosphericPressure(final Intent request, final Intent response, final String serviceId) {
        final LinkingBeaconManager mgr = getLinkingBeaconManager();
        LinkingBeacon beacon = LinkingBeaconUtil.findLinkingBeacon(mgr, serviceId);
        if (beacon == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        mgr.addOnBeaconAtmosphericPressureEventListener(new OnBeaconAtmosphericPressureEventListenerImpl(beacon) {
            public void cleanup() {
                if (mCleanupFlag) {
                    return;
                }
                mCleanupFlag = true;

                mgr.removeOnBeaconAtmosphericPressureEventListener(this);

                mScheduledFuture.cancel(false);
                mExecutorService.shutdown();
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
                cleanup();
            }

            @Override
            public synchronized void onAtmosphericPressure(final LinkingBeacon beacon, final AtmosphericPressureData atmosphericPressure) {
                if (mCleanupFlag && !beacon.equals(mBeacon)) {
                    return;
                }

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onAtmosphericPressure: beacon=" + beacon.getDisplayName() + " atmosphericPressure=" + atmosphericPressure.getValue());
                }

                setResult(response, DConnectMessage.RESULT_OK);
                setAtmosphericPressure(response, atmosphericPressure.getValue());
                setTimeStamp(response, atmosphericPressure.getTimeStamp());
                sendResponse(response);
                cleanup();
            }
        });
        mgr.startBeaconScan(TIMEOUT);

//        if (!beacon.isOnline()) {
//            MessageUtils.setIllegalDeviceStateError(response, beacon.getDisplayName() + " is offline.");
//            return true;
//        }
//
//        AtmosphericPressureData atmosphericPressureData = beacon.getAtmosphericPressureData();
//        if (atmosphericPressureData == null) {
//            MessageUtils.setNotSupportProfileError(response);
//            return true;
//        }
//
//        setResult(response, DConnectMessage.RESULT_OK);
//        setAtmosphericPressure(response, atmosphericPressureData.getValue());
//        setTimeStamp(response, atmosphericPressureData.getTimeStamp());

        return false;
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDeviceService service = (LinkingDeviceService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private abstract class OnBeaconAtmosphericPressureEventListenerImpl implements LinkingBeaconManager.OnBeaconAtmosphericPressureEventListener, Runnable {
        protected LinkingBeacon mBeacon;
        protected ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
        protected ScheduledFuture<?> mScheduledFuture;
        protected boolean mCleanupFlag;
        OnBeaconAtmosphericPressureEventListenerImpl(final LinkingBeacon beacon) {
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
