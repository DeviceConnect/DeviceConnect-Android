/*
 HostProximityProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.util.List;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ProximityProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

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

    /**
     * Sensor Manager.
     */
    private SensorManager mSensorManagerProximity;

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkserviceId(final String serviceId) {
        return HostServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyserviceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * セッションキーが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptySessionKey(final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response);
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
    }

    @Override
    protected boolean onPutOnUserProximity(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                sServiceId = serviceId;
                this.getContext();
                mSensorManagerProximity = (SensorManager) this.getContext().getSystemService(Context.SENSOR_SERVICE);
                List<Sensor> sensors = mSensorManagerProximity.getSensorList(Sensor.TYPE_PROXIMITY);

                if (sensors.size() > 0) {
                    Sensor sensor = sensors.get(0);
                    mSensorManagerProximity.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                }

                setResult(response, DConnectMessage.RESULT_OK);

                return true;

            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);

                return true;
            }

        }
        return true;
    }

    @Override
    protected boolean onDeleteOnUserProximity(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyserviceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                mSensorManagerProximity.unregisterListener(this);

                return false;
            } else {
                MessageUtils.setError(response, 100, "Can not unregister event.");
                return true;
            }
        }

        return true;
    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {

            List<Event> events = EventManager.INSTANCE.getEventList(sServiceId,
                    ProximityProfile.PROFILE_NAME,
                    null,
                    ProximityProfile.ATTRIBUTE_ON_USER_PROXIMITY);

            Bundle mProximityBundle = new Bundle();

            if (sensorEvent.values[0] == 0.0) {
                ProximityProfile.setNear(mProximityBundle, true);
            } else {
                ProximityProfile.setNear(mProximityBundle, false);
            }

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent mIntent = EventManager.createEventMessage(event);
                ProximityProfile.setProximity(mIntent, mProximityBundle);
                getContext().sendBroadcast(mIntent);
            }
        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // No operation.
    }

}
