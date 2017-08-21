/*
 HOGPJoystickProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.profiles;

import android.content.Intent;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Joystickプロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPJoystickProfile extends DConnectProfile {

    public HOGPJoystickProfile() {

        // POST /joystick
        addApi(new PostApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "joystick";
    }
}