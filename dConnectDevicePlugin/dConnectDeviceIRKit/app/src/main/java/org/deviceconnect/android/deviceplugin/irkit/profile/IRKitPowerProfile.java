/*
 IRKitPowerProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualProfileData;
import org.deviceconnect.android.deviceplugin.irkit.service.VirtualService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PutApi;

import java.util.List;
/**
 * 仮想デバイスのPowerプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class IRKitPowerProfile extends DConnectProfile {

    public IRKitPowerProfile() {
        // 内部的にはTVProfileを呼ぶ
        // PUT /gotapi/power/
        addApi(new PutApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return ((VirtualService) getService()).sendTVRequest(getServiceID(request), "PUT", "/tv", response);
            }
        });

        // GET /gotapi/power/ Unsupported API


        // DELETE /gotapi/power/
        addApi(new DeleteApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return ((VirtualService) getService()).sendTVRequest(getServiceID(request), "DELETE", "/tv", response);
            }
        });

    }

    @Override
    public String getProfileName() {
        return "power";
    }

}