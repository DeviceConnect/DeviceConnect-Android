/*
 LinkingTemperatureProfile.java
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
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.TemperatureData;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.TemperatureProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LinkingTemperatureProfile extends TemperatureProfile {

    private static final String TAG = "LinkingPlugin";
    private static final int TIMEOUT = 30;

    @Override
    protected boolean onGetTemperature(final Intent request, final Intent response, final String serviceId) {
        final LinkingBeaconManager mgr = getLinkingBeaconManager();
        LinkingBeacon beacon = LinkingBeaconUtil.findLinkingBeacon(mgr, serviceId);
        if (beacon == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        mgr.addOnBeaconTemperatureEventListener(new OnBeaconTemperatureEventListenerImpl(beacon) {

            public void cleanup() {
                if (mCleanupFlag) {
                    return;
                }
                mCleanupFlag = true;

                mgr.removeOnBeaconTemperatureEventListener(this);

                mScheduledFuture.cancel(false);
                mExecutorService.shutdown();
            }

            @Override
            public synchronized void onTimeout() {
                if (mCleanupFlag) {
                    return;
                }

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onTemperature: timeout");
                }

                MessageUtils.setTimeoutError(response);
                sendResponse(response);
                cleanup();
            }

            @Override
            public synchronized void onTemperature(final LinkingBeacon beacon, final TemperatureData temperature) {
                if (mCleanupFlag && !beacon.equals(mBeacon)) {
                    return;
                }

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onTemperature: beacon=" + beacon.getDisplayName() + " temperature=" + temperature.getValue());
                }

                setResult(response, DConnectMessage.RESULT_OK);
                setTemperature(response, temperature.getValue());
                setTemperatureType(response, TemperatureType.TYPE_CELSIUS);
                setTimeStamp(response, temperature.getTimeStamp());
                sendResponse(response);
                cleanup();
            }
        });
        getLinkingBeaconManager().startBeaconScan(TIMEOUT);

//        TemperatureData temperatureData = beacon.getTemperatureData();
//        if (temperatureData == null) {
//            MessageUtils.setNotSupportProfileError(response);
//            return true;
//        }
//
//        setResult(response, DConnectMessage.RESULT_OK);
//        setTemperature(response, temperatureData.getValue());
//        setTemperatureType(response, TemperatureType.TYPE_CELSIUS);
//        setTimeStamp(response, temperatureData.getTimeStamp());

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

    private abstract class OnBeaconTemperatureEventListenerImpl implements LinkingBeaconManager.OnBeaconTemperatureEventListener, Runnable {
        protected LinkingBeacon mBeacon;
        protected ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
        protected ScheduledFuture<?> mScheduledFuture;
        protected boolean mCleanupFlag;
        OnBeaconTemperatureEventListenerImpl(final LinkingBeacon beacon) {
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
