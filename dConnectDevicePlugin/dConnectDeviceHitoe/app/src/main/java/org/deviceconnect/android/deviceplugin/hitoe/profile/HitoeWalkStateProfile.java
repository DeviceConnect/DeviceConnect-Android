/*
 HitoeWalkStateProfile
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
import org.deviceconnect.android.deviceplugin.hitoe.data.WalkStateData;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.WalkStateProfile;
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
            new HitoeManager.OnHitoeWalkStateEventListener() {
                @Override
                public void onReceivedData(final HitoeDevice device, final WalkStateData data) {
                    notifyWalkStateData(device, data);
                }
            };

    /**
     * Constructor.
     * @param mgr instance of {@link HitoeManager}
     */
    public HitoeWalkStateProfile(final HitoeManager mgr) {
        mgr.setHitoeWalkStateEventListener(mWalkStateEventListener);
    }
    @Override
    public boolean onGetWalkState(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else {
            WalkStateData data = getManager().getWalkStateData(serviceId);
            if (data == null) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                setResult(response, DConnectMessage.RESULT_OK);
                setWalk(response, data.toBundle());
            }
        }
        return true;
    }

    @Override
    public boolean onPutWalkState(final Intent request, final Intent response,
                                  final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found serviceID:" + serviceId);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found sessionKey:" + sessionKey);
        } else {
            WalkStateData data = getManager().getWalkStateData(serviceId);
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
    public boolean onDeleteWalkState(final Intent request, final Intent response,
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
     * Notify the stress estimation event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of Stress Estimation
     */
    private void notifyWalkStateData(final HitoeDevice device, final WalkStateData data) {
        HitoeDeviceService service = (HitoeDeviceService) getContext();
        List<Event> events = EventManager.INSTANCE.getEventList(device.getId(),
                getProfileName(), null, ATTRIBUTE_ON_WALK_STATE);
        synchronized (events) {
            for (Event event : events) {
                if (data == null) {
                    break;
                }

                Intent intent = EventManager.createEventMessage(event);

                setWalk(intent, data.toBundle());
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
