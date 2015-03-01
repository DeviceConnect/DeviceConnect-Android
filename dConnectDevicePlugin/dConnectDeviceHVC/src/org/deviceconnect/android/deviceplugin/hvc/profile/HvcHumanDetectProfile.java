/*
 HvcHumanDetectProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import omron.HVC.HVC;
import omron.HVC.HVC_RES;
import omron.HVC.HVC_RES.DetectionResult;
import omron.HVC.HVC_RES.FaceResult;

import org.deviceconnect.android.deviceplugin.hvc.BuildConfig;
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
import android.os.Bundle;
import android.util.Log;


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
    protected static final String ERROR_DEVICE_COMM_BUSY = "device communication busy.";
    
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

            // イベントの登録
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

            // イベントの登録
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

            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
//dumpIntent(request, "AAA", "request ");
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

            // イベントの解除
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

            // イベントの解除
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

            // イベントの解除
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
                setDetectResultResponse(response, requestParams, result, detectKind);
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
            MessageUtils.setIllegalDeviceStateError(response, ERROR_DEVICE_COMM_BUSY);
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
     * set response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     * @param detectKind detectKind
     */
    private void setDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result, final HumanDetectKind detectKind) {

        // body detects response.
        if (detectKind == HumanDetectKind.BODY && result.body.size() > 0) {

            List<Bundle> bodyDetects = new LinkedList<Bundle>();
            for (DetectionResult r : result.body) {

                // threshold check
                if (r.confidence >= requestParams.getFace().getHvcThreshold()) {
                    Bundle bodyDetect = new Bundle();
                    setParamX(bodyDetect, HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamY(bodyDetect, HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamWidth(bodyDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamHeight(bodyDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamConfidence(bodyDetect,
                            HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));
                    
                    bodyDetects.add(bodyDetect);
                }
            }
            if (bodyDetects.size() > 0) {
                setBodyDetects(response, bodyDetects.toArray(new Bundle[bodyDetects.size()]));
            }
        }

        // hand detects response.
        if (detectKind == HumanDetectKind.HAND && result.hand.size() > 0) {

            List<Bundle> handDetects = new LinkedList<Bundle>();
            for (DetectionResult r : result.hand) {

                // threshold check
                if (r.confidence >= requestParams.getHand().getHvcThreshold()) {
                    Bundle handDetect = new Bundle();
                    setParamX(handDetect, HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamY(handDetect, HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamWidth(handDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamHeight(handDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamConfidence(handDetect,
                            HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));
                    
                    handDetects.add(handDetect);
                }
            }
            if (handDetects.size() > 0) {
                setHandDetects(response, handDetects.toArray(new Bundle[handDetects.size()]));
            }
        }

        // face detects response.
        if (detectKind == HumanDetectKind.FACE && result.face.size() > 0) {

            List<Bundle> faceDetects = new LinkedList<Bundle>();
            for (FaceResult r : result.face) {

                // threshold check
                if (r.confidence >= requestParams.getFace().getHvcThreshold()) {
                    Bundle faceDetect = new Bundle();
                    setParamX(faceDetect, HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamY(faceDetect, HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamWidth(faceDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamHeight(faceDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamConfidence(faceDetect,
                            HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));

                    // face direction.
                    if ((result.executedFunc & HVC.HVC_ACTIV_FACE_DIRECTION) != 0) {

                        // threshold check
                        if (r.dir.confidence >= requestParams.getFace().getHvcFaceDirectionThreshold()) {
                            Bundle faceDirectionResult = new Bundle();
                            setParamYaw(faceDirectionResult, r.dir.yaw);
                            setParamPitch(faceDirectionResult, r.dir.pitch);
                            setParamRoll(faceDirectionResult, r.dir.roll);
                            setParamConfidence(faceDirectionResult,
                                    HvcConvertUtils.convertToNormalizeConfidence(r.dir.confidence));
                            setParamFaceDirectionResult(faceDetect, faceDirectionResult);
                        }
                    }
                    // age.
                    if ((result.executedFunc & HVC.HVC_ACTIV_AGE_ESTIMATION) != 0) {

                        // threshold check
                        if (r.age.confidence >= requestParams.getFace().getHvcAgeThreshold()) {
                            Bundle ageResult = new Bundle();
                            setParamAge(ageResult, r.age.age);
                            setParamConfidence(ageResult,
                                    HvcConvertUtils.convertToNormalizeConfidence(r.age.confidence));
                            setParamAgeResult(faceDetect, ageResult);
                        }
                    }
                    // gender.
                    if ((result.executedFunc & HVC.HVC_ACTIV_GENDER_ESTIMATION) != 0) {

                        // threshold check
                        if (r.gen.confidence >= requestParams.getFace().getHvcGenderThreshold()) {
                            Bundle genderResult = new Bundle();
                            setParamGender(genderResult, r.gen.gender == HVC.HVC_GEN_MALE ? VALUE_GENDER_MALE
                                    : VALUE_GENDER_FEMALE);
                            setParamConfidence(genderResult,
                                    HvcConvertUtils.convertToNormalizeConfidence(r.gen.confidence));
                            setParamGenderResult(faceDetect, genderResult);
                        }
                    }
                    // gaze.
                    if ((result.executedFunc & HVC.HVC_ACTIV_GAZE_ESTIMATION) != 0) {
                        Bundle gazeResult = new Bundle();
                        setParamGazeLR(gazeResult, r.gaze.gazeLR);
                        setParamGazeUD(gazeResult, r.gaze.gazeUD);
                        setParamConfidence(gazeResult,
                                HvcConvertUtils.convertToNormalizeConfidence(HvcConstants.CONFIDENCE_MAX));
                        setParamGazeResult(faceDetect, gazeResult);
                    }
                    // blink.
                    if ((result.executedFunc & HVC.HVC_ACTIV_BLINK_ESTIMATION) != 0) {
                        Bundle blinkResult = new Bundle();
                        setParamLeftEye(blinkResult,
                                HvcConvertUtils.convertToNormalize(r.blink.ratioL, HvcConstants.BLINK_MAX));
                        setParamRightEye(blinkResult,
                                HvcConvertUtils.convertToNormalize(r.blink.ratioR, HvcConstants.BLINK_MAX));
                        setParamConfidence(blinkResult,
                                HvcConvertUtils.convertToNormalizeConfidence(HvcConstants.CONFIDENCE_MAX));
                        setParamBlinkResult(faceDetect, blinkResult);
                    }
                    // expression.
                    if ((result.executedFunc & HVC.HVC_ACTIV_EXPRESSION_ESTIMATION) != 0) {

                        // threshold check
                        double normalizeExpressionScore = HvcConvertUtils
                                .convertToNormalizeExpressionScore(r.exp.score);
                        if (normalizeExpressionScore >= requestParams.getFace().getExpressionThreshold()) {
                            Bundle expressionResult = new Bundle();
                            setParamExpression(expressionResult,
                                    HvcConvertUtils.convertToNormalizeExpression(r.exp.expression));
                            setParamConfidence(expressionResult, normalizeExpressionScore);
                            setParamExpressionResult(faceDetect, expressionResult);
                        }
                    }
                    
                    faceDetects.add(faceDetect);
                }
            }
            if (faceDetects.size() > 0) {
                setFaceDetects(response, faceDetects.toArray(new Bundle[faceDetects.size()]));
            }
        }
    }


    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {

        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * セッションキーが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptySessionKey(final Intent response) {

        MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "SessionKey not found");
    }
    
    public static void dumpIntent(Intent intent, String tag, String prefix){
        if (BuildConfig.DEBUG) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Set<String> keys = bundle.keySet();
                Iterator<String> it = keys.iterator();
                Log.d(tag, prefix + " dump start");
                while (it.hasNext()) {
                    String key = it.next();
                    Log.d(tag, prefix + " [" + key + "=" + bundle.get(key) + "]");
                }
                Log.d(tag, prefix + " dump end");
            }
        }
    }
}

