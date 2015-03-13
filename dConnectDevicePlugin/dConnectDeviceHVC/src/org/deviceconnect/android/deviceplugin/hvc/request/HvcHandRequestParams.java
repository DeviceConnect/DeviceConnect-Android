/*
 HvcHandRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.request;

import org.deviceconnect.android.deviceplugin.hvc.comm.HvcConvertUtils;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectHandRequestParams;

/**
 * HVC hand detect request parameter class.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcHandRequestParams {

    /**
     * human detect hand request parameters.
     */
    private HumanDetectHandRequestParams mHandRequestParams;
    
    
    /**
     * Constructor(with HumanDetectHandRequestParams).
     * @param handRequestParams hand request parameters.
     */
    public HvcHandRequestParams(final HumanDetectHandRequestParams handRequestParams) {
        mHandRequestParams = handRequestParams;
    }
    
    /**
     * get request parameters.
     * @return request parameters
     */
    public HumanDetectHandRequestParams getRequestParams() {
        return mHandRequestParams;
    }
    
    /**
     * Get threshold value(HVC device value).
     * @return HVC threshold(HVC device value)
     */
    public int getHvcThreshold() {
        int hvcThreshold = HvcConvertUtils.convertToHvcThreshold(mHandRequestParams.getThreshold());
        return hvcThreshold;
    }
    
    /**
     * Get min width value(HVC device value).
     * @return HVC min width value(HVC device value)
     */
    public int getHvcMinWidth() {
        int hvcMinWidth = HvcConvertUtils.convertToHvcWidth(mHandRequestParams.getMinWidth());
        return hvcMinWidth;
    }
    
    /**
     * Get min height value(HVC device value).
     * @return HVC min height value(HVC device value)
     */
    public int getHvcMinHeight() {
        int hvcMinHeight = HvcConvertUtils.convertToHvcHeight(mHandRequestParams.getMinHeight());
        return hvcMinHeight;
    }
    
    /**
     * Get max width value(HVC device value).
     * @return HVC max width value(HVC device value)
     */
    public int getHvcMaxWidth() {
        int hvcMaxWidth = HvcConvertUtils.convertToHvcWidth(mHandRequestParams.getMaxWidth());
        return hvcMaxWidth;
    }
    
    /**
     * Get max height value(HVC device value).
     * @return HVC max height value(HVC device value)
     */
    public int getHvcMaxHeight() {
        int hvcMaxHeight = HvcConvertUtils.convertToHvcHeight(mHandRequestParams.getMaxHeight());
        return hvcMaxHeight;
    }

    
}
