/*
 HitoeStressEstimationProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.HitoeDeviceService;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.data.StressEstimationData;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventDispatcher;
import org.deviceconnect.android.event.EventDispatcherFactory;
import org.deviceconnect.android.event.EventDispatcherManager;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.StressEstimationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * Implement StressEstimationProfile.
 * @author NTT DOCOMO, INC.
 */
public class HitoeStressEstimationProfile extends StressEstimationProfile {

    /**
     * Implementation of {@link HitoeManager.OnHitoeStressEstimationEventListener}.
     */
    private final HitoeManager.OnHitoeStressEstimationEventListener mStressEstimationEventListener =
            new HitoeManager.OnHitoeStressEstimationEventListener() {
                @Override
                public void onReceivedData(final HitoeDevice device, final StressEstimationData data) {
                    notifyStressEstimationData(device, data);
                }
            };
    /**
     * Event Dispatcher object.
     */
    private EventDispatcherManager mDispatcherManager;

    /**
     * Constructor.
     * @param mgr instance of {@link HitoeManager}
     */
    public HitoeStressEstimationProfile(final HitoeManager mgr) {
        mgr.setHitoeStressEstimationEventListener(mStressEstimationEventListener);
        mDispatcherManager = new EventDispatcherManager();
        addApi(mGetOnStress);
        addApi(mPutOnStress);
        addApi(mDeleteOnStress);
    }

    /**
     * Get Stress estimation.
     */
    private final DConnectApi mGetOnStress = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_STRESS_ESTIMATION;
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
                StressEstimationData data = mgr.getStressEstimationData(serviceId);
                if (data == null) {
                    MessageUtils.setNotFoundServiceError(response);
                } else {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setStress(response, data.toBundle());
                }
            }
            return true;
        }
    };

    /**
     * Register event stress estimation.
     */
    private final DConnectApi mPutOnStress = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_STRESS_ESTIMATION;
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
                StressEstimationData data = mgr.getStressEstimationData(serviceId);
                if (data == null) {
                    MessageUtils.setNotFoundServiceError(response);
                } else {
                    EventError error = EventManager.INSTANCE.addEvent(request);
                    if (error == EventError.NONE) {
                        mgr.setHitoeStressEstimationEventListener(mStressEstimationEventListener);
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
     * Unregister event stress estimation.
     */
    private final DConnectApi mDeleteOnStress = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_STRESS_ESTIMATION;
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
    private void notifyStressEstimationData(final HitoeDevice device, final StressEstimationData data) {
        List<Event> events = EventManager.INSTANCE.getEventList(device.getId(),
                getProfileName(), null, ATTRIBUTE_ON_STRESS_ESTIMATION);
        synchronized (events) {
            for (Event event : events) {
                if (data == null) {
                    break;
                }
                Intent intent = EventManager.createEventMessage(event);

                setStress(intent, data.toBundle());
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
