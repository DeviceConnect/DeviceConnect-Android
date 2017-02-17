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
                return sendPowerRequest(getServiceID(request), "PUT", "/tv", response);
            }
        });

        // GET /gotapi/power/ Unsupported API


        // DELETE /gotapi/power/
        addApi(new DeleteApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return sendPowerRequest(getServiceID(request), "DELETE", "/tv", response);
            }
        });

    }

    @Override
    public String getProfileName() {
        return "power";
    }


    /**
     * ライト用の赤外線を送信する.
     * @param serviceId サービスID
     * @param method HTTP Method
     * @param uri URI
     * @param response レスポンス
     * @return true:同期　false:非同期
     */
    private boolean sendPowerRequest(final String serviceId, final String method, final String uri,
                                     final Intent response) {
        boolean send = true;
        IRKitDBHelper helper = new IRKitDBHelper(getContext());
        List<VirtualProfileData> requests = helper.getVirtualProfiles(serviceId, "TV");
        if (requests.size() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "Invalid ServiceId");
            return send;
        }
        VirtualProfileData vData = null;
        for (VirtualProfileData req : requests) {
            if (req.getUri().equalsIgnoreCase(uri)
                    && req.getMethod().equals(method)
                    && req.getIr() != null) {
                vData = req;
                break;
            }
        }
        if (vData != null) {
            send = ((VirtualService) getService()).sendIR(vData.getIr(), response);
        } else {
            MessageUtils.setInvalidRequestParameterError(response, "IR is not registered for that request");
        }
        return send;
    }
}