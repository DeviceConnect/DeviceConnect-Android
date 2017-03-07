/*
 KadecotPowerProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotHomeAirConditioner;
import org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice.KadecotResult;
import org.deviceconnect.android.deviceplugin.kadecot.service.KadecotService;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.profile.api.DeleteApi;

/**
 * Power Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotPowerProfile extends DConnectProfile {

    public KadecotPowerProfile() {

        // PUT /gotapi/power/
        addApi(new PutApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                powerOn(request, response);
                return false;
            }
        });

        // GET /gotapi/power/
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                getPowerStatus(request, response);
                return false;
            }
        });

        // DELETE /gotapi/power/
        addApi(new DeleteApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                powerOff(request, response);
                return false;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "power";
    }

    /**
     * Put ppower status.
     *
     * @param request Request.
     * @param response Response.
     */
    private void powerOn(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request),
                KadecotHomeAirConditioner.POWERSTATE_ON);
        KadecotService.powerOn(response, result);
        sendResponse(response);
    }
    /**
     * Get power status.
     *
     * @param request Request.
     * @param response Response.
     */
    private void getPowerStatus(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request),
                KadecotHomeAirConditioner.POWERSTATE_GET);
        KadecotService.getPowerStatus(response, result);
        sendResponse(response);
    }

    /**
     * Power Off.
     *
     * @param request Request.
     * @param response Response.
     */
    private void powerOff(final Intent request, final Intent response) {
        KadecotResult result = KadecotService.requestKadecotServer(getContext(),response, getServiceID(request),
                KadecotHomeAirConditioner.POWERSTATE_OFF);
        KadecotService.powerOff(response, result);
        sendResponse(response);
    }
}