/*
 HvcSystemProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.hvc.setting.HvcServiceListActivity;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * HVC DevicePlugin, System Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcSystemProfile extends SystemProfile {

    private final DConnectApi mDeleteEventsApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_EVENTS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            if (EventManager.INSTANCE.removeEvents(getOrigin(request))) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownError(response);
            }

            return true;
        }
    };

    public HvcSystemProfile() {
        super();
        addApi(mDeleteEventsApi);
    }

    /**
     * set setting activity.
     * 
     * @param request request
     * @param bundle bundle
     * 
     * @return setting activity
     */
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle bundle) {
        return HvcServiceListActivity.class;
    }
}
