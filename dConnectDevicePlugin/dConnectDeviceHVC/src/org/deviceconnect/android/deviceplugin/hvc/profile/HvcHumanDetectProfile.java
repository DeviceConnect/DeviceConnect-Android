/*
 HvcHumanDetectProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import omron.HVC.HVC;
import omron.HVC.HVC_RES;
import omron.HVC.HVC_RES.DetectionResult;
import omron.HVC.HVC_RES.FaceResult;

import org.deviceconnect.android.deviceplugin.hvc.utils.HVCCommunicationManager;
import org.deviceconnect.android.deviceplugin.hvc.utils.HVCDetectListener;
import org.deviceconnect.android.deviceplugin.hvc.utils.HvcConvertUtils;
import org.deviceconnect.android.deviceplugin.hvc.utils.HvcDetectRequestParams;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HumanDetectProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.os.Bundle;

/**
 * HVC HumanDetectProfile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcHumanDetectProfile extends HumanDetectProfile {

    /**
     * userFunc get error value.
     */
    private static final int ERROR_USEFINC_VALUE = -1;

    /**
     * Detect kind.
     */
    enum DetectKind {

        /**
         * body detect.
         */
        BODY("DETECT_BODY"),

        /**
         * hand detect.
         */
        HAND("DETECT_HAND"),

        /**
         * face detect.
         */
        FACE("DETECT_FACE");

        /**
         * id.
         */
        private final String mId;

        /**
         * constructor.
         * 
         * @param id id
         */
        private DetectKind(final String id) {
            mId = id;
        }

        @Override
        public String toString() {
            return mId;
        }
    };

    /**
     * HVC Communication Manager.
     */
    private HVCCommunicationManager mCommManager = new HVCCommunicationManager();

    @Override
    protected boolean onGetBodyDetection(final Intent request, final Intent response, final String serviceId,
            final List<String> options) {

        // get parameter.
        HvcDetectRequestParams requestParams = getRequestParams(request, DetectKind.BODY);
        if (requestParams == null) {
            // invalid request parameter error
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }

        // start detection
        boolean result = startGetDetectionProc(requestParams, response, serviceId, options, DetectKind.BODY);
        return result;
    }

    @Override
    protected boolean onGetHandDetection(final Intent request, final Intent response, final String serviceId,
            final List<String> options) {
        // get parameter.
        HvcDetectRequestParams requestParams = getRequestParams(request, DetectKind.HAND);
        if (requestParams == null) {
            // invalid request parameter error
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }

        // start detection
        boolean result = startGetDetectionProc(requestParams, response, serviceId, options, DetectKind.HAND);
        return result;
    }

    @Override
    protected boolean onGetFaceDetection(final Intent request, final Intent response, final String serviceId,
            final List<String> options) {
        // get parameter.
        HvcDetectRequestParams requestParams = getRequestParams(request, DetectKind.FACE);
        if (requestParams == null) {
            // invalid request parameter error
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }

        // start detection
        boolean result = startGetDetectionProc(requestParams, response, serviceId, options, DetectKind.FACE);
        return result;
    }

    /**
     * get request parameter.
     * 
     * @param request request
     * @param detectKind detect kind
     * @return true: success / false: invalid request parameter error
     */
    private HvcDetectRequestParams getRequestParams(final Intent request, final DetectKind detectKind) {
        HvcDetectRequestParams requestParams = new HvcDetectRequestParams();

        try {
            // get parameters.(different type error, throw
            // NumberFormatException)
            Double threshold = getThreshold(request);
            Double minWidth = getMinWidth(request);
            Double minHeight = getMinHeight(request);
            Double maxWidth = getMaxWidth(request);
            Double maxHeight = getMaxHeight(request);

            // store parameter.(if data exist, to set. if data not exist, use default value.)
            if (detectKind == DetectKind.BODY) {
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
            } else if (detectKind == DetectKind.HAND) {
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
            } else if (detectKind == DetectKind.FACE) {
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
                return null;
            }
        } catch (NumberFormatException e) {
            // invalid request parameter error
            return null;
        }
        
        // success
        return requestParams;
    }

    /**
     * Start Detection Process.
     * 
     * @param requestParams request
     * @param response response
     * @param serviceId serviceId
     * @param options options
     * @param detectKind detectKind
     * @return send response flag.(true:sent / false: unsent (Send after the
     *         thread has been completed))
     */
    protected boolean startGetDetectionProc(final HvcDetectRequestParams requestParams, final Intent response,
            final String serviceId, final List<String> options, final DetectKind detectKind) {

        // convert useFunc
        int useFunc = convertUseFunc(detectKind, options);
        if (useFunc == ERROR_USEFINC_VALUE) {
            // options unknown parameter.
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }

        // start detect thread.
        HVCCommunicationManager.DetectionResult result = mCommManager.startDetectThread(getContext(),
        /* serviceId */null, useFunc, requestParams, new HVCDetectListener() {
            @Override
            public void onDetectFinished(final HVC_RES result) {
                // set response
                setFaceDetectResultResponse(response, requestParams, result, detectKind);
                // success
                setResult(response, DConnectMessage.RESULT_OK);
                getContext().sendBroadcast(response);
            }

            @Override
            public void onDetectFaceTimeout() {
                // timeout
                MessageUtils.setTimeoutError(response);
                getContext().sendBroadcast(response);
            }

            @Override
            public void onDetectFaceDisconnected() {
                // disconnect
                // TODO: disconnectのエラーコードを再確認する
                MessageUtils.setNotFoundServiceError(response);
                getContext().sendBroadcast(response);
            }
        });
        if (result == HVCCommunicationManager.DetectionResult.RESULT_ERR_SERVICEID_NOT_FOUND) {
            // serviceId not found
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        else if (result == HVCCommunicationManager.DetectionResult.RESULT_ERR_THREAD_ALIVE) {
            // comm thread running
            // TODO: 通信中にリクエストがきた場合のエラーコードを再確認する
            MessageUtils.setIllegalDeviceStateError(response);
            return true;
        }
        else if (result != HVCCommunicationManager.DetectionResult.RESULT_SUCCESS) {
            // BUG: result unknown value.
            MessageUtils.setUnknownError(response, "result unknown value. result:" +  result);
            return true;
        }

        // Since returning the response asynchronously, it returns false.
        return false;
    }

    /**
     * convert useFunc.
     * 
     * @param detectKind detectKind
     * @param options options
     * @return useFunc (if ERROR_USEFINC_VALUE error unknown parameter)
     */
    private int convertUseFunc(final DetectKind detectKind, final List<String> options) {

        ArrayList<OptionInfo> convertDetectKindArray = new ArrayList<OptionInfo>();
        convertDetectKindArray.add(new OptionInfo(DetectKind.BODY.toString(), HVC.HVC_ACTIV_BODY_DETECTION));
        convertDetectKindArray.add(new OptionInfo(DetectKind.HAND.toString(), HVC.HVC_ACTIV_HAND_DETECTION));
        convertDetectKindArray.add(new OptionInfo(DetectKind.FACE.toString(), HVC.HVC_ACTIV_FACE_DETECTION));

        int detectBitFlag = 0;
        OptionInfo convertDetectKind = OptionInfoUtils.search(convertDetectKindArray, detectKind.toString());
        if (convertDetectKind == null) {
            // not match
            return ERROR_USEFINC_VALUE;
        }

        ArrayList<OptionInfo> convertOptionArray = new ArrayList<OptionInfo>();
        convertOptionArray.add(new OptionInfo(VALUE_OPTION_FACE_DIRECTION, HVC.HVC_ACTIV_FACE_DIRECTION));
        convertOptionArray.add(new OptionInfo(VALUE_OPTION_AGE, HVC.HVC_ACTIV_AGE_ESTIMATION));
        convertOptionArray.add(new OptionInfo(VALUE_OPTION_GENDER, HVC.HVC_ACTIV_GENDER_ESTIMATION));
        convertOptionArray.add(new OptionInfo(VALUE_OPTION_GAZE, HVC.HVC_ACTIV_GAZE_ESTIMATION));
        convertOptionArray.add(new OptionInfo(VALUE_OPTION_BLINK, HVC.HVC_ACTIV_BLINK_ESTIMATION));
        convertOptionArray.add(new OptionInfo(VALUE_OPTION_EXPRESSION, HVC.HVC_ACTIV_EXPRESSION_ESTIMATION));

        int optionBitFlag = 0;
        for (String option : options) {
            OptionInfo optionInfo = OptionInfoUtils.search(convertOptionArray, option);
            if (optionInfo == null) {
                // not match
                return ERROR_USEFINC_VALUE;
            }
            optionBitFlag |= optionInfo.getUseFuncBit();
        }

        int useFunc = detectBitFlag | optionBitFlag;
        return useFunc;
    }

    /**
     * set response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     * @param detectKind detectKind
     */
    private void setFaceDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result, final DetectKind detectKind) {

        // body detects response.
        if (detectKind == DetectKind.BODY && result.body.size() > 0) {

            List<Bundle> bodyDetects = new LinkedList<Bundle>();
            for (DetectionResult r : result.body) {

                // threshold check
                if (r.confidence >= requestParams.getFace().getHvcThreshold()) {
                    Bundle bodyDetect = new Bundle();
                    setParamX(bodyDetect, convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamY(bodyDetect, convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamWidth(bodyDetect, convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamHeight(bodyDetect, convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamConfidence(bodyDetect, convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));
                }
            }
            if (bodyDetects.size() > 0) {
                setBodyDetects(response, bodyDetects.toArray(new Bundle[bodyDetects.size()]));
            }
        }

        // hand detects response.
        if (detectKind == DetectKind.HAND && result.hand.size() > 0) {

            List<Bundle> handDetects = new LinkedList<Bundle>();
            for (DetectionResult r : result.hand) {

                // threshold check
                if (r.confidence >= requestParams.getHand().getHvcThreshold()) {
                    Bundle handDetect = new Bundle();
                    setParamX(handDetect, convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamY(handDetect, convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamWidth(handDetect, convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamHeight(handDetect, convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamConfidence(handDetect, convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));
                }
            }
            if (handDetects.size() > 0) {
                setHandDetects(response, handDetects.toArray(new Bundle[handDetects.size()]));
            }
        }

        // face detects response.
        if (detectKind == DetectKind.FACE && result.face.size() > 0) {

            List<Bundle> faceDetects = new LinkedList<Bundle>();
            for (FaceResult r : result.face) {

                // threshold check
                if (r.confidence >= requestParams.getFace().getHvcThreshold()) {
                    Bundle faceDetect = new Bundle();
                    setParamX(faceDetect, convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamY(faceDetect, convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamWidth(faceDetect, convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                    setParamHeight(faceDetect, convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    setParamConfidence(faceDetect, convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));

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
                        setParamLeftEye(blinkResult, convertToNormalize(r.blink.ratioL, HvcConstants.BLINK_MAX));
                        setParamRightEye(blinkResult, convertToNormalize(r.blink.ratioR, HvcConstants.BLINK_MAX));
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
                }
            }
            if (faceDetects.size() > 0) {
                setFaceDetects(response, faceDetects.toArray(new Bundle[faceDetects.size()]));
            }
        }
    }

    /**
     * convert to normalize value from device value.
     * 
     * @param deviceValue device value
     * @param deviceMaxValue device value
     * @return normalizeValue
     */
    private double convertToNormalize(final int deviceValue, final int deviceMaxValue) {
        double normalizeValue = (double) deviceValue / (double) deviceMaxValue;
        return normalizeValue;
    }

    /**
     * OptionInfo.
     */
    private class OptionInfo {
        /**
         * Option string.
         */
        private String mOptionString;
        /**
         * Use function bit flag.
         */
        private int mUseFuncBit;

        /**
         * Constructor.
         * 
         * @param optionString Option string
         * @param useFuncBit Use function bit flag
         */
        public OptionInfo(final String optionString, final int useFuncBit) {
            mOptionString = optionString;
            mUseFuncBit = useFuncBit;
        }

        /**
         * Get option string.
         * 
         * @return option string
         */
        public String getOptionString() {
            return mOptionString;
        }

        /**
         * Get use function bit flag.
         * 
         * @return use function bit flag
         */
        public int getUseFuncBit() {
            return mUseFuncBit;
        }
    }

    /**
     * OptionInfo utility.
     */
    private static class OptionInfoUtils {
        /**
         * Search OptionInfo record.
         * 
         * @param optionInfoArray OptionInfo array.
         * @param optionString option string(search key).
         * @return OptionInfo record(if the search key match) / null (not match)
         */
        public static OptionInfo search(final List<OptionInfo> optionInfoArray, final String optionString) {
            for (OptionInfo optionInfo : optionInfoArray) {
                if (optionString.equals(optionInfo.getOptionString())) {
                    return optionInfo;
                }
            }
            return null;
        }
    }
}
