/*
 HeartRateHealthProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.heartrate.HeartRateDeviceService;
import org.deviceconnect.android.deviceplugin.heartrate.HeartRateManager;
import org.deviceconnect.android.deviceplugin.heartrate.ble.BleUtils;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateData;
import org.deviceconnect.android.deviceplugin.heartrate.data.HeartRateDevice;
import org.deviceconnect.android.deviceplugin.heartrate.data.health.TargetDeviceData;
import org.deviceconnect.android.deviceplugin.heartrate.util.RawDataParseUtils;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventDispatcher;
import org.deviceconnect.android.event.EventDispatcherFactory;
import org.deviceconnect.android.event.EventDispatcherManager;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
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
     * Event Dispatcher object.
     */
    private EventDispatcherManager mDispatcherManager;

    /**
     * Implementation of {@link HeartRateManager.OnHeartRateEventListener}.
     */
    private final HeartRateManager.OnHeartRateEventListener mHeartRateEventListener =
        (device, data) -> {
            notifyHeartRateDataOld(device, data);
            notifyHeartRateData(device, data);
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
     * Get Heart rate.
     */
    private final DConnectApi mGetHeart = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_HEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return getHeart(request, response);
        }
    };

    /**
     * Register event heart rate.
     */
    private final DConnectApi mPutHeart = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_HEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            registerHeartEvent(request, response);
            return true;
        }
    };

    /**
     * Unregister event heart rate.
     */
    private final DConnectApi mDeleteHeart = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_HEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            unregisterHeartEvent(request, response);
            return true;
        }
    };

    /**
     * Get Heart rate.
     */
    private final DConnectApi mGetOnHeart = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ONHEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return getHeart(request, response);
        }
    };

    /**
     * Register event heart rate.
     */
    private final DConnectApi mPutOnHeart = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ONHEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            registerHeartEvent(request, response);
            return true;
        }
    };

    /**
     * Unregister event heart rate.
     */
    private final DConnectApi mDeleteOnHeart = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ONHEART;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            unregisterHeartEvent(request, response);
            return true;
        }
    };

    /**
     * Get Heart Rate bundle object.
     * @param data heart rate data
     * @return bundle object
     */
    private Bundle getHeartRateBundle(final HeartRateDevice device, final HeartRateData data) {
        Bundle heart = new Bundle();
        double rri = data.getRRInterval();
        org.deviceconnect.android.deviceplugin.heartrate.data.health.HeartRateData health
                = new org.deviceconnect.android.deviceplugin.heartrate.data.health.HeartRateData();
        health.setRRInterval(RawDataParseUtils.parseRRI((float) rri));
        health.setHeartRate(RawDataParseUtils.parseHeartRate((float) data.getHeartRate()));
        health.setEnergyExpended(RawDataParseUtils.parseEnergyExpended((float) data.getEnergyExpended()));
        HealthProfile.setRate(heart, health.getHeartRate().toBundle());
        if (health.getRRInterval() != null) {
            HealthProfile.setRRI(heart, health.getRRInterval().toBundle());
        }
        if (health.getEnergyExpended() != null) {
            HealthProfile.setEnergyExtended(heart, health.getEnergyExpended().toBundle());
        }
        if (device != null) {
            TargetDeviceData target = RawDataParseUtils.parseDeviceData(device, -1);
            HealthProfile.setDevice(heart, target.toBundle());
        }
        return heart;
    }

    /**
     * Constructor.
     * @param mgr instance of {@link HeartRateManager}
     */
    public HeartRateHealthProfile(final HeartRateManager mgr) {
        mgr.setOnHeartRateEventListener(mHeartRateEventListener);
        mDispatcherManager = new EventDispatcherManager();

        addApi(mGetHeartRateApi);
        addApi(mPutHeartRateApi);
        addApi(mDeleteHeartRateApi);
        addApi(mGetOnHeart);
        addApi(mPutOnHeart);
        addApi(mDeleteOnHeart);
        // GotAPI 1.1
        addApi(mGetHeart);
        addApi(mPutHeart);
        addApi(mDeleteHeart);
    }

    /**
     * Bluetoothの有効設定やパーミッション設定を確認します.
     * @param callback 確認した結果を通知するリスナー
     */
    private void checkBleSettings(final BleUtils.BleRequestCallback callback) {
        HeartRateManager mgr = getManager();
        if (mgr == null) {
            callback.onFail("Failed to initialize a HeartRateManager.");
            return;
        }

        if (!mgr.isEnabledBle()) {
            BleUtils.requestBluetoothEnabled(getContext(), new BleUtils.BleRequestCallback() {
                @Override
                public void onSuccess() {
                    BleUtils.requestBLEPermission(getContext(), callback);
                }
                @Override
                public void onFail(final String deniedPermission) {
                    callback.onFail(deniedPermission);
                }
            });
        } else {
            BleUtils.requestBLEPermission(getContext(), callback);
        }
    }

    /**
     * Register HeartRate Event.
     * @param request Request Message
     * @param response Response Message
     */
    private void registerHeartEvent(Intent request, Intent response) {
        String serviceId = getServiceID(request);
        if (serviceId == null) {
            MessageUtils.setNotFoundServiceError(response, "Not found serviceID");
        } else {
            HeartRateData data = getManager().getHeartRateData(serviceId);
            if (data == null) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                EventError error = EventManager.INSTANCE.addEvent(request);
                if (error == EventError.NONE) {
                    addEventDispatcher(request);
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response);
                }
            }
        }
    }
    /**
     * Get HeartRate Response.
     * @param request Request Message
     * @param response Response Message
     * @return true:sync false:async
     */
    private boolean getHeart(Intent request, Intent response) {
        String serviceId = getServiceID(request);
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
        } else {
            HeartRateData data = getManager().getHeartRateData(serviceId);
            HeartRateDevice device = getManager().getHeartRateDevice(serviceId);
            if (data == null || device == null) {
                MessageUtils.setNotFoundServiceError(response);
            } else {
                setResult(response, DConnectMessage.RESULT_OK);
                setHeart(response, getHeartRateBundle(device, data));
            }
        }
        return true;
    }

    /**
     * Unregister Heart rate Event.
     * @param request Request Message
     * @param response Response Message
     */
    private void unregisterHeartEvent(Intent request, Intent response) {
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
    }
    /**
     * Notify the heart rate event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of heart rate
     */
    private void notifyHeartRateDataOld(final HeartRateDevice device, final HeartRateData data) {
        HeartRateDeviceService service = (HeartRateDeviceService) getContext();
        List<Event> events = EventManager.INSTANCE.getEventList(device.getAddress(),
                getProfileName(), null, ATTRIBUTE_HEART_RATE);
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            setHeartRate(intent, data.getHeartRate());
            service.sendEvent(intent, event.getAccessToken());
        }
    }
    /**
     * Notify the heart rate event to DeviceConnectManager.
     * @param device Identifies the remote device
     * @param data Data of heart rate
     */
    private void notifyHeartRateData(final HeartRateDevice device, final HeartRateData data) {
        List<Event> events = EventManager.INSTANCE.getEventList(device.getAddress(),
                getProfileName(), null, ATTRIBUTE_HEART);
        for (Event event : events) {
            if (data == null) {
                break;
            }

            Intent intent = EventManager.createEventMessage(event);

            setHeart(intent, getHeartRateBundle(device, data));
            // The interval's supported is new health profile only.
            mDispatcherManager.sendEvent(event, intent);
        }

        events = EventManager.INSTANCE.getEventList(device.getAddress(),
                getProfileName(), null, ATTRIBUTE_ONHEART);
        for (Event event : events) {
            if (data == null) {
                break;
            }

            Intent intent = EventManager.createEventMessage(event);

            setHeart(intent, getHeartRateBundle(device, data));
            // The interval's supported is new health profile only.
            mDispatcherManager.sendEvent(event, intent);
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
     * Gets a instance of HeartRateManager.
     *
     * @return {@link HeartRateManager}, or null on error
     */
    private HeartRateManager getManager() {
        HeartRateDeviceService service = (HeartRateDeviceService) getContext();
        if (service == null) {
            return null;
        }
        return service.getManager();
    }
}
