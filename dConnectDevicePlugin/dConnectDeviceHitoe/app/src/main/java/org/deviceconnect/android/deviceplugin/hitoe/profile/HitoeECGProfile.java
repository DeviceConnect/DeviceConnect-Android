/*
 HitoeECGProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.HitoeDeviceService;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventDispatcherFactory;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ECGProfile;
import org.deviceconnect.android.event.EventDispatcher;
import org.deviceconnect.android.event.EventDispatcherManager;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * Implement ECGProfile.
 * @author NTT DOCOMO, INC.
 */
public class HitoeECGProfile extends ECGProfile {

    /**
     * Implementation of {@link HitoeManager.OnHitoeECGEventListener}.
     */
    private final HitoeManager.OnHitoeECGEventListener mECGEventListener =
            this::notifyECGData;
    /**
     * Event Dispatcher object.
     */
    private EventDispatcherManager mDispatcherManager;

    /**
     * Constructor.
     * @param mgr instance of {@link HitoeManager}
     */
    public HitoeECGProfile(final HitoeManager mgr) {
        mgr.setHitoeECGEventListener(mECGEventListener);
        mDispatcherManager = new EventDispatcherManager();
        addApi(mGetOnECG);
        addApi(mPutOnECG);
        addApi(mDeleteOnECG);
    }

    /**
     * Get ECG.
     */
    private final DConnectApi mGetOnECG = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_ECG;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            if (serviceId == null) {
                MessageUtils.setEmptyServiceIdError(response);
            } else {
                HitoeManager mgr = getManager();
                if (mgr == null) {
                    MessageUtils.setNotFoundServiceError(response);
                    return true;
                }
                HeartRateData data = mgr.getECGData(serviceId);
                if (data == null) {
                    MessageUtils.setNotFoundServiceError(response);
                } else {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setECG(response, data.getECG().toBundle());
                }
            }
            return true;
        }
    };

    /**
     * Register event ECG.
     */
    private final DConnectApi mPutOnECG = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_ECG;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            if (serviceId == null) {
                MessageUtils.setNotFoundServiceError(response, "Not found serviceID");
            } else {
                HitoeManager mgr = getManager();
                if (mgr == null) {
                    MessageUtils.setNotFoundServiceError(response);
                    return true;
                }
                HeartRateData data = mgr.getECGData(serviceId);
                if (data == null) {
                    MessageUtils.setNotFoundServiceError(response);
                } else {
                    EventError error = EventManager.INSTANCE.addEvent(request);
                    if (error == EventError.NONE) {
                        mgr.setHitoeECGEventListener(mECGEventListener);
                        addEventDispatcher(request);
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setUnknownError(response);
                    }
                }
            }
            return true;
        }
    };

    /**
     * Unregister event ECG.
     */
    private final DConnectApi mDeleteOnECG = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_ECG;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            if (serviceId == null) {
                MessageUtils.setEmptyServiceIdError(response);
            } else {
                removeEventDispatcher(request);
                EventError error = EventManager.INSTANCE.removeEvent(request);
                if (error == EventError.NONE) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else if (error == EventError.INVALID_PARAMETER) {
                    MessageUtils.setInvalidRequestParameterError(response);
                } else if (error == EventError.FAILED) {
                    MessageUtils.setUnknownError(response, "Failed to delete event.");
                } else if (error == EventError.NOT_FOUND) {
                    MessageUtils.setUnknownError(response, "Not found event.");
                } else {
                    MessageUtils.setUnknownError(response);
                }
            }
            return true;
        }
    };

    /**
     * Notify the ECG event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of ecg
     */
    private void notifyECGData(final HitoeDevice device, final HeartRateData data) {
        List<Event> events = EventManager.INSTANCE.getEventList(device.getId(),
                getProfileName(), null, ATTRIBUTE_ON_ECG);
        synchronized (events) {
            for (Event event : events) {
                if (data == null) {
                    break;
                }

                Intent intent = EventManager.createEventMessage(event);

                setECG(intent, data.getECG().toBundle());
                mDispatcherManager.sendEvent(event, intent);
            }
        }
    }

    /**
     * Add Event Dispatcher.
     * @param request request parameter
     */
    private void addEventDispatcher(final Intent request) {
        Event event = EventManager.INSTANCE.getEvent(request);
        EventDispatcher dispatcher = EventDispatcherFactory.createEventDispatcher(
                (DConnectMessageService) getContext(), request);
        mDispatcherManager.addEventDispatcher(event, dispatcher);
    }

    /**
     * Remove Event Dispatcher.
     * @param request request parameter
     */
    private void removeEventDispatcher(final Intent request) {
        Event event = EventManager.INSTANCE.getEvent(request);
        mDispatcherManager.removeEventDispatcher(event);
    }

    /**
     * Gets a instance of HitoeManager.
     *
     * @return {@link HitoeManager}, or null on error
     */
    private HitoeManager getManager() {
        HitoeDeviceService service = (HitoeDeviceService) getContext();
        if (service == null) {
            return null;
        }
        HitoeApplication app = (HitoeApplication) service.getApplication();
        if (app == null) {
            return null;
        }
        return app.getHitoeManager();
    }
}
