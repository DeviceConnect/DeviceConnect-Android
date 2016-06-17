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
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ECGProfile;
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
            new HitoeManager.OnHitoeECGEventListener() {
                @Override
                public void onReceivedData(final HitoeDevice device, final HeartRateData data) {
                    notifyECGData(device, data);
                }
            };

    /**
     * Constructor.
     * @param mgr instance of {@link HitoeManager}
     */
    public HitoeECGProfile(final HitoeManager mgr) {
        mgr.setHitoeECGEventListener(mECGEventListener);
    }
    @Override
    public boolean onGetECG(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else {
            HeartRateData data = getManager().getECGData(serviceId);
            if (data == null) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                setResult(response, DConnectMessage.RESULT_OK);
                setECG(response, data.getECG().toBundle());
            }
        }
        return true;
    }

    @Override
    public boolean onPutECG(final Intent request, final Intent response,
                                  final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found serviceID:" + serviceId);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found sessionKey:" + sessionKey);
        } else {
            HeartRateData data = getManager().getECGData(serviceId);
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
    public boolean onDeleteECG(final Intent request, final Intent response,
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
     * Notify the ECG event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of ecg
     */
    private void notifyECGData(final HitoeDevice device, final HeartRateData data) {
        HitoeDeviceService service = (HitoeDeviceService) getContext();
        List<Event> events = EventManager.INSTANCE.getEventList(device.getId(),
                getProfileName(), null, ATTRIBUTE_ON_ECG);
        synchronized (events) {
            for (Event event : events) {
                if (data == null) {
                    break;
                }

                Intent intent = EventManager.createEventMessage(event);

                setECG(intent, data.getECG().toBundle());
                service.sendEvent(intent, event.getAccessToken());
            }
        }
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
