/*
 HvcCommManagerUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.comm;

import java.util.List;

/**
 * HVC comm manager utility.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class HvcCommManagerUtils {

    /**
     * Constructor.
     */
    private HvcCommManagerUtils() {
        
    }
    
    /**
     * search comm manager by serviceId.
     * @param commManagerArray array.
     * @param serviceId serviceId
     * @return not null : commManager/ null : not found
     */
    public static HvcCommManager search(final List<HvcCommManager> commManagerArray, final String serviceId) {
        
        for (HvcCommManager commManager : commManagerArray) {
            if (serviceId.equals(commManager.getServiceId())) {
                return commManager;
            }
        }
        
        return null;
    }

    /**
     * check exist event by interval.
     * @param commManagerArray array.
     * @param interval interval.
     * @return true: exist / false: not exist
     */
    public static boolean checkExistEventByInterval(final List<HvcCommManager> commManagerArray, final long interval) {
        
        for (HvcCommManager commManager : commManagerArray) {
            if (commManager.checkExistEventByInterval(interval)) {
                return true;
            }
        }
        return false;
    }

    /**
     * check exist event by interval.
     * @param commManagerArray array.
     * @return true: exist / false: not exist
     */
    public static boolean checkExistEvent(final List<HvcCommManager> commManagerArray) {
        for (HvcCommManager commManager : commManagerArray) {
            if (commManager.getEventCount() > 0) {
                return true;
            }
        }
        return false;
    }

}
