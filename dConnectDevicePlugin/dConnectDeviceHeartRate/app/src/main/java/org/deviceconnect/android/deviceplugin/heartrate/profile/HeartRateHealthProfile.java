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
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
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

    /**
     * Constructor.
     * @param mgr instance of {@link HeartRateManager}
     */
    public HeartRateHealthProfile(HeartRateManager mgr) {
        mgr.setOnHeartRateEventListener(mHeartRateEventListener);
    }

    @Override
    public boolean onGetHeartRate(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else {
            HeartRateData data = getManager().getHeartRateData(serviceId);
            if (data == null) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                setResult(response, DConnectMessage.RESULT_OK);
                setHeartRate(response, data.getHeartRate());
            }
        }
        return true;
    }

    @Override
    public boolean onPutHeartRate(final Intent request, final Intent response,
                                  final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found serviceID:" + serviceId);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found sessionKey:" + sessionKey);
        } else {
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
        }
        return true;
    }

    @Override
    public boolean onDeleteHeartRate(final Intent request, final Intent response,
                                     final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "There is no sessionKey.");
        } else {
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
        HeartRateApplication app = (HeartRateApplication) service.getApplication();
        return app.getHeartRateManager();
    }
}
