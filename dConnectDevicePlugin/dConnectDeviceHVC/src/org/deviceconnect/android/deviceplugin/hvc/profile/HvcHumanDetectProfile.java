/*
 HvcHumanDetectProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import java.util.List;

import org.deviceconnect.android.deviceplugin.hvc.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvc.HvcDeviceService;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestUtils;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HumanDetectProfile;

import android.content.Intent;
import android.content.pm.PackageManager;


/**
 * HVC HumanDetectProfile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcHumanDetectProfile extends HumanDetectProfile {

    /**
     * log tag.
     */
    private static final String TAG = HvcHumanDetectProfile.class.getSimpleName();

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
    
    @Override
    protected boolean onGetBodyDetection(final Intent request, final Intent response, final String serviceId,
            final List<String> options) {

        return doGetDetectionProc(request, response, serviceId, options, HumanDetectKind.BODY);
    }

    @Override
    protected boolean onGetHandDetection(final Intent request, final Intent response, final String serviceId,
            final List<String> options) {

        return doGetDetectionProc(request, response, serviceId, options, HumanDetectKind.HAND);
    }

    @Override
    protected boolean onGetFaceDetection(final Intent request, final Intent response, final String serviceId,
            final List<String> options) {

        return doGetDetectionProc(request, response, serviceId, options, HumanDetectKind.FACE);
    }

    //
    // Put Detection API
    //
    
    @Override
    protected boolean onPutOnBodyDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        return doPutDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.BODY);
    }
    
    @Override
    protected boolean onPutOnHandDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        return doPutDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.HAND);
    }
    
    @Override
    protected boolean onPutOnFaceDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        return doPutDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.FACE);
    }
    
    //
    // Delete Detection API
    //
    
    @Override
    protected boolean onDeleteOnBodyDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        return doDeleteDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.BODY);
    }
    
    @Override
    protected boolean onDeleteOnHandDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        return doDeleteDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.HAND);
    }
    
    @Override
    protected boolean onDeleteOnFaceDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        return doDeleteDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.FACE);
    }
    
    /**
     * Get Detection Process.
     * 
     * @param request request
     * @param response response
     * @param serviceId serviceId
     * @param options options
     * @param detectKind detectKind
     * @return send response flag.(true:sent / false: unsent (Send after the
     *         thread has been completed))
     */
    protected boolean doGetDetectionProc(final Intent request, final Intent response,
            final String serviceId, final List<String> options, final HumanDetectKind detectKind) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // ble not available.
            MessageUtils.setNotSupportProfileError(response, ERROR_BLE_NOT_AVAILABLE);
            return true;
        } else {
            // get parameter.
            HumanDetectRequestParams requestParams = null;
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
     * @param sessionKey sessionKey
     * @param detectKind detectKind
     * @return send response flag.(true:sent / false: unsent (Send after the
     *         thread has been completed))
     */
    protected boolean doPutDetectionProc(final Intent request, final Intent response,
            final String serviceId, final String sessionKey, final HumanDetectKind detectKind) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // ble not available.
            MessageUtils.setNotSupportProfileError(response, ERROR_BLE_NOT_AVAILABLE);
            return true;
        } else {
            // get parameter.
            HumanDetectRequestParams requestParams = null;
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
            requestParams.dumpLog(TAG);
            
            // register event.
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                ((HvcDeviceService) getContext()).registerDetectionEvent(detectKind, requestParams, response,
                        serviceId, sessionKey);
                return false;
            } else {
                MessageUtils.setIllegalDeviceStateError(response, "Can not register event.");
                return true;
            }

        }
        return true;
    }
    
    /**
     * do delete detection process.
     * @param request request
     * @param response response
     * @param serviceId serviceId
     * @param sessionKey sessionKey
     * @param detectKind detectKind
     * @return send response flag.(true:sent / false: unsent (Send after the
     *         thread has been completed))
     */
    private boolean doDeleteDetectionProc(final Intent request, final Intent response, final String serviceId,
            final String sessionKey, final HumanDetectKind detectKind) {
        
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // unregister event.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                ((HvcDeviceService) getContext()).unregisterDetectionEvent(detectKind, response, serviceId,
                        sessionKey);
                return false;
            } else {
                MessageUtils.setIllegalDeviceStateError(response, "Can not unregister event.");
                return true;
            }
        }
        return true;
    }
    
    
    /**
     * create empty serviceId error response.
     * 
     * @param response response
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * create empty session key error response.
     * 
     * @param response response
     */
    private void createEmptySessionKey(final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response, "SessionKey not found");
    }
}

