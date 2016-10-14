/*
 LinkingKeyEventProfile.java
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
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

public class LinkingKeyEventProfile extends KeyEventProfile {

    private static final String TAG = "LinkingPlugIn";

    public LinkingKeyEventProfile(final DConnectMessageService service) {
        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.addOnBeaconButtonEventListener(mListener);

        addApi(mPutOnDown);
        addApi(mDeleteOnDown);
    }

    private final LinkingBeaconManager.OnBeaconButtonEventListener mListener = new LinkingBeaconManager.OnBeaconButtonEventListener() {
        @Override
        public void onClickButton(final LinkingBeacon beacon, final int keyCode, final long timeStamp) {
            notifyKeyEvent(beacon, keyCode, timeStamp);
        }
    };

    private final DConnectApi mPutOnDown = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
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

    private final DConnectApi mDeleteOnDown = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
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

    public void destroy() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingKeyEventProfile#destroy: " + getService().getId());
        }
        getLinkingBeaconManager().removeOnBeaconButtonEventListener(mListener);
    }

    private Bundle createKeyEvent(final int keyCode) {
        Bundle keyEvent = new Bundle();
        keyEvent.putString(PARAM_ID, String.valueOf(KeyEventProfile.KEYTYPE_STD_KEY + keyCode));
        return keyEvent;
    }

    private Bundle createKeyEvent(final int keyCode, final long timeStamp) {
        Bundle keyEvent = createKeyEvent(keyCode);
        keyEvent.putString(PARAM_CONFIG, "" + timeStamp);
        return keyEvent;
    }

    private void setKeyEvent(final Intent intent, final Bundle keyEvent) {
        intent.putExtra(PARAM_KEYEVENT, keyEvent);
    }

    private void notifyKeyEvent(final LinkingBeacon beacon, final int keyCode, final long timeStamp) {
        String serviceId = beacon.getServiceId();
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_DOWN);
        if (events != null && events.size() > 0) {
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);
                setKeyEvent(intent, createKeyEvent(keyCode, timeStamp));
                sendEvent(intent, event.getAccessToken());
            }
        }
    }

    private LinkingBeaconManager getLinkingBeaconManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingBeaconManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }
}
