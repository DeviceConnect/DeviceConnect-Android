package org.deviceconnect.android.deviceplugin.hvc.profile;

/**
 * HVC constants value.
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
     * HVC-C threshold max value.しきい値
     */
    public static final int THRESHOLD_MAX = 1000;
    
    /**
     * HVC-C confidence max value.信頼度
     */
    public static final int CONFIDENCE_MAX = 1000;
    
    /**
     * HVC-C blink max value(large: eye closed).目つむり度
     */
    public static final int BLINK_MAX = 1000;
    
    
    /**
     * HVC-C expression unknown.無表情
     */
    public static final String EXPRESSION_UNKNOWN = "unknown";
    
    /**
     * HVC-C expression smile.喜び
     */
    public static final String EXPRESSION_SMILE = "smile";
    
    /**
     * HVC-C expression surprise.驚き
     */
    public static final String EXPRESSION_SURPRISE = "surprise";
    
    /**
     * HVC-C expression mad.怒り
     */
    public static final String EXPRESSION_MAD = "mad";
    
    /**
     * HVC-C expression sad.悲しみ
     */
    public static final String EXPRESSION_SAD = "sad";
    
    /**
     * HVC-C expression score max.表情推定のスコア最大値
     */
    public static final int EXPRESSION_SCORE_MAX = 100;
    
    /**
     * HVC-C threshold default value.しきい値
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
    

}
