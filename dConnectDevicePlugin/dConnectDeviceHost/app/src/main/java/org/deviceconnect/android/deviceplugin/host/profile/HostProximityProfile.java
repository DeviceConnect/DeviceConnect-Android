/*
 HostProximityProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.ProximityProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Proximity Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostProximityProfile extends ProximityProfile implements SensorEventListener {

    /**
     * サービスID.
     */
    private static String sServiceId = "";

    private final DConnectApi mGetOnUserProximityApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_USER_PROXIMITY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return getOnUserProximity(response);
        }
    };

    private final DConnectApi mPutOnUserProximityApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_USER_PROXIMITY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                sServiceId = serviceId;
                if (registerProximity()) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setNotSupportAttributeError(response,
                        "This device is not support proximity.");
                }
            } else {
                MessageUtils.setUnknownError(response,
                    "Failed to register event.");
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnUserProximityApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_USER_PROXIMITY;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                if (unregisterProximity()) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setNotSupportAttributeError(response,
                        "This device is not support proximity.");
                }
            } else {
                MessageUtils.setUnknownError(response,
                    "Failed to unregister event.");
            }
            return true;
        }
    };

    public HostProximityProfile() {
        addApi(mGetOnUserProximityApi);
        addApi(mPutOnUserProximityApi);
        addApi(mDeleteOnUserProximityApi);
    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            Bundle mProximityBundle = new Bundle();
            if (sensorEvent.values[0] == 0.0) {
                ProximityProfile.setNear(mProximityBundle, true);
            } else {
                ProximityProfile.setNear(mProximityBundle, false);
            }

            List<Event> events = EventManager.INSTANCE.getEventList(sServiceId,
                    ProximityProfile.PROFILE_NAME, null,
                    ProximityProfile.ATTRIBUTE_ON_USER_PROXIMITY);

            if (events == null || events.size() == 0) {
                unregisterProximity();
                return;
            }

            synchronized (events) {
                for (Event event : events) {
                    Intent evtIntent = EventManager.createEventMessage(event);
                    ProximityProfile.setProximity(evtIntent, mProximityBundle);
                    sendEvent(evtIntent, event.getAccessToken());
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // No operation.
    }

    /**
     * Proximityのセンサーリストを取得する.
     * <p>
     * Proximityのセンサーに対応していない場合には、空の配列が返却される。
     * @return センサーリスト
     */
    private List<Sensor> getSensorList() {
        SensorManager mgr = getSensorManager();
        if (mgr == null) {
            return new ArrayList<Sensor>();
        }
        return mgr.getSensorList(Sensor.TYPE_PROXIMITY);
    }

    /**
     * センサー管理クラスを取得する.
     * @return センサー管理クラス
     */
    private SensorManager getSensorManager() {
        return (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * Proximityのセンサーを登録する.
     * @return trueの場合には登録成功、false場合には失敗
     */
    private boolean registerProximity() {
        List<Sensor> sensors = getSensorList();
        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            getSensorManager().registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Proximityのセンサーを解除する.
     * @return 解除に成功した場合はtrue、それ以外はfalse
     */
    private boolean unregisterProximity() {
        SensorManager mgr = getSensorManager();
        if (mgr == null) {
            return false;
        }
        mgr.unregisterListener(this);
        return true;
    }

    /**
     * Proximityを一回だけ取得します.
     * @param response レスポンス
     * @return 即座に返却する場合はtrue、それ以外はfalse
     */
    private boolean getOnUserProximity(final Intent response) {
        final SensorEventListener l = new SensorEventListener() {
            @Override
            public void onSensorChanged(final SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    Bundle mProximityBundle = new Bundle();
                    if (event.values[0] == 0.0) {
                        ProximityProfile.setNear(mProximityBundle, true);
                    } else {
                        ProximityProfile.setNear(mProximityBundle, false);
                    }
                    ProximityProfile.setProximity(response, mProximityBundle);
                    DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                    getSensorManager().unregisterListener(this);
                }
            }
            @Override
            public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
            }
        };

        List<Sensor> sensors = getSensorList();
        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            getSensorManager().registerListener(l, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            return false;
        } else {
            MessageUtils.setNotSupportAttributeError(response,
                    "This device is not support proximity.");
            return true;
        }
    }
}
