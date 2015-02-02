/*
 PebbleCanvasProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.profile;

import org.deviceconnect.android.deviceplugin.pebble.PebbleDeviceService;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager.OnSendDataListener;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;

/**
 * Pebble 用 Canvasプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class PebbleCanvasProfile extends CanvasProfile {

    /**
     * コンストラクタ.
     */
    public PebbleCanvasProfile() {

    }

    @Override
    protected boolean onPostDrawImage(final Intent request, final Intent response,
            final String serviceId, final String mimeType, final byte[] data, final double x, final double y,
            final String mode) {

        if (data == null) {
            MessageUtils.setInvalidRequestParameterError(response, "data is not specied to update a file.");
            return true;
        }

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (!PebbleUtil.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }

        PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
        byte[] buf = PebbleManager.convertImage(data, mode, x, y);
        if (buf == null) {
        	/* unknown mode-value. */
        	MessageUtils.setInvalidRequestParameterError(response);
        	return true;
        }
        
        mgr.sendDataToPebble(buf, new OnSendDataListener() {
            @Override
            public void onSend(final boolean successed) {
                if (successed) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response);
                }
                getContext().sendBroadcast(response);
            }
        });
        return false;
    }
}

