/*
 SpheroDriveControllerProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.profile;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.sphero.BuildConfig;
import org.deviceconnect.android.deviceplugin.sphero.SpheroManager;
import org.deviceconnect.android.deviceplugin.sphero.data.DeviceInfo;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Controller Profile.
 * @author NTT DOCOMO, INC.
 */
public class SpheroDriveControllerProfile extends DConnectProfile {

    /**
     * プロファイル名.
     */
    public static final String PROFILE_NAME = "driveController";

    /**
     * アトリビュート : {@value} .
     */
    public static final String ATTRIBUTE_MOVE = "move";
    
    /**
     * アトリビュート : {@value} .
     */
    public static final String ATTRIBUTE_STOP = "stop";

    /**
     * アトリビュート : {@value} .
     */
    public static final String ATTRIBUTE_ROTATE = "rotate";

    /**
     * パラメータ: {@value} .
     */
    public static final String PARAM_ANGLE = "angle";

    /**
     * パラメータ: {@value} .
     */
    public static final String PARAM_SPEED = "speed";

    public SpheroDriveControllerProfile() {
        addApi(mPutRotateApi);
        addApi(mDeleteStopApi);
        addApi(mPostMoveApi);
    }



    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    private final DConnectApi mPutRotateApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ROTATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            DeviceInfo info = SpheroManager.INSTANCE.getDevice(serviceId);

            if (info != null) {
                Double angle = parseDouble(request, PARAM_ANGLE);
                if (angle == null || angle < 0 || angle > 360) {
                    MessageUtils.setInvalidRequestParameterError(response);
                } else {
                    synchronized (info) {
                        info.getDevice().rotate(angle.floatValue());
                    }
                    setResult(response, DConnectMessage.RESULT_OK);
                }
            } else {
                MessageUtils.setNotFoundServiceError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteStopApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_STOP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            DeviceInfo info = SpheroManager.INSTANCE.getDevice(serviceId);

            if (info != null) {
                synchronized (info) {
                    info.getDevice().stop();
                }
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setNotFoundServiceError(response);
            }

            return true;
        }
    };

    private final DConnectApi mPostMoveApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_MOVE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            DeviceInfo info = SpheroManager.INSTANCE.getDevice(serviceId);

            if (info != null) {
                final Double angle = parseDouble(request, PARAM_ANGLE);
                final Double speed = parseDouble(request, PARAM_SPEED);
                if (angle == null || speed == null || angle < 0 || angle > 360 || speed < 0 || speed > 1.0) {
                    MessageUtils.setInvalidRequestParameterError(response);
                } else {
                    synchronized (info) {
                        info.getDevice().drive(angle.floatValue(), speed.floatValue());
                    }
                    if (BuildConfig.DEBUG) {
                        Log.d("", "angle : " + angle.floatValue());
                        Log.d("", "speed : " + speed.floatValue());
                    }
                    setResult(response, DConnectMessage.RESULT_OK);
                }
            } else {
                MessageUtils.setNotFoundServiceError(response);
            }
            return true;
        }
    };

}
