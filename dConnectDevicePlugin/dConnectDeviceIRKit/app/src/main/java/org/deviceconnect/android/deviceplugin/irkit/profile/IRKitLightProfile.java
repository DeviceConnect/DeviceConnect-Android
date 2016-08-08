/*
 SpheroLightProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualProfileData;
import org.deviceconnect.android.deviceplugin.irkit.service.VirtualService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * 仮想デバイスのLightプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class IRKitLightProfile extends LightProfile {

    /**
     * ライトのID.
     */
    private static final String LIGHT_ID = "1";

    /**
     * ライトの名前.
     */
    private static final String LIGHT_NAME = "照明";

    public IRKitLightProfile() {
        addApi(mGetLightApi);
        addApi(mPostLightApi);
        addApi(mDeleteLightApi);
    }

    private final DConnectApi mGetLightApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            IRKitDBHelper helper = new IRKitDBHelper(getContext());
            List<VirtualProfileData> requests =
                helper.getVirtualProfiles(getServiceID(request), "Light");
            if (requests.size() == 0) {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }

            Bundle[] lights = new Bundle[1];
            lights[0] = new Bundle();
            lights[0].putString(PARAM_LIGHT_ID, LIGHT_ID);
            lights[0].putString(PARAM_NAME, LIGHT_NAME);
            lights[0].putBoolean(PARAM_ON, false);
            response.putExtra(PARAM_LIGHTS, lights);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mPostLightApi = new PostApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return sendLightRequest(getServiceID(request), getLightId(request), "POST", response);
        }
    };

    private final DConnectApi mDeleteLightApi = new DeleteApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return sendLightRequest(getServiceID(request), getLightId(request), "DELETE", response);
        }
    };

    /**
     * ライト用の赤外線を送信する.
     * @param serviceId サービスID
     * @param lightId ライトID
     * @param method HTTP Method
     * @param response レスポンス
     * @return true:同期　false:非同期
     */
    private boolean sendLightRequest(final String serviceId, final String lightId,
                                     final String method, final Intent response) {
        IRKitDBHelper helper = new IRKitDBHelper(getContext());
        List<VirtualProfileData> requests = helper.getVirtualProfiles(serviceId, "Light");
        if (requests.size() == 0) {
            MessageUtils.setNotSupportAttributeError(response);
            return true;
        }

        if (lightId != null && !LIGHT_ID.equals(lightId)) {
            MessageUtils.setInvalidRequestParameterError(response, "Invalid lightId.");
            return true;
        }

        for (VirtualProfileData req : requests) {
            String uri = req.getUri();
            if (req.getUri().equalsIgnoreCase(uri)
                    && req.getMethod().equals(method)
                    && req.getIr() != null) {
                return ((VirtualService) getService()).sendIR(req.getIr(), response);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "IR is not registered for that request");
            }
        }
        return true;
    }

}
