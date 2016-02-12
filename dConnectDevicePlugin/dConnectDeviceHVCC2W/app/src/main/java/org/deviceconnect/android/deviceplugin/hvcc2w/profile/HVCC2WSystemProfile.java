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

import org.deviceconnect.android.deviceplugin.hvcc2w.setting.SettingActivity;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;

/**
 * HVC-C2W System Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WSystemProfile extends SystemProfile {

    @Override
    protected Class<? extends Activity> getSettingPageActivity(Intent request, Bundle param) {
        return SettingActivity.class;
    }
    @Override
    protected boolean onDeleteEvents(final Intent request, final Intent response, final String sessionKey) {

        if (sessionKey == null || sessionKey.length() == 0) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else if (EventManager.INSTANCE.removeEvents(sessionKey)) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            MessageUtils.setUnknownError(response);
        }

        return true;
    }
}
