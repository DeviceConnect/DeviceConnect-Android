/*
 LinkingHumidityProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconUtil;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.HumidityData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HumidityProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LinkingHumidityProfile extends HumidityProfile {
    private static final String TAG = "LinkingPlugIn";
    private static final int TIMEOUT = 30;

    @Override
    protected boolean onGetHumidity(final Intent request, final Intent response, final String serviceId) {
        final LinkingBeaconManager mgr = getLinkingBeaconManager();
        LinkingBeacon beacon = LinkingBeaconUtil.findLinkingBeacon(mgr, serviceId);
        if (beacon == null) {
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        mgr.addOnBeaconHumidityEventListener(new OnBeaconHumidityEventListenerImpl(beacon) {
            public void cleanup() {
                if (mCleanupFlag) {
                    return;
                }
                mCleanupFlag = true;

                mgr.removeOnBeaconHumidityEventListener(this);

                mScheduledFuture.cancel(false);
                mExecutorService.shutdown();
            }

            @Override
            public synchronized void onTimeout() {
                if (mCleanupFlag) {
                    return;
                }

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onHumidity: timeout");
                }

                MessageUtils.setTimeoutError(response);
                sendResponse(response);
                cleanup();
            }

            @Override
            public synchronized void onHumidity(final LinkingBeacon beacon, final HumidityData humidity) {
                if (mCleanupFlag && !beacon.equals(mBeacon)) {
                    return;
                }

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onHumidity: beacon=" + beacon.getDisplayName() + " humidity=" + humidity.getValue());
                }

                setResult(response, DConnectMessage.RESULT_OK);
                setHumidity(response, humidity.getValue() / 100.0f);
                setTimeStamp(response, humidity.getTimeStamp());
                sendResponse(response);
                cleanup();
            }
        });
        mgr.startBeaconScan(TIMEOUT);

//        HumidityData humidityData = beacon.getHumidityData();
//        if (humidityData == null) {
//            MessageUtils.setNotSupportProfileError(response);
//            return true;
//        }
//
//        setResult(response, DConnectMessage.RESULT_OK);
//        setHumidity(response, humidityData.getValue() / 100.0f);
//        setTimeStamp(response, humidityData.getTimeStamp());

        return false;
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private abstract class OnBeaconHumidityEventListenerImpl implements LinkingBeaconManager.OnBeaconHumidityEventListener, Runnable {
        protected LinkingBeacon mBeacon;
        protected ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
        protected ScheduledFuture<?> mScheduledFuture;
        protected boolean mCleanupFlag;
        OnBeaconHumidityEventListenerImpl(final LinkingBeacon beacon) {
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
