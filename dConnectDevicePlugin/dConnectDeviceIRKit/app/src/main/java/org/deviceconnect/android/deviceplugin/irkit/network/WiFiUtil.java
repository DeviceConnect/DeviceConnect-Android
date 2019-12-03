/*
 WiFiUtil.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.network;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import org.deviceconnect.android.activity.PermissionUtility;

/**
 * WiFi周りのユーティリティクラス.
 * @author NTT DOCOMO, INC.
 */
public final class WiFiUtil {




    /**
     * ユーティリティクラスのためprivate.
     */
    private WiFiUtil() {
    }

    /**
     * 現在のSSIDを取得する.
     * 
     * @param context コンテキストオブジェクト
     * @return SSID文字列。WiFiでは無い場合はnullを返す。
     */
    public static String getCurrentSSID(final Context context) {

        String ssid = null;

        if (isOnWiFi(context)) {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (manager.isWifiEnabled()) {
                WifiInfo wifiInfo = manager.getConnectionInfo();
                if (wifiInfo != null) {
                    NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                    if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                        // ダブルクォーテーションを含んでいるので外す
                        ssid = wifiInfo.getSSID().replaceAll("\"", "");
                    }
                }
            }
        }

        return ssid;
    }

    /**
     * WiFiにつながっているかどうかチェックする.
     * 
     * @param context コンテキストオブジェクト
     * @return つながっている場合true、その他はfalseを返す。
     */
    public static boolean isOnWiFi(final Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni == null || !ni.isConnected() || (ni.getType() != ConnectivityManager.TYPE_WIFI)) {
            return false;
        }

        return true;
    }
    
    /**
     * 
     * 
     * @param context context.
     * @param oldSSID 古い ssid.
     * @return SSID が変更になったかどうか.
     */
    public static boolean isChangedSSID(final Context context, final String oldSSID) {
        String current = getCurrentSSID(context);
        if (current == null) {
            return (oldSSID != null);
        }
        return !current.equals(oldSSID);
    }
    /**
     * WiFiスキャンを行うには位置情報のパーミッション許可が必要なので、確認を行う.
     * @param context context.
     * @param callback 認証ダイアログの選択結果を返すCallback.
     */
    public static void checkLocationPermission(final Context context, final PermissionUtility.PermissionRequestCallback callback) {
        // WiFi scan requires location permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && context.checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                callback.onSuccess();
            } else {
                PermissionUtility.requestPermissions(context, new Handler(Looper.getMainLooper()),
                        new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION },
                            callback);
            }
        }
    }
}
