/*
 HVCC2WHumanDetectionProfile
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcp.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hvcp.HVCPDeviceService;
import org.deviceconnect.android.deviceplugin.hvcp.manager.data.HumanDetectKind;
import org.deviceconnect.android.profile.HumanDetectionProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;

/**
 * HVC-P Human Detect Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCPHumanDetectionProfile extends HumanDetectionProfile {

    public HVCPHumanDetectionProfile() {
        addApi(mPutOnDetectionApi);
        addApi(mPutOnBodyDetectionApi);
        addApi(mPutOnHandDetectionApi);
        addApi(mPutOnFaceDetectionApi);
        addApi(mDeleteOnDetectionApi);
        addApi(mDeleteOnBodyDetectionApi);
        addApi(mDeleteOnHandDetectionApi);
        addApi(mDeleteOnFaceDetectionApi);
        addApi(mGetDetectionApi);
        addApi(mGetBodyDetectionApi);
        addApi(mGetHandDetectionApi);
        addApi(mGetFaceDetectionApi);
    }

    private final DConnectApi mPutOnDetectionApi = new PutApi() {
        @Override
        public String getAttribute() {
            return "onDetection";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCPDeviceService) getContext())
                    .registerHumanDetectEvent(request, response, getServiceID(request), HumanDetectKind.HUMAN);
            return false;
        }
    };
    private final DConnectApi mPutOnBodyDetectionApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BODY_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCPDeviceService) getContext())
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
            ((HVCPDeviceService) getContext())
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
            ((HVCPDeviceService) getContext())
                .registerHumanDetectEvent(request, response, getServiceID(request), HumanDetectKind.FACE);
            return false;
        }
    };
    private final DConnectApi mDeleteOnDetectionApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return "onDetection";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCPDeviceService) getContext())
                    .unregisterHumanDetectionProfileEvent(request, response, getServiceID(request), HumanDetectKind.HUMAN);
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
            ((HVCPDeviceService) getContext())
                .unregisterHumanDetectionProfileEvent(request, response, getServiceID(request), HumanDetectKind.BODY);
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
            ((HVCPDeviceService) getContext())
                .unregisterHumanDetectionProfileEvent(request, response, getServiceID(request), HumanDetectKind.HAND);
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
            ((HVCPDeviceService) getContext())
                .unregisterHumanDetectionProfileEvent(request, response, getServiceID(request), HumanDetectKind.FACE);
            return false;
        }
    };
    private final DConnectApi mGetDetectionApi = new GetApi() {

        @Override
        public String getAttribute() {
            return "onDetection";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCPDeviceService) getContext())
                    .doGetHumanDetectionProfile(request, response, getServiceID(request), HumanDetectKind.HUMAN, getOptions(request));
            return false;
        }
    };
    private final DConnectApi mGetBodyDetectionApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BODY_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCPDeviceService) getContext())
                .doGetHumanDetectionProfile(request, response, getServiceID(request), HumanDetectKind.BODY, getOptions(request));
            return false;
        }
    };

    private final DConnectApi mGetHandDetectionApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_HAND_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCPDeviceService) getContext())
                .doGetHumanDetectionProfile(request, response, getServiceID(request), HumanDetectKind.HAND, getOptions(request));
            return false;
        }
    };

    private final DConnectApi mGetFaceDetectionApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_FACE_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ((HVCPDeviceService) getContext())
                .doGetHumanDetectionProfile(request, response, getServiceID(request), HumanDetectKind.FACE, getOptions(request));
            return false;
        }
    };

}
