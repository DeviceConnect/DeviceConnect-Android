/*
 SpheroProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.profile;

import java.util.List;

import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.sensor.DeviceSensorsData;

import org.deviceconnect.android.deviceplugin.sphero.SpheroDeviceService;
import org.deviceconnect.android.deviceplugin.sphero.SpheroManager;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo.DeviceCollisionListener;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo.DeviceSensorListener;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.Bundle;

/**
 * spheroプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SpheroProfile extends DConnectProfile {

    /**
     * プロファイル名.
     */
    public static final String PROFILE_NAME = "sphero";

    /**
     * インターフェース : {@value} .
     */
    public static final String INTER_QUATERNION = "quaternion";

    /**
     * インターフェース : {@value} .
     */
    public static final String INTER_LOCATOR = "locator";

    /**
     * インターフェース : {@value} .
     */
    public static final String INTER_COLLISION = "collision";

    /**
     * 属性 : {@value} .
     */
    public static final String ATTR_ON_QUATERNION = "onquaternion";

    /**
     * 属性 : {@value} .
     */
    public static final String ATTR_ON_LOCATOR = "onlocator";

    /**
     * 属性 : {@value} .
     */
    public static final String ATTR_ON_COLLISION = "oncollision";

    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_Q0 = "q0";

    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_Q1 = "q1";

    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_Q2 = "q2";

    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_Q3 = "q3";

    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_QUATERNION = "quaternion";

    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_INTERVAL = "interval";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_LOCATOR = "locator";

    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_POSITION_X = "positionX";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_POSITION_Y = "positionY";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_VELOCITY_X = "velocityX";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_VELOCITY_Y = "velocityY";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_COLLISION = "collision";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_IMPACT_ACCELERATION = "impactAcceleration";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_IMPACT_AXIS = "impactAxis";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_IMPACT_POWER = "impactPower";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_IMPACT_SPEED = "impactSpeed";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_IMPACT_TIMESTAMP = "impactTimestamp";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_X = "x";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_Y = "y";
    
    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_Z = "z";
    
    /**
     * リクエストタイプ QUATERNION.
     */
    private static final int TYPE_QUA = 1;

    /**
     * リクエストタイプ LOCATOR.
     */
    private static final int TYPE_LOC = 2;

    /**
     * リクエストタイプ COLLISTION.
     */
    private static final int TYPE_COL = 3;

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        boolean result = true;
        String inter = getInterface(request);
        String attribute = getAttribute(request);
        if (INTER_QUATERNION.equals(inter) && ATTR_ON_QUATERNION.equals(attribute)) {
            result = onGetQuaternion(request, response, getServiceID(request));
        } else if (INTER_LOCATOR.equals(inter) && ATTR_ON_LOCATOR.equals(attribute)) {
            result = onGetLocator(request, response, getServiceID(request));
        } else if (INTER_COLLISION.equals(inter) && ATTR_ON_COLLISION.equals(attribute)) {
            result = onGetCollision(request, response, getServiceID(request));
        } else {
            MessageUtils.setNotSupportAttributeError(response);
        }
        return result;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {

        String inter = getInterface(request);
        String attribute = getAttribute(request);

        int type = 0;

        if (INTER_QUATERNION.equals(inter) && ATTR_ON_QUATERNION.equals(attribute)) {
            type = TYPE_QUA;
        } else if (INTER_LOCATOR.equals(inter) && ATTR_ON_LOCATOR.equals(attribute)) {
            type = TYPE_LOC;
        } else if (INTER_COLLISION.equals(inter) && ATTR_ON_COLLISION.equals(attribute)) {
            type = TYPE_COL;
        } else {
            MessageUtils.setNotSupportAttributeError(response);
            return true;
        }

        String serviceId = getServiceID(request);
        DeviceInfo device = SpheroManager.INSTANCE.getDevice(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        EventError error = EventManager.INSTANCE.addEvent(request);
        boolean registeredEvent = false;
        switch (error) {
        case NONE:
            registeredEvent = true;
            setResult(response, DConnectMessage.RESULT_OK);
            break;
        case INVALID_PARAMETER:
            MessageUtils.setInvalidRequestParameterError(response);
            break;
        default:
            MessageUtils.setUnknownError(response);
            break;
        }

        if (registeredEvent) {
            switch (type) {
            case TYPE_QUA:
            case TYPE_LOC:
                SpheroManager.INSTANCE.startSensor(device);
                break;
            case TYPE_COL:
                SpheroManager.INSTANCE.startCollision(device);
                break;
            default:
                break;
            }
        }

        return true;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {

        String inter = getInterface(request);
        String attribute = getAttribute(request);

        int type = 0;

        if (INTER_QUATERNION.equals(inter) && ATTR_ON_QUATERNION.equals(attribute)) {
            type = TYPE_QUA;
        } else if (INTER_LOCATOR.equals(inter) && ATTR_ON_LOCATOR.equals(attribute)) {
            type = TYPE_LOC;
        } else if (INTER_COLLISION.equals(inter) && ATTR_ON_COLLISION.equals(attribute)) {
            type = TYPE_COL;
        } else {
            MessageUtils.setNotSupportAttributeError(response);
            return true;
        }

        EventError error = EventManager.INSTANCE.removeEvent(request);
        boolean removedEvent = false;
        switch (error) {
        case NONE:
            removedEvent = true;
            setResult(response, DConnectMessage.RESULT_OK);
            break;
        case INVALID_PARAMETER:
            MessageUtils.setInvalidRequestParameterError(response);
            break;
        default:
            MessageUtils.setUnknownError(response);
            break;
        }

        String serviceId = getServiceID(request);
        DeviceInfo device = SpheroManager.INSTANCE.getDevice(serviceId);

        if (removedEvent) {
            switch (type) {
            case TYPE_QUA:
            case TYPE_LOC:
                if (device != null && !SpheroManager.INSTANCE.hasSensorEvent(device)) {
                    SpheroManager.INSTANCE.stopSensor(device);
                }
                break;
            case TYPE_COL:
                List<Event> events = EventManager.INSTANCE.getEventList(
                        serviceId, PROFILE_NAME, 
                        INTER_COLLISION, ATTR_ON_COLLISION);
                
                if (device != null && events.size() == 0) {
                    SpheroManager.INSTANCE.stopCollision(device);
                }
                break;
            default:
                break;
            }
        }

        return true;
    }

    /**
     * Spheroのクォータニオンを取得する.
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @return 即座に返却する場合はtrue、それ以外はfalse
     */
    private boolean onGetQuaternion(final Intent request, final Intent response,
            final String serviceId) {
        DeviceInfo device = SpheroManager.INSTANCE.getDevice(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        SpheroManager.INSTANCE.startSensor(device, new DeviceSensorListener() {
            @Override
            public void sensorUpdated(final DeviceInfo info, final DeviceSensorsData data, final long interval) {
                SpheroDeviceService service = (SpheroDeviceService) getContext();
                Bundle quaternion = SpheroManager.createQuaternion(data, interval);
                setResult(response, DConnectMessage.RESULT_OK);
                response.putExtra(SpheroProfile.PARAM_QUATERNION, quaternion);
                service.sendResponse(response);
            }
        });
        return false;
    }

    /**
     * Spheroのlocatorを取得する.
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @return 即座に返却する場合はtrue、それ以外はfalse
     */
    private boolean onGetLocator(final Intent request, final Intent response,
            final String serviceId) {
        DeviceInfo device = SpheroManager.INSTANCE.getDevice(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        SpheroManager.INSTANCE.startSensor(device, new DeviceSensorListener() {
            @Override
            public void sensorUpdated(final DeviceInfo info, final DeviceSensorsData data, final long interval) {
                SpheroDeviceService service = (SpheroDeviceService) getContext();
                Bundle locator = SpheroManager.createLocator(data);
                setResult(response, DConnectMessage.RESULT_OK);
                response.putExtra(SpheroProfile.PARAM_LOCATOR, locator);
                service.sendResponse(response);
            }
        });
        return false;
    }

    /**
     * Spheroの衝突を取得する.
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @return 即座に返却する場合はtrue、それ以外はfalse
     */
    private boolean onGetCollision(final Intent request, final Intent response,
            final String serviceId) {
        DeviceInfo device = SpheroManager.INSTANCE.getDevice(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        SpheroManager.INSTANCE.startCollision(device, new DeviceCollisionListener() {
            @Override
            public void collisionDetected(final DeviceInfo info, final CollisionDetectedAsyncData data) {
                SpheroDeviceService service = (SpheroDeviceService) getContext();
                Bundle collision = SpheroManager.createCollision(data);
                setResult(response, DConnectMessage.RESULT_OK);
                response.putExtra(SpheroProfile.PARAM_COLLISION, collision);
                service.sendResponse(response);
            }
        });
        return false;
    }
}
