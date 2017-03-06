/*
 HumanDetectProfileConstants.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * Human Detection Profile API constant group.<br>
 * Parameter name of HumanDetect Profile API, interface names, attribute names, define the profile name.
 *
 * @deprecated swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 * @author NTT DOCOMO, INC.
 */
public interface HumanDetectionProfileConstants extends DConnectProfileConstants {
    /**
     * profile name: {@value} .
     */
    public static final String PROFILE_NAME = "humanDetection";

    /**
     * attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_BODY_DETECTION = "onBodyDetection";

    /**
     * attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_HAND_DETECTION = "onHandDetection";

    /**
     * attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_FACE_DETECTION = "onFaceDetection";

    /**
     * path: {@value}.
     */
    public static final String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * path: {@value} .
     */
    public static final String PATH_ON_BODY_DETECTION = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_BODY_DETECTION;

    /**
     * path: {@value} .
     */
    public static final String PATH_ON_HAND_DETECTION = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_HAND_DETECTION;

    /**
     * path: {@value} .
     */
    public static final String PATH_ON_FACE_DETECTION = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_FACE_DETECTION;



    /*--- request ---*/


    /**
     * parameter: {@value} .
     */
    public static final String PARAM_THRESHOLD = "threshold";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_MINWIDTH = "minWidth";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_MINHEIGHT = "minHeight";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_MAXWIDTH = "maxWidth";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_MAXHEIGHT = "maxHeight";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_OPTIONS = "options";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_INTERVAL = "interval";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_EYE_THRESHOLD = "eyeThreshold";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_NOSE_THRESHOLD = "noseThreshold";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_MOUTH_THRESHOLD = "mouthThreshold";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_BLINK_THRESHOLD = "blinkThreshold";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_AGE_THRESHOLD = "ageThreshold";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_GENDER_THRESHOLD = "genderThreshold";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_FACE_DIRECTION_THRESHOLD = "faceDirectionThreshold";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_GAZE_THRESHOLD = "gazeThreshold";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_EXPRESSION_THRESHOLD = "expressionThreshold";





    /**
     * value: {@value} .
     */
    public static final String VALUE_OPTION_EYE = "eye";

    /**
     * value: {@value} .
     */
    public static final String VALUE_OPTION_NOSE = "nose";

    /**
     * value: {@value} .
     */
    public static final String VALUE_OPTION_MOUTH = "mouth";

    /**
     * value: {@value} .
     */
    public static final String VALUE_OPTION_BLINK = "blink";

    /**
     * value: {@value} .
     */
    public static final String VALUE_OPTION_AGE = "age";

    /**
     * value: {@value} .
     */
    public static final String VALUE_OPTION_GENDER = "gender";

    /**
     * value: {@value} .
     */
    public static final String VALUE_OPTION_FACE_DIRECTION = "faceDirection";

    /**
     * value: {@value} .
     */
    public static final String VALUE_OPTION_GAZE = "gaze";

    /**
     * value: {@value} .
     */
    public static final String VALUE_OPTION_EXPRESSION = "expression";



    /*--- response ---*/

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_BODYDETECTS = "bodyDetects";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_HANDDETECTS = "handDetects";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_FACEDETECTS = "faceDetects";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_X = "x";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_Y = "y";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_WIDTH = "width";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_HEIGHT = "height";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_CONFIDENCE = "confidence";





    /**
     * parameter: {@value} .
     */
    public static final String PARAM_EYEPOINTS = "eyePoints";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_LEFTEYE_X = "leftEyeX";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_LEFTEYE_Y = "leftEyeY";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_LEFTEYE_WIDTH = "leftEyeWidth";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_LEFTEYE_HEIGHT = "leftEyeHeight";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_RIGHTEYE_X = "rightEyeX";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_RIGHTEYE_Y = "rightEyeY";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_RIGHTEYE_WIDTH = "rightEyeWidth";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_RIGHTEYE_HEIGHT = "rightEyeHeight";





    /**
     * parameter: {@value} .
     */
    public static final String PARAM_NOSEPOINTS = "nosePoints";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_NOSE_X = "noseX";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_NOSE_Y = "noseY";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_NOSE_WIDTH = "noseWidth";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_NOSE_HEIGHT = "noseHeight";





    /**
     * parameter: {@value} .
     */
    public static final String PARAM_MOUTHPOINTS = "mouthPoints";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_MOUTH_X = "mouthX";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_MOUTH_Y = "mouthY";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_MOUTH_WIDTH = "mouthWidth";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_MOUTH_HEIGHT = "mouthHeight";





    /**
     * parameter: {@value} .
     */
    public static final String PARAM_BLINKRESULTS = "blinkResults";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_LEFTEYE = "leftEye";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_RIGHTEYE = "rightEye";





