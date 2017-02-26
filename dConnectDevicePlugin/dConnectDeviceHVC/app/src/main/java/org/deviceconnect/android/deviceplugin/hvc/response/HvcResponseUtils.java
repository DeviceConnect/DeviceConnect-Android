/*
 HvcResponseUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvc.response;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hvc.comm.HvcConvertUtils;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestParams;
import org.deviceconnect.android.profile.HumanDetectionProfile;

import java.util.LinkedList;
import java.util.List;

import omron.HVC.HVC;
import omron.HVC.HVC_RES;
import omron.HVC.HVC_RES.DetectionResult;
import omron.HVC.HVC_RES.FaceResult;

/**
 * HVC response utility.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class HvcResponseUtils {

    /**
     * Constructor.
     */
    private HvcResponseUtils() {
        
    }
    
    
    /**
     * set response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     * @param detectKind detectKind
     */
    public static void setDetectResultResponse(final Intent response, final HumanDetectRequestParams requestParams,
            final HVC_RES result, final HumanDetectKind detectKind) {
        // body detects response.
        if (detectKind == HumanDetectKind.BODY && result.body.size() > 0) {
            setBodyDetectResultResponse(response, new HvcDetectRequestParams(requestParams), result);
        }

        // hand detects response.
        if (detectKind == HumanDetectKind.HAND && result.hand.size() > 0) {
            setHandDetectResultResponse(response, new HvcDetectRequestParams(requestParams), result);
        }

        // face detects response.
        if (detectKind == HumanDetectKind.FACE && result.face.size() > 0) {
            setFaceDetectResultResponse(response, new HvcDetectRequestParams(requestParams), result);
        }

        // Human Detect response.
        if (detectKind == HumanDetectKind.HUMAN) {
            if (result.body.size() > 0 || result.hand.size() > 0 || result.face.size() > 0) {
                setHumanDetectResultResponse(response, true);
            } else {
                setHumanDetectResultResponse(response, false);
            }
        }
    }

    /**
     * set body detect result response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     */
    private static void setBodyDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result) {

        List<Bundle> bodyDetects = new LinkedList<Bundle>();
        for (omron.HVC.HVC_RES.DetectionResult r : result.body) {

            // threshold check
            if (r.confidence >= requestParams.getBody().getHvcThreshold()) {
                Bundle bodyDetect = new Bundle();
                HumanDetectionProfile.setParamX(bodyDetect,
                        HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectionProfile.setParamY(bodyDetect,
                        HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectionProfile.setParamWidth(bodyDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectionProfile.setParamHeight(bodyDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectionProfile.setParamConfidence(bodyDetect,
                        HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));

                bodyDetects.add(bodyDetect);
            }
        }
        if (bodyDetects.size() > 0) {
            HumanDetectionProfile.setBodyDetects(response, bodyDetects.toArray(new Bundle[bodyDetects.size()]));
        }
    }

    /**
     * set hand detect result response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     */
    private static void setHandDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result) {

        List<Bundle> handDetects = new LinkedList<Bundle>();
        for (DetectionResult r : result.hand) {

            // threshold check
            if (r.confidence >= requestParams.getHand().getHvcThreshold()) {
                Bundle handDetect = new Bundle();
                HumanDetectionProfile.setParamX(handDetect,
                        HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectionProfile.setParamY(handDetect,
                        HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectionProfile.setParamWidth(handDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectionProfile.setParamHeight(handDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectionProfile.setParamConfidence(handDetect,
                        HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));

                handDetects.add(handDetect);
            }
        }
        if (handDetects.size() > 0) {
            HumanDetectionProfile.setHandDetects(response, handDetects.toArray(new Bundle[handDetects.size()]));
        }
    }

    /**
     * set face detect result response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     */
    private static void setFaceDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result) {

        List<Bundle> faceDetects = new LinkedList<Bundle>();
        for (FaceResult r : result.face) {

            // threshold check
            if (r.confidence >= requestParams.getFace().getHvcThreshold()) {
                Bundle faceDetect = new Bundle();
                HumanDetectionProfile.setParamX(faceDetect,
                        HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectionProfile.setParamY(faceDetect,
                        HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectionProfile.setParamWidth(faceDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectionProfile.setParamHeight(faceDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectionProfile.setParamConfidence(faceDetect,
                        HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));

                // face direction.
                if ((result.executedFunc & HVC.HVC_ACTIV_FACE_DIRECTION) != 0) {
                    // threshold check
                    if (r.dir.confidence >= requestParams.getFace().getHvcFaceDirectionThreshold()) {
                        Bundle faceDirectionResult = new Bundle();
                        HumanDetectionProfile.setParamYaw(faceDirectionResult, r.dir.yaw);
                        HumanDetectionProfile.setParamPitch(faceDirectionResult, r.dir.pitch);
                        HumanDetectionProfile.setParamRoll(faceDirectionResult, r.dir.roll);
                        HumanDetectionProfile.setParamConfidence(faceDirectionResult,
                                HvcConvertUtils.convertToNormalizeConfidence(r.dir.confidence));
                        HumanDetectionProfile.setParamFaceDirectionResults(faceDetect, faceDirectionResult);
                    }
                }
                // age.
                if ((result.executedFunc & HVC.HVC_ACTIV_AGE_ESTIMATION) != 0) {
                    // threshold check
                    if (r.age.confidence >= requestParams.getFace().getHvcAgeThreshold()) {
                        Bundle ageResult = new Bundle();
                        HumanDetectionProfile.setParamAge(ageResult, r.age.age);
                        HumanDetectionProfile.setParamConfidence(ageResult,
                                HvcConvertUtils.convertToNormalizeConfidence(r.age.confidence));
                        HumanDetectionProfile.setParamAgeResults(faceDetect, ageResult);
                    }
                }
                // gender.
                if ((result.executedFunc & HVC.HVC_ACTIV_GENDER_ESTIMATION) != 0) {
                    // threshold check
                    if (r.gen.confidence >= requestParams.getFace().getHvcGenderThreshold()) {
                        Bundle genderResult = new Bundle();
                        HumanDetectionProfile.setParamGender(genderResult,
                                (r.gen.gender == HVC.HVC_GEN_MALE ? HumanDetectionProfile.VALUE_GENDER_MALE
                                        : HumanDetectionProfile.VALUE_GENDER_FEMALE));
                        HumanDetectionProfile.setParamConfidence(genderResult,
                                HvcConvertUtils.convertToNormalizeConfidence(r.gen.confidence));
                        HumanDetectionProfile.setParamGenderResults(faceDetect, genderResult);
                    }
                }
                // gaze.
                if ((result.executedFunc & HVC.HVC_ACTIV_GAZE_ESTIMATION) != 0) {
                    Bundle gazeResult = new Bundle();
                    HumanDetectionProfile.setParamGazeLR(gazeResult, r.gaze.gazeLR);
                    HumanDetectionProfile.setParamGazeUD(gazeResult, r.gaze.gazeUD);
                    HumanDetectionProfile.setParamConfidence(gazeResult,
                            HvcConvertUtils.convertToNormalizeConfidence(HvcConstants.CONFIDENCE_MAX));
                    HumanDetectionProfile.setParamGazeResults(faceDetect, gazeResult);
                }
                // blink.
                if ((result.executedFunc & HVC.HVC_ACTIV_BLINK_ESTIMATION) != 0) {
                    Bundle blinkResult = new Bundle();
                    HumanDetectionProfile.setParamLeftEye(blinkResult,
                            HvcConvertUtils.convertToNormalize(r.blink.ratioL, HvcConstants.BLINK_MAX));
                    HumanDetectionProfile.setParamRightEye(blinkResult,
                            HvcConvertUtils.convertToNormalize(r.blink.ratioR, HvcConstants.BLINK_MAX));
                    HumanDetectionProfile.setParamConfidence(blinkResult,
                            HvcConvertUtils.convertToNormalizeConfidence(HvcConstants.CONFIDENCE_MAX));
                    HumanDetectionProfile.setParamBlinkResults(faceDetect, blinkResult);
                }
                // expression.
                if ((result.executedFunc & HVC.HVC_ACTIV_EXPRESSION_ESTIMATION) != 0) {
                    // threshold check
                    double normalizeExpressionScore = HvcConvertUtils.convertToNormalizeExpressionScore(r.exp.score);
                    HumanDetectRequestParams humanDetectRequestParams = requestParams.getHumanDetectRequestParams();
                    if (normalizeExpressionScore >= humanDetectRequestParams.getFace().getExpressionThreshold()) {
                        Bundle expressionResult = new Bundle();
                        HumanDetectionProfile.setParamExpression(expressionResult,
                                HvcConvertUtils.convertToNormalizeExpression(r.exp.expression));
                        HumanDetectionProfile.setParamConfidence(expressionResult, normalizeExpressionScore);
                        HumanDetectionProfile.setParamExpressionResults(faceDetect, expressionResult);
                    }
                }

                faceDetects.add(faceDetect);
            }
        }
        if (faceDetects.size() > 0) {
            HumanDetectionProfile.setFaceDetects(response, faceDetects.toArray(new Bundle[faceDetects.size()]));
        }
    }

    /**
     * Set Human Detection.
     * @param response response message
     * @param exist Human exist
     */
    private static void setHumanDetectResultResponse(final Intent response, final boolean exist) {
        Bundle humanDetect = new Bundle();
        humanDetect.putBoolean("exist", exist);
        response.putExtra("humanDetect", humanDetect);
    }
    /**
     * debug log.
     * @param hvcRes HVC response
     * @param tag tag
     */
    public static void debugLogHvcRes(final HVC_RES hvcRes, final String tag) {
        
        Log.d(tag, "--- [HVC_RES] ---");
        
        // bodyDetects.
        for (omron.HVC.HVC_RES.DetectionResult r : hvcRes.body) {
            Log.d(tag, "[body] posX:" + r.posX + " posY:" + r.posY + " size" + r.size + " confidence:" + r.confidence);
        }
        
        // handDetects.
        for (omron.HVC.HVC_RES.DetectionResult r : hvcRes.hand) {
            Log.d(tag, "[hand] posX:" + r.posX + " posY:" + r.posY + " size" + r.size + " confidence:" + r.confidence);
        }
        
        // faceDetects.
        for (omron.HVC.HVC_RES.FaceResult r : hvcRes.face) {
            Log.d(tag, "[face] posX:" + r.posX + " posY:" + r.posY + " size" + r.size + " confidence:" + r.confidence);
            
            // face direction.
            Log.d(tag, "  [faceDirection] yaw:" + r.dir.yaw + " pitch:" + r.dir.pitch + " roll" + r.dir.roll
                    + " confidence:" + r.dir.confidence);
            // age.
            Log.d(tag, "  [age] age:" + r.age + " confidence:" + r.age.confidence);
            // gender.
            Log.d(tag, "  [gender] gender:" + r.gen.gender + " confidence:" + r.gen.confidence);
            // gaze.
            Log.d(tag, "  [gaze] gazeLR:" + r.gaze.gazeLR + " gazeUD:" + r.gaze.gazeUD);
            // blink.
            Log.d(tag, "  [blink] ratioL:" + r.blink.ratioL + " ratioR:" + r.blink.ratioR);
            // expression.
            Log.d(tag, "  [expression] r.exp.expression:" + r.exp.expression);
            
        }
    }
}
