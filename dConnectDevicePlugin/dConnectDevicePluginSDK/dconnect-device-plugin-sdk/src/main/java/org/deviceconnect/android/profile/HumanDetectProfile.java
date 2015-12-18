/*
 HumanDetectProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.deviceconnect.android.message.MessageUtils;

import android.content.Intent;
import android.os.Bundle;

/**
 * Human Detect Profile.
 * 
 * <p>
 * API that provides Setting, the Detection feature for Human Detect Device.<br/>
 * 
 * DevicePlugin that provides a HumanDetect operation function of for smart device inherits an
 * equivalent class, and implements the corresponding API thing. <br/>
 * </p>
 * 
 * <h1>API provides methods</h1>
 * <p>
 * For requests to each API of HumanDetectProfile, following callback method group is automatically
 * invoked.<br/>
 * Subclasses override the methods for API provided by the DevicePlugin from the following methods
 * group, to implement the functionality that.<br/>
 * Features that are not overridden automatically return the response as non-compliant API.
 * </p>
 * @author NTT DOCOMO, INC.
 */
public abstract class HumanDetectProfile extends DConnectProfile {
    
    /**
     * profile name: {@value} .
     */
    public static final String PROFILE_NAME = "humandetect";

    /**
     * interface: {@value} .
     */
    public static final String INTERFACE_DETECTION = "detection";

    /**
     * attribute: {@value} .
     */
    public static final String ATTRIBUTE_BODY_DETECTION = "body";

    /**
     * attribute: {@value} .
     */
    public static final String ATTRIBUTE_HAND_DETECTION = "hand";

    /**
     * attribute: {@value} .
     */
    public static final String ATTRIBUTE_FACE_DETECTION = "face";

    /**
     * attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_BODY_DETECTION = "onbodydetection";

    /**
     * attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_HAND_DETECTION = "onhanddetection";

    /**
     * attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_FACE_DETECTION = "onfacedetection";

    /**
     * path: {@value}.
     */
    public static final String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;
    
    /**
     * path: {@value} .
     */
    public static final String PATH_BODY_DETECTION = PATH_PROFILE + SEPARATOR + INTERFACE_DETECTION
            + SEPARATOR + ATTRIBUTE_BODY_DETECTION;
    
    /**
     * path: {@value} .
     */
    public static final String PATH_HAND_DETECTION = PATH_PROFILE + SEPARATOR + INTERFACE_DETECTION
            + SEPARATOR + ATTRIBUTE_HAND_DETECTION;
    
    /**
     * path: {@value} .
     */
    public static final String PATH_FACE_DETECTION = PATH_PROFILE + SEPARATOR + INTERFACE_DETECTION
            + SEPARATOR + ATTRIBUTE_FACE_DETECTION;
    
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
    private static final String ERROR_THRESHOLD_DIFFERENT_TYPE = "threshold is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_THRESHOLD_OUT_OF_RANGE = 
            "threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_MINWIDTH_DIFFERENT_TYPE = "minWidth is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_MINWIDTH_OUT_OF_RANGE = 
            "minWidth is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_MAXWIDTH_DIFFERENT_TYPE = "maxWidth is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_MAXWIDTH_OUT_OF_RANGE = 
            "maxWidth is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_MINHEIGHT_DIFFERENT_TYPE = "minHeight is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_MINHEIGHT_OUT_OF_RANGE = 
            "minHeight is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_MAXHEIGHT_DIFFERENT_TYPE = "maxHeight is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_MAXHEIGHT_OUT_OF_RANGE = 
            "maxHeight is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_INTERVAL_DIFFERENT_TYPE = "interval is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_INTERVAL_OUT_OF_RANGE = 
            "interval is out of range. range:{ %d - %d } [msec]";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_EYE_THRESHOLD_DIFFERENT_TYPE = "eye threshold is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_EYE_THRESHOLD_OUT_OF_RANGE = 
            "eye threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_NOSE_THRESHOLD_DIFFERENT_TYPE = "nose threshold is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_NOSE_THRESHOLD_OUT_OF_RANGE = 
            "nose threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_MOUTH_THRESHOLD_DIFFERENT_TYPE = "mouth threshold is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_MOUTH_THRESHOLD_OUT_OF_RANGE = 
            "mouth threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_BLINK_THRESHOLD_DIFFERENT_TYPE = "blink threshold is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_BLINK_THRESHOLD_OUT_OF_RANGE = 
            "blink threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_AGE_THRESHOLD_DIFFERENT_TYPE = "age threshold is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_AGE_THRESHOLD_OUT_OF_RANGE = 
            "age threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_GENDER_THRESHOLD_DIFFERENT_TYPE = "gender threshold is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_GENDER_THRESHOLD_OUT_OF_RANGE = 
            "gender threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_FACE_DIRECTION_THRESHOLD_DIFFERENT_TYPE =
            "face direction threshold is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_FACE_DIRECTION_THRESHOLD_OUT_OF_RANGE = 
            "face direction threshold is out of range. range:{ "
                    + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_GAZE_THRESHOLD_DIFFERENT_TYPE = "gaze threshold is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_GAZE_THRESHOLD_OUT_OF_RANGE = 
            "gaze threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_EXPRESSION_THRESHOLD_DIFFERENT_TYPE = "expression threshold is different type.";

