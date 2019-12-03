/*
 LinkingDeviceOrientationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.lib.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.LinkingDestroy;
import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingSensorData;
import org.deviceconnect.android.deviceplugin.linking.linking.service.LinkingDeviceService;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventDispatcher;
import org.deviceconnect.android.event.EventDispatcherFactory;
import org.deviceconnect.android.event.EventDispatcherManager;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

public class LinkingDeviceOrientationProfile extends DeviceOrientationProfile implements LinkingDestroy {

    private static final String TAG = "LinkingPlugIn";

    private static final String PARAM_COMPASS = "compass";

    private EventDispatcherManager mDispatcherManager;
    private SensorHolder mSensorHolder;

    public LinkingDeviceOrientationProfile() {
        mDispatcherManager = new EventDispatcherManager();

        addApi(mGetOnDeviceOrientation);
        addApi(mPutOnDeviceOrientation);
        addApi(mDeleteOnDeviceOrientation);
    }

    private final LinkingDeviceManager.OnSensorListener mListener = this::notifyOrientation;

    private final DConnectApi mGetOnDeviceOrientation = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            final LinkingDeviceManager deviceManager = getLinkingDeviceManager();
            getLinkingDeviceManager().enableListenSensor(device, new OnSensorListenerImpl(device) {
                @Override
                public void onCleanup() {
                    deviceManager.disableListenSensor(mDevice, this);
                }

                @Override
                public void onTimeout() {
                    if (mCleanupFlag) {
                        return;
                    }

                    MessageUtils.setTimeoutError(response);
                    sendResponse(response);
                }

                @Override
                public synchronized void onChangeSensor(final LinkingDevice device, final LinkingSensorData sensor) {
                    if (mCleanupFlag || !mDevice.equals(device)) {
                        return;
                    }

                    updateOrientation(mSensorHolder, sensor, 0);

                    if (mSensorHolder.isFlag()) {
                        mSensorHolder.clearFlag();

                        setResult(response, DConnectMessage.RESULT_OK);
                        setOrientation(response, mSensorHolder.getOrientation());
                        sendResponse(response);

                        cleanup();
                    }
                }
            });
            return false;
        }
    };

    private final DConnectApi mPutOnDeviceOrientation = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                getLinkingDeviceManager().enableListenSensor(device, mListener);
                if (mSensorHolder == null) {
                    mSensorHolder = createSensorHolder(device, getInterval(request));
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
    };

    private final DConnectApi mDeleteOnDeviceOrientation = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            LinkingDevice device = getDevice(response);
            if (device == null) {
                return true;
            }

            removeEventDispatcher(request);

            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                if (isEmptyEventList(getServiceID(request))) {
                    getLinkingDeviceManager().disableListenSensor(device, mListener);
                    mSensorHolder = null;
                }
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingDeviceOrientationProfile#destroy: " + getService().getId());
        }
        getLinkingDeviceManager().disableListenSensor(getDevice(), mListener);
        mDispatcherManager.removeAllEventDispatcher();
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
        if (mSensorHolder == null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "holder is not exist.");
            }
            return;
        }

        updateOrientation(mSensorHolder, sensor, 0);

        if (mSensorHolder.isFlag()) {
            mSensorHolder.clearFlag();

            setInterval(mSensorHolder.getOrientation(), mSensorHolder.getInterval());
            String serviceId = device.getBdAddress();
            List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                    PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_ORIENTATION);
            if (events != null && events.size() > 0) {
                for (Event event : events) {
                    Intent intent = EventManager.createEventMessage(event);
                    setOrientation(intent, mSensorHolder.getOrientation());
                    mDispatcherManager.sendEvent(event, intent);
                }
            }
        }
    }

    private LinkingDevice getDevice() {
        return ((LinkingDeviceService) getService()).getLinkingDevice();
    }

    private LinkingDevice getDevice(final Intent response) {
        LinkingDevice device = getDevice();

        if (!device.isConnected()) {
            MessageUtils.setIllegalDeviceStateError(response, "device not connected");
            return null;
        }

        if (!device.isSupportGyro() && !device.isSupportAcceleration() && !device.isSupportCompass()) {
            MessageUtils.setIllegalDeviceStateError(response, "device has not Sensor");
            return null;
        }

        return device;
    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = getLinkingApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingApplication getLinkingApplication() {
        LinkingDevicePluginService service = (LinkingDevicePluginService) getContext();
        return (LinkingApplication) service.getApplication();
    }

    private SensorHolder createSensorHolder(final LinkingDevice device, final int interval) {
        SensorHolder holder = new SensorHolder();
        holder.setSupportGyro(device.isSupportGyro());
        holder.setSupportAcceleration(device.isSupportAcceleration());
        holder.setSupportCompass(device.isSupportCompass());
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

    private abstract class OnSensorListenerImpl extends TimeoutSchedule implements LinkingDeviceManager.OnSensorListener {
        protected SensorHolder mSensorHolder;
        OnSensorListenerImpl(final LinkingDevice device) {
            super(device);
            mSensorHolder = createSensorHolder(device, 0);
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
