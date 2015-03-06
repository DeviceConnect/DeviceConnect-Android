/*
 TouchProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.TouchProfileConstants;

import android.content.Intent;
import android.os.Bundle;

/**
 * Touch Profile.
 * 
 * <p>
 * API that provides a smart device touch operation function.<br/>
 * Device plug-in that provides a touch operation function by extending this
 * class, and implements the corresponding API that.<br/>
 * </p>
 * 
 * <h1>API provides methods</h1>
 * <p>
 * The request to each API of Touch Profile, following callback method group is
 * automatically invoked.<br/>
 * Subclass is to implement the functionality by overriding the method for the
 * API provided by the device plug-in from the following methods group.<br/>
 * Features that are not overridden automatically return the response as
 * non-compliant API.
 * </p>
 * <ul>
 * <li>Touch API [GET] :
 * {@link TouchProfile#onGetOnTouch(Intent, Intent, String)}</li>
 * <li>Touch Start API [GET] :
 * {@link TouchProfile#onGetOnTouchStart(Intent, Intent, String)}</li>
 * <li>Touch End API [GET] :
 * {@link TouchProfile#onGetOnTouchEnd(Intent, Intent, String)}</li>
 * <li>Double Tap API [GET] :
 * {@link TouchProfile#onGetOnDoubleTap(Intent, Intent, String)}</li>
 * <li>Touch Move API [GET] :
 * {@link TouchProfile#onGetOnTouchMove(Intent, Intent, String)}</li>
 * <li>Touch Cancel API [GET] :
 * {@link TouchProfile#onGetOnTouchCancel(Intent, Intent, String)}</li>
 * <li>Touch Event API [Register] :
 * {@link TouchProfile#onPutOnTouch(Intent, Intent, String, String)}</li>
 * <li>Touch API [Unregister] :
 * {@link TouchProfile#onDeleteOnTouch(Intent, Intent, String, String)}</li>
 * <li>Touch Start Event API [Register] :
 * {@link TouchProfile#onPutOnTouchStart(Intent, Intent, String, String)}</li>
 * <li>Touch Start API [Unregister] :
 * {@link TouchProfile#onDeleteOnTouchStart(Intent, Intent, String, String)}</li>
 * <li>Touch End Event API [Register] :
 * {@link TouchProfile#onPutOnTouchEnd(Intent, Intent, String, String)}</li>
 * <li>Touch End API [Unregister] :
 * {@link TouchProfile#onDeleteOnTouchEnd(Intent, Intent, String, String)}</li>
 * <li>Double Tap Event API [Register] :
 * {@link TouchProfile#onPutOnDoubleTap(Intent, Intent, String, String)}</li>
 * <li>Double Tap API [Unregister] :
 * {@link TouchProfile#onDeleteOnDoubleTap(Intent, Intent, String, String)}</li>
 * <li>Touch Move Event API [Register] :
 * {@link TouchProfile#onPutOnTouchMove(Intent, Intent, String, String)}</li>
 * <li>Touch Move API [Unregister] :
 * {@link TouchProfile#onDeleteOnTouchMove(Intent, Intent, String, String)}</li>
 * <li>Touch Cancel Event API [Register] :
 * {@link TouchProfile#onPutOnTouchCancel(Intent, Intent, String, String)}</li>
 * <li>Touch Cancel API [Unregister] :
 * {@link TouchProfile#onDeleteOnTouchCancel(Intent, Intent, String, String)}</li>
 * </ul>
 * 
 * @author NTT DOCOMO, INC.
 */
