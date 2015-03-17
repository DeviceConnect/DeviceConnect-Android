/*
 HumanDetectEventRequestParams.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.humandetect;

/**
 * event detect request parameter.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HumanDetectEventRequestParams implements Cloneable {
    
    /**
     * interval [msec].
     */
    private long mInterval;
    
    /**
     * Constructor.
     * @param interval interval[msec]
     */
    public HumanDetectEventRequestParams(final long interval) {
        mInterval = interval;
    }
    
    /**
     * get interval.
     * @return interval[msec]
     */
    public long getInterval() {
        return mInterval;
    }
    
    /**
     * set interval.
     * @param interval interval[msec]
     */
    public void setInterval(final long interval) {
        mInterval = interval;
    }
    
}

