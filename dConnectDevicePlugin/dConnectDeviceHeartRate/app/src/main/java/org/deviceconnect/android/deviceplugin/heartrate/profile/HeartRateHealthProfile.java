/*
 HeartRateHealthProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.heartrate.HeartRateApplication;
import org.deviceconnect.android.deviceplugin.heartrate.HeartRateDeviceService;
import org.deviceconnect.android.deviceplugin.heartrate.HeartRateManager;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
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
public class HeartRateHealthProfile extends HealthProfile {
    /**
     * Implementation of {@link HeartRateManager.OnHeartRateEventListener}.
     */
    private final HeartRateManager.OnHeartRateEventListener mHeartRateEventListener =
        new HeartRateManager.OnHeartRateEventListener() {
            @Override
            public void onReceivedData(final HeartRateDevice device, final HeartRateData data) {
                notifyHeartRateData(device, data);
            }
        };

    private final DConnectApi mGetHeartRateApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_HEART_RATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            HeartRateData data = getManager().getHeartRateData(serviceId);
            if (data == null) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                setResult(response, DConnectMessage.RESULT_OK);
                setHeartRate(response, data.getHeartRate());
            }
            return true;
        }
    };

    private final DConnectApi mPutHeartRateApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_HEART_RATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            HeartRateData data = getManager().getHeartRateData(serviceId);
            if (data == null) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                EventError error = EventManager.INSTANCE.addEvent(request);
                if (error == EventError.NONE) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response);
                }
            }
            return true;
        }
    };

    private final DConnectApi mDeleteHeartRateApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_HEART_RATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
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
            return true;
        }
    };

    /**
     * Constructor.
     * @param mgr instance of {@link HeartRateManager}
     */
    public HeartRateHealthProfile(final HeartRateManager mgr) {
        mgr.setOnHeartRateEventListener(mHeartRateEventListener);

        addApi(mGetHeartRateApi);
        addApi(mPutHeartRateApi);
        addApi(mDeleteHeartRateApi);
    }

    /**
     * Notify the heart rate event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of heart rate
     */
    private void notifyHeartRateData(final HeartRateDevice device, final HeartRateData data) {
        HeartRateDeviceService service = (HeartRateDeviceService) getContext();
        List<Event> events = EventManager.INSTANCE.getEventList(device.getAddress(),
                getProfileName(), null, ATTRIBUTE_HEART_RATE);
        synchronized (events) {
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);
                setHeartRate(intent, data.getHeartRate());
                service.sendEvent(intent, event.getAccessToken());
            }
        }
    }

    /**
     * Gets a instance of HeartRateManager.
     *
     * @return {@link HeartRateManager}, or null on error
     */
    private HeartRateManager getManager() {
        HeartRateDeviceService service = (HeartRateDeviceService) getContext();
        if (service == null) {
            return null;
        }
        HeartRateApplication app = (HeartRateApplication) service.getApplication();
        if (app == null) {
            return null;
        }
        return app.getHeartRateManager();
    }
}
