/*
 PebbleBatteryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.profile;

import android.content.Intent;
import android.os.Bundle;

import com.getpebble.android.kit.util.PebbleDictionary;

import org.deviceconnect.android.deviceplugin.pebble.PebbleDeviceService;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager.OnReceivedEventListener;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager.OnSendCommandListener;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;


/**
 * Pebble用 Batteryプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class PebbleBatteryProfile extends BatteryProfile {
    /** パーセント値にする時の定数. */
    private static final double TO_PERCENT = 100.0;
    /** sessionKeyが設定されていないときのエラーメッセージ. */
    private static final String ERROR_MESSAGE = "sessionKey must be specified.";
    /**
     * コンストラクタ.
     * @param service Pebble デバイスサービス
     */
    public PebbleBatteryProfile(final PebbleDeviceService service) {
        service.getPebbleManager().addEventListener(PebbleManager.PROFILE_BATTERY, new OnReceivedEventListener() {
            @Override
            public void onReceivedEvent(final PebbleDictionary dic) {
                Long attribute = dic.getInteger(PebbleManager.KEY_ATTRIBUTE);
                if (attribute == null) {
                    return;
                }

                switch (attribute.intValue()) {
                case PebbleManager.BATTERY_ATTRIBUTE_ON_BATTERY_CHANGE:
                    sendOnBatteryChange(dic);
                    break;
                case PebbleManager.BATTERY_ATTRIBUTE_ON_CHARGING_CHANGE:
                    sendOnChargingChange(dic);
                    break;
                default:
                    break;
                }
            }
        });
    }

    @Override
    protected boolean onGetAll(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else {
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_BATTERY);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.BATTERY_ATTRIBUTE_ALL);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_GET);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        errorPebbleSideApplicationNotFound(response);
                    } else {
                        Long level = dic.getInteger(PebbleManager.KEY_PARAM_BATTERY_LEVEL);
                        Long charging = dic.getInteger(PebbleManager.KEY_PARAM_BATTERY_CHARGING);
                        if (charging == null || level == null) {
                            MessageUtils.setUnknownError(response);
                        } else {
                            double l = level.intValue() / TO_PERCENT;
                            boolean isCharging = (charging.intValue() == PebbleManager.BATTERY_CHARGING_ON);
                            setResult(response, DConnectMessage.RESULT_OK);
                            setCharging(response, isCharging);
                            setLevel(response, l);
                        }
                    }
                    sendResponse(response);
                }
            });
            // レスポンスを非同期で返却するので、falseを返す
            return false;
        }
    }

    @Override
    protected boolean onGetLevel(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else {
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_BATTERY);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.BATTERY_ATTRIBUTE_LEVEL);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_GET);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        errorPebbleSideApplicationNotFound(response);
                    } else {
                        Long level = dic.getInteger(PebbleManager.KEY_PARAM_BATTERY_LEVEL);
                        if (level == null) {
                            MessageUtils.setUnknownError(response);
                        } else {
                            double l = level.intValue() / TO_PERCENT;
                            setResult(response, DConnectMessage.RESULT_OK);
                            setLevel(response, l);
                        }
                    }
                    sendResponse(response);
                }
            });
            // レスポンスを非同期で返却するので、falseを返す
            return false;
        }
    }

    @Override
    protected boolean onGetCharging(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else {
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_BATTERY);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.BATTERY_ATTRIBUTE_CHARING);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_GET);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        errorPebbleSideApplicationNotFound(response);
                    } else {
                        Long charging = dic.getInteger(PebbleManager.KEY_PARAM_BATTERY_CHARGING);
                        if (charging == null) {
                            MessageUtils.setUnknownError(response);
                        } else {
                            boolean isCharging = (charging.intValue() == PebbleManager.BATTERY_CHARGING_ON);
                            setResult(response, DConnectMessage.RESULT_OK);
                            setCharging(response, isCharging);
                        }
                    }
                    sendResponse(response);
                }
            });
            // レスポンスを非同期で返却するので、falseを返す
            return false;
        }
    }

    @Override
    protected boolean onPutOnBatteryChange(final Intent request, final Intent response, 
            final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
           return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, ERROR_MESSAGE);
            return true;
        } else {
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_BATTERY);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.BATTERY_ATTRIBUTE_ON_BATTERY_CHANGE);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_PUT);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        MessageUtils.setUnknownError(response);
                    } else {
                       EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else if (error == EventError.INVALID_PARAMETER) {
                            MessageUtils.setInvalidRequestParameterError(response);
                        } else {
                            MessageUtils.setUnknownError(response);
                        }
                    }
                    sendResponse(response);
                }
            });
            // レスポンスを非同期で返却するので、falseを返す
            return false;
        }
    }

    @Override
    protected boolean onPutOnChargingChange(final Intent request, final Intent response, 
            final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, ERROR_MESSAGE);
            return true;
        } else {
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_BATTERY);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.BATTERY_ATTRIBUTE_ON_CHARGING_CHANGE);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_PUT);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        MessageUtils.setUnknownError(response);
                    } else {
                       EventError error = EventManager.INSTANCE.addEvent(request);
                        if (error == EventError.NONE) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else if (error == EventError.INVALID_PARAMETER) {
                            MessageUtils.setInvalidRequestParameterError(response);
                        } else {
                            MessageUtils.setUnknownError(response);
                        }
                    }
                    sendResponse(response);
                }
            });
            // レスポンスを非同期で返却するので、falseを返す
            return false;
        }
    }

    @Override
    protected boolean onDeleteOnBatteryChange(final Intent request, final Intent response,
                          final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, ERROR_MESSAGE);
            return true;
        } else {
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_BATTERY);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.BATTERY_ATTRIBUTE_ON_BATTERY_CHANGE);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_DELETE);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    // do nothing.
                }
            });
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    }

    @Override
    protected boolean onDeleteOnChargingChange(final Intent request, final Intent response, 
            final String serviceId, final String sessionKey) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, ERROR_MESSAGE);
            return true;
        } else {
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_BATTERY);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.BATTERY_ATTRIBUTE_ON_CHARGING_CHANGE);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_DELETE);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    // do nothing.
                }
            });
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    }

    /**
     * バッテリーの状態変更イベントを送信する.
     * 
     * @param dic バッテリー状態変更イベント
     */
    private void sendOnBatteryChange(final PebbleDictionary dic) {
        PebbleDeviceService service = (PebbleDeviceService) getContext();

        Long level = dic.getInteger(PebbleManager.KEY_PARAM_BATTERY_LEVEL);
        if (level == null) {
            return;
        }

        Bundle battery = new Bundle();
        setLevel(battery, level.intValue() / TO_PERCENT);

        List<Event> evts = EventManager.INSTANCE.getEventList(service.getServiceId(),
                PROFILE_NAME, null, ATTRIBUTE_ON_BATTERY_CHANGE);
        for (Event evt : evts) {
            Intent intent = EventManager.createEventMessage(evt);
            setBattery(intent, battery);
            sendEvent(intent, evt.getAccessToken());
        }
    }

    /**
     * バッテリーチャージングイベントを返却する.
     * 
     * @param dic チャージングイベント
     */
    private void sendOnChargingChange(final PebbleDictionary dic) {
        PebbleDeviceService service = (PebbleDeviceService) getContext();

        Long charging = dic.getInteger(PebbleManager.KEY_PARAM_BATTERY_CHARGING);
        if (charging == null) {
            return;
        }

        boolean isCharging = (charging.intValue() == PebbleManager.BATTERY_CHARGING_ON);

        Bundle battery = new Bundle();
        setCharging(battery, isCharging);

        List<Event> evts = EventManager.INSTANCE.getEventList(service.getServiceId(),
                PROFILE_NAME, null, ATTRIBUTE_ON_CHARGING_CHANGE);
        for (Event evt : evts) {
            Intent intent = EventManager.createEventMessage(evt);
            setBattery(intent, battery);
            sendEvent(intent, evt.getAccessToken());
        }
    }
    /**
     * Pebble 側のアプリケーションが存在しない場合のエラーメッセージ.
     * @param response レスポンス.
     */
    private void errorPebbleSideApplicationNotFound(final Intent response) {
        MessageUtils.setTimeoutError(response, "Pebble side application is NOT FOUND!");
    }
}