    /**
     * parameter: {@value} .
     */
    public static final String PARAM_AGERESULTS = "ageResults";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_AGE = "age";





    /**
     * parameter: {@value} .
     */
    public static final String PARAM_GENDERRESULTS = "genderResults";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_GENDER = "gender";

    /**
     * parameter: {@value} .
     */
    public static final String VALUE_GENDER_MALE = "male";

    /**
     * parameter: {@value} .
     */
    public static final String VALUE_GENDER_FEMALE = "female";





    /**
     * parameter: {@value} .
     */
    public static final String PARAM_FACEDIRECTIONRESULTS = "faceDirectionResults";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_YAW = "yaw";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_PITCH = "pitch";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_ROLL = "roll";





    /**
     * parameter: {@value} .
     */
    public static final String PARAM_GAZERESULTS = "gazeResults";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_GAZE_LR = "gazeLR";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_GAZE_UD = "gazeUD";





    /**
     * parameter: {@value} .
     */
    public static final String PARAM_EXPRESSIONRESULTS = "expressionResults";

    /**
     * parameter: {@value} .
     */
    public static final String PARAM_EXPRESSION = "expression";

    /**
     * value: {@value} .
     */
    public static final String VALUE_EXPRESSION_UNKNOWN = "unknown";

    /**
     * value: {@value} .
     */
    public static final String VALUE_EXPRESSION_SMILE = "smile";

    /**
     * value: {@value} .
     */
    public static final String VALUE_EXPRESSION_SURPRISE = "surprise";

    /**
     * value: {@value} .
     */
    public static final String VALUE_EXPRESSION_MAD = "mad";

    /**
     * value: {@value} .
     */
    public static final String VALUE_EXPRESSION_SAD = "sad";

    /**
     * normalize min value.
     */
    public static final double NORMALIZE_VALUE_MIN = 0.0;

    /**
     * normalize max value.
     */
    public static final double NORMALIZE_VALUE_MAX = 1.0;


    /**
     * error: {@value} .
     */
    public static final String ERROR_THRESHOLD_DIFFERENT_TYPE = "threshold is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_THRESHOLD_OUT_OF_RANGE =
            "threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_MINWIDTH_DIFFERENT_TYPE = "minWidth is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_MINWIDTH_OUT_OF_RANGE =
            "minWidth is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_MAXWIDTH_DIFFERENT_TYPE = "maxWidth is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_MAXWIDTH_OUT_OF_RANGE =
            "maxWidth is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_MINHEIGHT_DIFFERENT_TYPE = "minHeight is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_MINHEIGHT_OUT_OF_RANGE =
            "minHeight is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_MAXHEIGHT_DIFFERENT_TYPE = "maxHeight is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_MAXHEIGHT_OUT_OF_RANGE =
            "maxHeight is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_INTERVAL_DIFFERENT_TYPE = "interval is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_INTERVAL_OUT_OF_RANGE =
            "interval is out of range. range:{ %d - %d } [msec]";

    /**
     * error: {@value} .
     */
    public static final String ERROR_EYE_THRESHOLD_DIFFERENT_TYPE = "eye threshold is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_EYE_THRESHOLD_OUT_OF_RANGE =
            "eye threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_NOSE_THRESHOLD_DIFFERENT_TYPE = "nose threshold is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_NOSE_THRESHOLD_OUT_OF_RANGE =
            "nose threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_MOUTH_THRESHOLD_DIFFERENT_TYPE = "mouth threshold is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_MOUTH_THRESHOLD_OUT_OF_RANGE =
            "mouth threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_BLINK_THRESHOLD_DIFFERENT_TYPE = "blink threshold is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_BLINK_THRESHOLD_OUT_OF_RANGE =
            "blink threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_AGE_THRESHOLD_DIFFERENT_TYPE = "age threshold is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_AGE_THRESHOLD_OUT_OF_RANGE =
            "age threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_GENDER_THRESHOLD_DIFFERENT_TYPE = "gender threshold is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_GENDER_THRESHOLD_OUT_OF_RANGE =
            "gender threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_FACE_DIRECTION_THRESHOLD_DIFFERENT_TYPE =
            "face direction threshold is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_FACE_DIRECTION_THRESHOLD_OUT_OF_RANGE =
            "face direction threshold is out of range. range:{ "
                    + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_GAZE_THRESHOLD_DIFFERENT_TYPE = "gaze threshold is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_GAZE_THRESHOLD_OUT_OF_RANGE =
            "gaze threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /**
     * error: {@value} .
     */
    public static final String ERROR_EXPRESSION_THRESHOLD_DIFFERENT_TYPE = "expression threshold is different type.";

    /**
     * error: {@value} .
     */
    public static final String ERROR_EXPRESSION_THRESHOLD_OUT_OF_RANGE =
            "expression threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";


}
