/*
 PebbleUtil.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.profile;

import org.deviceconnect.android.deviceplugin.pebble.service.PebbleService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * プロファイルで使用するユーティリティクラス.
 * @author NTT DOCOMO, INC.
 */
final class PebbleUtil {
    /**
     * コンストラクタ.
     */
    private PebbleUtil() {
    }
    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    public static boolean checkServiceId(final String serviceId) {
        String regex = PebbleService.PREFIX_SERVICE_ID;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(serviceId);
        return m.find();
    }
}
