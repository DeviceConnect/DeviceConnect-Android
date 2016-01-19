/*
 HVCC2WFaceRecognizeProfile
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hvcc2w.HVCC2WDeviceService;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCStorage;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.FaceRecognitionObject;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HumanDetectKind;
import org.deviceconnect.android.profile.FaceRecognizeProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.List;

/**
 * HVC-C2W Face Recognize Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WFaceRecognizeProfile extends FaceRecognizeProfile {

    @Override
    protected boolean onPutOnFaceRecognize(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {
        ((HVCC2WDeviceService) getContext()).registerHumanDetectEvent(request, response, serviceId, HumanDetectKind.RECOGNIZE);

        return false;

    }

    @Override
    protected boolean onDeleteOnFaceRecognize(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {
        ((HVCC2WDeviceService) getContext()).unregisterHumanDetectProfileEvent(request, response, serviceId, HumanDetectKind.RECOGNIZE);
        return false;

    }

    @Override
    protected boolean onGetOnFaceRecognize(final Intent request, final Intent response,
                                           final String serviceId, final List<String> options) {
        ((HVCC2WDeviceService) getContext()).doGetHumanDetectProfile(request, response, serviceId, HumanDetectKind.RECOGNIZE, options);
        return false;

    }

    @Override
    protected boolean onGetFaceRecognize(final Intent request, final Intent response,
                                         final String serviceId) {
        List<FaceRecognitionObject> results = HVCStorage.INSTANCE.getFaceRecognitionDatas(null);
        String[] recognizes = new String[results.size()];
        int index = 0;
        for (FaceRecognitionObject o : results) {
            recognizes[index++] = o.getName();
        }
        FaceRecognizeProfile.setParamNames(response, recognizes);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;

    }
}
