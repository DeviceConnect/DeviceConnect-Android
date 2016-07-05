package org.deviceconnect.android.deviceplugin.test.profile.unique;


import android.content.Intent;

import org.deviceconnect.android.deviceplugin.test.profile.TestServiceDiscoveryProfile;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;

public class TestDriveControllerProfile extends DConnectProfile {

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


    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        if (ATTRIBUTE_ROTATE.equalsIgnoreCase(attribute)) {
            String serviceId = getServiceID(request);
            if (checkServiceId(serviceId)) {
                Integer angle = parseInteger(request, PARAM_ANGLE);
                if (angle == null || angle < 0 || angle > 360) {
                    MessageUtils.setInvalidRequestParameterError(response);
                } else {
                    setResult(response, DConnectMessage.RESULT_OK);
                }
            } else {
                MessageUtils.setNotFoundServiceError(response);
            }
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return true;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        if (ATTRIBUTE_STOP.equalsIgnoreCase(attribute)) {
            String serviceId = getServiceID(request);
            if (checkServiceId(serviceId)) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setNotFoundServiceError(response);
            }
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return true;
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        if (ATTRIBUTE_MOVE.equalsIgnoreCase(attribute)) {
            String serviceId = getServiceID(request);
            if (checkServiceId(serviceId)) {
                final Integer angle = parseInteger(request, PARAM_ANGLE);
                final Double speed = parseDouble(request, PARAM_SPEED);
                if (angle == null || speed == null || angle < 0 || angle > 360 || speed < 0 || speed > 1.0) {
                    MessageUtils.setInvalidRequestParameterError(response);
                } else {
                    setResult(response, DConnectMessage.RESULT_OK);
                }
            } else {
                MessageUtils.setNotFoundServiceError(response);
            }
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return true;
    }

    private boolean checkServiceId(final String serviceId) {
        return TestServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

}
