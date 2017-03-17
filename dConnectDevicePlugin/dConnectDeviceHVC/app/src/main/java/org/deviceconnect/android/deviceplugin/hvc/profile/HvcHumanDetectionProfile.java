/*
 HvcHumanDetectProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import org.deviceconnect.android.deviceplugin.hvc.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvc.HvcDeviceApplication;
import org.deviceconnect.android.deviceplugin.hvc.HvcDeviceService;
import org.deviceconnect.android.deviceplugin.hvc.R;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestUtils;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HumanDetectionProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;


/**
 * HVC HumanDetectProfile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcHumanDetectionProfile extends HumanDetectionProfile {

    /**
     * log tag.
     */
    private static final String TAG = HvcHumanDetectionProfile.class.getSimpleName();

    /**
     * Debug.
     */
    private static final Boolean DEBUG = BuildConfig.DEBUG;

    /**
     * error message. {@value}
     */
    protected static final String ERROR_BLE_NOT_AVAILABLE = "ble not available.";

    //
    // Get Detection API
    //
    private final DConnectApi mGetHumanDetectionApi = new GetApi() {
        @Override
        public String getAttribute() {
            return "onDetection";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doGetDetectionProc(request, response, getServiceID(request),
                    HumanDetectKind.HUMAN);
        }
    };

    private final DConnectApi mGetBodyDetectionApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BODY_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doGetDetectionProc(request, response, getServiceID(request),
                HumanDetectKind.BODY);
        }
    };

    private final DConnectApi mGetHandDetectionApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_HAND_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doGetDetectionProc(request, response, getServiceID(request),
                HumanDetectKind.HAND);
        }
    };

    private final DConnectApi mGetFaceDetectionApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_FACE_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doGetDetectionProc(request, response, getServiceID(request),
                HumanDetectKind.FACE);
        }
    };

    //
    // Put Detection API
    //
    private final DConnectApi mPutHumanDetectionApi = new PutApi() {
        @Override
        public String getAttribute() {
            return "onDetection";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doPutDetectionProc(request, response, getServiceID(request),
                    getOrigin(request), HumanDetectKind.HUMAN);
        }
    };

    private final DConnectApi mPutBodyDetectionApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BODY_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doPutDetectionProc(request, response, getServiceID(request),
                getOrigin(request), HumanDetectKind.BODY);
        }
    };

    private final DConnectApi mPutHandDetectionApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_HAND_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doPutDetectionProc(request, response, getServiceID(request),
                getOrigin(request), HumanDetectKind.HAND);
        }
    };

    private final DConnectApi mPutFaceDetectionApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_FACE_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doPutDetectionProc(request, response, getServiceID(request),
                getOrigin(request), HumanDetectKind.FACE);
        }
    };
    
    //
    // Delete Detection API
    //
    private final DConnectApi mDeleteHumanDetectionApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return "onDetection";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doDeleteDetectionProc(request, response, getServiceID(request),
                    getOrigin(request), HumanDetectKind.HUMAN);
        }
    };
    private final DConnectApi mDeleteBodyDetectionApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BODY_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doDeleteDetectionProc(request, response, getServiceID(request),
                getOrigin(request), HumanDetectKind.BODY);
        }
    };

    private final DConnectApi mDeleteHandDetectionApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_HAND_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doDeleteDetectionProc(request, response, getServiceID(request),
                getOrigin(request), HumanDetectKind.HAND);
        }
    };

    private final DConnectApi mDeleteFaceDetectionApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_FACE_DETECTION;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return doDeleteDetectionProc(request, response, getServiceID(request),
                getOrigin(request), HumanDetectKind.FACE);
        }
    };

    /**
     * Constructor.
     */
    public HvcHumanDetectionProfile() {
        addApi(mGetHumanDetectionApi);
        addApi(mGetBodyDetectionApi);
        addApi(mGetHandDetectionApi);
        addApi(mGetFaceDetectionApi);
        addApi(mPutHumanDetectionApi);
        addApi(mPutBodyDetectionApi);
        addApi(mPutHandDetectionApi);
        addApi(mPutFaceDetectionApi);
        addApi(mDeleteHumanDetectionApi);
        addApi(mDeleteBodyDetectionApi);
        addApi(mDeleteHandDetectionApi);
        addApi(mDeleteFaceDetectionApi);
    }
    
    /**
     * Get Detection Process.
     * 
     * @param request request
     * @param response response
     * @param serviceId serviceId
     * @param detectKind detectKind
     * @return send response flag.(true:sent / false: unsent (Send after the
     *         thread has been completed))
     */
    private boolean doGetDetectionProc(final Intent request, final Intent response,
            final String serviceId, final HumanDetectKind detectKind) {

        HvcDeviceApplication.getInstance().checkLocationEnable();

        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // ble not available.
            MessageUtils.setNotSupportProfileError(response, ERROR_BLE_NOT_AVAILABLE);
            return true;
        } else {
            // get parameter.
            HumanDetectRequestParams requestParams;
            try {
                requestParams = HvcDetectRequestUtils.getRequestParams(request, response, detectKind);
            } catch (IllegalStateException e) {
                // BUG: detectKind unknown value.
                MessageUtils.setUnknownError(response, e.getMessage());
                return true;
            } catch (NumberFormatException e) {
                // invalid request parameter error
                MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                return true;
            } catch (IllegalArgumentException e) {
                // invalid request parameter error
                MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                return true;
            }
            if (DEBUG) {
                requestParams.dumpLog(TAG);
            }
            
            // GET API.
            ((HvcDeviceService) getContext()).doGetDetectionProc(detectKind, requestParams, response,
                    serviceId);
            
            // Since returning the response asynchronously, it returns false.
            return false;
        }
    }

    /**
     * do put detection process.
     * @param request request
     * @param response response
     * @param serviceId serviceId
     * @param origin origin
     * @param detectKind detectKind
     * @return send response flag.(true:sent / false: unsent (Send after the
     *         thread has been completed))
     */
    private boolean doPutDetectionProc(final Intent request, final Intent response,
            final String serviceId, final String origin, final HumanDetectKind detectKind) {

        HvcDeviceApplication.getInstance().checkLocationEnable();

        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // ble not available.
            MessageUtils.setNotSupportProfileError(response, ERROR_BLE_NOT_AVAILABLE);
            return true;
        } else {
            // get parameter.
            HumanDetectRequestParams requestParams;
            try {
                requestParams = HvcDetectRequestUtils.getRequestParams(request, response, detectKind);
            } catch (IllegalStateException e) {
                // BUG: detectKind unknown value.
                MessageUtils.setUnknownError(response, e.getMessage());
                return true;
            } catch (NumberFormatException e) {
                // invalid request parameter error
                MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                return true;
            } catch (IllegalArgumentException e) {
                // invalid request parameter error
                MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                return true;
            }
            if (DEBUG) {
                requestParams.dumpLog(TAG);
            }
            
            // register event.
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                ((HvcDeviceService) getContext()).registerDetectionEvent(detectKind, requestParams, response,
                        serviceId, origin);
                return false;
            } else {
                MessageUtils.setIllegalDeviceStateError(response, "Can not register event.");
                return true;
            }

        }
    }
    
    /**
     * do delete detection process.
     * @param request request
     * @param response response
     * @param serviceId serviceId
     * @param origin origin
     * @param detectKind detectKind
     * @return send response flag.(true:sent / false: unsent (Send after the
     *         thread has been completed))
     */
    private boolean doDeleteDetectionProc(final Intent request, final Intent response, final String serviceId,
            final String origin, final HumanDetectKind detectKind) {

        // unregister event.
        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            ((HvcDeviceService) getContext()).unregisterDetectionEvent(detectKind, response, serviceId,
                origin);
            return false;
        } else {
            MessageUtils.setIllegalDeviceStateError(response, "Can not unregister event.");
            return true;
        }
    }
}

