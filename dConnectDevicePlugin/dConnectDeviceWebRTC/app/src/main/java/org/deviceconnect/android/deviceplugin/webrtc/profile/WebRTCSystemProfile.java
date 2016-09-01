/*
 WebRTCSystemProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.webrtc.WebRTCApplication;
import org.deviceconnect.android.deviceplugin.webrtc.setting.SettingActivity;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * System Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebRTCSystemProfile extends SystemProfile {

    private final DConnectApi mDeleteEventsApi = new DeleteApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String sessionKey = getSessionKey(request);

            if (sessionKey == null || sessionKey.length() == 0) {
                MessageUtils.setInvalidRequestParameterError(response);
                return true;
            }
            boolean eventsDeleted = deleteEvents();
            if (!eventsDeleted) {
                MessageUtils.setUnknownError(response);
            }
            if (EventManager.INSTANCE.removeEvents(sessionKey)) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    public WebRTCSystemProfile() {
        addApi(mDeleteEventsApi);
    }

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return SettingActivity.class;
    }

    private boolean deleteEvents() {
        DConnectMessageService s = (DConnectMessageService) getContext();
        WebRTCApplication app = (WebRTCApplication) s.getApplication();
        app.destroyPeer();
        return true;
    }
}
