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

    protected boolean onGetView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onPutView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onDeleteView(final Intent request, final Intent response, final String serviceId,
                                   final String uri) {
        setUnsupportedError(response);
        return true;
    }

    protected boolean onPutSettings(final Intent request, final Intent response, final String serviceId,
                                    final String uri) {
        setUnsupportedError(response);
        return true;
    }

    public static String getSource(final Intent message) {
        return message.getStringExtra(PARAM_SOURCE);
    }

    public static String getURI(final Intent message) {
        return message.getStringExtra(PARAM_URI);
    }

    public static void setURI(final Intent message, final String uri) {
        message.putExtra(PARAM_URI, uri);
    }

    public static Double getX(final Intent message) {
        return parseDouble(message, PARAM_X);
    }

    public static Double getY(final Intent message) {
        return parseDouble(message, PARAM_Y);
    }

    public static Double getZ(final Intent message) {
        return parseDouble(message, PARAM_Z);
    }

    public static Double getRoll(final Intent message) {
        return parseDouble(message, PARAM_ROLL);
    }

    public static Double getPitch(final Intent message) {
        return parseDouble(message, PARAM_PITCH);
    }

    public static Double getYaw(final Intent message) {
        return parseDouble(message, PARAM_YAW);
    }

    public static Double getFOV(final Intent message) {
        return parseDouble(message, PARAM_FOV);
    }

    public static Double getSphereSize(final Intent message) {
        return parseDouble(message, PARAM_SPHERE_SIZE);
    }

    public static Integer getWidth(final Intent message) {
        return parseInteger(message, PARAM_WIDTH);
    }

    public static Integer getHeight(final Intent message) {
        return parseInteger(message, PARAM_HEIGHT);
    }

    public static Boolean getStereo(final Intent message) {
        return parseBoolean(message, PARAM_STEREO);
    }

    public static Boolean getVR(final Intent message) {
        return parseBoolean(message, PARAM_VR);
    }
}
