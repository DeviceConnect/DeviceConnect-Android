/*
 HvcBodyRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.request;

import org.deviceconnect.android.deviceplugin.hvc.comm.HvcConvertUtils;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectBodyRequestParams;

/**
 * HVC body detect request parameter class.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcBodyRequestParams {

    
    /**
     * body request parameters.
     */
    private HumanDetectBodyRequestParams mBodyRequestParams;
    
    
    /**
     * Constructor(with HumanDetectBodyRequestParams).
     * @param bodyRequestParams body request parameters.
     */
    public HvcBodyRequestParams(final HumanDetectBodyRequestParams bodyRequestParams) {
        mBodyRequestParams = bodyRequestParams;
    }
    
    /**
     * get request parameters.
     * @return request parameters.
     */
    public HumanDetectBodyRequestParams getRequestParams() {
        return mBodyRequestParams;
    }
    
    /**
     * Get threshold value(HVC device value).
     * @return HVC threshold(HVC device value)
     */
    public int getHvcThreshold() {
        int hvcThreshold = HvcConvertUtils.convertToHvcThreshold(mBodyRequestParams.getThreshold());
        return hvcThreshold;
    }
    
    /**
     * Get min width value(HVC device value).
     * @return HVC min width value(HVC device value)
     */
    public int getHvcMinWidth() {
        int hvcMinWidth = HvcConvertUtils.convertToHvcWidth(mBodyRequestParams.getMinWidth());
        return hvcMinWidth;
    }
    
    /**
     * Get min height value(HVC device value).
     * @return HVC min height value(HVC device value)
     */
    public int getHvcMinHeight() {
        int hvcMinHeight = HvcConvertUtils.convertToHvcHeight(mBodyRequestParams.getMinHeight());
        return hvcMinHeight;
    }
    
    /**
     * Get max width value(HVC device value).
     * @return HVC max width value(HVC device value)
     */
    public int getHvcMaxWidth() {
        int hvcMaxWidth = HvcConvertUtils.convertToHvcWidth(mBodyRequestParams.getMaxWidth());
        return hvcMaxWidth;
    }
    
    /**
     * Get max height value(HVC device value).
     * @return HVC max height value(HVC device value)
     */
    public int getHvcMaxHeight() {
        int hvcMaxHeight = HvcConvertUtils.convertToHvcHeight(mBodyRequestParams.getMaxHeight());
        return hvcMaxHeight;
    }
    
}
