/*
 LinkingKeyEventProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.lib.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDestroy;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.service.LinkingDeviceService;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

public class LinkingKeyEventProfile extends KeyEventProfile implements LinkingDestroy {

    private static final String TAG = "LinkingPlugIn";

    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_KEY_CHANGE = "onKeyChange";

    /**
     * Parameter: {@value} .
     */
    public static final String PARAM_STATE = "state";

    public LinkingKeyEventProfile() {
        addApi(mGetOnDown);
        addApi(mPutOnDown);
        addApi(mDeleteOnDown);
        addApi(mGetOnKeyChangeApi);
        addApi(mPutOnKeyChangeApi);
        addApi(mDeleteOnKeyChangeApi);
    }

    private final LinkingDeviceManager.OnButtonEventListener mListener = new LinkingDeviceManager.OnButtonEventListener() {
        @Override
        public void onButtonEvent(final LinkingDevice device, final int keyCode) {
            notifyKeyEvent(device, keyCode);
        }
    };

    private final DConnectApi mGetOnDown = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            final LinkingDeviceManager deviceManager = getLinkingDeviceManager();
            deviceManager.enableListenButtonEvent(device, new OnKeyEventListenerImpl(device) {
                @Override
                public void onCleanup() {
                    deviceManager.disableListenButtonEvent(mDevice, this);
                }

                @Override
                public void onTimeout() {
                    if (mCleanupFlag) {
                        return;
                    }

                    MessageUtils.setTimeoutError(response);
                    sendResponse(response);
                }

                @Override
                public void onButtonEvent(final LinkingDevice device, final int keyCode) {
                    if (mCleanupFlag || !mDevice.equals(device)) {
                        return;
                    }

                    setKeyEvent(response, createKeyEvent(keyCode, System.currentTimeMillis()));
                    sendResponse(response);
                    cleanup();
                }
            });
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
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                getLinkingDeviceManager().enableListenButtonEvent(device, mListener);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    private DConnectApi mDeleteOnDown = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                if (isEmptyEventList(device)) {
                    getLinkingDeviceManager().disableListenButtonEvent(device, mListener);
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
            final LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            final LinkingDeviceManager deviceManager = getLinkingDeviceManager();
            deviceManager.enableListenButtonEvent(device, new OnKeyEventListenerImpl(device) {
                @Override
                public void onCleanup() {
                    deviceManager.disableListenButtonEvent(mDevice, this);
                }

                @Override
                public void onTimeout() {
                    if (mCleanupFlag) {
                        return;
                    }

                    MessageUtils.setTimeoutError(response);
                    sendResponse(response);
                }

                @Override
                public void onButtonEvent(final LinkingDevice device, final int keyCode) {
                    if (mCleanupFlag || !mDevice.equals(device)) {
                        return;
                    }

                    Bundle keyEvent = createKeyEvent(keyCode, System.currentTimeMillis());
                    keyEvent.putString(PARAM_STATE, "down");
                    setKeyEvent(response, keyEvent);
                    sendResponse(response);
                    cleanup();
                }
            });
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
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                getLinkingDeviceManager().enableListenButtonEvent(device, mListener);
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
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                if (isEmptyEventList(device)) {
                    getLinkingDeviceManager().disableListenButtonEvent(device, mListener);
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
        getLinkingDeviceManager().disableListenButtonEvent(getDevice(), mListener);
    }

    private boolean isEmptyEventList(final LinkingDevice device) {
        List<Event> keyDownEvents = EventManager.INSTANCE.getEventList(
                device.getBdAddress(), PROFILE_NAME, null, ATTRIBUTE_ON_DOWN);
        List<Event> keyChangeEvents = EventManager.INSTANCE.getEventList(
                device.getBdAddress(), PROFILE_NAME, null, ATTRIBUTE_ON_KEY_CHANGE);
        return keyDownEvents.isEmpty() && keyChangeEvents.isEmpty();
    }

    private LinkingDevice getDevice() {
        return ((LinkingDeviceService) getService()).getLinkingDevice();
    }

    private LinkingDevice getDevice(final Intent response) {
        LinkingDevice device = getDevice();

        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }

        return device;
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

    private void notifyKeyEvent(final LinkingDevice device, final int keyCode) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "notifyKeyEvent: " + device.getDisplayName() + "[" + keyCode + "]");
        }

        String serviceId = device.getBdAddress();
        List<Event> keyDownEvents = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_DOWN);
        if (keyDownEvents != null && keyDownEvents.size() > 0) {
            for (Event event : keyDownEvents) {
                Intent intent = EventManager.createEventMessage(event);
                setKeyEvent(intent, createKeyEvent(keyCode));
                sendEvent(intent, event.getAccessToken());
            }
        }

        List<Event> keyChangeEvents = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_KEY_CHANGE);
        if (keyChangeEvents != null) {
            for (Event event : keyChangeEvents) {
                Bundle keyEvent = createKeyEvent(keyCode);
                keyEvent.putString(PARAM_STATE, "down");
                Intent intent = EventManager.createEventMessage(event);
                setKeyEvent(intent, keyEvent);
                sendEvent(intent, event.getAccessToken());
            }
        }
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private abstract class OnKeyEventListenerImpl extends TimeoutSchedule implements LinkingDeviceManager.OnButtonEventListener {
        OnKeyEventListenerImpl(final LinkingDevice device) {
            super(device);
        }
    }
}
