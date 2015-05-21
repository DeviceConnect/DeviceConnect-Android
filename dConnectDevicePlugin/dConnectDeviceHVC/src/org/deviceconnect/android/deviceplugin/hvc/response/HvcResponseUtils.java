/*
 HvcResponseUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvc.response;

import java.util.LinkedList;
import java.util.List;

import omron.HVC.HVC;
import omron.HVC.HVC_RES;
import omron.HVC.HVC_RES.DetectionResult;
import omron.HVC.HVC_RES.FaceResult;

import org.deviceconnect.android.deviceplugin.hvc.comm.HvcConvertUtils;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestParams;
import org.deviceconnect.android.profile.HumanDetectProfile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
    }

    /**
     * set body detect result response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     */
    public static void setBodyDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result) {

        List<Bundle> bodyDetects = new LinkedList<Bundle>();
        for (omron.HVC.HVC_RES.DetectionResult r : result.body) {

            // threshold check
            if (r.confidence >= requestParams.getBody().getHvcThreshold()) {
                Bundle bodyDetect = new Bundle();
                HumanDetectProfile.setParamX(bodyDetect,
                        HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectProfile.setParamY(bodyDetect,
                        HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectProfile.setParamWidth(bodyDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectProfile.setParamHeight(bodyDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectProfile.setParamConfidence(bodyDetect,
                        HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));

                bodyDetects.add(bodyDetect);
            }
        }
        if (bodyDetects.size() > 0) {
            HumanDetectProfile.setBodyDetects(response, bodyDetects.toArray(new Bundle[bodyDetects.size()]));
        }
    }

    /**
     * set hand detect result response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     */
    public static void setHandDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result) {

        List<Bundle> handDetects = new LinkedList<Bundle>();
        for (DetectionResult r : result.hand) {

            // threshold check
            if (r.confidence >= requestParams.getHand().getHvcThreshold()) {
                Bundle handDetect = new Bundle();
                HumanDetectProfile.setParamX(handDetect,
                        HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectProfile.setParamY(handDetect,
                        HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectProfile.setParamWidth(handDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectProfile.setParamHeight(handDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectProfile.setParamConfidence(handDetect,
                        HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));

                handDetects.add(handDetect);
            }
        }
        if (handDetects.size() > 0) {
            HumanDetectProfile.setHandDetects(response, handDetects.toArray(new Bundle[handDetects.size()]));
        }
    }

    /**
     * set face detect result response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     */
    public static void setFaceDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result) {

        List<Bundle> faceDetects = new LinkedList<Bundle>();
        for (FaceResult r : result.face) {

            // threshold check
            if (r.confidence >= requestParams.getFace().getHvcThreshold()) {
                Bundle faceDetect = new Bundle();
                HumanDetectProfile.setParamX(faceDetect,
                        HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectProfile.setParamY(faceDetect,
                        HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectProfile.setParamWidth(faceDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                HumanDetectProfile.setParamHeight(faceDetect,
                        HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                HumanDetectProfile.setParamConfidence(faceDetect,
                        HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));

                // face direction.
                if ((result.executedFunc & HVC.HVC_ACTIV_FACE_DIRECTION) != 0) {
                    // threshold check
                    if (r.dir.confidence >= requestParams.getFace().getHvcFaceDirectionThreshold()) {
                        Bundle faceDirectionResult = new Bundle();
                        HumanDetectProfile.setParamYaw(faceDirectionResult, r.dir.yaw);
                        HumanDetectProfile.setParamPitch(faceDirectionResult, r.dir.pitch);
                        HumanDetectProfile.setParamRoll(faceDirectionResult, r.dir.roll);
                        HumanDetectProfile.setParamConfidence(faceDirectionResult,
                                HvcConvertUtils.convertToNormalizeConfidence(r.dir.confidence));
                        HumanDetectProfile.setParamFaceDirectionResults(faceDetect, faceDirectionResult);
                    }
                }
                // age.
                if ((result.executedFunc & HVC.HVC_ACTIV_AGE_ESTIMATION) != 0) {
                    // threshold check
                    if (r.age.confidence >= requestParams.getFace().getHvcAgeThreshold()) {
                        Bundle ageResult = new Bundle();
                        HumanDetectProfile.setParamAge(ageResult, r.age.age);
                        HumanDetectProfile.setParamConfidence(ageResult,
                                HvcConvertUtils.convertToNormalizeConfidence(r.age.confidence));
                        HumanDetectProfile.setParamAgeResults(faceDetect, ageResult);
                    }
                }
                // gender.
                if ((result.executedFunc & HVC.HVC_ACTIV_GENDER_ESTIMATION) != 0) {
                    // threshold check
                    if (r.gen.confidence >= requestParams.getFace().getHvcGenderThreshold()) {
                        Bundle genderResult = new Bundle();
                        HumanDetectProfile.setParamGender(genderResult,
                                (r.gen.gender == HVC.HVC_GEN_MALE ? HumanDetectProfile.VALUE_GENDER_MALE
                                        : HumanDetectProfile.VALUE_GENDER_FEMALE));
                        HumanDetectProfile.setParamConfidence(genderResult,
                                HvcConvertUtils.convertToNormalizeConfidence(r.gen.confidence));
                        HumanDetectProfile.setParamGenderResults(faceDetect, genderResult);
                    }
                }
                // gaze.
                if ((result.executedFunc & HVC.HVC_ACTIV_GAZE_ESTIMATION) != 0) {
                    Bundle gazeResult = new Bundle();
                    HumanDetectProfile.setParamGazeLR(gazeResult, r.gaze.gazeLR);
                    HumanDetectProfile.setParamGazeUD(gazeResult, r.gaze.gazeUD);
                    HumanDetectProfile.setParamConfidence(gazeResult,
                            HvcConvertUtils.convertToNormalizeConfidence(HvcConstants.CONFIDENCE_MAX));
                    HumanDetectProfile.setParamGazeResults(faceDetect, gazeResult);
                }
                // blink.
                if ((result.executedFunc & HVC.HVC_ACTIV_BLINK_ESTIMATION) != 0) {
                    Bundle blinkResult = new Bundle();
                    HumanDetectProfile.setParamLeftEye(blinkResult,
                            HvcConvertUtils.convertToNormalize(r.blink.ratioL, HvcConstants.BLINK_MAX));
                    HumanDetectProfile.setParamRightEye(blinkResult,
                            HvcConvertUtils.convertToNormalize(r.blink.ratioR, HvcConstants.BLINK_MAX));
                    HumanDetectProfile.setParamConfidence(blinkResult,
                            HvcConvertUtils.convertToNormalizeConfidence(HvcConstants.CONFIDENCE_MAX));
                    HumanDetectProfile.setParamBlinkResults(faceDetect, blinkResult);
                }
                // expression.
                if ((result.executedFunc & HVC.HVC_ACTIV_EXPRESSION_ESTIMATION) != 0) {
                    // threshold check
                    double normalizeExpressionScore = HvcConvertUtils.convertToNormalizeExpressionScore(r.exp.score);
                    HumanDetectRequestParams humanDetectRequestParams = requestParams.getHumanDetectRequestParams();
                    if (normalizeExpressionScore >= humanDetectRequestParams.getFace().getExpressionThreshold()) {
                        Bundle expressionResult = new Bundle();
                        HumanDetectProfile.setParamExpression(expressionResult,
                                HvcConvertUtils.convertToNormalizeExpression(r.exp.expression));
                        HumanDetectProfile.setParamConfidence(expressionResult, normalizeExpressionScore);
                        HumanDetectProfile.setParamExpressionResults(faceDetect, expressionResult);
                    }
                }

                faceDetects.add(faceDetect);
            }
        }
        if (faceDetects.size() > 0) {
            HumanDetectProfile.setFaceDetects(response, faceDetects.toArray(new Bundle[faceDetects.size()]));
        }
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