public class TouchProfile extends DConnectProfile implements TouchProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (ATTRIBUTE_ON_TOUCH.equals(attribute)) {
            result = onGetOnTouch(request, response, getServiceID(request));
        } else if (ATTRIBUTE_ON_TOUCH_START.equals(attribute)) {
            result = onGetOnTouchStart(request, response, getServiceID(request));
        } else if (ATTRIBUTE_ON_TOUCH_END.equals(attribute)) {
            result = onGetOnTouchEnd(request, response, getServiceID(request));
        } else if (ATTRIBUTE_ON_DOUBLE_TAP.equals(attribute)) {
            result = onGetOnDoubleTap(request, response, getServiceID(request));
        } else if (ATTRIBUTE_ON_TOUCH_MOVE.equals(attribute)) {
            result = onGetOnTouchMove(request, response, getServiceID(request));
        } else if (ATTRIBUTE_ON_TOUCH_CANCEL.equals(attribute)) {
            result = onGetOnTouchCancel(request, response, getServiceID(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (ATTRIBUTE_ON_TOUCH.equals(attribute)) {
            result = onPutOnTouch(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_TOUCH_START.equals(attribute)) {
            result = onPutOnTouchStart(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_TOUCH_END.equals(attribute)) {
            result = onPutOnTouchEnd(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_DOUBLE_TAP.equals(attribute)) {
            result = onPutOnDoubleTap(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_TOUCH_MOVE.equals(attribute)) {
            result = onPutOnTouchMove(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_TOUCH_CANCEL.equals(attribute)) {
            result = onPutOnTouchCancel(request, response, getServiceID(request), getSessionKey(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (ATTRIBUTE_ON_TOUCH.equals(attribute)) {
            result = onDeleteOnTouch(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_TOUCH_START.equals(attribute)) {
            result = onDeleteOnTouchStart(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_TOUCH_END.equals(attribute)) {
            result = onDeleteOnTouchEnd(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_DOUBLE_TAP.equals(attribute)) {
            result = onDeleteOnDoubleTap(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_TOUCH_MOVE.equals(attribute)) {
            result = onDeleteOnTouchMove(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_TOUCH_CANCEL.equals(attribute)) {
            result = onDeleteOnTouchCancel(request, response, getServiceID(request), getSessionKey(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    // ------------------------------------
    // GET
    // ------------------------------------
    /**
     * ontouch get request handler.<br/>
     * Get the ontouch result and store in the response parameter.
     * If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetOnTouch(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchstart get request handler.<br/>
     * Get the ontouchstart result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetOnTouchStart(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchend get request handler.<br/>
     * Get the ontouchend result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetOnTouchEnd(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ondoubletap get request handler.<br/>
     * Get the ondoubletap result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetOnDoubleTap(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchmove get request handler.<br/>
     * Get the ontouchmove result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetOnTouchMove(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchcancel get request handler.<br/>
     * Get the ontouchcancel result and store in the response parameter.
     * If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetOnTouchCancel(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // PUT
    // ------------------------------------

    /**
     * ontouch callback registration request handler.<br/>
     * Register the ontouch call back, and the result is stored in the response
     * parameters. If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutOnTouch(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchstart callback registration request handler.<br/>
     * Register the ontouchstart call back, and the result is stored in the
     * response parameters. If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutOnTouchStart(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchend callback registration request handler.<br/>
     * Register the ontouchend call back, and the result is stored in the
     * response parameters. If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutOnTouchEnd(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ondoubletap callback registration request handler.<br/>
     * Register the ondoubletap call back, and the result is stored in the
     * response parameters. If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutOnDoubleTap(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchmove callback registration request handler.<br/>
     * Register the ontouchmove call back, and the result is stored in the
     * response parameters. If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutOnTouchMove(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchcancel callback registration request handler.<br/>
     * Register the ontouchcancel call back, and the result is stored in the
     * response parameters. If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutOnTouchCancel(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // DELETE
    // ------------------------------------

    /**
     * ontouch callback release request handler.<br/>
     * Release the ontouch call back, and the result is stored in the response
     * parameters. If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onDeleteOnTouch(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchstart callback release request handler.<br/>
     * Release the ontouchstart call back, and the result is stored in the
     * response parameters. If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onDeleteOnTouchStart(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchend callback release request handler.<br/>
     * Release the ontouchend call back, and the result is stored in the
     * response parameters. If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onDeleteOnTouchEnd(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ondoubletap callback release request handler.<br/>
     * Release the ondoubletap call back, and the result is stored in the
     * response parameters. If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onDeleteOnDoubleTap(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchmove callback release request handler.<br/>
     * Release the ontouchmove call back, and the result is stored in the
     * response parameters. If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onDeleteOnTouchMove(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ontouchcancel callback release request handler.<br/>
     * Release the ontouchcancel call back, and the result is stored in the
     * response parameters. If you have ready to transmit the response parameter
     * that you specify the true return value. If you are not ready to be
     * submitted response parameters, be false for the return value. Then, in
     * the thread to launch the threads eventually doing the transmission of
     * response parameters.
     * 
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param sessionKey session Key.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onDeleteOnTouchCancel(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // Message setter method group
    // ------------------------------------

    /**
     * Set touch information to touch object.
     * 
     * @param touchobject touch object.
     * @param touch touch information.
     */
    public static void setTouch(final Bundle touchobject, final Bundle touch) {
        touchobject.putBundle(PARAM_TOUCH, touch);
    }

    /**
     * Set touch coordinate information to Touch information.
     * 
     * @param touch touch information.
     * @param touches touch coordinate information.
     */
    public static void setTouches(final Bundle touch, final Bundle touches) {
        touch.putBundle(PARAM_TOUCHES, touches);
    }

    /**
     * Set the identification number to touch coordinate information.
     * 
     * @param touches touch coordinate information.
     * @param id identification number.
     */
    public static void setId(final Bundle touches, final int id) {
        touches.putInt(PARAM_ID, id);
    }

    /**
     * Set the X coordinates to touch coordinate information.
     * 
     * @param touches touch coordinate information.
     * @param x X coordinate.
     */
    public static void setX(final Bundle touches, final double x) {
        touches.putDouble(PARAM_X, x);
    }

    /**
     * Set the Y coordinates to touch coordinate information.
     * 
     * @param touches touch coordinate information.
     * @param y Y coordinate.
     */
    public static void setY(final Bundle touches, final double y) {
        touches.putDouble(PARAM_Y, y);
    }
}
