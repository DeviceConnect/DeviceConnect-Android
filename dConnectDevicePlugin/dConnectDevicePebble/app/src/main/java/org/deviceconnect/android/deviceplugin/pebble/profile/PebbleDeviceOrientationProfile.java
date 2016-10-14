/*
 PebbleDeviceOrientationProfile.java
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
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * Pebble用 Device Orientationプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class PebbleDeviceOrientationProfile extends DeviceOrientationProfile {
    /** milli G を m/s^2 の値にする係数. */
    private static final double G_TO_MS2_COEFFICIENT =  9.81 / 1000.0;

    /** Orientationデータをキャッシュする変数. */
    private Bundle mCacheOrientation;

    private final DConnectApi mGetOnDeviceOrientationApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            if (isStartSensor()) {
                if (mCacheOrientation != null) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setOrientation(response, mCacheOrientation);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response,
                        "Device is not ready.");
                }
                return true;
            } else {
                startSensorOnce(response);
                return false;
            }
        }
    };

    private final DConnectApi mPutOnDeviceOrientationApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            startSensor(new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        MessageUtils.setUnknownError(response);
                    } else {
                        // イベントリスナーを登録
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
    };

    private final DConnectApi mDeleteOnDeviceOrientationApiApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DEVICE_ORIENTATION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            stopSensor();
            // イベントリスナーを解除
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
    };

    /**
     * コンストラクタ.
     * @param service Pebble デバイスサービス
     */
    public PebbleDeviceOrientationProfile(final PebbleDeviceService service) {
        service.getPebbleManager().addEventListener(PebbleManager.PROFILE_DEVICE_ORIENTATION
                , new OnReceivedEventListener() {
            @Override
            public void onReceivedEvent(final PebbleDictionary dic) {
                // Pebbleから加速度が送られてきたら、登録されたイベントに対して通知を送る
                Bundle orientation = createOrientation(dic);
                mCacheOrientation = orientation;
                // 登録されたイベントリスナー一覧を取得
                List<Event> evts = EventManager.INSTANCE.getEventList(service.getServiceId(),
                        PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_ORIENTATION);
                if (evts != null && evts.size() > 0) {
                    synchronized (evts) {
                        for (Event evt : evts) {
                            // 各イベントリスナーに通知
                            Intent intent = EventManager.createEventMessage(evt);
                            setOrientation(intent, orientation);
                            sendEvent(intent, evt.getAccessToken());
                        }
                    }
                }
            }
        });

        addApi(mGetOnDeviceOrientationApi);
        addApi(mPutOnDeviceOrientationApi);
        addApi(mDeleteOnDeviceOrientationApiApi);
    }

    /**
     * Pebbleから送られてきたデータからOrientationデータを作成する.
     * @param dic 受信したデータ
     * @return Orientationデータ
     */
    private Bundle createOrientation(final PebbleDictionary dic) {
        Long x = dic.getInteger(PebbleManager.KEY_PARAM_DEVICE_ORIENTATION_X);
        Long y = dic.getInteger(PebbleManager.KEY_PARAM_DEVICE_ORIENTATION_Y);
        Long z = dic.getInteger(PebbleManager.KEY_PARAM_DEVICE_ORIENTATION_Z);
        Long interval = dic.getInteger(PebbleManager.KEY_PARAM_DEVICE_ORIENTATION_INTERVAL);

        // Pebbleからの加速度をdConnectの単位に正規化してdConnect用のデータを作成
        Bundle orientation = new Bundle();
        Bundle accelerationIncludingGravity = new Bundle();
        setX(accelerationIncludingGravity, x.intValue() * G_TO_MS2_COEFFICIENT);
        setY(accelerationIncludingGravity, y.intValue() * G_TO_MS2_COEFFICIENT);
        setZ(accelerationIncludingGravity, z.intValue() * G_TO_MS2_COEFFICIENT);
        setAccelerationIncludingGravity(orientation, accelerationIncludingGravity);
        setInterval(orientation, interval.longValue());
        return orientation;
    }

    /**
     * センサーが開始されているかチェックする.
     * @return 開始されている場合はtrue、それ以外はfalse
     */
    private boolean isStartSensor() {
        PebbleDeviceService service = (PebbleDeviceService) getContext();
        List<Event> evts = EventManager.INSTANCE.getEventList(service.getServiceId(),
                PROFILE_NAME, null, ATTRIBUTE_ON_DEVICE_ORIENTATION);
        return evts != null && evts.size() > 0;
    }

    /**
     * 1回だけセンサーを起動します.
     * @param response レスポンス
     */
    private void startSensorOnce(final Intent response) {
        final PebbleDeviceService service = (PebbleDeviceService) getContext();
        final PebbleManager mgr = service.getPebbleManager();
        final OnReceivedEventListener l = new OnReceivedEventListener() {
            @Override
            public void onReceivedEvent(final PebbleDictionary dic) {
                Bundle orientation = createOrientation(dic);
                setResult(response, DConnectMessage.RESULT_OK);
                setOrientation(response, orientation);
                service.sendResponse(response);
                mgr.removeEventListener(PebbleManager.PROFILE_DEVICE_ORIENTATION, this);
                if (!isStartSensor()) {
                    stopSensor();
                }
            }
        };
        mgr.addEventListener(PebbleManager.PROFILE_DEVICE_ORIENTATION, l);

        startSensor(new OnSendCommandListener() {
            @Override
            public void onReceivedData(final PebbleDictionary dic) {
                if (dic == null) {
                    MessageUtils.setUnknownError(response);
                    service.sendResponse(response);
                    mgr.removeEventListener(PebbleManager.PROFILE_DEVICE_ORIENTATION, l);
                    stopSensor();
                }
            }
        });
    }

    /**
     * センサーを開始する.
     * @param listener 開始結果を通知するリスナー
     */
    private void startSensor(final OnSendCommandListener listener) {
        PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
        // Pebbleで加速度センサーの登録依頼を送る
        PebbleDictionary dic = new PebbleDictionary();
        dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_DEVICE_ORIENTATION);
        dic.addInt8(PebbleManager.KEY_ATTRIBUTE
                , (byte) PebbleManager.DEVICE_ORIENTATION_ATTRIBUTE_ON_DEVICE_ORIENTATION);
        dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_PUT);
        mgr.sendCommandToPebble(dic, listener);
    }

    /**
     * センサーを停止する.
     */
    private void stopSensor() {
        PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
        // Pebbleに加速度センサーの解除依頼を送る
        PebbleDictionary dic = new PebbleDictionary();
        dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_DEVICE_ORIENTATION);
        dic.addInt8(PebbleManager.KEY_ATTRIBUTE
                , (byte) PebbleManager.DEVICE_ORIENTATION_ATTRIBUTE_ON_DEVICE_ORIENTATION);
        dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_DELETE);
        mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
            @Override
            public void onReceivedData(final PebbleDictionary dic) {
                // No operation.
            }
        });
    }
}
