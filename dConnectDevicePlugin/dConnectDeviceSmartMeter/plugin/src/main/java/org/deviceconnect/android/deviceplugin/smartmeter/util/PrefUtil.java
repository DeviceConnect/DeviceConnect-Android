/*
 PrefUtil.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Preferenceアクセスクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class PrefUtil {
    /** Bルート認証ID用キー. */
    private static final String KEY_B_ROUTE_ID = "bRouteId";
    /** Bルート認証パスワード用キー. */
    private static final String KEY_B_ROUTE_PASS = "bRoutePass";
    /** アクセスIPv6アドレス用キー. */
    private static final String KEY_ACCESS_IPV6 = "accessIpv6";
    /** SharedPreferencesインスタンス. */
    private SharedPreferences mPreferences;

    /**
     * コンストラクタ.
     * @param context Context.
     */
    public PrefUtil(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Get B Route ID.
     * @return B Route ID.
     */
    public String getBRouteId() {
        return getString(KEY_B_ROUTE_ID);
    }

    /**
     * Get B Route Password.
     * @return B Route Password.
     */
    public String getBRoutePass() {
        return getString(KEY_B_ROUTE_PASS);
    }

    /**
     * Get Access IPv6 Address.
     * @return Access IPv6 Address.
     */
    public String getAccessIpv6() {
        return getString(KEY_ACCESS_IPV6);
    }

    /**
     * Set B Route ID.
     * @param bRouteId B Route ID.
     */
    public void setBRouteId(final String bRouteId) {
        putValue(KEY_B_ROUTE_ID, bRouteId);
    }

    /**
     * Set B Route Password.
     * @param bRoutePass B Route Password.
     */
    public void setBRoutePass(final String bRoutePass) {
        putValue(KEY_B_ROUTE_PASS, bRoutePass);
    }

    /**
     * Set Access IPv6 Address.
     * @param accessIpv6 IPv6 Address.
     */
    public void setAccessIpv6(final String accessIpv6) {
        putValue(KEY_ACCESS_IPV6, accessIpv6);
    }

    /**
     * SharedPreferences保存用関数.
     * @param key キー.
     * @param value 保存値.
     */
    private void putValue(final String key, final Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        if (value == null) {
            throw new IllegalArgumentException("value is null.");
        }

        SharedPreferences.Editor editor = mPreferences.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else {
            throw new IllegalArgumentException();
        }
        editor.apply();
    }

    /**
     * 文字列取得.
     * @param key キー
     * @return 文字列. 該当キーが存在しない場合はnull.
     */
    private String getString(final String key) {
        return mPreferences.getString(key, null);
    }
}
