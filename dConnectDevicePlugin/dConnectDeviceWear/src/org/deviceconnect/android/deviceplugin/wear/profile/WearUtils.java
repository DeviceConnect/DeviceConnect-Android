/*
WearUtil.java
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wear Utils.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class WearUtils {

    /**
     * Constructor.
     */
    private WearUtils() {
    }
    /**
     * Check serviceId.
     * 
     * @param serviceId Service ID
     * @return If <code>serviceId</code> is equal to test for the service ID is true, if it is not false.
     */
    public static boolean checkServiceId(final String serviceId) {
        String regex = WearConst.SERVICE_ID;
        Pattern mPattern = Pattern.compile(regex);
        Matcher match = mPattern.matcher(serviceId);
        return match.find();
    }
}
