/*
 HumanDetectProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;



/**
 * Human Detect Profile API constants.<br/>
 * Define the parameter name, interface name, attribute name, profile name of HumanDetectAPI .
 * 
 * @author NTT DOCOMO, INC.
 */
public interface HumanDetectProfileConstants extends DConnectProfileConstants {

    /**
     * HVC device name prefix.
     */
    String DEVICE_NAME_PREFIX = "OMRON_HVC.*|omron_hvc.*";
    
    
    
    
    
    /**
     * profile nme: {@value} .
     */
    String PROFILE_NAME = "humandetect";

    /**
     * interface: {@value} .
     */
    String INTERFACE_DETECTION = "detection";

    /**
     * attribute: {@value} .
     */
    String ATTRIBUTE_BODY_DETECTION = "body";

    /**
     * attribute: {@value} .
     */
    String ATTRIBUTE_HAND_DETECTION = "hand";

    /**
     * attribute: {@value} .
     */
    String ATTRIBUTE_FACE_DETECTION = "face";

    /**
     * attribute: {@value} .
     */
    String ATTRIBUTE_ON_BODY_DETECTION = "onbodydetection";

    /**
     * attribute: {@value} .
     */
    String ATTRIBUTE_ON_HAND_DETECTION = "onhanddetection";

    /**
     * attribute: {@value} .
     */
    String ATTRIBUTE_ON_FACE_DETECTION = "onfacedetection";

    /**
     * path: {@value}.
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;
    
    /**
     * path: {@value} .
     */
    String PATH_BODY_DETECTION = PATH_PROFILE + SEPARATOR + INTERFACE_DETECTION + SEPARATOR + ATTRIBUTE_BODY_DETECTION;
    
    /**
     * path: {@value} .
     */
    String PATH_HAND_DETECTION = PATH_PROFILE + SEPARATOR + INTERFACE_DETECTION + SEPARATOR + ATTRIBUTE_HAND_DETECTION;
    
    /**
     * path: {@value} .
     */
    String PATH_FACE_DETECTION = PATH_PROFILE + SEPARATOR + INTERFACE_DETECTION + SEPARATOR + ATTRIBUTE_FACE_DETECTION;
    
    /**
     * path: {@value} .
     */
    String PATH_ON_BODY_DETECTION = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_BODY_DETECTION;
    
    /**
     * path: {@value} .
     */
    String PATH_ON_HAND_DETECTION = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_HAND_DETECTION;
    
    /**
     * path: {@value} .
     */
    String PATH_ON_FACE_DETECTION = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ON_FACE_DETECTION;
    
    
    
    /*--- request ---*/
    
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_THRESHOLD = "threshold";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_MINWIDTH = "minWidth";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_MINHEIGHT = "minHeight";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_MAXWIDTH = "maxWidth";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_MAXHEIGHT = "maxHeight";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_OPTIONS = "options";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_EYE_THRESHOLD = "eyeThreshold";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_NOSE_THRESHOLD = "noseThreshold";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_MOUTH_THRESHOLD = "mouthThreshold";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_BLINK_THRESHOLD = "blinkThreshold";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_AGE_THRESHOLD = "ageThreshold";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_GENDER_THRESHOLD = "genderThreshold";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_FACE_DIRECTION_THRESHOLD = "faceDirectionThreshold";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_GAZE_THRESHOLD = "gazeThreshold";
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_EXPRESSION_THRESHOLD = "expressionThreshold";
    
    
    
    
    
    /** 
     * value: {@value} .
     */
    String VALUE_OPTION_EYE = "eye";
    
    /** 
     * value: {@value} .
     */
    String VALUE_OPTION_NOSE = "nose";
    
    /** 
     * value: {@value} .
     */
    String VALUE_OPTION_MOUTH = "mouth";
    
    /** 
     * value: {@value} .
     */
    String VALUE_OPTION_BLINK = "blink";
    
    /** 
     * value: {@value} .
     */
    String VALUE_OPTION_AGE = "age";
    
    /** 
     * value: {@value} .
     */
    String VALUE_OPTION_GENDER = "gender";
    
    /** 
     * value: {@value} .
     */
    String VALUE_OPTION_FACE_DIRECTION = "faceDirection";
    
    /** 
     * value: {@value} .
     */
    String VALUE_OPTION_GAZE = "gaze";
    
    /** 
     * value: {@value} .
     */
    String VALUE_OPTION_EXPRESSION = "expression";



    /*--- response ---*/

    /** 
     * parameter: {@value} .
     */
    String PARAM_BODYDETECTS = "bodyDetects";

    /** 
     * parameter: {@value} .
     */
    String PARAM_HANDDETECTS = "handDetects";

    /** 
     * parameter: {@value} .
     */
    String PARAM_FACEDETECTS = "faceDetects";

    /** 
     * parameter: {@value} .
     */
    String PARAM_X = "x";

    /** 
     * parameter: {@value} .
     */
    String PARAM_Y = "y";

    /** 
     * parameter: {@value} .
     */
    String PARAM_WIDTH = "width";

