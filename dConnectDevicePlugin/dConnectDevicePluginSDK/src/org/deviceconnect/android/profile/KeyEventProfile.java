/*
 KeyEventProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.KeyEventProfileConstants;

import android.content.Intent;
import android.os.Bundle;

/**
 * Key Event Profile.
 * 
 * <p>
 * API that provides a smart device key event operation function.<br/>
 * Device plug-in that provides a button operation function by extending this
 * class, and implements the corresponding API that.<br/>
 * </p>
 * 
 * <h1>API provides methods</h1>
 * <p>
 * The request to each API of Key Event Profile, following callback method group
 * is automatically invoked.<br/>
 * Subclass is to implement the functionality by overriding the method for the
 * API provided by the device plug-in from the following methods group.<br/>
 * Features that are not overridden automatically return the response as
 * non-compliant API.
 * </p>
 * <ul>
 * <li>Key Down API [GET] :
 * {@link KeyEventProfile#onGetOnDown(Intent, Intent, String)}</li>
 * <li>Key Up API [GET] :
 * {@link KeyEventProfile#onGetOnUp(Intent, Intent, String)}</li>
 * <li>Key Down Event API [Register] :
 * {@link KeyEventProfile#onPutOnDown(Intent, Intent, String, String)}</li>
 * <li>Key Up Event API [Register] :
 * {@link KeyEventProfile#onPutOnUp(Intent, Intent, String, String)}</li>
 * <li>Key Down Event API [Unregister] :
 * {@link KeyEventProfile#onDeleteOnDown(Intent, Intent, String, String)}</li>
 * <li>Key Up Event API [Unregister] :
 * {@link KeyEventProfile#onDeleteOnUp(Intent, Intent, String, String)}</li>
 * </ul>
 * 
 * @author NTT DOCOMO, INC.
 */
public class KeyEventProfile extends DConnectProfile implements KeyEventProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (ATTRIBUTE_ON_DOWN.equals(attribute)) {
            result = onGetOnDown(request, response, getServiceID(request));
        } else if (ATTRIBUTE_ON_UP.equals(attribute)) {
            result = onGetOnUp(request, response, getServiceID(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (ATTRIBUTE_ON_DOWN.equals(attribute)) {
            result = onPutOnDown(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_UP.equals(attribute)) {
            result = onPutOnUp(request, response, getServiceID(request), getSessionKey(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (ATTRIBUTE_ON_DOWN.equals(attribute)) {
            result = onDeleteOnDown(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_UP.equals(attribute)) {
            result = onDeleteOnUp(request, response, getServiceID(request), getSessionKey(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    // ------------------------------------
    // GET
    // ------------------------------------
    /**
     * ondown get request handler.<br/>
     * Get the ondown result and store in the response parameter. If you have
     * ready to transmit the response parameter that you specify the true return
     * value. If you are not ready to be submitted response parameters, be false
     * for the return value. Then, in the thread to launch the threads
     * eventually doing the transmission of response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetOnDown(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onup get request handler.<br/>
     * Get the onup result and store in the response parameter. If you have
     * ready to transmit the response parameter that you specify the true return
     * value. If you are not ready to be submitted response parameters, be false
     * for the return value. Then, in the thread to launch the threads
     * eventually doing the transmission of response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetOnUp(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // PUT
    // ------------------------------------

    /**
     * ondown callback registration request handler.<br/>
     * Register the ondown call back, and the result is stored in the response
     * parameters. If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     * 
     * @param request request parameter
     * @param response response parameter
     * @param serviceId service ID
     * @param sessionKey session Key
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutOnDown(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onup callback registration request handler.<br/>
     * Register the onup call back, and the result is stored in the response
     * parameters. If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     * 
     * @param request request parameter
     * @param response response parameter
     * @param serviceId service ID
     * @param sessionKey session Key
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutOnUp(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // DELETE
    // ------------------------------------

    /**
     * ondown callback release request handler.<br/>
     * Release the ondown call back, and the result is stored in the response
     * parameters. If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     * 
     * @param request request parameter
     * @param response response parameter
     * @param serviceId service ID
     * @param sessionKey session Key
     * @return Whether or not to send the response parameters.
     */
    protected boolean onDeleteOnDown(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onup callback release request handler.<br/>
     * Release the onup call back, and the result is stored in the response
     * parameters. If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     * 
     * @param request request parameter
     * @param response response parameter
     * @param serviceId service ID
     * @param sessionKey session Key
     * @return Whether or not to send the response parameters.
     */
    protected boolean onDeleteOnUp(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // Message setter method group
    // ------------------------------------

    /**
     * Set key event information to key event object.
     * 
     * @param keyeventobject key event object
     * @param keyevent key event information
     */
    public static void setKeyevent(final Bundle keyeventobject, final Bundle keyevent) {
        keyeventobject.putBundle(PARAM_KEYEVENT, keyevent);
    }

    /**
     * Set the identification number to key event information.
     * 
     * @param keyevent key event information
     * @param id identification number
     */
    public static void setId(final Bundle keyevent, final int id) {
        keyevent.putInt(PARAM_ID, id);
    }

    /**
     * Set key configure to key event information.
     * 
     * @param keyevent key event information
     * @param config key configure
     */
    public static void setConfig(final Bundle keyevent, final String config) {
        keyevent.putString(PARAM_CONFIG, config);
    }

}
