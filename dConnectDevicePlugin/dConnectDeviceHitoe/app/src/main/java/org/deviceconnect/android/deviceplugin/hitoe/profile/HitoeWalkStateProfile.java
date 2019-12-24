/*
 HitoeWalkStateProfile
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.HitoeDeviceService;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.data.WalkStateData;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventDispatcher;
import org.deviceconnect.android.event.EventDispatcherFactory;
import org.deviceconnect.android.event.EventDispatcherManager;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.WalkStateProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * Implement WalkStateProfile.
 * @author NTT DOCOMO, INC.
 */
public class HitoeWalkStateProfile extends WalkStateProfile {

    /**
     * Implementation of {@link HitoeManager.OnHitoeWalkStateEventListener}.
     */
    private final HitoeManager.OnHitoeWalkStateEventListener mWalkStateEventListener =
            this::notifyWalkStateData;
    /**
     * Event Dispatcher object.
     */
    private EventDispatcherManager mDispatcherManager;


    /**
     * Constructor.
     * @param mgr instance of {@link HitoeManager}
     */
    public HitoeWalkStateProfile(final HitoeManager mgr) {
        mgr.setHitoeWalkStateEventListener(mWalkStateEventListener);
        mDispatcherManager = new EventDispatcherManager();
        addApi(mGetOnWalk);
        addApi(mPutOnWalk);
        addApi(mDeleteOnWalk);

    }

    /**
     * Get walk state.
     */
    private final DConnectApi mGetOnWalk = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_WALK_STATE;
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
                WalkStateData data = mgr.getWalkStateData(serviceId);
                if (data == null) {
                    MessageUtils.setNotFoundServiceError(response);
                } else {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setWalk(response, data.toBundle());
                }
            }
            return true;
        }
    };

    /**
     * Register event walk state.
     */
    private final DConnectApi mPutOnWalk = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_WALK_STATE;
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
                WalkStateData data = mgr.getWalkStateData(serviceId);
                if (data == null) {
                    MessageUtils.setNotFoundServiceError(response);
                } else {
                    EventError error = EventManager.INSTANCE.addEvent(request);
                    if (error == EventError.NONE) {
                        mgr.setHitoeWalkStateEventListener(mWalkStateEventListener);
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
     * Unregister event walk state.
     */
    private final DConnectApi mDeleteOnWalk = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_WALK_STATE;
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
     * Notify the stress estimation event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of Stress Estimation
     */
    private void notifyWalkStateData(final HitoeDevice device, final WalkStateData data) {
        List<Event> events = EventManager.INSTANCE.getEventList(device.getId(),
                getProfileName(), null, ATTRIBUTE_ON_WALK_STATE);
        synchronized (events) {
            for (Event event : events) {
                if (data == null) {
                    break;
                }

                Intent intent = EventManager.createEventMessage(event);
                setWalk(intent, data.toBundle());
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
