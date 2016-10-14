/*
 HitoePoseEstimationProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.HitoeDeviceService;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.PoseEstimationData;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventDispatcher;
import org.deviceconnect.android.event.EventDispatcherFactory;
import org.deviceconnect.android.event.EventDispatcherManager;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.PoseEstimationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * Implement PoseEstimationProfile.
 * @author NTT DOCOMO, INC.
 */
public class HitoePoseEstimationProfile extends PoseEstimationProfile {

    /**
     * Implementation of {@link HitoeManager.OnHitoePoseEstimationEventListener}.
     */
    private final HitoeManager.OnHitoePoseEstimationEventListener mPoseEstimationEventListener =
            new HitoeManager.OnHitoePoseEstimationEventListener() {
                @Override
                public void onReceivedData(final HitoeDevice device, final PoseEstimationData data) {
                    notifyPoseEstimationData(device, data);
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
    public HitoePoseEstimationProfile(final HitoeManager mgr) {
        mgr.setHitoePoseEstimationEventListener(mPoseEstimationEventListener);
        mDispatcherManager = new EventDispatcherManager();
        addApi(mGetOnPose);
        addApi(mPutOnPose);
        addApi(mDeleteOnPose);
    }

    /**
     * Get Pose estimation.
     */
    private final DConnectApi mGetOnPose = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_POSE_ESTIMATION;
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

                PoseEstimationData data = mgr.getPoseEstimationData(serviceId);
                if (data == null) {
                    MessageUtils.setNotFoundServiceError(response);
                } else {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setPose(response, data.toBundle());
                }
            }
            return true;
        }
    };

    /**
     * Register event pose estimation.
     */
    private final DConnectApi mPutOnPose = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_POSE_ESTIMATION;
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
                PoseEstimationData data = mgr.getPoseEstimationData(serviceId);
                if (data == null) {
                    MessageUtils.setNotFoundServiceError(response);
                } else {
                    EventError error = EventManager.INSTANCE.addEvent(request);
                    if (error == EventError.NONE) {
                        mgr.setHitoePoseEstimationEventListener(mPoseEstimationEventListener);
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
     * Unregister event pose estimation.
     */
    private final DConnectApi mDeleteOnPose = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_POSE_ESTIMATION;
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
    private void notifyPoseEstimationData(final HitoeDevice device, final PoseEstimationData data) {
        List<Event> events = EventManager.INSTANCE.getEventList(device.getId(),
                getProfileName(), null, ATTRIBUTE_ON_POSE_ESTIMATION);
        synchronized (events) {
            for (Event event : events) {
                if (data == null) {
                    break;
                }

                Intent intent = EventManager.createEventMessage(event);

                setPose(intent, data.toBundle());
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
