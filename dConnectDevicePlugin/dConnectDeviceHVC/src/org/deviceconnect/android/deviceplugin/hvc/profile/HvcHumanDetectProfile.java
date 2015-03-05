/*
 HvcHumanDetectProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import java.util.List;

import omron.HVC.HVC_RES;

import org.deviceconnect.android.deviceplugin.hvc.HvcDeviceService;
import org.deviceconnect.android.deviceplugin.hvc.comm.HvcCommManager;
import org.deviceconnect.android.deviceplugin.hvc.comm.HvcDetectListener;
import org.deviceconnect.android.deviceplugin.hvc.comm.HvcConvertUtils;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestUtils;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HumanDetectProfile;
import org.deviceconnect.message.DConnectMessage;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;


/**
 * HVC HumanDetectProfile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcHumanDetectProfile extends HumanDetectProfile {

    /** Error. */
    private static final int ERROR_VALUE_IS_NULL = 100;

    /**
     * error message. {@value}
     */
    protected static final String ERROR_BLE_NOT_AVAILABLE = "ble not available.";
    
    /**
     * error message. {@value}
     */
    protected static final String ERROR_DEVICE_ERROR_STATUS = "device error. status:";
    
    /**
     * error message. {@value}
     */
    protected static final String ERROR_DEVICE_IS_BUSY = "device is busy.";
    
    /**
     * error message. {@value}
     */
    protected static final String ERROR_RESULT_UNKNOWN_VALUE = "result unknown value. status:";
    
    /**
     * error message. {@value}
     */
    protected static final String ERROR_DETECT = "detect error. status:";
    
    /**
     * error message. {@value}
     */
    protected static final String ERROR_DEVICE_CONNECT = "device connect error. status:";
    
    /**
     * error message. {@value}
     */
    protected static final String ERROR_REQUEST_DETECT = "request detect error. status:";
    
// TODO: timeout error.
    
    
    
    //
    // Get Detection API
    //
    
    @Override
    protected boolean onGetBodyDetection(final Intent request, final Intent response, final String serviceId,
            final List<String> options) {
        
        boolean result = doGetDetectionProc(request, response, serviceId, options, HumanDetectKind.BODY);
        return result;
    }

    @Override
    protected boolean onGetHandDetection(final Intent request, final Intent response, final String serviceId,
            final List<String> options) {
        
        boolean result = doGetDetectionProc(request, response, serviceId, options, HumanDetectKind.HAND);
        return result;
    }

    @Override
    protected boolean onGetFaceDetection(final Intent request, final Intent response, final String serviceId,
            final List<String> options) {
        
        boolean result = doGetDetectionProc(request, response, serviceId, options, HumanDetectKind.FACE);
        return result;
    }

    //
    // Put Detection API
    //
    
    @Override
    protected boolean onPutOnBodyDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        
        boolean result = doPutDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.BODY);
        return result;
    }
    
    @Override
    protected boolean onPutOnHandDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        
        boolean result = doPutDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.HAND);
        return result;
    }
    
    @Override
    protected boolean onPutOnFaceDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        
        boolean result = doPutDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.FACE);
        return result;
    }
    
    //
    // Delete Detection API
    //
    
    @Override
    protected boolean onDeleteOnBodyDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        boolean result = doDeleteDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.BODY);
        return result;
    }
    
    @Override
    protected boolean onDeleteOnHandDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        boolean result = doDeleteDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.HAND);
        return result;
    }
    
    @Override
    protected boolean onDeleteOnFaceDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        boolean result = doDeleteDetectionProc(request, response, serviceId, sessionKey, HumanDetectKind.FACE);
        return result;
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
        
        // get bluetooth device from serviceId.
        BluetoothDevice device = HvcCommManager.searchDevices(serviceId);
        if (device == null) {
            // bluetooth device not found.
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        
        // ble os available?
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // ble not available.
            MessageUtils.setNotSupportProfileError(response, ERROR_BLE_NOT_AVAILABLE);
            return true;
        }
        
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
            MessageUtils
                    .setInvalidRequestParameterError(response, HvcDetectRequestUtils.ERROR_PARAMETER_DIFFERENT_TYPE);
            return true;
        }
        final HumanDetectRequestParams requestParamsFinal = requestParams;
        
        // convert useFunc
        Integer useFunc = HvcConvertUtils.convertUseFunc(detectKind, options);
        if (useFunc == null) {
            // options unknown parameter.
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }
        
        // get comm manager.
        HvcCommManager commManager = ((HvcDeviceService) getContext()).getCommManager(serviceId);

        // start detect thread.
        HvcCommManager.DetectionResult result = commManager.startDetectThread(getContext(), device, useFunc,
                requestParamsFinal, new HvcDetectListener() {
            @Override
            public void onDetectFinished(final HVC_RES result) {
                // set response
                HvcDeviceService.setDetectResultResponse(response, requestParamsFinal, result, detectKind);
                // success
                setResult(response, DConnectMessage.RESULT_OK);
                getContext().sendBroadcast(response);
            }

            @Override
            public void onDetectFaceDisconnected() {
                // disconnect
            }

            @Override
            public void onDetectError(final int status) {
                // device error
                MessageUtils.setUnknownError(response, ERROR_DETECT + status);
                getContext().sendBroadcast(response);
            }

            @Override
            public void onConnectError(final int status) {
                // device error
                MessageUtils.setUnknownError(response, ERROR_DEVICE_CONNECT + status);
                getContext().sendBroadcast(response);
            }

            @Override
            public void onRequestDetectError(final int status) {
                // device error
                MessageUtils.setUnknownError(response, ERROR_REQUEST_DETECT + status);
                getContext().sendBroadcast(response);
            }
        });
        if (result == HvcCommManager.DetectionResult.RESULT_ERR_SERVICEID_NOT_FOUND) {
            // serviceId not found
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else if (result == HvcCommManager.DetectionResult.RESULT_ERR_THREAD_ALIVE) {
            // comm thread running
            MessageUtils.setIllegalDeviceStateError(response, ERROR_DEVICE_IS_BUSY);
            return true;
        } else if (result != HvcCommManager.DetectionResult.RESULT_SUCCESS) {
            // BUG: result unknown value.
            MessageUtils.setUnknownError(response, ERROR_RESULT_UNKNOWN_VALUE +  result);
            return true;
        }

        // Since returning the response asynchronously, it returns false.
        return false;
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
                MessageUtils.setInvalidRequestParameterError(response,
                        HvcDetectRequestUtils.ERROR_PARAMETER_DIFFERENT_TYPE);
                return true;
            }
            
            // register event.
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                ((HvcDeviceService) getContext()).registerDetectionEvent(detectKind, requestParams, response, serviceId, sessionKey);
                return false;
            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not register event.");
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
// TODO: イベント削除しようとすると「Can not unregister event.」と表示される不具合を調査する。
            // unregister event.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                ((HvcDeviceService) getContext()).unregisterDetectionEvent(detectKind, response, serviceId, sessionKey);
                return false;

            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not unregister event.");
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

        MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "SessionKey not found");
    }
}