    /** 
     * error: {@value} .
     */
    private static final String ERROR_EXPRESSION_THRESHOLD_OUT_OF_RANGE = 
            "expression threshold is out of range. range:{ " + NORMALIZE_VALUE_MIN + " - " + NORMALIZE_VALUE_MAX + " }";
    
    
    

    /**
     * Constructor.
     */
    public HumanDetectProfile() {
    }

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }
    
    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        String interfac = getInterface(request);
        String attribute = getAttribute(request);
        boolean result = true;
        if (INTERFACE_DETECTION.equals(interfac) && ATTRIBUTE_BODY_DETECTION.equals(attribute)) {
            String serviceId = getServiceID(request);
            List<String> options = getOptions(request);
            result = onGetBodyDetection(request, response, serviceId, options);
        } else if (INTERFACE_DETECTION.equals(interfac) && ATTRIBUTE_HAND_DETECTION.equals(attribute)) {
            String serviceId = getServiceID(request);
            List<String> options = getOptions(request);
            result = onGetHandDetection(request, response, serviceId, options);
        } else if (INTERFACE_DETECTION.equals(interfac) && ATTRIBUTE_FACE_DETECTION.equals(attribute)) {
            String serviceId = getServiceID(request);
            List<String> options = getOptions(request);
            result = onGetFaceDetection(request, response, serviceId, options);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        String interfac = getInterface(request);
        String attribute = getAttribute(request);
        boolean result = true;
        if (INTERFACE_DETECTION.equals(interfac) && ATTRIBUTE_BODY_DETECTION.equals(attribute)) {
            String serviceId = getServiceID(request);
            List<String> options = getOptions(request);
            result = onPostBodyDetection(request, response, serviceId, options);
        } else if (INTERFACE_DETECTION.equals(interfac) && ATTRIBUTE_HAND_DETECTION.equals(attribute)) {
            String serviceId = getServiceID(request);
            List<String> options = getOptions(request);
            result = onPostHandDetection(request, response, serviceId, options);
        } else if (INTERFACE_DETECTION.equals(interfac) && ATTRIBUTE_FACE_DETECTION.equals(attribute)) {
            String serviceId = getServiceID(request);
            List<String> options = getOptions(request);
            result = onPostFaceDetection(request, response, serviceId, options);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }
    
    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);

        if (ATTRIBUTE_ON_BODY_DETECTION.equals(attribute)) {
            result = onPutOnBodyDetection(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_HAND_DETECTION.equals(attribute)) {
            result = onPutOnHandDetection(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_FACE_DETECTION.equals(attribute)) {
            result = onPutOnFaceDetection(request, response, getServiceID(request), getSessionKey(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        boolean result = true;
        String attribute = getAttribute(request);
        if (ATTRIBUTE_ON_BODY_DETECTION.equals(attribute)) {
            result = onDeleteOnBodyDetection(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_HAND_DETECTION.equals(attribute)) {
            result = onDeleteOnHandDetection(request, response, getServiceID(request), getSessionKey(request));
        } else if (ATTRIBUTE_ON_FACE_DETECTION.equals(attribute)) {
            result = onDeleteOnFaceDetection(request, response, getServiceID(request), getSessionKey(request));
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }
    // ------------------------------------
    // PUT
    // ------------------------------------

    /**
     * onbodydetectionコールバック登録リクエストハンドラー.<br/>
     * onbodydetectionコールバックを登録し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutOnBodyDetection(final Intent request, final Intent response, 
            final String serviceId, final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onhanddetectionコールバック登録リクエストハンドラー.<br/>
     * onhanddetectionコールバックを登録し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutOnHandDetection(final Intent request, final Intent response, 
            final String serviceId, final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onfacedetectionコールバック登録リクエストハンドラー.<br/>
     * onfacedetectionコールバックを登録し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutOnFaceDetection(final Intent request, final Intent response, 
            final String serviceId, final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // DELETE
    // ------------------------------------

    /**
     * onbodydetectionコールバック解除リクエストハンドラー.<br/>
     * onbodydetectionコールバックを解除し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteOnBodyDetection(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onhanddetectionコールバック解除リクエストハンドラー.<br/>
     * onhanddetectionコールバックを解除し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteOnHandDetection(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onfacedetectionコールバック解除リクエストハンドラー.<br/>
     * onfacedetectionコールバックを解除し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteOnFaceDetection(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }
    
    // ------------------------------------
    // GET
    // ------------------------------------

    /**
     * body detection attribute request handler.<br/>
     * And ask the human body detection, and the result is stored in the response parameters.
     * If the response parameter is ready, please return true.
     * If you are not ready, please return false to start the process in the thread.
     * Once the thread is complete, send the response parameters.
     * @param request request parameter
     * @param response response parameter.
     * @param serviceId serviceID
     * @param options options.
     * @return send response flag.(true:sent / unsent (Send after the thread has been completed))
     */
    protected boolean onGetBodyDetection(final Intent request, final Intent response,
                                         final String serviceId, final List<String> options) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * hand detection attribute request handler.<br/>
     * And ask the human hand detection, and the result is stored in the response parameters.
     * If the response parameter is ready, please return true.
     * If you are not ready, please return false to start the process in the thread.
     * Once the thread is complete, send the response parameters.
     * @param request request parameter
     * @param response response parameter.
     * @param serviceId serviceID
     * @param options options.
     * @return send response flag.(true:sent / unsent (Send after the thread has been completed))
     */
    protected boolean onGetHandDetection(final Intent request, final Intent response,
                                         final String serviceId, final List<String> options) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * face detection attribute request handler.<br/>
     * And ask the human face detection, and the result is stored in the response parameters.
     * If the response parameter is ready, please return true.
     * If you are not ready, please return false to start the process in the thread.
     * Once the thread is complete, send the response parameters.
     * @param request request parameter
     * @param response response parameter.
     * @param serviceId serviceID
     * @param options options.
     * @return send response flag.(true:sent / unsent (Send after the thread has been completed))
     */
    protected boolean onGetFaceDetection(final Intent request, final Intent response,
                                         final String serviceId, final List<String> options) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // POST
    // ------------------------------------

    /**
     * body detection attribute request handler.<br/>
     * And ask the human body detection, and the result is stored in the response parameters.
     * If the response parameter is ready, please return true.
     * If you are not ready, please return false to start the process in the thread.
     * Once the thread is complete, send the response parameters.
     * @param request request parameter
     * @param response response parameter.
     * @param serviceId serviceID
     * @param options options.
     * @return send response flag.(true:sent / unsent (Send after the thread has been completed))
     */
    protected boolean onPostBodyDetection(final Intent request, final Intent response,
                                          final String serviceId, final List<String> options) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * hand detection attribute request handler.<br/>
     * And ask the human hand detection, and the result is stored in the response parameters.
     * If the response parameter is ready, please return true.
     * If you are not ready, please return false to start the process in the thread.
     * Once the thread is complete, send the response parameters.
     * @param request request parameter
     * @param response response parameter.
     * @param serviceId serviceID
     * @param options options.
     * @return send response flag.(true:sent / unsent (Send after the thread has been completed))
     */
    protected boolean onPostHandDetection(final Intent request, final Intent response,
                                          final String serviceId, final List<String> options) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * face detection attribute request handler.<br/>
     * And ask the human face detection, and the result is stored in the response parameters.
     * If the response parameter is ready, please return true.
     * If you are not ready, please return false to start the process in the thread.
     * Once the thread is complete, send the response parameters.
     * @param request request parameter
     * @param response response parameter.
     * @param serviceId serviceID
     * @param options options.
     * @return send response flag.(true:sent / unsent (Send after the thread has been completed))
     */
    protected boolean onPostFaceDetection(final Intent request, final Intent response,
                                          final String serviceId, final List<String> options) {
        setUnsupportedError(response);
        return true;
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
