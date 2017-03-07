/*
 HvcDetectRequestUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.request;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectBodyRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectEventRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectFaceRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectHandRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;
import org.deviceconnect.android.profile.HumanDetectionProfile;

import java.util.List;

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
     * error message. {@value}
     */
    public static final String ERROR_INVERVAL_PARAMETER_TOO_MINIMUM =
            "interval parameter too minimum. range: %ld <= interval <= %ld";

    /**
     * error message. {@value}
     */
    public static final String ERROR_INVERVAL_PARAMETER_TOO_MAXIMUM =
            "interval parameter too maximum. range: %ld <= interval <= %ld";

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

        HumanDetectRequestParams requestParams = new HumanDetectRequestParams();
        requestParams.setEvent(HvcDetectRequestParams.getDefaultEventRequestParameter());

        // get options parameter.
        List<String> options = HumanDetectionProfile.getOptions(request);

        // get parameters.(different type error, throw
        // NumberFormatException)
        Double threshold = HumanDetectionProfile.getThreshold(request);
        Double minWidth = HumanDetectionProfile.getMinWidth(request);
        Double minHeight = HumanDetectionProfile.getMinHeight(request);
        Double maxWidth = HumanDetectionProfile.getMaxWidth(request);
        Double maxHeight = HumanDetectionProfile.getMaxHeight(request);

        // get event interval.
        Long eventInterval = HumanDetectionProfile.getInterval(request, HvcConstants.PARAM_INTERVAL_MIN,
                HvcConstants.PARAM_INTERVAL_MAX);

        // store parameter.(if data exist, to set. if data not exist, use default value.)
        if (detectKind == HumanDetectKind.BODY) {
            HumanDetectBodyRequestParams bodyRequestParams = getBodyRequestParams(options,
                    threshold, minWidth, minHeight, maxWidth, maxHeight);
            requestParams.setBody(bodyRequestParams);

        } else if (detectKind == HumanDetectKind.HAND) {
            HumanDetectHandRequestParams handRequestParams = getHandRequestParams(options,
                    threshold, minWidth, minHeight, maxWidth, maxHeight);
            requestParams.setHand(handRequestParams);
        } else if (detectKind == HumanDetectKind.FACE) {

            // default value.
            HumanDetectFaceRequestParams faceRequestParams = HvcDetectRequestParams.getDefaultFaceRequestParameter();

            // set value.
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

            // get parameters.(different type error, throw
            // NumberFormatException)
            Double eyeThreshold = HumanDetectionProfile.getEyeThreshold(request);
            Double noseThreshold = HumanDetectionProfile.getNoseThreshold(request);
            Double mouthThreshold = HumanDetectionProfile.getMouthThreshold(request);
            Double blinkThreshold = HumanDetectionProfile.getBlinkThreshold(request);
            Double ageThreshold = HumanDetectionProfile.getAgeThreshold(request);
            Double genderThreshold = HumanDetectionProfile.getGenderThreshold(request);
            Double faceDirectionThreshold = HumanDetectionProfile.getFaceDirectionThreshold(request);
            Double gazeThreshold = HumanDetectionProfile.getGazeThreshold(request);
            Double expressionThreshold = HumanDetectionProfile.getExpressionThreshold(request);

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

            // store.
            requestParams.setFace(faceRequestParams);
        } else if (detectKind == HumanDetectKind.HUMAN) {
            HumanDetectBodyRequestParams bodyRequestParams = getBodyRequestParams(options,
                    threshold, minWidth, minHeight, maxWidth, maxHeight);
            requestParams.setBody(bodyRequestParams);
            HumanDetectHandRequestParams handRequestParams = getHandRequestParams(options,
                    threshold, minWidth, minHeight, maxWidth, maxHeight);
            requestParams.setHand(handRequestParams);
            HumanDetectFaceRequestParams faceRequestParams = HvcDetectRequestParams.getDefaultFaceRequestParameter();
            requestParams.setFace(faceRequestParams);

        } else {
            // BUG: detectKind unknown value.
            throw new IllegalStateException(ERROR_DETECTKIND_UNKNOWN_VALUE + detectKind.ordinal());
        }

        // event parameter
        if (eventInterval != null) {
            HumanDetectEventRequestParams event = requestParams.getEvent();
            if (eventInterval == 0) {
                event.setInterval(HvcConstants.PARAM_INTERVAL_DEFAULT);
            } else if (eventInterval < HvcConstants.PARAM_INTERVAL_MIN) {
                String error = String.format(ERROR_INVERVAL_PARAMETER_TOO_MINIMUM,
                        HvcConstants.PARAM_INTERVAL_MIN, HvcConstants.PARAM_INTERVAL_MAX);
                throw new IllegalArgumentException(error);
            } else if (eventInterval > HvcConstants.PARAM_INTERVAL_MAX) {
                String error = String.format(ERROR_INVERVAL_PARAMETER_TOO_MAXIMUM,
                        HvcConstants.PARAM_INTERVAL_MIN, HvcConstants.PARAM_INTERVAL_MAX);
                throw new IllegalArgumentException(error);
            } else {
                event.setInterval(eventInterval);
            }
        }

        // success
        return requestParams;
    }

    /**
     * get body request parameter.
     * @param options options
     * @param threshold threshold
     * @param minWidth minWidth
     * @param minHeight minHeight
     * @param maxWidth maxWidth
     * @param maxHeight maxHeight
     * @return body request parameter
     */
    private static HumanDetectBodyRequestParams getBodyRequestParams(final List<String> options,
                          final Double threshold, final Double minWidth, final Double minHeight,
                          final Double maxWidth, final Double maxHeight) {

        // default value.
        HumanDetectBodyRequestParams bodyRequestParams = HvcDetectRequestParams.getDefaultBodyRequestParameter();

        // set value.
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
        return bodyRequestParams;
    }

    /**
     * get hand request parameter.
     * @param options options
     * @param threshold threshold
     * @param minWidth minWidth
     * @param minHeight minHeight
     * @param maxWidth maxWidth
     * @param maxHeight maxHeight
     * @return body request parameter
     */
    private static HumanDetectHandRequestParams getHandRequestParams(final List<String> options,
                          final Double threshold, final Double minWidth, final Double minHeight,
                          final Double maxWidth, final Double maxHeight) {

        // default value.
        HumanDetectHandRequestParams handRequestParams = HvcDetectRequestParams.getDefaultHandRequestParameter();

        // set value.
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
        return handRequestParams;
    }
}
