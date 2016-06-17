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
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDeviceService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingSensorData;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventDispatcher;
import org.deviceconnect.android.event.EventDispatcherFactory;
import org.deviceconnect.android.event.EventDispatcherManager;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LinkingDeviceOrientationProfile extends DeviceOrientationProfile {

    private static final String TAG = "LinkingPlugIn";

    private static final String PARAM_COMPASS = "compass";

    private static final int TIMEOUT = 30;

    private ConcurrentHashMap<String, SensorHolder> mSensorHolderMap = new ConcurrentHashMap<>();

    private EventDispatcherManager mDispatcherManager;

    public LinkingDeviceOrientationProfile(final DConnectMessageService service) {
        LinkingApplication app = (LinkingApplication) service.getApplication();
        LinkingDeviceManager deviceManager = app.getLinkingDeviceManager();
        deviceManager.addSensorListener(new LinkingDeviceManager.SensorListener() {
            @Override
            public void onChangeSensor(final LinkingDevice device, final LinkingSensorData sensor) {
                notifyOrientation(device, sensor);
            }
        });

        mDispatcherManager = new EventDispatcherManager();
    }

    @Override
    protected boolean onGetOnDeviceOrientation(final Intent request, final Intent response, final String serviceId) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }

        final LinkingDeviceManager deviceManager = getLinkingDeviceManager();
        deviceManager.addSensorListener(new SensorListenerImpl(device) {

            private boolean mDestroyFlag;

            private void destroy() {
                if (mDestroyFlag) {
                    return;
                }
                mDestroyFlag = true;

                if (isEmptyEventList(mDevice.getBdAddress())) {
                    getLinkingDeviceManager().stopSensor(mDevice);
                }
                deviceManager.removeSensorListener(this);

                mScheduledFuture.cancel(false);
                mExecutorService.shutdown();
            }

            @Override
            public synchronized void run() {
                if (mDestroyFlag) {
                    return;
                }

                MessageUtils.setTimeoutError(response);
                sendResponse(response);
                destroy();
            }

            @Override
            public synchronized void onChangeSensor(final LinkingDevice device, final LinkingSensorData sensor) {
                if (mDestroyFlag) {
                    return;
                }

                if (!mDevice.equals(device)) {
                    return;
                }

                updateOrientation(mSensorHolder, sensor, 0);

                if (mSensorHolder.isFlag()) {
                    mSensorHolder.clearFlag();

                    setResult(response, DConnectMessage.RESULT_OK);
                    setOrientation(response, mSensorHolder.getOrientation());
                    sendResponse(response);

                    destroy();
                }
            }
        });
        getLinkingDeviceManager().startSensor(device);

        return false;
    }

    @Override
    protected boolean onPutOnDeviceOrientation(final Intent request, final Intent response,
                                               final String serviceId, final String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }

        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            if (!getLinkingDeviceManager().isStartSensor(device)) {
                getLinkingDeviceManager().startSensor(device, getInterval(request));
                mSensorHolderMap.put(device.getBdAddress(), createSensorHolder(device, getInterval(request)));
            }
            addEventDispatcher(request);
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDeviceOrientation(final Intent request, final Intent response,
                                                  final String serviceId, final String sessionKey) {
        LinkingDevice device = getDevice(serviceId, response);
        if (device == null) {
            return true;
        }

        removeEventDispatcher(request);

        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            if (isEmptyEventList(serviceId)) {
                getLinkingDeviceManager().stopSensor(device);
                mSensorHolderMap.remove(device.getBdAddress());
            }
            setResult(response, DConnectMessage.RESULT_OK);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    private boolean isEmptyEventList(final String serviceId) {
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_ORIENTATION);
        return events.isEmpty();
    }

    private void addEventDispatcher(final Intent request) {
        Event event = EventManager.INSTANCE.getEvent(request);
        EventDispatcher dispatcher = EventDispatcherFactory.createEventDispatcher(
                (DConnectMessageService)getContext(), request);
        mDispatcherManager.addEventDispatcher(event, dispatcher);
    }

    private void removeEventDispatcher(final Intent request) {
        Event event = EventManager.INSTANCE.getEvent(request);
        mDispatcherManager.removeEventDispatcher(event);
    }

    private void updateOrientation(final SensorHolder holder, final LinkingSensorData data, final long interval) {
        switch (data.getType()) {
            case GYRO:
                setGyroValuesToBundle(holder.getOrientation(), data);
                break;
            case ACCELERATION:
                setAccelerationValuesToBundle(holder.getOrientation(), data);
                break;
            case COMPASS:
                setCompassValuesToBundle(holder.getOrientation(), data);
                break;
            default:
                throw new IllegalArgumentException("unknown type");
        }
        setInterval(holder.getOrientation(), interval);
        holder.setFlag(data);
    }

    private void setGyroValuesToBundle(final Bundle bundle, final LinkingSensorData data) {
        Bundle gyro = new Bundle();
        setBeta(gyro, data.getX());
        setGamma(gyro, data.getY());
        setAlpha(gyro, data.getZ());
        setRotationRate(bundle, gyro);
    }

    private void setAccelerationValuesToBundle(final Bundle bundle, final LinkingSensorData data) {
        Bundle acceleration = new Bundle();
        setX(acceleration, data.getX() * 10);
        setY(acceleration, data.getY() * 10);
        setZ(acceleration, data.getZ() * 10);
        setAccelerationIncludingGravity(bundle, acceleration);
    }

    private void setCompassValuesToBundle(final Bundle bundle, final LinkingSensorData data) {
        Bundle compass = new Bundle();
        setX(compass, data.getX());
        compass.putDouble("beta", data.getX());
        compass.putDouble("gamma", data.getY());
        compass.putDouble("alpha", data.getZ());
        bundle.putBundle(PARAM_COMPASS, compass);
    }

    private void notifyOrientation(final LinkingDevice device, final LinkingSensorData sensor) {
        final SensorHolder holder = mSensorHolderMap.get(device.getBdAddress());
        if (holder == null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "holder is not exist.");
            }
            return;
        }

        updateOrientation(holder, sensor, 0);

        if (holder.isFlag()) {
            holder.clearFlag();

            setInterval(holder.getOrientation(), holder.getInterval());

            String serviceId = device.getBdAddress();
            List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                    PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_ORIENTATION);
            if (events != null && events.size() > 0) {
                synchronized (events) {
                    for (Event event : events) {
                        Intent intent = EventManager.createEventMessage(event);
                        setOrientation(intent, holder.getOrientation());
                        mDispatcherManager.sendEvent(event, intent);
                    }
                }
            }
        }
    }

    private LinkingDevice getDevice(final String serviceId, final Intent response) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return null;
        }
        LinkingDevice device = getLinkingDeviceManager().findDeviceByBdAddress(serviceId);
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

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDeviceService service = (LinkingDeviceService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private SensorHolder createSensorHolder(final LinkingDevice device, final int interval) {
        SensorHolder holder = new SensorHolder();
        holder.setSupportGyro(device.isGyro());
        holder.setSupportAcceleration(device.isAcceleration());
        holder.setSupportCompass(device.isCompass());
        holder.setTime(System.currentTimeMillis());
        holder.setInterval(interval);
        return holder;
    }

    private static int getInterval(final Intent request) {
        try {
            String interval = request.getStringExtra("interval");
            return Integer.parseInt(interval);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private abstract class SensorListenerImpl implements Runnable, LinkingDeviceManager.SensorListener {
        protected LinkingDevice mDevice;
        protected SensorHolder mSensorHolder;
        protected ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
        protected ScheduledFuture<?> mScheduledFuture;
        SensorListenerImpl(final LinkingDevice device) {
            mDevice = device;
            mSensorHolder = createSensorHolder(device, 0);
            mScheduledFuture = mExecutorService.schedule(this, TIMEOUT, TimeUnit.SECONDS);
        }
    }

    private class SensorHolder {
        private long mTime = System.currentTimeMillis();
        private Bundle mOrientation = new Bundle();
        private int mFlag;
        private int mInterval;

        private boolean mSupportGyro;
        private boolean mSupportAcceleration;
        private boolean mSupportCompass;

        public Bundle getOrientation() {
            return mOrientation;
        }

        public void setTime(long time) {
            mTime = time;
        }

        public void setInterval(int interval) {
            mInterval = interval;
        }

        public long getInterval() {
            if (mInterval <= 0) {
                long interval = System.currentTimeMillis() - mTime;
                mTime = System.currentTimeMillis();
                return interval;
            } else {
                return mInterval;
            }
        }

        public void setSupportGyro(boolean supportGyro) {
            mSupportGyro = supportGyro;
        }

        public void setSupportAcceleration(boolean supportAcceleration) {
            mSupportAcceleration = supportAcceleration;
        }

        public void setSupportCompass(boolean supportCompass) {
            mSupportCompass = supportCompass;
        }

        public void clearFlag() {
            mFlag = 0;
        }

        public boolean isFlag() {
            int f = 0;
            if (mSupportGyro) {
                f |= LinkingDevice.GYRO;
            }
            if (mSupportAcceleration) {
                f |= LinkingDevice.ACCELERATION;
            }
            if (mSupportCompass) {
                f |= LinkingDevice.COMPASS;
            }
            return mFlag == f;
        }

        private void setFlag(final LinkingSensorData sensor) {
            switch (sensor.getType()) {
                case GYRO:
                    mFlag |= LinkingDevice.GYRO;
                    break;
                case ACCELERATION:
                    mFlag |= LinkingDevice.ACCELERATION;
                    break;
                case COMPASS:
                    mFlag |= LinkingDevice.COMPASS;
                    break;
                default:
                    throw new IllegalArgumentException("unknown type");
            }
        }
    }
}
