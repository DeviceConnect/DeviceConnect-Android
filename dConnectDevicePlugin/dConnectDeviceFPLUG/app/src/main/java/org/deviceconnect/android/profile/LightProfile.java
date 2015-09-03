/*
 LightProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;

/**
 * Light Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class LightProfile extends DConnectProfile implements LightProfileConstants {

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        if (isNullAttribute(request)) {
            return onGetLight(request, response);
        } else if (isLightGroupAttribute(request)) {
            return onGetLightGroup(request, response);
        } else {
            return onGetOther(request, response);
        }
    }

    /**
     * Handler of POST method.
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        if (isNullAttribute(request)) {
            return onPostLight(request, response);
        } else if (isLightGroupAttribute(request)) {
            return onPostLightGroup(request, response);
        } else if (isLightGroupCreateAttribute(request)) {
            return onPostLightGroupCreate(request, response);
        } else {
            return onPostOther(request, response);
        }
    }

    /**
     * Handler of DELETE method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        if (isNullAttribute(request)) {
            return onDeleteLight(request, response);
        } else if (isLightGroupAttribute(request)) {
            return onDeleteLightGroup(request, response);
        } else if (isLightGroupClearAttribute(request)) {
            return onDeleteLightGroupClear(request, response);
        } else {
            return onDeleteOther(request, response);
        }
    }

    /**
     * Handler of PUT method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        if (isNullAttribute(request)) {
            return onPutLight(request, response);
        } else if (isLightGroupAttribute(request)) {
            return onPutLightGroup(request, response);
        } else {
            return onPutOther(request, response);
        }
    }

    /**
     * Handler of onGetLight method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onGetLight(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onPostLight method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onPostLight(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onDeleteLight method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onDeleteLight(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onPutLight method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onPutLight(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onGetLightGroup method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onGetLightGroup(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onPostLightGroup method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onPostLightGroup(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onDeleteLightGroup method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onDeleteLightGroup(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onPutLightGroup method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onPutLightGroup(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onPostLightGroupCreate method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onPostLightGroupCreate(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onDeleteLightGroupClear method.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onDeleteLightGroupClear(final Intent request, final Intent response) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Handler of onGetOther method. Override this method If there is Attribute or Interface.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onGetOther(final Intent request, final Intent response) {
        setErrAttribute(response);
        return true;
    }

    /**
     * Handler of onPostOther method. Override this method If there is Attribute or Interface.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onPostOther(final Intent request, final Intent response) {
        setErrAttribute(response);
        return true;
    }

    /**
     * Handler of onDeleteOther method. Override this method If there is Attribute or Interface.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onDeleteOther(final Intent request, final Intent response) {
        setErrAttribute(response);
        return true;
    }

    /**
     * Handler of onPutOther method. Override this method If there is Attribute or Interface.<br>
     *
     * @param request  request parameter
     * @param response response parameter
     * @return send response parameter, or not.
     */
    protected boolean onPutOther(final Intent request, final Intent response) {
        setErrAttribute(response);
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

    /**
     * Returns true if interface is null otherwise returns false.
     *
     * @param request request parameter
     * @return true if interface is null otherwise false.
     */
    protected boolean isNullInterface(final Intent request) {
        return getInterface(request) == null;
    }

    /**
     * Returns true if attribute is light/group otherwise returns false.
     *
     * @param request request parameter
     * @return true if attribute is light/group otherwise false.
     */
    protected boolean isLightGroupAttribute(final Intent request) {
        String attribute = getAttribute(request);
        return isNullInterface(request) && ATTRIBUTE_GROUP.equals(attribute);
    }

    /**
     * Returns true if attribute is light/group/create otherwise returns false.
     *
     * @param request requests parameter
     * @return true if attribute is light/group/create otherwise false.
     */
    protected boolean isLightGroupCreateAttribute(final Intent request) {
        String myInterface = getInterface(request);
        String attribute = getAttribute(request);
        return INTERFACE_GROUP.equals(myInterface) && ATTRIBUTE_CREATE.equals(attribute);
    }

    /**
     * Returns true if attribute is light/group/clear otherwise returns false.
     *
     * @param request request parameter
     * @return true if attribute is light/group/clear otherwise false.
     */
    protected boolean isLightGroupClearAttribute(final Intent request) {
        String myInterface = getInterface(request);
        String attribute = getAttribute(request);
        return INTERFACE_GROUP.equals(myInterface) && ATTRIBUTE_CLEAR.equals(attribute);
    }

    /**
     * Return NotSupportAction.
     *
     * @param response response parameter
     */
    protected void setErrNotSupportAction(final Intent response) {
        MessageUtils.setNotSupportActionError(response);
    }

    /**
     * Return UnknownAttributeError.
     *
     * @param response response parameter
     */
    protected void setErrAttribute(final Intent response) {
        MessageUtils.setUnknownAttributeError(response);
    }

    /**
     * Return InvalidRequestParameterError.
     *
     * @param e        exception parameter
     * @param response response parameter
     */
    protected void setErrParameter(final Exception e, final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
    }

    /**
     * Return UnknownError.
     *
     * @param e        exception parameter
     * @param response response parameter
     */
    protected void setErrUnknown(final Exception e, final Intent response) {
        MessageUtils.setUnknownError(response);
    }

}
