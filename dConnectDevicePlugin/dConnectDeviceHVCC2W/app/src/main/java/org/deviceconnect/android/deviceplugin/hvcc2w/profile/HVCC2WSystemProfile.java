/*
 HVCC2WSystemProfile
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.hvcc2w.setting.HVCC2WServiceListActivity;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * HVC-C2W System Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WSystemProfile extends SystemProfile {

    private final DConnectApi mDeleteEventsApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_EVENTS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String sessionKey = getSessionKey(request);
            if (sessionKey == null || sessionKey.length() == 0) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else if (EventManager.INSTANCE.removeEvents(sessionKey)) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    public HVCC2WSystemProfile() {
        addApi(mDeleteEventsApi);
    }

    @Override
    protected Class<? extends Activity> getSettingPageActivity(Intent request, Bundle param) {
        return HVCC2WServiceListActivity.class;
    }
}
