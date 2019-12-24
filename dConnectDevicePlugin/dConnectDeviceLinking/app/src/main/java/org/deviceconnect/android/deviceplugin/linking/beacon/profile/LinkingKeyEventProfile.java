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

import org.deviceconnect.android.deviceplugin.linking.lib.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDestroy;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.beacon.LinkingBeaconManager;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.ButtonData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.service.LinkingBeaconService;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

public class LinkingKeyEventProfile extends KeyEventProfile implements LinkingDestroy {
    private static final int TIMEOUT = 30 * 1000;
    private static final String TAG = "LinkingPlugIn";

    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_KEY_CHANGE = "onKeyChange";

    /**
     * Parameter: {@value} .
     */
    public static final String PARAM_STATE = "state";

    public LinkingKeyEventProfile(final DConnectMessageService service) {
        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingBeaconManager mgr = app.getLinkingBeaconManager();
        mgr.addOnBeaconButtonEventListener(mListener);

        addApi(mGetOnDown);
        addApi(mPutOnDown);
        addApi(mDeleteOnDown);
        addApi(mGetOnKeyChangeApi);
        addApi(mPutOnKeyChangeApi);
        addApi(mDeleteOnKeyChangeApi);
    }

    private final LinkingBeaconManager.OnBeaconButtonEventListener mListener = this::notifyKeyEvent;

    private final DConnectApi mGetOnDown = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingBeaconManager mgr = getLinkingBeaconManager();
            LinkingBeacon beacon = ((LinkingBeaconService) getService()).getLinkingBeacon();

            ButtonData button = beacon.getButtonData();
            if (button != null && System.currentTimeMillis() - button.getTimeStamp() < TIMEOUT) {
                setKeyEvent(response, createKeyEvent(button.getKeyCode(), button.getTimeStamp()));
                mgr.startBeaconScanWithTimeout(TIMEOUT);
                return true;
            }

            mgr.addOnBeaconButtonEventListener(new OnBeaconButtonEventListenerImpl(mgr, beacon) {
                @Override
                public void onClickButton(final LinkingBeacon beacon, final int keyCode, final long timeStamp) {
                    if (mCleanupFlag || !beacon.equals(mBeacon)) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onClickButton: beacon=" + beacon.getDisplayName() + " button=" + keyCode);
                    }

                    setKeyEvent(response, createKeyEvent(keyCode, timeStamp));
                    sendResponse(response);
                    cleanup();
                }

                @Override
                public void onDisableScan(final String message) {
                    if (mCleanupFlag) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onClickButton: disable scan.");
                    }

                    MessageUtils.setIllegalDeviceStateError(response, message);
                    sendResponse(response);
                }

                @Override
                public void onCleanup() {
                    mBeaconManager.removeOnBeaconButtonEventListener(this);
                }

                @Override
                public void onTimeout() {
                    if (mCleanupFlag) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onClickButton: timeout");
                    }

                    MessageUtils.setTimeoutError(response);
                    sendResponse(response);
                }
            });
            mgr.startBeaconScanWithTimeout(10 * 1000);
            return false;
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

    private final DConnectApi mGetOnKeyChangeApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingBeaconManager mgr = getLinkingBeaconManager();
            LinkingBeacon beacon = ((LinkingBeaconService) getService()).getLinkingBeacon();

            ButtonData button = beacon.getButtonData();
            if (button != null && System.currentTimeMillis() - button.getTimeStamp() < TIMEOUT) {
                setKeyEvent(response, createKeyEvent(button.getKeyCode(), button.getTimeStamp()));
                mgr.startBeaconScanWithTimeout(TIMEOUT);
                return true;
            }

            mgr.addOnBeaconButtonEventListener(new OnBeaconButtonEventListenerImpl(mgr, beacon) {
                @Override
                public void onClickButton(final LinkingBeacon beacon, final int keyCode, final long timeStamp) {
                    if (mCleanupFlag || !beacon.equals(mBeacon)) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onClickButton: beacon=" + beacon.getDisplayName() + " button=" + keyCode);
                    }

                    Bundle keyEvent = createKeyEvent(keyCode, timeStamp);
                    keyEvent.putString(PARAM_STATE, "down");
                    setKeyEvent(response, keyEvent);
                    sendResponse(response);
                    cleanup();
                }

                @Override
                public void onDisableScan(final String message) {
                    if (mCleanupFlag) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onClickButton: disable scan.");
                    }

                    MessageUtils.setIllegalDeviceStateError(response, message);
                    sendResponse(response);
                }

                @Override
                public void onCleanup() {
                    mBeaconManager.removeOnBeaconButtonEventListener(this);
                }

                @Override
                public void onTimeout() {
                    if (mCleanupFlag) {
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onClickButton: timeout");
                    }

                    MessageUtils.setTimeoutError(response);
                    sendResponse(response);
                }
            });
            mgr.startBeaconScanWithTimeout(10 * 1000);
            return false;
        }
    };

    private final DConnectApi mPutOnKeyChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
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

    private final DConnectApi mDeleteOnKeyChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
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
        if (!beacon.equals(getLinkingBeacon())) {
            return;
        }

        String serviceId = beacon.getServiceId();
        List<Event> keyDownEvents = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_DOWN);
        if (keyDownEvents != null && keyDownEvents.size() > 0) {
            for (Event event : keyDownEvents) {
                Intent intent = EventManager.createEventMessage(event);
                setKeyEvent(intent, createKeyEvent(keyCode, timeStamp));
                sendEvent(intent, event.getAccessToken());
            }
        }

        List<Event> keyChangeEvents = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_KEY_CHANGE);
        if (keyChangeEvents != null && keyChangeEvents.size() > 0) {
            for (Event event : keyChangeEvents) {
                Bundle keyEvent = createKeyEvent(keyCode, timeStamp);
                keyEvent.putString(PARAM_STATE, "down");
                Intent intent = EventManager.createEventMessage(event);
                setKeyEvent(intent, keyEvent);
                sendEvent(intent, event.getAccessToken());
            }
        }
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

    private abstract class OnBeaconButtonEventListenerImpl extends TimeoutSchedule implements
            LinkingBeaconManager.OnBeaconButtonEventListener, Runnable {
        OnBeaconButtonEventListenerImpl(final LinkingBeaconManager mgr, final LinkingBeacon beacon) {
            super(mgr, beacon);
        }
    }
}
