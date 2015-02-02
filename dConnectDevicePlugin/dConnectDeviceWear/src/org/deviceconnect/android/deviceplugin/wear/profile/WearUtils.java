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
     * コンストラクタ.
     */
    private WearUtils() {
    }
    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    public static boolean checkServiceId(final String serviceId) {
        String regex = WearServiceDiscoveryProfile.SERVICE_ID;
        Pattern mPattern = Pattern.compile(regex);
        Matcher match = mPattern.matcher(serviceId);
        return match.find();
    }
}
