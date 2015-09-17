/*
 SpheroLightProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.irkit.IRKitDeviceService;
import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualProfileData;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * Lightプロファイル.
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



    @Override
    protected boolean onGetLight(final Intent request, final Intent response, final String serviceId) {

        Bundle[] lights = new Bundle[1];
        lights[0] = new Bundle();
        lights[0].putString(PARAM_LIGHT_ID, LIGHT_ID);
        lights[0].putString(PARAM_NAME, LIGHT_NAME);
        lights[0].putBoolean(PARAM_ON, false);
        response.putExtra(PARAM_LIGHTS, lights);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onPostLight(final Intent request, final Intent response, final String serviceId,
                                  final String lightId, final Integer color, final Double brightness,
                                  final long[] flashing) {
        return sendLightRequest(serviceId, "POST", response);
    }

    @Override
    protected boolean onDeleteLight(final Intent request, final Intent response, final String serviceId,
                                    final String lightId) {
        return sendLightRequest(serviceId, "DELETE", response);
    }

    /**
     * ライト用の赤外線を送信する.
     * @param serviceId サービスID
     * @param method HTTP Method
     * @param response レスポンス
     * @return true:同期　false:非同期
     */
    private boolean sendLightRequest(final String serviceId, final String method,
                                     final Intent response) {
        boolean send = true;
        IRKitDBHelper helper = new IRKitDBHelper(getContext());
        List<VirtualProfileData> requests = helper.getVirtualProfiles(serviceId);
        if (requests.size() == 0) {
            MessageUtils.setInvalidRequestParameterError(response, "Invalid ServiceId");
            return send;
        }
        for (VirtualProfileData req : requests) {
            String uri = req.getUri();
            if (req.getUri().equals(uri)
                    && req.getMethod().equals(method)
                    && req.getIr() != null) {
                final IRKitDeviceService service = (IRKitDeviceService) getContext();
                send = service.sendIR(serviceId, req.getIr(), response);
                break;
            } else {
                MessageUtils.setIllegalServerStateError(response , "IR not register.");
            }
        }
        return send;
    }

}
