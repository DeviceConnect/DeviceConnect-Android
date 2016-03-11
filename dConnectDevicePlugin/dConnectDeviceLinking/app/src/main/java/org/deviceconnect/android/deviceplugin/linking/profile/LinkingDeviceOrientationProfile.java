/*
 LinkingDeviceOrientationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEvent;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEventListener;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingEventManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingSensorData;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingSensorEvent;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LinkingDeviceOrientationProfile extends DeviceOrientationProfile {

    private static final int TIMEOUT = 30;//second
    private ScheduledExecutorService mScheduleService = Executors.newScheduledThreadPool(4);

    @Override
    protected boolean onGetOnDeviceOrientation(Intent request, final Intent response, String serviceId) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        final LinkingEvent linkingEvent = new LinkingSensorEvent(getContext().getApplicationContext(), device);
        linkingEvent.setEventInfo(request);
        linkingEvent.setLinkingEventListener(new LinkingEventListener() {
            @Override
            public void onReceiveEvent(Event event, Bundle parameters) {
                linkingEvent.invalidate();
                LinkingSensorData data = parameters.getParcelable(LinkingSensorEvent.EXTRA_SENSOR);
                if (data == null) {
                    throw new IllegalArgumentException("data must be specified");
                }
                setOrientation(response, createOrientation(data, 0));
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }
        });
        linkingEvent.listen();
        mScheduleService.schedule(new Runnable() {
            @Override
            public void run() {
                linkingEvent.invalidate();
                MessageUtils.setTimeoutError(response);
                sendResponse(response);
            }
        }, TIMEOUT, TimeUnit.SECONDS);
        return false;
    }

    @Override
    protected boolean onPutOnDeviceOrientation(Intent request, Intent response, String serviceId, String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }
        final LinkingEvent linkingEvent = new LinkingSensorEvent(getContext().getApplicationContext(), device);
        linkingEvent.setEventInfo(request);
        linkingEvent.setLinkingEventListener(new LinkingEventListener() {
            private long mReceiveSensorDataLastTime = 0;

            @Override
            public void onReceiveEvent(Event event, Bundle parameters) {
                LinkingSensorData data = parameters.getParcelable(LinkingSensorEvent.EXTRA_SENSOR);
                if (data == null) {
                    throw new IllegalArgumentException("data must be specified");
                }
                long interval = mReceiveSensorDataLastTime == 0 ? 0 : System.currentTimeMillis() - mReceiveSensorDataLastTime;

                Bundle orientation = new Bundle();
                orientation.putBundle(PARAM_ORIENTATION, createOrientation(data, interval));
                sendEvent(event, orientation);
                mReceiveSensorDataLastTime = System.currentTimeMillis();
            }
        });

        LinkingEventManager manager = LinkingEventManager.getInstance();
        manager.add(linkingEvent);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onDeleteOnDeviceOrientation(Intent request, Intent response, String serviceId, String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        LinkingEventManager.getInstance().remove(request);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private Bundle createOrientation(LinkingSensorData data, long interval) {
        Bundle orientation = new Bundle();
        switch (data.getType()) {
            case GYRO:
                Bundle gyro = new Bundle();
                setBeta(gyro, data.getX());
                setGamma(gyro, data.getY());
                setAlpha(gyro, data.getZ());
                setRotationRate(orientation, gyro);
                break;
            case ACCELERATION:
                Bundle acceleration = new Bundle();
                setX(acceleration, data.getX());
                setY(acceleration, data.getY());
                setZ(acceleration, data.getZ());
                setAcceleration(orientation, null);
                break;
            case COMPASS:
                //TODO:undefined values on profile.
                Bundle compass = new Bundle();
                setX(compass, data.getX());
                compass.putDouble("beta", data.getX());
                compass.putDouble("gamma", data.getY());
                compass.putDouble("alpha", data.getZ());
                orientation.putBundle("compass", compass);
                break;
            default:
                throw new IllegalArgumentException("unknown type");
        }
//        setAccelerationIncludingGravity(orientation, null);TODO: The data can be taken from Linking Device?
        setInterval(orientation, interval);
        return orientation;
    }

    private LinkingDevice getDevice(String serviceId, Intent response) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return null;
        }
        LinkingDevice device = LinkingUtil.getLinkingDevice(getContext(), serviceId);
        if (device == null) {
            MessageUtils.setIllegalDeviceStateError(response, "device not found");
            return null;
        }
        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }
        if (!LinkingUtil.hasSensor(device)) {
            MessageUtils.setIllegalDeviceStateError(response, "device has not sensor");
            return null;
        }
        return device;
    }

}
