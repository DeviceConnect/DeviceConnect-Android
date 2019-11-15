/*
 HitoeHealthProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.HitoeDeviceService;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventDispatcher;
import org.deviceconnect.android.event.EventDispatcherFactory;
import org.deviceconnect.android.event.EventDispatcherManager;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HealthProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * Implement HealthProfile.
 * @author NTT DOCOMO, INC.
 */
public class HitoeHealthProfile extends HealthProfile {

    /**
     * Implementation of {@link HitoeManager.OnHitoeHeartRateEventListener}.
     */
    private final HitoeManager.OnHitoeHeartRateEventListener mHeartRateEventListener =
            this::notifyHeartRateData;

    /**
     * Event Dispatcher object.
     */
    private EventDispatcherManager mDispatcherManager;

    /**
     * Constructor.
     * @param mgr instance of {@link HitoeManager}
     */
    public HitoeHealthProfile(final HitoeManager mgr) {
        mgr.setHitoeHeartRateEventListener(mHeartRateEventListener);
        mDispatcherManager = new EventDispatcherManager();
        addApi(mGetHeart);
        addApi(mPutHeart);
        addApi(mDeleteHeart);
        addApi(mGetOnHeart);
        addApi(mPutOnHeart);
        addApi(mDeleteOnHeart);
    }

    /**
     * Get Heart rate.
     */
    private final DConnectApi mGetHeart = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_HEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return getHeart(request, response);
        }
    };


    /**
     * Register event heartrate.
     */
    private final DConnectApi mPutHeart = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_HEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return registerHeartEvent(request, response);
        }
    };


    /**
     * Unregister event heartrate.
     */
    private final DConnectApi mDeleteHeart = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_HEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            unregisterHeartEvent(request, response);
            return true;
        }
    };

    /**
     * Get Heart rate.
     */
    private final DConnectApi mGetOnHeart = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ONHEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return getHeart(request, response);
        }
    };


    /**
     * Register event heartrate.
     */
    private final DConnectApi mPutOnHeart = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ONHEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return registerHeartEvent(request, response);
        }
    };


    /**
     * Unregister event heartrate.
     */
    private final DConnectApi mDeleteOnHeart = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ONHEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            unregisterHeartEvent(request, response);
            return true;
        }
    };


    /**
     * Get HeartRate Response.
     * @param request Request Message
     * @param response Response Message
     * @return true:sync false:async
     */
    private boolean getHeart(Intent request, Intent response) {
        String serviceId = getServiceID(request);
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else {
            HitoeManager mgr = getManager();
            if (mgr == null) {
                MessageUtils.setNotFoundServiceError(response);
                return true;
            }
            HeartRateData data = mgr.getHeartRateData(serviceId);
            if (data == null) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                setResult(response, DConnectMessage.RESULT_OK);
                setHeart(response, getHeartRateBundle(data));
            }
        }
        return true;
    }

    /**
     * Register Heartrate Event.
     * @param request Request Message
     * @param response Response Message
     */
    private boolean registerHeartEvent(Intent request, Intent response) {
        String serviceId = getServiceID(request);
        if (serviceId == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found serviceID");
        } else {
            HitoeManager mgr = getManager();
            if (mgr == null) {
                MessageUtils.setNotFoundServiceError(response);
                return true;
            }

            HeartRateData data = mgr.getHeartRateData(serviceId);
            if (data == null) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                EventError error = EventManager.INSTANCE.addEvent(request);
                if (error == EventError.NONE) {
                    mgr.setHitoeHeartRateEventListener(mHeartRateEventListener);
                    addEventDispatcher(request);
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response);
                }
            }
        }
        return true;
    }
    /**
     * Unregister Heartrate Event.
     * @param request Request Message
     * @param response Response Message
     */
    private void unregisterHeartEvent(Intent request, Intent response) {
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
    }

    /**
     * Notify the heart rate event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of heart rate
     */
    private void notifyHeartRateData(final HitoeDevice device, final HeartRateData data) {
        List<Event> events = EventManager.INSTANCE.getEventList(device.getId(),
                getProfileName(), null, ATTRIBUTE_HEART);
        synchronized (events) {
            for (Event event : events) {
                if (data == null) {
                    break;
                }

                Intent intent = EventManager.createEventMessage(event);

                setHeart(intent, getHeartRateBundle(data));
                mDispatcherManager.sendEvent(event, intent);
            }
        }
        events = EventManager.INSTANCE.getEventList(device.getId(),
                getProfileName(), null, ATTRIBUTE_ONHEART);
        synchronized (events) {
            for (Event event : events) {
                if (data == null) {
                    break;
                }

                Intent intent = EventManager.createEventMessage(event);

                setHeart(intent, getHeartRateBundle(data));
                mDispatcherManager.sendEvent(event, intent);
            }
        }
    }

    /**
     * Get Heartrate bundle object.
     * @param data heartrate data
     * @return bundle object
     */
    private Bundle getHeartRateBundle(final HeartRateData data) {
        Bundle heart = new Bundle();
        HealthProfile.setRate(heart, data.getHeartRate().toBundle());
        if (data.getRRInterval() != null) {
            HealthProfile.setRRI(heart, data.getRRInterval().toBundle());
        }
        if (data.getEnergyExpended() != null) {
            HealthProfile.setEnergyExtended(heart, data.getEnergyExpended().toBundle());
        }
        if (data.getDevice() != null) {
            HealthProfile.setDevice(heart, data.getDevice().toBundle());
        }
        return heart;
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
