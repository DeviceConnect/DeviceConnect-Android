/*
 HumanDetectBasicRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.humandetect;

import java.util.ArrayList;
import java.util.List;

/**
 * detect request parameter.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HumanDetectBasicRequestParams implements Cloneable {
    
    /**
     * options.
     */
    private List<String> mOptions;
    
    /**
     * threshold value(normalized value).
     */
    private double mNormalizeThreshold;
    
    /**
     * min width value(normalized value).
     */
    private double mNormalizeMinWidth;
    /**
     * min width value(normalized value).
     */
    private double mNormalizeMinHeight;
    /**
     * min width value(normalized value).
     */
    private double mNormalizeMaxWidth;
    /**
     * max height value(normalized value).
     */
    private double mNormalizeMaxHeight;

    /**
     * Constructor(with default value).
     * @param options options
     * @param normalizeThreshold threshold
     * @param normalizeMinWidth minWidth
     * @param normalizeMinHeight minHeight
     * @param normalizeMaxWidth maxWidth
     * @param normalizeMaxHeight maxHeight
     */
    public HumanDetectBasicRequestParams(final List<String> options, final double normalizeThreshold,
            final double normalizeMinWidth, final double normalizeMinHeight, final double normalizeMaxWidth,
            final double normalizeMaxHeight) {
        mOptions = options;
        mNormalizeThreshold = normalizeThreshold;
        mNormalizeMinWidth = normalizeMinWidth;
        mNormalizeMinHeight = normalizeMinHeight;
        mNormalizeMaxWidth = normalizeMaxWidth;
        mNormalizeMaxHeight = normalizeMaxHeight;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        
        // deep copy
        HumanDetectBasicRequestParams params = (HumanDetectBasicRequestParams) super.clone();
        params.setOptions(new ArrayList<>(mOptions));
        
        return params;
    }
    
    /**
     * set options.
     * @param options options.
     */
    public void setOptions(final List<String> options) {
        mOptions = options;
    }

    /**
     * set threshold.
     * @param threshold normalize threshold value.
     */
    public void setThreshold(final double threshold) {
        mNormalizeThreshold = threshold;
    }
    
    /**
     * set min width.
     * @param minWidth normalize min width value.
     */
    public void setMinWidth(final double minWidth) {
        mNormalizeMinWidth = minWidth;
    }
    
    /**
     * set min height.
     * @param minHeight normalize min height value.
     */
    public void setMinHeight(final double minHeight) {
        mNormalizeMinHeight = minHeight;
    }
    
    /**
     * set max width.
     * @param maxWidth normalize max width value.
     */
    public void setMaxWidth(final double maxWidth) {
        mNormalizeMaxWidth = maxWidth;
    }
    
    /**
     * set max height.
     * @param maxHeight normalize max height value.
     */
    public void setMaxHeight(final double maxHeight) {
        mNormalizeMaxHeight = maxHeight;
    }
    
    
    /**
     * get options.
     * @return options options.
     */
    public List<String> getOptions() {
        return mOptions;
    }
    
    /**
     * get threshold.
     * @return normalize threshold value.
     */
    public double getThreshold() {
        return mNormalizeThreshold;
    }
    
    /**
     * get min width.
     * @return normalize min width value.
     */
    public double getMinWidth() {
        return mNormalizeMinWidth;
    }
    
    /**
     * get min height.
     * @return normalize min height value.
     */
    public double getMinHeight() {
        return mNormalizeMinHeight;
    }
     
    /**
     * get max width.
     * @return normalize max width value.
     */
    public double getMaxWidth() {
        return mNormalizeMaxWidth;
    }
    
    /**
     * get max height.
     * @return normalize max height value.
     */
    public double getMaxHeight() {
        return mNormalizeMaxHeight;
    }
}
