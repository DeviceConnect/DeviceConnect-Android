/*
 HOGPHogpProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.profiles;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * HOGPプロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPHogpProfile extends DConnectProfile {

    public HOGPHogpProfile() {

        // DELETE /hogp
        addApi(new DeleteApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                HOGPMessageService service = (HOGPMessageService) getContext();
                if (service.getHOGPServer() != null) {
                    service.stopHOGPServer();
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP Server is not running.");
                }

                return true;
            }
        });

        // POST /hogp
        addApi(new PostApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                HOGPMessageService service = (HOGPMessageService) getContext();
                if (service.getHOGPServer() != null) {
                    response.putExtra("message", "HOGP Server is already running.");
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    try {
                        service.startHOGPServer();
                        setResult(response, DConnectMessage.RESULT_OK);
                    } catch (Exception e) {
                        MessageUtils.setUnknownError(response, "Failed to start HOGP Server. message=" + e.getMessage());
                    }
                }
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "hogp";
    }
}