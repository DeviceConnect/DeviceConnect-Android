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

}
