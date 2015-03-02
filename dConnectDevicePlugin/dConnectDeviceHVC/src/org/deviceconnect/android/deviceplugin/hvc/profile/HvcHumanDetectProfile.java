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
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestParams;
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
    protected static final String ERROR_DETECTKIND_UNKNOWN_VALUE = "detectKind unknown value. detectKind:";
    
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
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {

            // register event.
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                ((HvcDeviceService) getContext()).registerBodyDetectionEvent(request, response, serviceId, sessionKey);
                return false;
            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not register event.");
                return true;
            }

        }
        return true;
    }
    
    @Override
    protected boolean onPutOnHandDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {

            // register event.
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                ((HvcDeviceService) getContext()).registerHandDetectionEvent(request, response, serviceId, sessionKey);
                return false;
            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not register event.");
                return true;
            }

        }
        return true;
    }
    
    @Override
    protected boolean onPutOnFaceDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {

            // register event.
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                ((HvcDeviceService) getContext()).registerFaceDetectionEvent(request, response, serviceId, sessionKey);
                return false;
            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not register event.");
                return true;
            }

        }
        return true;
    }
    
    @Override
    protected boolean onDeleteOnBodyDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {

            // unregister event.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                ((HvcDeviceService) getContext()).unregisterBodyDetectionEvent(response, serviceId, sessionKey);
                return false;

            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not unregister event.");
                return true;

            }
        }
        return true;
    }
    
    @Override
    protected boolean onDeleteOnHandDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {

            // unregister event.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                ((HvcDeviceService) getContext()).unregisterHandDetectionEvent(response, serviceId, sessionKey);
                return false;

            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not unregister event.");
                return true;

            }
        }
        return true;
    }
    
    @Override
    protected boolean onDeleteOnFaceDetection(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {

            // unregister event.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                ((HvcDeviceService) getContext()).unregisterFaceDetectionEvent(response, serviceId, sessionKey);
                return false;

            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not unregister event.");
                return true;

            }
        }
        return true;
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
        final HvcDetectRequestParams requestParams = new HvcDetectRequestParams();
        if (!getRequestParams(requestParams, request, response, HumanDetectKind.BODY)) {
            // error
            return true;
        }
        
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
                requestParams, new HvcDetectListener() {
            @Override
            public void onDetectFinished(final HVC_RES result) {
                // set response
                HvcDeviceService.setDetectResultResponse(response, requestParams, result, detectKind);
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
     * get request parameter.
     * 
     * @param requestParams requestParams
     * @param request request
     * @param response response
     * @param detectKind detect kind
     * @return true: success / false: invalid request parameter error
     */
    private boolean getRequestParams(final HvcDetectRequestParams requestParams, final Intent request,
            final Intent response, final HumanDetectKind detectKind) {

        try {
            // get parameters.(different type error, throw
            // NumberFormatException)
            Double threshold = getThreshold(request);
            Double minWidth = getMinWidth(request);
            Double minHeight = getMinHeight(request);
            Double maxWidth = getMaxWidth(request);
            Double maxHeight = getMaxHeight(request);

            // store parameter.(if data exist, to set. if data not exist, use default value.)
            if (detectKind == HumanDetectKind.BODY) {
                if (threshold != null) {
                    requestParams.setBodyNormalizeThreshold(threshold);
                }
                if (minWidth != null) {
                    requestParams.setBodyNormalizeMinWidth(minWidth);
                }
                if (minHeight != null) {
                    requestParams.setBodyNormalizeMinHeight(minHeight);
                }
                if (maxWidth != null) {
                    requestParams.setBodyNormalizeMaxWidth(maxWidth);
                }
                if (maxHeight != null) {
                    requestParams.setBodyNormalizeMaxHeight(maxHeight);
                }
            } else if (detectKind == HumanDetectKind.HAND) {
                if (threshold != null) {
                    requestParams.setHandNormalizeThreshold(threshold);
                }
                if (minWidth != null) {
                    requestParams.setHandNormalizeMinWidth(minWidth);
                }
                if (minHeight != null) {
                    requestParams.setHandNormalizeMinHeight(minHeight);
                }
                if (maxWidth != null) {
                    requestParams.setHandNormalizeMaxWidth(maxWidth);
                }
                if (maxHeight != null) {
                    requestParams.setHandNormalizeMaxHeight(maxHeight);
                }
            } else if (detectKind == HumanDetectKind.FACE) {
                if (threshold != null) {
                    requestParams.setFaceNormalizeThreshold(threshold);
                }
                if (minWidth != null) {
                    requestParams.setFaceNormalizeMinWidth(minWidth);
                }
                if (minHeight != null) {
                    requestParams.setFaceNormalizeMinHeight(minHeight);
                }
                if (maxWidth != null) {
                    requestParams.setFaceNormalizeMaxWidth(maxWidth);
                }
                if (maxHeight != null) {
                    requestParams.setFaceNormalizeMaxHeight(maxHeight);
                }

                // get parameters.(different type error, throw
                // NumberFormatException)
                Double eyeThreshold = getEyeThreshold(request);
                Double noseThreshold = getNoseThreshold(request);
                Double mouthThreshold = getMouthThreshold(request);
                Double blinkThreshold = getBlinkThreshold(request);
                Double ageThreshold = getAgeThreshold(request);
                Double genderThreshold = getGenderThreshold(request);
                Double faceDirectionThreshold = getFaceDirectionThreshold(request);
                Double gazeThreshold = getGazeThreshold(request);
                Double expressionThreshold = getExpressionThreshold(request);

                if (eyeThreshold != null) {
                    requestParams.setFaceEyeNormalizeThreshold(eyeThreshold);
                }
                if (noseThreshold != null) {
                    requestParams.setFaceNoseNormalizeThreshold(noseThreshold);
                }
                if (mouthThreshold != null) {
                    requestParams.setFaceMouthNormalizeThreshold(mouthThreshold);
                }
                if (blinkThreshold != null) {
                    requestParams.setFaceBlinkNormalizeThreshold(blinkThreshold);
                }
                if (ageThreshold != null) {
                    requestParams.setFaceAgeNormalizeThreshold(ageThreshold);
                }
                if (genderThreshold != null) {
                    requestParams.setFaceGenderNormalizeThreshold(genderThreshold);
                }
                if (faceDirectionThreshold != null) {
                    requestParams.setFaceDirectionNormalizeThreshold(faceDirectionThreshold);
                }
                if (gazeThreshold != null) {
                    requestParams.setFaceGazeNormalizeThreshold(gazeThreshold);
                }
                if (expressionThreshold != null) {
                    requestParams.setFaceExpressionNormalizeThreshold(expressionThreshold);
                }
                
            } else {
                // BUG: detectKind unknown value.
                MessageUtils.setUnknownError(response, ERROR_DETECTKIND_UNKNOWN_VALUE + detectKind.ordinal());
                return false;
            }
        } catch (NumberFormatException e) {
            // invalid request parameter error
            MessageUtils.setInvalidRequestParameterError(response);
            return false;
        }
        
        // success
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

