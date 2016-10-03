/*
SonyCameraSettingsProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.sonycamera.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraDeviceService;
import org.deviceconnect.android.profile.SettingsProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.PutApi;

/**
 * Sony Camera 用 Settings プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraSettingsProfile extends SettingsProfile {

    private final DConnectApi mPutDateApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_DATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return ((SonyCameraDeviceService) getContext()).onPutDate(request, response,
                getServiceID(request), getDate(request));
        }
    };

    public SonyCameraSettingsProfile() {
        addApi(mPutDateApi);
    }
}
