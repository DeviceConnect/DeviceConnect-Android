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

import android.content.Context;
import android.content.Intent;


/**
 * Canvas Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostCanvasProfile extends CanvasProfile {

    @Override
    protected boolean onPostDrawImage(final Intent request, final Intent response,
            final String deviceId, final String mimeType, final byte[] data, final double x, final double y,
            final String mode) {
        
        if (data == null) {
            MessageUtils.setInvalidRequestParameterError(response, "data is not specied to update a file.");
            return true;
        }

        if (deviceId == null) {
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
        CanvasDrawImageObject sendDrawObject = new CanvasDrawImageObject(data, enumMode, x, y);

        // start CanvasProfileActivity
        Context context = getContext();
        Intent intent = new Intent();
        intent.setClass(context, CanvasProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendDrawObject.setValueToIntent(intent);
        context.startActivity(intent);

        // return result.
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }
}
