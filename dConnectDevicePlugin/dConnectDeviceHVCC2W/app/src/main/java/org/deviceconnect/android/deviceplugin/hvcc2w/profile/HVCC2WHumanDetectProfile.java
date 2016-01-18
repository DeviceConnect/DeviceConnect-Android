/*
 HVCC2WHumanDetectProfile
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hvcc2w.HVCC2WDeviceService;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HumanDetectKind;
import org.deviceconnect.android.profile.HumanDetectProfile;

import java.util.List;

/**
 * HVC-C2W Human Detect Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WHumanDetectProfile extends HumanDetectProfile {


    @Override
    protected boolean onPutOnBodyDetection(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {

        ((HVCC2WDeviceService) getContext()).registerHumanDetectEvent(request, response, serviceId, HumanDetectKind.BODY);
        return false;
    }


    @Override
    protected boolean onPutOnHandDetection(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {
        ((HVCC2WDeviceService) getContext()).registerHumanDetectEvent(request, response, serviceId, HumanDetectKind.HAND);
        return false;
    }

    @Override
    protected boolean onPutOnFaceDetection(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {
        ((HVCC2WDeviceService) getContext()).registerHumanDetectEvent(request, response, serviceId, HumanDetectKind.FACE);
        return false;
    }

    @Override
    protected boolean onDeleteOnBodyDetection(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {

        ((HVCC2WDeviceService) getContext()).unregisterHumanDetectProfileEvent(request, response, serviceId, HumanDetectKind.BODY);
        return false;
    }


    @Override
    protected boolean onDeleteOnHandDetection(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {

        ((HVCC2WDeviceService) getContext()).unregisterHumanDetectProfileEvent(request, response, serviceId, HumanDetectKind.HAND);
        return false;
    }

    @Override
    protected boolean onDeleteOnFaceDetection(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {
        ((HVCC2WDeviceService) getContext()).unregisterHumanDetectProfileEvent(request, response, serviceId, HumanDetectKind.FACE);
        return false;
    }

    @Override
    protected boolean onGetBodyDetection(final Intent request, final Intent response,
                                         final String serviceId, final List<String> options) {

        ((HVCC2WDeviceService) getContext()).doGetHumanDetectProfile(request, response, serviceId, HumanDetectKind.BODY);
        return false;
    }



    @Override
    protected boolean onGetHandDetection(final Intent request, final Intent response,
                                         final String serviceId, final List<String> options) {

        ((HVCC2WDeviceService) getContext()).doGetHumanDetectProfile(request, response, serviceId, HumanDetectKind.HAND);
        return false;
    }

    @Override
    protected boolean onGetFaceDetection(final Intent request, final Intent response,
                                         final String serviceId, final List<String> options) {

        ((HVCC2WDeviceService) getContext()).doGetHumanDetectProfile(request, response, serviceId, HumanDetectKind.FACE);
        return false;
    }



}
