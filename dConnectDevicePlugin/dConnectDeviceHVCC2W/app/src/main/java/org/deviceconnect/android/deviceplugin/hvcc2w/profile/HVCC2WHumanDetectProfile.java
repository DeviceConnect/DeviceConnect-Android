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
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;

/**
 * HVC-C2W Human Detect Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WHumanDetectProfile extends HumanDetectProfile {

    public HVCC2WHumanDetectProfile() {
        addApi(mPutOnBodyDetectionApi);
        addApi(mPutOnHandDetectionApi);
        addApi(mPutOnFaceDetectionApi);
        addApi(mDeleteOnBodyDetectionApi);
        addApi(mDeleteOnHandDetectionApi);
        addApi(mDeleteOnFaceDetectionApi);
        addApi(mGetBodyDetectionApi);
        addApi(mGetHandDetectionApi);
        addApi(mGetFaceDetectionApi);
    }

    private final DConnectApi mPutOnBodyDetectionApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BODY_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCC2WDeviceService) getContext())
                .registerHumanDetectEvent(request, response, getServiceID(request), HumanDetectKind.BODY);
            return false;
        }
    };

    private final DConnectApi mPutOnHandDetectionApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_HAND_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCC2WDeviceService) getContext())
                .registerHumanDetectEvent(request, response, getServiceID(request), HumanDetectKind.HAND);
            return false;
        }
    };

    private final DConnectApi mPutOnFaceDetectionApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_FACE_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCC2WDeviceService) getContext())
                .registerHumanDetectEvent(request, response, getServiceID(request), HumanDetectKind.FACE);
            return false;
        }
    };

    private final DConnectApi mDeleteOnBodyDetectionApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BODY_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCC2WDeviceService) getContext())
                .unregisterHumanDetectProfileEvent(request, response, getServiceID(request), HumanDetectKind.BODY);
            return false;
        }
    };

    private final DConnectApi mDeleteOnHandDetectionApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_HAND_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCC2WDeviceService) getContext())
                .unregisterHumanDetectProfileEvent(request, response, getServiceID(request), HumanDetectKind.HAND);
            return false;
        }
    };

    private final DConnectApi mDeleteOnFaceDetectionApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_FACE_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCC2WDeviceService) getContext())
                .unregisterHumanDetectProfileEvent(request, response, getServiceID(request), HumanDetectKind.FACE);
            return false;
        }
    };

    private final DConnectApi mGetBodyDetectionApi = new GetApi() {
        @Override
        public String getInterface() {
            return INTERFACE_DETECTION;
        }

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BODY_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCC2WDeviceService) getContext())
                .doGetHumanDetectProfile(request, response, getServiceID(request), HumanDetectKind.BODY, getOptions(request));
            return false;
        }
    };

    private final DConnectApi mGetHandDetectionApi = new GetApi() {
        @Override
        public String getInterface() {
            return INTERFACE_DETECTION;
        }

        @Override
        public String getAttribute() {
            return ATTRIBUTE_HAND_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCC2WDeviceService) getContext())
                .doGetHumanDetectProfile(request, response, getServiceID(request), HumanDetectKind.HAND, getOptions(request));
            return false;
        }
    };

    private final DConnectApi mGetFaceDetectionApi = new GetApi() {
        @Override
        public String getInterface() {
            return INTERFACE_DETECTION;
        }

        @Override
        public String getAttribute() {
            return ATTRIBUTE_FACE_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCC2WDeviceService) getContext())
                .doGetHumanDetectProfile(request, response, getServiceID(request), HumanDetectKind.FACE, getOptions(request));
            return false;
        }
    };

}
