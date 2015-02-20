/*
 HostCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import org.deviceconnect.android.deviceplugin.host.activity.CanvasProfileActivity;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawImageObject;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;

/**
 * Canvas Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostCanvasProfile extends CanvasProfile {

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        if (ATTRIBUTE_DRAW_IMAGE.equals(attribute)) {
            String serviceId = getServiceID(request);
            String mimeType = getMIMEType(request);
            String uri = request.getStringExtra(CanvasProfile.PARAM_URI);
            double x = getX(request);
            double y = getY(request);
            String mode = getMode(request);
            result = onPostDrawImageForHost(request, response, serviceId, mimeType, uri, x, y, mode);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    /**
     * Execute a request.
     * @param request request intent
     * @param response response
     * @param serviceId serviceId
     * @param mimeType mime type
     * @param uri uri of image
     * @param x x coordinate for drawing
     * @param y y coordinate for drawing
     * @param mode mode of rendering
     * @return true if send response immediately, false otherwise
     */
    private boolean onPostDrawImageForHost(final Intent request, final Intent response,
            final String serviceId, final String mimeType, final String uri, 
            final double x, final double y, final String mode) {
        if (uri == null) {
            MessageUtils.setInvalidRequestParameterError(response, "uri is not specied to update a file.");
            return true;
        }

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        // convert mode (if null, invalid value)
        CanvasDrawImageObject.Mode enumMode = CanvasDrawImageObject.convertMode(mode);
        if (enumMode == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }

        // storing parameter to draw object.
        CanvasDrawImageObject drawObj = new CanvasDrawImageObject(uri, enumMode, x, y);

        // start CanvasProfileActivity
        Intent intent = new Intent();
        intent.setClass(getContext(), CanvasProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        drawObj.setValueToIntent(intent);
        getContext().startActivity(intent);

        // return result.
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }
}
