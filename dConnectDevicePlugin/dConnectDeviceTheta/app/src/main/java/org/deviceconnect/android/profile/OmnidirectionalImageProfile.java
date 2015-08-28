/*
 OmnidirectionalImageProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.OmnidirectionalImageProfileConstants;

/**
 * Omnidirectional Image Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class OmnidirectionalImageProfile extends DConnectProfile
    implements OmnidirectionalImageProfileConstants {

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        String interfaceName = getInterface(request);
        String attributeName = getAttribute(request);
        if (interfaceName == null && ATTRIBUTE_ROI.equals(attributeName)) {
            return onGetView(request, response, getServiceID(request), getSource(request));
        }
        MessageUtils.setUnknownAttributeError(response);
        return true;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        String interfaceName = getInterface(request);
        String attributeName = getAttribute(request);
        if (interfaceName == null && ATTRIBUTE_ROI.equals(attributeName)) {
            return onPutView(request, response, getServiceID(request), getSource(request));
        } else if (INTERFACE_ROI.equals(interfaceName) && ATTRIBUTE_SETTINGS.equals(attributeName)) {
            return onPutSettings(request, response, getServiceID(request), getURI(request));
        }
        MessageUtils.setUnknownAttributeError(response);
        return true;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        String interfaceName = getInterface(request);
        String attributeName = getAttribute(request);
        if (interfaceName == null && ATTRIBUTE_ROI.equals(attributeName)) {
            return onDeleteView(request, response, getServiceID(request), getURI(request));
        }
        MessageUtils.setUnknownAttributeError(response);
        return true;
    }

    // ------------------------------------
    // GET
    // ------------------------------------
    /**
     * ROI Image View 'GET' request handler.
     *
     * <p>
     * Creates an JPEG image of ROI and returns the URI of the image.
     * </p>
     *
     * <p>
     * If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     * </p>
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param source URI of an omnidirectional image.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onGetView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // PUT
    // ------------------------------------
    /**
     * ROI Image View 'PUT' request handler.
     *
     * <p>
     * Creates an MotionJPEG image of ROI and returns the URI of the image.
     * </p>
     *
     * <p>
     * If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     * </p>
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param source URI of an omnidirectional image.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * ROI Image View Settings request handler.
     *
     * <p>
     * If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     * </p>
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param uri URI of an ROI image.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onPutSettings(final Intent request, final Intent response, final String serviceId,
                                    final String uri) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // DELETE
    // ------------------------------------
    /**
     * ROI Image View 'DELETE' request handler.
     *
     * <p>
     * Removes an image of ROI which is specified by <code>uri</code> parameter.
     * </p>
     *
     * <p>
     * If you have ready to transmit the response parameter that you
     * specify the true return value. If you are not ready to be submitted
     * response parameters, be false for the return value. Then, in the thread
     * to launch the threads eventually doing the transmission of response
     * parameters.
     * </p>
     *
     * @param request request parameter.
     * @param response response parameter.
     * @param serviceId service ID.
     * @param uri URI of an ROI image.
     * @return Whether or not to send the response parameters.
     */
    protected boolean onDeleteView(final Intent request, final Intent response, final String serviceId,
                                   final String uri) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * Gets the URI of an omnidirectional image.
     * @param request Request data
     * @return the URI of an omnidirectional image
     */
    public static String getSource(final Intent request) {
        return request.getStringExtra(PARAM_SOURCE);
    }

    /**
     * Gets the URI of an ROI image from the specified request.
     * @param request Request data
     * @return the URI of an ROI image
     */
    public static String getURI(final Intent request) {
        return request.getStringExtra(PARAM_URI);
    }

    /**
     * Sets the URI of an ROI image the the specified response.
     * @param response Request data
     * @param uri the URI of an ROI image
     */
    public static void setURI(final Intent response, final String uri) {
        response.putExtra(PARAM_URI, uri);
    }

    /**
     * Gets X-coordinate of camera for ROI from the specified request.
     * @param request Request data
     * @return X-coordinate of camera for ROI
     */
    public static Double getX(final Intent request) {
        return parseDouble(request, PARAM_X);
    }

    /**
     * Gets Y-coordinate of camera for ROI from the specified request.
     * @param request Request data
     * @return Y-coordinate of camera for ROI
     */
    public static Double getY(final Intent request) {
        return parseDouble(request, PARAM_Y);
    }

    /**
     * Gets Z-coordinate of camera for ROI from the specified request.
     * @param request Request data
     * @return Z-coordinate of camera for ROI
     */
    public static Double getZ(final Intent request) {
        return parseDouble(request, PARAM_Z);
    }

    /**
     * Gets roll degree of camera for ROI from the specified request.
     * @param request Request data
     * @return roll degree of camera for ROI
     */
    public static Double getRoll(final Intent request) {
        return parseDouble(request, PARAM_ROLL);
    }

    /**
     * Gets pitch degree of camera for ROI from the specified request.
     * @param request Request data
     * @return pitch degree of camera for ROI
     */
    public static Double getPitch(final Intent request) {
        return parseDouble(request, PARAM_PITCH);
    }

    /**
     * Gets yaw degree of camera for ROI from the specified request.
     * @param request Request data
     * @return yaw degree of camera for ROI
     */
    public static Double getYaw(final Intent request) {
        return parseDouble(request, PARAM_YAW);
    }

    /**
     * Gets FOV of camera for ROI from the specified request.
     * @param request Request data
     * @return FOV of camera for ROI
     */
    public static Double getFOV(final Intent request) {
        return parseDouble(request, PARAM_FOV);
    }

    /**
     * Gets sphere size for ROI from the specified request.
     * @param request Request data
     * @return sphere size for ROI
     */
    public static Double getSphereSize(final Intent request) {
        return parseDouble(request, PARAM_SPHERE_SIZE);
    }

    /**
     * Gets width of resolution of ROI image from the specified request.
     * @param request Request data
     * @return width of resolution of ROI image
     */
    public static Integer getWidth(final Intent request) {
        return parseInteger(request, PARAM_WIDTH);
    }

    /**
     * Gets height of resolution of ROI image from the specified request.
     * @param request Request data
     * @return height of resolution of ROI image
     */
    public static Integer getHeight(final Intent request) {
        return parseInteger(request, PARAM_HEIGHT);
    }

    /**
     * Gets flag of Stereo mode from the specified request.
     * @param request Request data
     * @return flag of Stereo mode
     */
    public static Boolean getStereo(final Intent request) {
        return parseBoolean(request, PARAM_STEREO);
    }

    /**
     * Gets flag of VR mode from the specified request.
     * @param request Request data
     * @return flag of VR mode
     */
    public static Boolean getVR(final Intent request) {
        return parseBoolean(request, PARAM_VR);
    }
}
