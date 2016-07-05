/*
 LinkingSystemProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.linking.LinkingDevicePluginService;
import org.deviceconnect.android.deviceplugin.linking.setting.SettingActivity;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * System Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class LinkingSystemProfile extends SystemProfile {

    public static final String TAG = "LinkingPlugIn";

    public LinkingSystemProfile() {
        addApi(mDeleteEvents);
    }

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return SettingActivity.class;
    }

    private final DConnectApi mDeleteEvents = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_EVENTS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((LinkingDevicePluginService) getContext()).cleanupSession(getSessionKey(request));
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };
}
