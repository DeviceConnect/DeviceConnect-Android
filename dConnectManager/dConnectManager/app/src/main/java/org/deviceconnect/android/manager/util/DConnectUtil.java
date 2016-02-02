/*
 DConnectUtil.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.deviceconnect.android.manager.DConnectSettings;
import org.deviceconnect.android.manager.profile.DConnectFilesProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.message.intent.util.JSONFactory;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * ユーティリティクラス.
 * @author NTT DOCOMO, INC.
 */
public final class DConnectUtil {
    /**
     * Defined the permission.
     */
    public static final String[] PERMISSIONS = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * コンストラクタ.
     * ユーティリティクラスなので、privateとしておく。
     */
    private DConnectUtil() {
    }

    /**
     * HttpメソッドをDConnectメソッドに変換する.
     * @param method 変換するHttpメソッド
     * @return DConnectメソッド
     */
    public static String convertHttpMethod2DConnectMethod(final String method) {
        if (DConnectMessage.METHOD_GET.equals(method)) {
            return IntentDConnectMessage.ACTION_GET;
        } else if (DConnectMessage.METHOD_POST.equals(method)) {
            return IntentDConnectMessage.ACTION_POST;
        } else if (DConnectMessage.METHOD_PUT.equals(method)) {
            return IntentDConnectMessage.ACTION_PUT;
        } else if (DConnectMessage.METHOD_DELETE.equals(method)) {
            return IntentDConnectMessage.ACTION_DELETE;
        }
        return null;
    }

    /**
     * ファイルへのURIを作成する.
     * @param uri ファイルへのContentUri
     * @return URI
     */
    public static String createUri(final String uri) {
        DConnectSettings settings = DConnectSettings.getInstance();
        URIBuilder builder = new URIBuilder();
        if (settings.isSSL()) {
            builder.setScheme("https");
        } else {
            builder.setScheme("http");
        }
        builder.setHost(settings.getHost());
        builder.setPort(settings.getPort());
        builder.setProfile(DConnectFilesProfile.PROFILE_NAME);

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("uri", uri));
        builder.setParameters(params);

        return builder.toString();
    }

    /**
     * JSONの中に入っているuriを変換する.
     * 
     * <p>
     * 変換するuriはcontent://から始まるuriのみ変換する。<br/>
     * それ以外のuriは何も処理しない。
     * </p>
     * 
     * @param root 変換するJSONObject
     * @throws JSONException JSONの解析に失敗した場合
     */
    private static void convertUri(final JSONObject root) throws JSONException {
        @SuppressWarnings("unchecked") // Using legacy API
        Iterator<String> it = root.keys();
        while (it.hasNext()) {
            String key = it.next();
            Object value = root.opt(key);
            if (value instanceof String) {
                if ("uri".equals(key) && startWithContent((String) value)) {
                    String u = createUri((String) value);
                    root.put(key, u);
                }
            } else if (value instanceof JSONObject) {
                convertUri((JSONObject) value);
            }
        }
    }

    /**
     * 指定されたuriがcontent://から始まるかチェックする.
     * @param uri チェックするuri
     * @return content://から始まる場合はtrue、それ以外はfalse
     */
    private static boolean startWithContent(final String uri) {
        if (uri == null) {
            return false;
        }
        return (uri.startsWith("content://"));
    }

    /**
     * BundleからJSONObjectに変換する.
     * @param root JSONObjectに変換したデータを格納するオブジェクト
     * @param b 変換するBundle
     * @throws JSONException JSONへの変換に失敗した場合に発生
     */
    public static void convertBundleToJSON(
            final JSONObject root, final Bundle b) throws JSONException {
        JSONFactory.convertBundleToJSON(root, b);
        convertUri(root);
    }

    /**
     * AndroidManifest.xmlのversionNameを取得する.
     * 
     * @param context Context
     * @return versionName
     */
    public static String getVersionName(final Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            return "Unknown";
        }
    }

    public static String getIPAddress(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }


    /**
     * Checks whether permission allow by user.
     * @param context context of application
     * @return Returns true if permission allow, otherwise false
     */
    public static boolean isPermission(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            boolean result = true;
            for (int i = 0; i < PERMISSIONS.length; i++) {
                if (context.checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                    result = false;
                }
            }
            return result;
        }
    }
}
