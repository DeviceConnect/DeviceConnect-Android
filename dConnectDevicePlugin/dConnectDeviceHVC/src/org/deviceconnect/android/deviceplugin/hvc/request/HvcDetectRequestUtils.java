/*
 HvcDetectRequestUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.request;

import java.util.List;

import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectBodyRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectFaceRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectHandRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.profile.HumanDetectProfile;

import android.content.Intent;
import android.util.Log;

/**
 * HVC detect request utility.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class HvcDetectRequestUtils {

    /**
     * error message. {@value}
     */
    public static final String ERROR_DETECTKIND_UNKNOWN_VALUE = "detectKind unknown value. detectKind:";
    
    /**
     * error message. {@value}
     */
    public static final String ERROR_PARAMETER_DIFFERENT_TYPE = "parameter different type.";
    
    
    /**
     * Constructor.
     */
    private HvcDetectRequestUtils() {
        
    }
    
    /**
     * get request parameter.
     * 
     * @param request request
     * @param response response
     * @param detectKind detect kind
     * @return requestParams
     * @throws NumberFormatException parameter different type.
     * @throws IllegalStateException BUG: detectKind unknown value.
     */
    public static HumanDetectRequestParams getRequestParams(final Intent request, final Intent response,
            final HumanDetectKind detectKind) throws NumberFormatException, IllegalStateException {

        HumanDetectRequestParams requestParams = HvcDetectRequestParams.getDefaultRequestParameter();
        
        // get options parameter.
        List<String> options = HumanDetectProfile.getOptions(request);
        
        // get parameters.(different type error, throw
        // NumberFormatException)
        
        Double threshold = HumanDetectProfile.getThreshold(request);
        Double minWidth = HumanDetectProfile.getMinWidth(request);
        Double minHeight = HumanDetectProfile.getMinHeight(request);
        Double maxWidth = HumanDetectProfile.getMaxWidth(request);
        Double maxHeight = HumanDetectProfile.getMaxHeight(request);
        
        // get event interval.
        Long eventInterval = HumanDetectProfile.getInterval(request);

        // store parameter.(if data exist, to set. if data not exist, use default value.)
        if (detectKind == HumanDetectKind.BODY) {
            HumanDetectBodyRequestParams bodyRequestParams = requestParams.getBody();
            if (options != null) {
                bodyRequestParams.setOptions(options);
            }
            if (threshold != null) {
                bodyRequestParams.setThreshold(threshold);
            }
            if (minWidth != null) {
                bodyRequestParams.setMinWidth(minWidth);
            }
            if (minHeight != null) {
                bodyRequestParams.setMinHeight(minHeight);
            }
            if (maxWidth != null) {
                bodyRequestParams.setMaxWidth(maxWidth);
            }
            if (maxHeight != null) {
                bodyRequestParams.setMaxHeight(maxHeight);
            }
            if (eventInterval != null) {
                bodyRequestParams.setEventInterval(eventInterval);
            }
        } else if (detectKind == HumanDetectKind.HAND) {
            HumanDetectHandRequestParams handRequestParams = requestParams.getHand();
            if (options != null) {
                handRequestParams.setOptions(options);
            }
            if (threshold != null) {
                handRequestParams.setThreshold(threshold);
            }
            if (minWidth != null) {
                handRequestParams.setMinWidth(minWidth);
            }
            if (minHeight != null) {
                handRequestParams.setMinHeight(minHeight);
            }
            if (maxWidth != null) {
                handRequestParams.setMaxWidth(maxWidth);
            }
            if (maxHeight != null) {
                handRequestParams.setMaxHeight(maxHeight);
            }
            if (eventInterval != null) {
                handRequestParams.setEventInterval(eventInterval);
            }
        } else if (detectKind == HumanDetectKind.FACE) {
            HumanDetectFaceRequestParams faceRequestParams = requestParams.getFace();
            if (options != null) {
                faceRequestParams.setOptions(options);
            }
            if (threshold != null) {
                faceRequestParams.setThreshold(threshold);
            }
            if (minWidth != null) {
                faceRequestParams.setMinWidth(minWidth);
            }
            if (minHeight != null) {
                faceRequestParams.setMinHeight(minHeight);
            }
            if (maxWidth != null) {
                faceRequestParams.setMaxWidth(maxWidth);
            }
            if (maxHeight != null) {
                faceRequestParams.setMaxHeight(maxHeight);
            }
            if (eventInterval != null) {
                faceRequestParams.setEventInterval(eventInterval);
            }

            // get parameters.(different type error, throw
            // NumberFormatException)
            Double eyeThreshold = HumanDetectProfile.getEyeThreshold(request);
            Double noseThreshold = HumanDetectProfile.getNoseThreshold(request);
            Double mouthThreshold = HumanDetectProfile.getMouthThreshold(request);
            Double blinkThreshold = HumanDetectProfile.getBlinkThreshold(request);
            Double ageThreshold = HumanDetectProfile.getAgeThreshold(request);
            Double genderThreshold = HumanDetectProfile.getGenderThreshold(request);
            Double faceDirectionThreshold = HumanDetectProfile.getFaceDirectionThreshold(request);
            Double gazeThreshold = HumanDetectProfile.getGazeThreshold(request);
            Double expressionThreshold = HumanDetectProfile.getExpressionThreshold(request);

            if (eyeThreshold != null) {
                faceRequestParams.setEyeThreshold(eyeThreshold);
            }
            if (noseThreshold != null) {
                faceRequestParams.setNoseThreshold(noseThreshold);
            }
            if (mouthThreshold != null) {
                faceRequestParams.setMouthThreshold(mouthThreshold);
            }
            if (blinkThreshold != null) {
                faceRequestParams.setBlinkThreshold(blinkThreshold);
            }
            if (ageThreshold != null) {
                faceRequestParams.setAgeThreshold(ageThreshold);
            }
            if (genderThreshold != null) {
                faceRequestParams.setGenderThreshold(genderThreshold);
            }
            if (faceDirectionThreshold != null) {
                faceRequestParams.setFaceDirectionThreshold(faceDirectionThreshold);
            }
            if (gazeThreshold != null) {
                faceRequestParams.setGazeThreshold(gazeThreshold);
            }
            if (expressionThreshold != null) {
                faceRequestParams.setExpressionThreshold(expressionThreshold);
            }
            
        } else {
            // BUG: detectKind unknown value.
            throw new IllegalStateException(ERROR_DETECTKIND_UNKNOWN_VALUE + detectKind.ordinal());
        }
        
        // success
        return requestParams;
    }
}