    /** 
     * parameter: {@value} .
     */
    String PARAM_HEIGHT = "height";

    /** 
     * parameter: {@value} .
     */
    String PARAM_CONFIDENCE = "confidence";





    /** 
     * parameter: {@value} .
     */
    String PARAM_EYEPOINTS = "eyePoints";

    /** 
     * parameter: {@value} .
     */
    String PARAM_LEFTEYE_X = "leftEyeX";

    /** 
     * parameter: {@value} .
     */
    String PARAM_LEFTEYE_Y = "leftEyeY";

    /** 
     * parameter: {@value} .
     */
    String PARAM_LEFTEYE_WIDTH = "leftEyeWidth";

    /** 
     * parameter: {@value} .
     */
    String PARAM_LEFTEYE_HEIGHT = "leftEyeHeight";

    /** 
     * parameter: {@value} .
     */
    String PARAM_RIGHTEYE_X = "rightEyeX";

    /** 
     * parameter: {@value} .
     */
    String PARAM_RIGHTEYE_Y = "rightEyeY";

    /** 
     * parameter: {@value} .
     */
    String PARAM_RIGHTEYE_WIDTH = "rightEyeWidth";

    /** 
     * parameter: {@value} .
     */
    String PARAM_RIGHTEYE_HEIGHT = "rightEyeHeight";





    /** 
     * parameter: {@value} .
     */
    String PARAM_NOSEPOINTS = "nosePoints";

    /** 
     * parameter: {@value} .
     */
    String PARAM_NOSE_X = "noseX";

    /** 
     * parameter: {@value} .
     */
    String PARAM_NOSE_Y = "noseY";

    /** 
     * parameter: {@value} .
     */
    String PARAM_NOSE_WIDTH = "noseWidth";

    /** 
     * parameter: {@value} .
     */
    String PARAM_NOSE_HEIGHT = "noseHeight";





    /** 
     * parameter: {@value} .
     */
    String PARAM_MOUTHPOINTS = "mouthPoints";

    /** 
     * parameter: {@value} .
     */
    String PARAM_MOUTH_X = "mouthX";

    /** 
     * parameter: {@value} .
     */
    String PARAM_MOUTH_Y = "mouthY";

    /** 
     * parameter: {@value} .
     */
    String PARAM_MOUTH_WIDTH = "mouthWidth";

    /** 
     * parameter: {@value} .
     */
    String PARAM_MOUTH_HEIGHT = "mouthHeight";





    /** 
     * parameter: {@value} .
     */
    String PARAM_BLINKRESULTS = "blinkResults";

    /** 
     * parameter: {@value} .
     */
    String PARAM_LEFTEYE = "leftEye";

    /** 
     * parameter: {@value} .
     */
    String PARAM_RIGHTEYE = "rightEye";





    /** 
     * parameter: {@value} .
     */
    String PARAM_AGERESULTS = "ageResults";

    /** 
     * parameter: {@value} .
     */
    String PARAM_AGE = "age";





    /** 
     * parameter: {@value} .
     */
    String PARAM_GENDERRESULTS = "genderResults";

    /** 
     * parameter: {@value} .
     */
    String PARAM_GENDER = "gender";

    /** 
     * parameter: {@value} .
     */
    String VALUE_GENDER_MALE = "male";

    /** 
     * parameter: {@value} .
     */
    String VALUE_GENDER_FEMALE = "female";





    /** 
     * parameter: {@value} .
     */
    String PARAM_FACEDIRECTIONRESULTS = "faceDirectionResults";

    /** 
     * parameter: {@value} .
     */
    String PARAM_YAW = "yaw";

    /** 
     * parameter: {@value} .
     */
    String PARAM_PITCH = "pitch";

    /** 
     * parameter: {@value} .
     */
    String PARAM_ROLL = "roll";





    /** 
     * parameter: {@value} .
     */
    String PARAM_GAZERESULTS = "gazeResults";

    /** 
     * parameter: {@value} .
     */
    String PARAM_GAZE_LR = "gazeLR";

    /** 
     * parameter: {@value} .
     */
    String PARAM_GAZE_UD = "gazeUD";





    /** 
     * parameter: {@value} .
     */
    String PARAM_EXPRESSIONRESULTS = "expressionResults";

    /** 
     * parameter: {@value} .
     */
    String PARAM_EXPRESSION = "expression";

    /** 
     * value: {@value} .
     */
    String VALUE_EXPRESSION_UNKNOWN = "unknown";

    /** 
     * value: {@value} .
     */
    String VALUE_EXPRESSION_SMILE = "smile";

    /** 
     * value: {@value} .
     */
    String VALUE_EXPRESSION_SURPRISE = "surprise";

    /** 
     * value: {@value} .
     */
    String VALUE_EXPRESSION_MAD = "mad";

    /** 
     * value: {@value} .
     */
    String VALUE_EXPRESSION_SAD = "sad";





    /** 
     * normalize max value.
     */
    double NORMALIZE_VALUE_MAX = 1.0;
    
}
