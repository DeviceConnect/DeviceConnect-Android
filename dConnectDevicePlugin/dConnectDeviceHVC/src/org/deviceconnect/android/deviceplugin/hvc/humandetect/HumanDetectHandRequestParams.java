/*
 HumanDetectHandRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.humandetect;

import java.util.List;

/**
 * hand detect request parameter.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HumanDetectHandRequestParams extends HumanDetectBasicRequestParams {
    
    /**
     * Constructor(with default value).
     * @param options options
     * @param normalizeThreshold threshold
     * @param normalizeMinWidth minWidth
     * @param normalizeMinHeight minHeight
     * @param normalizeMaxWidth maxWidth
     * @param normalizeMaxHeight maxHeight
     * @param eventInterval event interval[msec]
     */
    public HumanDetectHandRequestParams(final List<String> options, final double normalizeThreshold,
            final double normalizeMinWidth, final double normalizeMinHeight, final double normalizeMaxWidth,
            final double normalizeMaxHeight, final long eventInterval) {
        super(options, normalizeThreshold, normalizeMinWidth, normalizeMinHeight, normalizeMaxWidth, normalizeMaxHeight,
                eventInterval);
        
    }
}
