/*
 HVCC2WHumanDetectProfile
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcp.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hvcp.HVCPDeviceService;
import org.deviceconnect.android.deviceplugin.hvcp.manager.data.HumanDetectKind;
import org.deviceconnect.android.profile.HumanDetectProfile;

import java.util.List;

/**
 * HVC-P Human Detect Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCPHumanDetectProfile extends HumanDetectProfile {


    @Override
    protected boolean onPutOnBodyDetection(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {

        ((HVCPDeviceService) getContext()).registerHumanDetectEvent(request, response, serviceId, HumanDetectKind.BODY);
        return false;
    }


    @Override
    protected boolean onPutOnHandDetection(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {
        ((HVCPDeviceService) getContext()).registerHumanDetectEvent(request, response, serviceId, HumanDetectKind.HAND);
        return false;
    }

    @Override
    protected boolean onPutOnFaceDetection(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {
        ((HVCPDeviceService) getContext()).registerHumanDetectEvent(request, response, serviceId, HumanDetectKind.FACE);
        return false;
    }

    @Override
    protected boolean onDeleteOnBodyDetection(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {

        ((HVCPDeviceService) getContext()).unregisterHumanDetectProfileEvent(request, response, serviceId, HumanDetectKind.BODY);
        return false;
    }


    @Override
    protected boolean onDeleteOnHandDetection(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {

        ((HVCPDeviceService) getContext()).unregisterHumanDetectProfileEvent(request, response, serviceId, HumanDetectKind.HAND);
        return false;
    }

    @Override
    protected boolean onDeleteOnFaceDetection(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {
        ((HVCPDeviceService) getContext()).unregisterHumanDetectProfileEvent(request, response, serviceId, HumanDetectKind.FACE);
        return false;
    }

    @Override
    protected boolean onGetBodyDetection(final Intent request, final Intent response,
                                         final String serviceId, final List<String> options) {

        ((HVCPDeviceService) getContext()).doGetHumanDetectProfile(request, response, serviceId, HumanDetectKind.BODY, options);
        return false;
    }



    @Override
    protected boolean onGetHandDetection(final Intent request, final Intent response,
                                         final String serviceId, final List<String> options) {

        ((HVCPDeviceService) getContext()).doGetHumanDetectProfile(request, response, serviceId, HumanDetectKind.HAND, options);
        return false;
    }

    @Override
    protected boolean onGetFaceDetection(final Intent request, final Intent response,
                                         final String serviceId, final List<String> options) {

        ((HVCPDeviceService) getContext()).doGetHumanDetectProfile(request, response, serviceId, HumanDetectKind.FACE, options);
        return false;
    }



}
