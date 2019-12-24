/*
 SpheroProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.profile;

import android.content.Intent;
import android.os.Bundle;

import com.orbotix.async.CollisionDetectedAsyncData;
import com.orbotix.async.DeviceSensorAsyncMessage;

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
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

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
    public static final String ATTR_ON_QUATERNION = "onQuaternion";

    /**
     * 属性 : {@value} .
     */
    public static final String ATTR_ON_LOCATOR = "onLocator";

    /**
     * 属性 : {@value} .
     */
    public static final String ATTR_ON_COLLISION = "onCollision";

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
    public static final String PARAM_IMPACT_TIMESTAMP = "impactTimeStamp";

    /**
     * パラメータ : {@value} .
     */
    public static final String PARAM_IMPACT_TIMESTAMPSTRING = "impactTimeStampString";

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

    /**
     * コンストラクタ.
     */
    public SpheroProfile() {
        addApi(mGetOnQuaternionApi);
        addApi(mGetOnCollisionApi);
        addApi(mGetOnLocatorApi);
        addApi(mPutOnQuaternionApi);
        addApi(mPutOnCollisionApi);
        addApi(mPutOnLocatorApi);
        addApi(mDeleteOnQuaternionApi);
        addApi(mDeleteOnCollisionApi);
        addApi(mDeleteOnLocatorApi);
    }


    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    /**
     * 各イベントの登録を行う.
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @return 同期・非同期
     */
    private boolean onPutRequest(final Intent request, final Intent response) {

        String inter = getInterface(request);
        String attribute = getAttribute(request);

        int type = 0;

        if (INTER_QUATERNION.equals(inter)) {
            type = TYPE_QUA;
        } else if (INTER_LOCATOR.equals(inter)) {
            type = TYPE_LOC;
        } else if (INTER_COLLISION.equals(inter)) {
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

    /**
     * 各イベントの解除を行う.
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @return 同期・非同期
     */
    private boolean onDeleteRequest(final Intent request, final Intent response) {

        String inter = getInterface(request);
        String attribute = getAttribute(request);

        int type = 0;

        if (INTER_QUATERNION.equals(inter)) {
            type = TYPE_QUA;
        } else if (INTER_LOCATOR.equals(inter)) {
            type = TYPE_LOC;
        } else if (INTER_COLLISION.equals(inter)) {
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

    private final DConnectApi mPutOnQuaternionApi = new PutApi() {
        @Override
        public String getInterface() {
            return INTER_QUATERNION;
        }

        @Override
        public String getAttribute() {
            return ATTR_ON_QUATERNION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onPutRequest(request, response);
        }
    };

    private final DConnectApi mDeleteOnQuaternionApi = new DeleteApi() {
        @Override
        public String getInterface() {
            return INTER_QUATERNION;
        }

        @Override
        public String getAttribute() {
            return ATTR_ON_QUATERNION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onDeleteRequest(request, response);
        }
    };

    private final DConnectApi mPutOnLocatorApi = new PutApi() {
        @Override
        public String getInterface() {
            return INTER_LOCATOR;
        }

        @Override
        public String getAttribute() {
            return ATTR_ON_LOCATOR;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onPutRequest(request, response);
        }
    };

    private final DConnectApi mDeleteOnLocatorApi = new DeleteApi() {
        @Override
        public String getInterface() {
            return INTER_LOCATOR;
        }

        @Override
        public String getAttribute() {
            return ATTR_ON_LOCATOR;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onDeleteRequest(request, response);
        }
    };
    private final DConnectApi mPutOnCollisionApi = new PutApi() {
        @Override
        public String getInterface() {
            return INTER_COLLISION;
        }

        @Override
        public String getAttribute() {
            return ATTR_ON_COLLISION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onPutRequest(request, response);
        }
    };

    private final DConnectApi mDeleteOnCollisionApi = new DeleteApi() {
        @Override
        public String getInterface() {
            return INTER_COLLISION;
        }

        @Override
        public String getAttribute() {
            return ATTR_ON_COLLISION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onDeleteRequest(request, response);
        }
    };
    /**
     * Spheroのクォータニオンを取得する.
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @return 即座に返却する場合はtrue、それ以外はfalse
     */
    private final DConnectApi mGetOnQuaternionApi = new GetApi() {
        @Override
        public String getInterface() {
            return INTER_QUATERNION;
        }
        @Override
        public String getAttribute() {
            return ATTR_ON_QUATERNION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            DeviceInfo device = SpheroManager.INSTANCE.getDevice(serviceId);
            if (device == null) {
                MessageUtils.setNotFoundServiceError(response);
                return true;
            }

            SpheroManager.INSTANCE.startSensor(device, (info, data, interval) -> {
                SpheroDeviceService service = (SpheroDeviceService) getContext();
                Bundle quaternion = SpheroManager.createQuaternion(data, interval);
                setResult(response, DConnectMessage.RESULT_OK);
                response.putExtra(SpheroProfile.PARAM_QUATERNION, quaternion);
                service.sendResponse(response);
            });
            return false;
        }
    };

    /**
     * Spheroのlocatorを取得する.
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @return 即座に返却する場合はtrue、それ以外はfalse
     */
    private final DConnectApi mGetOnLocatorApi = new GetApi() {
        @Override
        public String getInterface() {
            return INTER_LOCATOR;
        }

        @Override
        public String getAttribute() {
            return ATTR_ON_LOCATOR;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            DeviceInfo device = SpheroManager.INSTANCE.getDevice(serviceId);
            if (device == null) {
                MessageUtils.setNotFoundServiceError(response);
                return true;
            }

            SpheroManager.INSTANCE.startSensor(device, (info, data, interval) -> {
                SpheroDeviceService service = (SpheroDeviceService) getContext();
                Bundle locator = SpheroManager.createLocator(data);
                setResult(response, DConnectMessage.RESULT_OK);
                response.putExtra(SpheroProfile.PARAM_LOCATOR, locator);
                service.sendResponse(response);
            });
            return false;
        }
    };

    /**
     * Spheroの衝突を取得する.
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @return 即座に返却する場合はtrue、それ以外はfalse
     */
    private final DConnectApi mGetOnCollisionApi = new GetApi() {
        @Override
        public String getInterface() {
            return INTER_COLLISION;
        }

        @Override
        public String getAttribute() {
            return ATTR_ON_COLLISION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            DeviceInfo device = SpheroManager.INSTANCE.getDevice(serviceId);
            if (device == null) {
                MessageUtils.setNotFoundServiceError(response);
                return true;
            }
            SpheroManager.INSTANCE.startCollision(device, (info, data) -> {
                SpheroDeviceService service = (SpheroDeviceService) getContext();
                Bundle collision = SpheroManager.createCollision(data);
                setResult(response, DConnectMessage.RESULT_OK);
                response.putExtra(SpheroProfile.PARAM_COLLISION, collision);
                service.sendResponse(response);
            });
            return false;
        }
    };
}
