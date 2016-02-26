/*
 LinkingDeviceOrientationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
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
            boolean receivedFirstData = false;
            Bundle orientation = new Bundle();

            @Override
            public void onReceiveEvent(Event event, Bundle parameters) {
                if (receivedFirstData) {
                    LinkingSensorData data = parameters.getParcelable(LinkingSensorEvent.EXTRA_SENSOR);
                    if (data == null) {
                        throw new IllegalArgumentException("data must be specified");
                    }
                    updateOrientation(orientation, data, 0);
                } else {
                    //wait 3 seconds for waiting to receive 3 types of values.
                    mScheduleService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            linkingEvent.invalidate();
                            setOrientation(response, orientation);
                            setResult(response, DConnectMessage.RESULT_OK);
                            sendResponse(response);
                        }
                    }, 3, TimeUnit.SECONDS);
                    receivedFirstData = true;
                }
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

            boolean isKeep = false;
            Bundle orientation;
            int INTERVAL = 100;

            @Override
            public void onReceiveEvent(final Event event, Bundle parameters) {
                LinkingSensorData data = parameters.getParcelable(LinkingSensorEvent.EXTRA_SENSOR);
                if (data == null) {
                    throw new IllegalArgumentException("data must be specified");
                }
                if (BuildConfig.DEBUG) {
                    Log.i("LinkingPlugin", "onReceiveEvent : bd:" + data.getBdAddress() + "type:" + data.getType() + " x:" + data.getX() + " y:" + data.getY() + " z:" + data.getZ());
                }

                if (isKeep) {
                    updateOrientation(orientation, data, INTERVAL);
                } else {
                    isKeep = true;
                    orientation = new Bundle();
                    updateOrientation(orientation, data, INTERVAL);
                    mScheduleService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            Bundle eventObj = new Bundle();
                            eventObj.putBundle(PARAM_ORIENTATION, orientation);
                            sendEvent(event, eventObj);
                            isKeep = false;
                        }
                    }, INTERVAL, TimeUnit.MILLISECONDS);
                }
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
        return updateOrientation(orientation, data, interval);
    }

    private Bundle updateOrientation(Bundle orientation, LinkingSensorData data, long interval) {
        switch (data.getType()) {
            case GYRO:
                setGyroValuesToBundle(orientation, data);
                break;
            case ACCELERATION:
                setAccelerationValuesToBundle(orientation, data);
                break;
            case COMPASS:
                setCompassValuesToBundle(orientation, data);
                break;
            default:
                throw new IllegalArgumentException("unknown type");
        }
        setInterval(orientation, interval);
        return orientation;
    }

    private void setGyroValuesToBundle(Bundle bundle, LinkingSensorData data) {
        Bundle gyro = new Bundle();
        setBeta(gyro, data.getX());
        setGamma(gyro, data.getY());
        setAlpha(gyro, data.getZ());
        setRotationRate(bundle, gyro);
    }

    private void setAccelerationValuesToBundle(Bundle bundle, LinkingSensorData data) {
        Bundle acceleration = new Bundle();
        setX(acceleration, data.getX() * 10);
        setY(acceleration, data.getY() * 10);
        setZ(acceleration, data.getZ() * 10);
        setAccelerationIncludingGravity(bundle, acceleration);
    }

    private void setCompassValuesToBundle(Bundle bundle, LinkingSensorData data) {
        Bundle compass = new Bundle();
        setX(compass, data.getX());
        compass.putDouble("beta", data.getX());
        compass.putDouble("gamma", data.getY());
        compass.putDouble("alpha", data.getZ());
        bundle.putBundle("compass", compass);
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
