/*
 HvcConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

/**
 * HVC constants value.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class HvcConstants {

    /**
     * Constructor.
     */
    private HvcConstants() {
        
    }
    
    /**
     * HVC device name prefix.
     */
    public static final String HVC_DEVICE_NAME_PREFIX = "OMRON_HVC.*|omron_hvc.*";
    
    /**
     * HVC-C detect camera width[pixels].
     */
    public static final int HVC_C_CAMERA_WIDTH = 640;
    
    /**
     * HVC-C detect camera height[pixels].
     */
    public static final int HVC_C_CAMERA_HEIGHT = 480;
    
    /**
     * HVC-C threshold max value.
     */
    public static final int THRESHOLD_MAX = 1000;
    
    /**
     * HVC-C confidence max value.
     */
    public static final int CONFIDENCE_MAX = 1000;
    
    /**
     * HVC-C blink max value(large value: eye closed).
     */
    public static final int BLINK_MAX = 1000;
    
    
    /**
     * HVC-C expression unknown.
     */
    public static final String EXPRESSION_UNKNOWN = "unknown";
    
    /**
     * HVC-C expression smile.
     */
    public static final String EXPRESSION_SMILE = "smile";
    
    /**
     * HVC-C expression surprise.
     */
    public static final String EXPRESSION_SURPRISE = "surprise";
    
    /**
     * HVC-C expression mad.
     */
    public static final String EXPRESSION_MAD = "mad";
    
    /**
     * HVC-C expression sad.
     */
    public static final String EXPRESSION_SAD = "sad";
    
    /**
     * HVC-C expression score max.
     */
    public static final int EXPRESSION_SCORE_MAX = 100;
    
    /**
     * HVC-C threshold default value.
     */
    public static final int THRESHOLD_DEFAULT = 500;
    
    /**
     * HVC-C body min width default value.
     */
    public static final int BODY_MIN_WIDTH_DEFAULT = 30;
    
    /**
     * HVC-C body min height default value.
     */
    public static final int BODY_MIN_HEIGHT_DEFAULT = 30;
    
    /**
     * HVC-C body max width default value.
     */
    public static final int BODY_MAX_WIDTH_DEFAULT = HVC_C_CAMERA_WIDTH;
    
    /**
     * HVC-C body max height default value.
     */
    public static final int BODY_MAX_HEIGHT_DEFAULT = HVC_C_CAMERA_HEIGHT;
    
    
    /**
     * HVC-C hand min width default value.
     */
    public static final int HAND_MIN_WIDTH_DEFAULT = 40;
    
    /**
     * HVC-C hand min height default value.
     */
    public static final int HAND_MIN_HEIGHT_DEFAULT = 40;
    
    /**
     * HVC-C hand max width default value.
     */
    public static final int HAND_MAX_WIDTH_DEFAULT = HVC_C_CAMERA_WIDTH;
    
    /**
     * HVC-C hand max height default value.
     */
    public static final int HAND_MAX_HEIGHT_DEFAULT = HVC_C_CAMERA_HEIGHT;
    
    
    /**
     * HVC-C face min width default value.
     */
    public static final int FACE_MIN_WIDTH_DEFAULT = 64;
    
    /**
     * HVC-C face min height default value.
     */
    public static final int FACE_MIN_HEIGHT_DEFAULT = 64;
    
    /**
     * HVC-C face max width default value.
     */
    public static final int FACE_MAX_WIDTH_DEFAULT = HVC_C_CAMERA_WIDTH;
    
    /**
     * HVC-C face max height default value.
     */
    public static final int FACE_MAX_HEIGHT_DEFAULT = HVC_C_CAMERA_HEIGHT;

    /**
     * timeout judge timer interval[msec].
     */
    public static final long TIMEOUT_JUDGE_INTERVAL = 1 * 60 * 1000;
    
    /**
     * HVC connect timeout time[msec].
     */
    public static final long HVC_CONNECT_TIMEOUT_TIME = 30 * 60 * 1000;

    /**
     * number of times to retry, If HVC communication is busy.
     */
    public static final int HVC_COMM_RETRY_COUNT = 3;

    /**
     * interval to retry, If HVC communication is busy[msec].
     */
    public static final int HVC_COMM_RETRY_INTERVAL = 1000;

    /**
     * HVC interval parameter minimum value.
     */
    public static final long PARAM_INTERVAL_MIN = 3 * 1000;

    /**
     * HVC interval parameter maximum value.
     */
    public static final long PARAM_INTERVAL_MAX = 999999 * 1000;

    /**
     * HVC interval parameter default value.
     */
    public static final long PARAM_INTERVAL_DEFAULT = PARAM_INTERVAL_MIN;

}
