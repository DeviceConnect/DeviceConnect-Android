package org.deviceconnect.android.deviceplugin.hvc.utils;

import omron.HVC.HVC;

import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;

import android.util.SparseArray;

/**
 * HVC convert utility.
 *
 */
public final class HvcConvertUtils  {

    /**
     * Constructor.
     */
    private HvcConvertUtils() {
        
    }
    
    
    /**
     * Convert threshold value.
     * @param normalizeThreshold threshold value(normalize value)
     * @return threshold value(HVC device value)
     */
    public static int convertToHvcThreshold(final double normalizeThreshold) {
        int hvcThreshold = (int) (normalizeThreshold * HvcConstants.THRESHOLD_MAX);
        return hvcThreshold;
    }
    
    /**
     * Convert confidence value.
     * @param normalizeConfidence confidence value(normalize value)
     * @return confidence value(HVC device value)
     */
    public static int convertToHvcConfidence(final double normalizeConfidence) {
        int hvcConfidence = (int) (normalizeConfidence * HvcConstants.CONFIDENCE_MAX);
        return hvcConfidence;
    }
    
    /**
     * Convert width value.
     * @param normalizeWidth width value(normalize value)
     * @return width value(HVC device value)
     */
    public static int convertToHvcWidth(final double normalizeWidth) {
        int hvcWidth = (int) (normalizeWidth * HvcConstants.HVC_C_CAMERA_WIDTH);
        return hvcWidth;
    }
    
    /**
     * Convert height value.
     * @param normalizeHeight height value(normalize value)
     * @return height value(HVC device value)
     */
    public static int convertToHvcHeight(final double normalizeHeight) {
        int hvcHeight = (int) (normalizeHeight * HvcConstants.HVC_C_CAMERA_HEIGHT);
        return hvcHeight;
    }
    
    /**
     * Convert threshold value.
     * @param hvcThreshold threshold value(HVC device value)
     * @return threshold value(normalize value)
     */
    public static double convertToNormalizeThreshold(final int hvcThreshold) {
        double normalizeThreshold = (double) hvcThreshold / HvcConstants.THRESHOLD_MAX;
        return normalizeThreshold;
    }
    
    /**
     * Convert confidence value.
     * @param hvcConfidence confidence value(HVC device value)
     * @return confidence value(normalize value)
     */
    public static double convertToNormalizeConfidence(final int hvcConfidence) {
        double normalizeConfidence = (double) hvcConfidence / HvcConstants.CONFIDENCE_MAX;
        return normalizeConfidence;
    }
    
    /**
     * Convert width value.
     * @param hvcWidth width value(HVC device value)
     * @return width value(normalize device value)
     */
    public static double convertToNormalizeWidth(final int hvcWidth) {
        double normalizeWidth = (double) hvcWidth / HvcConstants.HVC_C_CAMERA_WIDTH;
        return normalizeWidth;
    }
    
    /**
     * Convert height value.
     * @param hvcHeight height value(HVC device value)
     * @return height value(normalize device value)
     */
    public static double convertToNormalizeHeight(final double hvcHeight) {
        double normalizeHeight = (double) hvcHeight / HvcConstants.HVC_C_CAMERA_HEIGHT;
        return normalizeHeight;
    }
    
    /**
     * Convert expression score value.
     * @param hvcExpressionScore expression score value(HVC device value)
     * @return expression score value(normalize value)
     */
    public static double convertToNormalizeExpressionScore(final double hvcExpressionScore) {
        double normalizeExpressionScore = (double) hvcExpressionScore / HvcConstants.EXPRESSION_SCORE_MAX;
        return normalizeExpressionScore;
    }
    
    
    
    /**
     * convert to normalize expression value.
     * @param hvcExpression expression(HVC value)
     * @return normalize expression value.
     */
    public static String convertToNormalizeExpression(final int hvcExpression) {
        
        String normalizeExpression = HvcConstants.EXPRESSION_UNKNOWN;
        
        SparseArray<String> map = new SparseArray<String>();
        map.put(HVC.HVC_EX_NEUTRAL, HvcConstants.EXPRESSION_UNKNOWN);
        map.put(HVC.HVC_EX_HAPPINESS, HvcConstants.EXPRESSION_SMILE);
        map.put(HVC.HVC_EX_SURPRISE, HvcConstants.EXPRESSION_SURPRISE);
        map.put(HVC.HVC_EX_ANGER, HvcConstants.EXPRESSION_MAD);
        map.put(HVC.HVC_EX_SADNESS, HvcConstants.EXPRESSION_SAD);
        
        String exp = map.get(hvcExpression);
        if (exp != null) {
            normalizeExpression = exp;
        }
        
        return normalizeExpression;
    }

}
