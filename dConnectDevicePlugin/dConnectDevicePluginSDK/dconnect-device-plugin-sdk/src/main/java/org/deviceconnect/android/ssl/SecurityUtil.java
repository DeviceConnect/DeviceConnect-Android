/*
 SecurityUtil.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.ssl;

import android.os.Build;


/**
 * セキュリティ関連のユーティリティ.
 *
 * @author NTT DOCOMO, INC.
 */
final class SecurityUtil {

    static {
        if (canUseBouncyCastleProvider()) {
//            Security.addProvider(new BouncyCastleProvider());
            SECURITY_PROVIDER = "BC";
        } else {
            SECURITY_PROVIDER = null; // デフォルトのプロバイダーを使用.
        }
    }

    private static final String SECURITY_PROVIDER;

    /**
     * 本 SDK の内部で使用するセキュリティプロバイダの名前を取得する.
     *
     * @return セキュリティプロバイダの名前. デフォルトのプロバイダを使用する場合は <code>null</code>
     */
    public static String getSecurityProvider() {
        return SECURITY_PROVIDER;
    }

    /**
     * セキュリティプロバイダとして BouncyCastleProvider を使用可能かどうかを確認する.
     *
     * @return BouncyCastleProvider を使用できる場合は <code>true</code>, そうでない場合は <code>false</code>
     */
    private static boolean canUseBouncyCastleProvider() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.P;
    }

    /**
     * プライベートコンストラクタ.
     */
    private SecurityUtil() {
    }

}
