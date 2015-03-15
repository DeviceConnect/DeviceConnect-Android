/*
 HvcResponseCache.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvc.comm;

import omron.HVC.HVC_PRM;
import omron.HVC.HVC_RES;

/**
 * HVC response cache.
 * 
 * @author NTT DOCOMO, INC.
 */
class HvcResponseCache {
    /**
     * HVC parameter.
     */
    private HVC_PRM mHvcPrm;
    /**
     * HVC response.
     */
    private HVC_RES mHvcRes;
    /**
     * Cache time.
     */
    private long mCacheTime;
    
    /**
     * Constructor.
     * @param hvcPrm HVC parameter.
     * @param hvcRes HVC response.
     */
    public HvcResponseCache(final HVC_PRM hvcPrm, final HVC_RES hvcRes) {
        mHvcPrm = hvcPrm;
        mHvcRes = hvcRes;
        mCacheTime = System.currentTimeMillis();
    }
    
    /**
     * Compare to cache parameter.
     * @param hvcPrm HVC parameter.
     * @return true: equal / false: not equal
     */
    public boolean compareHvcPrm(final HVC_PRM hvcPrm) {
        if (mHvcPrm != null) {
            if (mHvcPrm.equals(hvcPrm)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * check expired.
     * @param validateTime validate time[msec].
     * @return true: expired (can not use cache data.) / false: not expired.
     */
    public boolean isExpired(final long validateTime) {
        return ((System.currentTimeMillis() - mCacheTime) > validateTime);
    }
    
    /**
     * get cache HVC response.
     * @return cache HVC response.
     */
    public HVC_RES getHvcRes() {
        return mHvcRes;
    }

    /**
     * set HVC response.
     * @param hvcRes HVC response.
     */
    public void setHvcRes(final HVC_RES hvcRes) {
        mHvcRes = hvcRes;
    }
}
