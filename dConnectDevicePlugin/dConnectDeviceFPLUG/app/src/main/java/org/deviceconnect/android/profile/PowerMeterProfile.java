/*
 PowerMeterProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;

/**
 * PowerMeter Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class PowerMeterProfile extends DConnectProfile implements PowerMeterProfileConstants {

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;
        if (isNullAttribute(request)) {
            result = onGetPowerState(request, response);
        } else if (ATTR_INTEGRATEDPOWERVALUE.equals(attribute)) {
            result = onGetIntegratedPowerValue(request, response);
        } else if (ATTR_INSTANTANEOUSPOWERVALUE.equals(attribute)) {
            result = onGetInstantaneousPowerValue(request, response);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        MessageUtils.setUnknownAttributeError(response);
        return true;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        boolean result = true;
        if (isNullAttribute(request)) {
            result = onPutPowerMeter(request, response);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        boolean result = true;
        if (isNullAttribute(request)) {
            result = onDeletePowerMeter(request, response);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return result;
    }

    /**
     * Handler of onGetPowerState method.
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onGetPowerState(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onPutPowerMeter method.
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onPutPowerMeter(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onDeletePowerMeter method.
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onDeletePowerMeter(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onPostPowerMeter method.
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onPostPowerMeter(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onGetIntegratedPowerValue method.
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onGetIntegratedPowerValue(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onGetInstantaneousPowerValue method.
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onGetInstantaneousPowerValue(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Returns true if attribute is null otherwise returns false.
     *
     * @param request request parameter
     * @return true if attribute is null otherwise false.
     */
    protected boolean isNullAttribute(final Intent request) {
        return getAttribute(request) == null;
    }

}
