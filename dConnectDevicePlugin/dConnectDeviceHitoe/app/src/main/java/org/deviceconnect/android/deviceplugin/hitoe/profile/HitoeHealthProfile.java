/*
 HitoeHealthProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.hitoe.HitoeApplication;
import org.deviceconnect.android.deviceplugin.hitoe.HitoeDeviceService;
import org.deviceconnect.android.deviceplugin.hitoe.ble.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HealthProfile;
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
            new HitoeManager.OnHitoeHeartRateEventListener() {
                @Override
                public void onReceivedData(final HitoeDevice device, final HeartRateData data) {
                    notifyHeartRateData(device, data);
                }
            };

    /**
     * Constructor.
     * @param mgr instance of {@link HitoeManager}
     */
    public HitoeHealthProfile(final HitoeManager mgr) {
        mgr.setHitoeHeartRateEventListener(mHeartRateEventListener);
    }
    @Override
    public boolean onGetHeart(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else {
            HeartRateData data = getManager().getHeartRateData(serviceId);
            if (data == null) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                setResult(response, DConnectMessage.RESULT_OK);
                setHeart(response, getHeartRateBundle(data));
            }
        }
        return true;
    }

    @Override
    public boolean onPutHeart(final Intent request, final Intent response,
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
    public boolean onDeleteHeart(final Intent request, final Intent response,
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
    private void notifyHeartRateData(final HitoeDevice device, final HeartRateData data) {
        HitoeDeviceService service = (HitoeDeviceService) getContext();
        List<Event> events = EventManager.INSTANCE.getEventList(device.getId(),
                getProfileName(), null, ATTRIBUTE_HEART);
        synchronized (events) {
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);

                setHeart(intent, getHeartRateBundle(data));
                service.sendEvent(intent, event.getAccessToken());
            }
        }
    }

    @NonNull
    private Bundle getHeartRateBundle(HeartRateData data) {
        Bundle heart = new Bundle();
        HealthProfile.setRate(heart, data.getHeartRate().toBundle());
        if (data.getRRInterval() != null) {
            HealthProfile.setRRI(heart, data.getRRInterval().toBundle());
        }
        if (data.getEnergyExpended() != null) {
            HealthProfile.setEnergyExtended(heart, data.getEnergyExpended().toBundle());
        }
        return heart;
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
