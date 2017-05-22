/*
 HumanDetectProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.HumanDetectionProfileConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Human Detection Profile.
 * 
 * <p>
 * API that provides Setting, the Detection feature for Human Detect Device.<br>
 * 
 * DevicePlugin that provides a HumanDetect operation function of for smart device inherits an
 * equivalent class, and implements the corresponding API thing. <br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class HumanDetectionProfile extends DConnectProfile implements HumanDetectionProfileConstants {

    /**
     * Constructor.
     */
    public HumanDetectionProfile() {
    }

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }


    // ------------------------------------
    // Getter methods.
    // ------------------------------------
    
    /**
     * get options string array list from request.
     * 
     * @param request request parameter.
     * @return options. if nothing, null.
     */
    public static List<String> getOptions(final Intent request) {
        List<String> optionList = null;
        String strOptions = request.getStringExtra(PARAM_OPTIONS);
        if (strOptions != null) {
            String[] options = strOptions.split(",", 0);
            if (options != null) {
            optionList = Arrays.asList(options);
            }
        }
        return optionList;
    }
    
    /**
     * get threshold from request.
     * 
     * @param request request parameter.
     * @return threshold(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getThreshold(final Intent request) {
        if (!checkExistRequestData(request, PARAM_THRESHOLD)) {
            return null;
        }
        Double threshold = parseDouble(request, PARAM_THRESHOLD);
        if (threshold == null) {
            throw new NumberFormatException(ERROR_THRESHOLD_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= threshold && threshold <= NORMALIZE_VALUE_MAX) {
            return threshold;
        } else {
            throw new NumberFormatException(ERROR_THRESHOLD_OUT_OF_RANGE);
        }
    }
    
    /**
     * get minWidth from request.
     * 
     * @param request request parameter.
     * @return minWidth(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getMinWidth(final Intent request) {
        if (!checkExistRequestData(request, PARAM_MINWIDTH)) {
            return null;
        }
        Double minWidth = parseDouble(request, PARAM_MINWIDTH);
        if (minWidth == null) {
            throw new NumberFormatException(ERROR_MINWIDTH_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= minWidth && minWidth <= NORMALIZE_VALUE_MAX) {
            return minWidth;
        } else {
            throw new NumberFormatException(ERROR_MINWIDTH_OUT_OF_RANGE);
        }
    }
    /**
     * get maxWidth from request.
     * 
     * @param request request parameter.
     * @return maxWidth(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getMaxWidth(final Intent request) {
        if (!checkExistRequestData(request, PARAM_MAXWIDTH)) {
            return null;
        }
        Double maxWidth = parseDouble(request, PARAM_MAXWIDTH);
        if (maxWidth == null) {
            throw new NumberFormatException(ERROR_MAXWIDTH_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= maxWidth && maxWidth <= NORMALIZE_VALUE_MAX) {
            return maxWidth;
        } else {
            throw new NumberFormatException(ERROR_MAXWIDTH_OUT_OF_RANGE);
        }
    }
    
    /**
     * get minHeight from request.
     * 
     * @param request request parameter.
     * @return minHeight(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getMinHeight(final Intent request) {
        if (!checkExistRequestData(request, PARAM_MINHEIGHT)) {
            return null;
        }
        Double minHeight = parseDouble(request, PARAM_MINHEIGHT);
        if (minHeight == null) {
            throw new NumberFormatException(ERROR_MINHEIGHT_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= minHeight && minHeight <= NORMALIZE_VALUE_MAX) {
            return minHeight;
        } else {
            throw new NumberFormatException(ERROR_MINHEIGHT_OUT_OF_RANGE);
        }
    }
    /**
     * get maxHeight from request.
     * 
     * @param request request parameter.
     * @return maxHeight(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getMaxHeight(final Intent request) {
        if (!checkExistRequestData(request, PARAM_MAXHEIGHT)) {
            return null;
        }
        Double maxHeight = parseDouble(request, PARAM_MAXHEIGHT);
        if (maxHeight == null) {
            throw new NumberFormatException(ERROR_MAXHEIGHT_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= maxHeight && maxHeight <= NORMALIZE_VALUE_MAX) {
            return maxHeight;
        } else {
            throw new NumberFormatException(ERROR_MAXHEIGHT_OUT_OF_RANGE);
        }
    }
    
    /**
     * get interval from request.
     * 
     * @param request request parameter.
     * @param minInterval minimum interval[msec]
     * @param maxInterval maximum interval[msec]
     * @return interval[msec]. if nothing, null.
     * @throws NumberFormatException exception
     */
    public static Long getInterval(final Intent request,
                                   final long minInterval, final long maxInterval) {
        if (!checkExistRequestData(request, PARAM_INTERVAL)) {
            return null;
        }
        Long interval = parseLong(request, PARAM_INTERVAL);
        if (interval == null) {
            throw new NumberFormatException(ERROR_INTERVAL_DIFFERENT_TYPE);
        }
        if (interval == 0 || minInterval <= interval && interval <= maxInterval) {
            return interval;
        } else {
            String error = String.format(Locale.ENGLISH,
                    ERROR_INTERVAL_OUT_OF_RANGE, minInterval, maxInterval);
            throw new NumberFormatException(error);
        }
    }

    /**
     * get eye threshold from request.
     * 
     * @param request request parameter.
     * @return threshold(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getEyeThreshold(final Intent request) {
        if (!checkExistRequestData(request, PARAM_EYE_THRESHOLD)) {
            return null;
        }
        Double eyeThreshold = parseDouble(request, PARAM_EYE_THRESHOLD);
        if (eyeThreshold == null) {
            throw new NumberFormatException(ERROR_EYE_THRESHOLD_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= eyeThreshold && eyeThreshold <= NORMALIZE_VALUE_MAX) {
            return eyeThreshold;
        } else {
            throw new NumberFormatException(ERROR_EYE_THRESHOLD_OUT_OF_RANGE);
        }
    }

    /**
     * get nose threshold from request.
     * 
     * @param request request parameter.
     * @return threshold(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getNoseThreshold(final Intent request) {
        if (!checkExistRequestData(request, PARAM_NOSE_THRESHOLD)) {
            return null;
        }
        Double noseThreshold = parseDouble(request, PARAM_EYE_THRESHOLD);
        if (noseThreshold == null) {
            throw new NumberFormatException(ERROR_NOSE_THRESHOLD_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= noseThreshold && noseThreshold <= NORMALIZE_VALUE_MAX) {
            return noseThreshold;
        } else {
            throw new NumberFormatException(ERROR_NOSE_THRESHOLD_OUT_OF_RANGE);
        }
    }

    /**
     * get mouth threshold from request.
     * 
     * @param request request parameter.
     * @return threshold(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getMouthThreshold(final Intent request) {
        if (!checkExistRequestData(request, PARAM_MOUTH_THRESHOLD)) {
            return null;
        }
        Double mouthThreshold = parseDouble(request, PARAM_MOUTH_THRESHOLD);
        if (mouthThreshold == null) {
            throw new NumberFormatException(ERROR_MOUTH_THRESHOLD_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= mouthThreshold && mouthThreshold <= NORMALIZE_VALUE_MAX) {
            return mouthThreshold;
        } else {
            throw new NumberFormatException(ERROR_MOUTH_THRESHOLD_OUT_OF_RANGE);
        }
    }

    /**
     * get blink threshold from request.
     * 
     * @param request request parameter.
     * @return threshold(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getBlinkThreshold(final Intent request) {
        if (!checkExistRequestData(request, PARAM_BLINK_THRESHOLD)) {
            return null;
        }
        Double blinkThreshold = parseDouble(request, PARAM_BLINK_THRESHOLD);
        if (blinkThreshold == null) {
            throw new NumberFormatException(ERROR_BLINK_THRESHOLD_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= blinkThreshold && blinkThreshold <= NORMALIZE_VALUE_MAX) {
            return blinkThreshold;
        } else {
            throw new NumberFormatException(ERROR_BLINK_THRESHOLD_OUT_OF_RANGE);
        }
    }

    /**
     * get age threshold from request.
     * 
     * @param request request parameter.
     * @return threshold(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getAgeThreshold(final Intent request) {
        if (!checkExistRequestData(request, PARAM_AGE_THRESHOLD)) {
            return null;
        }
        Double ageThreshold = parseDouble(request, PARAM_AGE_THRESHOLD);
        if (ageThreshold == null) {
            throw new NumberFormatException(ERROR_AGE_THRESHOLD_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= ageThreshold && ageThreshold <= NORMALIZE_VALUE_MAX) {
            return ageThreshold;
        } else {
            throw new NumberFormatException(ERROR_AGE_THRESHOLD_OUT_OF_RANGE);
        }
    }

    /**
     * get gender threshold from request.
     * 
     * @param request request parameter.
     * @return threshold(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getGenderThreshold(final Intent request) {
        if (!checkExistRequestData(request, PARAM_GENDER_THRESHOLD)) {
            return null;
        }
        Double genderThreshold = parseDouble(request, PARAM_AGE_THRESHOLD);
        if (genderThreshold == null) {
            throw new NumberFormatException(ERROR_GENDER_THRESHOLD_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= genderThreshold && genderThreshold <= NORMALIZE_VALUE_MAX) {
            return genderThreshold;
        } else {
            throw new NumberFormatException(ERROR_GENDER_THRESHOLD_OUT_OF_RANGE);
        }
    }

    /**
     * get face direction threshold from request.
     * 
     * @param request request parameter.
     * @return threshold(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getFaceDirectionThreshold(final Intent request) {
        if (!checkExistRequestData(request, PARAM_FACE_DIRECTION_THRESHOLD)) {
            return null;
        }
        Double faceDirectionThreshold = parseDouble(request, PARAM_FACE_DIRECTION_THRESHOLD);
        if (faceDirectionThreshold == null) {
            throw new NumberFormatException(ERROR_FACE_DIRECTION_THRESHOLD_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= faceDirectionThreshold && faceDirectionThreshold <= NORMALIZE_VALUE_MAX) {
            return faceDirectionThreshold;
        } else {
            throw new NumberFormatException(ERROR_FACE_DIRECTION_THRESHOLD_OUT_OF_RANGE);
        }
    }

    /**
     * get gaze threshold from request.
     * 
     * @param request request parameter.
     * @return threshold(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getGazeThreshold(final Intent request) {
        if (!checkExistRequestData(request, PARAM_GAZE_THRESHOLD)) {
            return null;
        }
        Double gazeThreshold = parseDouble(request, PARAM_GAZE_THRESHOLD);
        if (gazeThreshold == null) {
            throw new NumberFormatException(ERROR_GAZE_THRESHOLD_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= gazeThreshold && gazeThreshold <= NORMALIZE_VALUE_MAX) {
            return gazeThreshold;
        } else {
            throw new NumberFormatException(ERROR_GAZE_THRESHOLD_OUT_OF_RANGE);
        }
    }

    /**
     * get expression threshold from request.
     * 
     * @param request request parameter.
     * @return threshold(0.0 ... 1.0). if nothing, null.
     * @throws NumberFormatException
     */
    public static Double getExpressionThreshold(final Intent request) {
        if (!checkExistRequestData(request, PARAM_EXPRESSION_THRESHOLD)) {
            return null;
        }
        Double expressionThreshold = parseDouble(request, PARAM_EXPRESSION_THRESHOLD);
        if (expressionThreshold == null) {
            throw new NumberFormatException(ERROR_EXPRESSION_THRESHOLD_DIFFERENT_TYPE);
        }
        if (NORMALIZE_VALUE_MIN <= expressionThreshold && expressionThreshold <= NORMALIZE_VALUE_MAX) {
            return expressionThreshold;
        } else {
            throw new NumberFormatException(ERROR_EXPRESSION_THRESHOLD_OUT_OF_RANGE);
        }
    }
    
    // ------------------------------------
    // Setter methods.
    // ------------------------------------

    /**
     * set body detects data to response.
     * 
     * @param response response
     * @param bodyDetects body detects data.
     */
    public static void setBodyDetects(final Intent response, final Bundle[] bodyDetects) {
        response.putExtra(PARAM_BODYDETECTS, bodyDetects);
    }

    /**
     * set hand detects data to response.
     * 
     * @param response response
     * @param handDetects hand detects data.
     */
    public static void setHandDetects(final Intent response, final Bundle[] handDetects) {
        response.putExtra(PARAM_HANDDETECTS, handDetects);
    }

    /**
     * set face detects data to response.
     * 
     * @param response response
     * @param faceDetects face detects data.
     */
    public static void setFaceDetects(final Intent response, final Bundle[] faceDetects) {
        response.putExtra(PARAM_FACEDETECTS, faceDetects);
    }

    /**
     * set normalize x value to bundle.
     * @param bundle bundle
     * @param normalizeX normalize X value.
     */
    public static void setParamX(final Bundle bundle, final double normalizeX) {
        bundle.putDouble(PARAM_X, normalizeX);
    }

    /**
     * set normalize y value to bundle.
     * @param bundle bundle
     * @param normalizeY normalize Y value.
     */
    public static void setParamY(final Bundle bundle, final double normalizeY) {
        bundle.putDouble(PARAM_Y, normalizeY);
    }

    /**
     * set normalize width value to bundle.
     * @param bundle bundle
     * @param normalizeWidth normalize width value
     */
    public static void setParamWidth(final Bundle bundle, final double normalizeWidth) {
        bundle.putDouble(PARAM_WIDTH, normalizeWidth);
    }

    /**
     * set normalize height value to bundle.
     * @param bundle bundle
     * @param normalizeHeight normalize height value
     */
    public static void setParamHeight(final Bundle bundle, final double normalizeHeight) {
        bundle.putDouble(PARAM_HEIGHT, normalizeHeight);
    }

    /**
     * set normalize confidence value to bundle.
     * @param bundle bundle
     * @param normalizeConfidence normalize confidence value
     */
    public static void setParamConfidence(final Bundle bundle, final double normalizeConfidence) {
        bundle.putDouble(PARAM_CONFIDENCE, normalizeConfidence);
    }
    
    /**
     * set yaw degree value to bundle.
     * @param bundle bundle
     * @param yawDegree yaw Degree value.
     */
    public static void setParamYaw(final Bundle bundle, final double yawDegree) {
        bundle.putDouble(PARAM_YAW, yawDegree);
    }
    
    /**
     * set roll degree value to bundle.
     * @param bundle bundle
     * @param rollDegree roll Degree value.
     */
    public static void setParamRoll(final Bundle bundle, final double rollDegree) {
        bundle.putDouble(PARAM_ROLL, rollDegree);
    }
    
    /**
     * set pitch degree value to bundle.
     * @param bundle bundle
     * @param pitchDegree pitch Degree value.
     */
    public static void setParamPitch(final Bundle bundle, final double pitchDegree) {
        bundle.putDouble(PARAM_PITCH, pitchDegree);
    }
    
    /**
     * set age value to bundle.
     * @param bundle bundle
     * @param age age value.
     */
    public static void setParamAge(final Bundle bundle, final int age) {
        bundle.putInt(PARAM_AGE, age);
    }
    
    /**
     * set gender value to bundle.
     * @param bundle bundle
     * @param gender gender value.
     */
    public static void setParamGender(final Bundle bundle, final String gender) {
        bundle.putString(PARAM_GENDER, gender);
    }
    
    /**
     * set gazeLR value to bundle.
     * @param bundle bundle
     * @param gazeLR gazeLR value.
     */
    public static void setParamGazeLR(final Bundle bundle, final double gazeLR) {
        bundle.putDouble(PARAM_GAZE_LR, gazeLR);
    }
    
    /**
     * set gazeUD value to bundle.
     * @param bundle bundle
     * @param gazeUD gazeUD value.
     */
    public static void setParamGazeUD(final Bundle bundle, final double gazeUD) {
        bundle.putDouble(PARAM_GAZE_UD, gazeUD);
    }
    
    /**
     * set left eye value to bundle.
     * @param bundle bundle
     * @param leftEye left eye value.
     */
    public static void setParamLeftEye(final Bundle bundle, final double leftEye) {
        bundle.putDouble(PARAM_LEFTEYE, leftEye);
    }
    
    /**
     * set right eye value to bundle.
     * @param bundle bundle
     * @param rightEye right eye value.
     */
    public static void setParamRightEye(final Bundle bundle, final double rightEye) {
        bundle.putDouble(PARAM_RIGHTEYE, rightEye);
    }
    
    /**
     * set expression value to bundle.
     * @param bundle bundle
     * @param expression expression value.
     */
    public static void setParamExpression(final Bundle bundle, final String expression) {
        bundle.putString(PARAM_EXPRESSION, expression);
    }
    
    
    
    
    
    /**
     * set face direction result to bundle.
     * @param bundle bundle
     * @param faceDirectionResults face direction results.
     */
    public static void setParamFaceDirectionResults(final Bundle bundle, final Bundle faceDirectionResults) {
        bundle.putParcelable(PARAM_FACEDIRECTIONRESULTS, faceDirectionResults);
    }
    
    /**
     * set age value to bundle.
     * @param bundle bundle
     * @param ageResults age results.
     */
    public static void setParamAgeResults(final Bundle bundle, final Bundle ageResults) {
        bundle.putParcelable(PARAM_AGERESULTS, ageResults);
    }
    
    /**
     * set gender value to bundle.
     * @param bundle bundle
     * @param genderResults gender results.
     */
    public static void setParamGenderResults(final Bundle bundle, final Bundle genderResults) {
        bundle.putParcelable(PARAM_GENDERRESULTS, genderResults);
    }
    
    /**
     * set gaze value to bundle.
     * @param bundle bundle
     * @param gazeResults gaze results.
     */
    public static void setParamGazeResults(final Bundle bundle, final Bundle gazeResults) {
        bundle.putParcelable(PARAM_GAZERESULTS, gazeResults);
    }
    
    /**
     * set blink value to bundle.
     * @param bundle bundle
     * @param blinkResults blink results.
     */
    public static void setParamBlinkResults(final Bundle bundle, final Bundle blinkResults) {
        bundle.putParcelable(PARAM_BLINKRESULTS, blinkResults);
    }
    
    /**
     * set expression value to bundle.
     * @param bundle bundle
     * @param expressionResults expression result.
     */
    public static void setParamExpressionResults(final Bundle bundle, final Bundle expressionResults) {
        bundle.putParcelable(PARAM_EXPRESSIONRESULTS, expressionResults);
    }
    
    /**
     * check exist request data. 
     * @param request request
     * @param param param
     * @return true: exist / false: not exist
     */
    private static boolean checkExistRequestData(final Intent request, final String param) {
        Bundle b = request.getExtras();
        return b != null && b.get(param) != null;
    }
}
