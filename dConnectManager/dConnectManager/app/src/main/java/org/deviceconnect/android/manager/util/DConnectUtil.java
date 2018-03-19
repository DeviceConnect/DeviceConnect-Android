/*
 DConnectUtil.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.manager.DConnectSettings;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

/**
 * ユーティリティクラス.
 * @author NTT DOCOMO, INC.
 */
public final class DConnectUtil {
    /** 乱数の最大値. */
    private static final int MAX_NUM = 10000;
    /** キーワードの桁数を定義. */
    private static final int DIGIT = 4;
    /** 10進数の定義. */
    private static final int DECIMAL = 10;

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
     * キーワードを作成する.
     *
     * @return キーワード
     */
    public static String createKeyword() {
        StringBuilder builder = new StringBuilder();
        builder.append("DCONNECT-");
        int rand = Math.abs(new Random().nextInt() % MAX_NUM);
        for (int i = 0; i < DIGIT; i++) {
            int r = rand % DECIMAL;
            builder.append(r);
            rand /= DECIMAL;
        }
        return builder.toString();
    }

    /**
     * Device Connect Managerの名前を生成する.
     *
     * @return Device Connect Managerの名前
     */
    public static String createName() {
        StringBuilder builder = new StringBuilder();
        builder.append("Manager-");
        int rand = Math.abs(new Random().nextInt() % MAX_NUM);
        for (int i = 0; i < DIGIT; i++) {
            int r = rand % DECIMAL;
            builder.append(r);
            rand /= DECIMAL;
        }
        return builder.toString();
    }

    /**
     * Device Connect Managerの識別子を生成する.
     *
     * @return Device Connect Managerの識別子
     */
    public static String createUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * HttpメソッドをDConnectメソッドに変換する.
     * @param method 変換するHttpメソッド
     * @return DConnectメソッド
     */
    public static String convertHttpMethod2DConnectMethod(final String method) {
        if (DConnectMessage.METHOD_GET.equalsIgnoreCase(method)) {
            return IntentDConnectMessage.ACTION_GET;
        } else if (DConnectMessage.METHOD_POST.equalsIgnoreCase(method)) {
            return IntentDConnectMessage.ACTION_POST;
        } else if (DConnectMessage.METHOD_PUT.equalsIgnoreCase(method)) {
            return IntentDConnectMessage.ACTION_PUT;
        } else if (DConnectMessage.METHOD_DELETE.equalsIgnoreCase(method)) {
            return IntentDConnectMessage.ACTION_DELETE;
        }
        return null;
    }

    /**
     * ファイルへのURIを作成する.
     * @param settings DeviceConnect設定
     * @param uri ファイルへのContentUri
     * @return URI
     */
    private static String createUri(final DConnectSettings settings, final String uri) {
        StringBuilder builder = new StringBuilder();
        builder.append(settings.isSSL() ? "https://" : "http://");
        builder.append(settings.getHost());
        builder.append(":");
        builder.append(settings.getPort());
        builder.append("/gotapi/files");
        builder.append("?uri=");
        try {
            builder.append(URLEncoder.encode(uri, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to convert a uri.");
        }
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
     * @param settings DeviceConnect設定
     * @param root 変換するJSONObject
     * @throws JSONException JSONの解析に失敗した場合
     */
    private static void convertUri(final DConnectSettings settings, final JSONObject root) throws JSONException {
        @SuppressWarnings("unchecked") // Using legacy API
        Iterator<String> it = root.keys();
        while (it.hasNext()) {
            String key = it.next();
            Object value = root.opt(key);
            if (value instanceof String) {
                if ("uri".equals(key) && startWithContent((String) value)) {
                    String u = createUri(settings, (String) value);
                    root.put(key, u);
                }
            } else if (value instanceof JSONObject) {
                convertUri(settings, (JSONObject) value);
            }
        }
    }

    /**
     * 指定されたuriがcontent://から始まるかチェックする.
     * @param uri チェックするuri
     * @return content://から始まる場合はtrue、それ以外はfalse
     */
    private static boolean startWithContent(final String uri) {
        return uri != null && (uri.startsWith("content://"));
    }

    /**
     * BundleからJSONObjectに変換する.
     *
     * @param settings DeviceConnect設定
     * @param root JSONObjectに変換したデータを格納するオブジェクト
     * @param b 変換するBundle
     * @throws JSONException JSONへの変換に失敗した場合に発生
     */
    public static void convertBundleToJSON(
            final DConnectSettings settings,
            final JSONObject root, final Bundle b) throws JSONException {
        JSONUtils.convertBundleToJSON(root, b);
        convertUri(settings, root);
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

    /**
     * Gets the ip address.
     * @return Returns ip address
     */
    public static String getIPAddress(final Context context) {
        Context appContext = context.getApplicationContext();
        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager cManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cManager.getActiveNetworkInfo();
        String en0Ip = null;
        if (network != null) {
            switch (network.getType()) {
                case ConnectivityManager.TYPE_ETHERNET:
                    try {
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                            NetworkInterface intf = en.nextElement();
                            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                                InetAddress inetAddress = enumIpAddr.nextElement();
                                if (inetAddress instanceof Inet4Address
                                        && !inetAddress.getHostAddress().equals("127.0.0.1")) {
                                    en0Ip = inetAddress.getHostAddress();
                                    break;
                                }
                            }
                        }
                    } catch (SocketException e) {
                        Log.e("DConnectUtil", "Get Ethernet IP Error", e);
                    }
            }
        }
        if (en0Ip != null) {
            return en0Ip;
        } else {
            return NetworkUtil.getIpAddress();
        }
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

    /**
     * ストレージのパーミッションを要求します.
     * @param context コンテキスト
     * @param callback パーミッションの許諾を通知するコールバック
     */
    public static void requestPermission(final Context context, final Handler handler, final PermissionUtility.PermissionRequestCallback callback) {
        PermissionUtility.requestPermissions(context, handler, PERMISSIONS, callback);
    }

    /**
     * 指定されたDrawableをグレースケール変換をする.
     * @param drawable 変換するDrawable
     * @return 変換後のDrawable
     */
    public static Drawable convertToGrayScale(final Drawable drawable) {
        Drawable clone = drawable.getConstantState().newDrawable().mutate();
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0.2f);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        clone.setColorFilter(filter);
        return clone;
    }

    /** マスクを定義. */
    private static final int MASK = 0xFF;

    /**
     * バイト配列を16進数の文字列に変換する.
     * @param buf 文字列に変換するバイト
     * @return 文字列
     */
    private static String hexToString(final byte[] buf) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            hexString.append(Integer.toHexString(MASK & buf[i]));
        }
        return hexString.toString();
    }

    /**
     * 指定された文字列をMD5の文字列に変換する.
     * <p>
     * MD5への変換に失敗した場合には{@code null}を返却する。
     * </p>
     * @param s MD5にする文字列
     * @return MD5にされた文字列
     * @throws UnsupportedEncodingException 文字列の解析に失敗した場合
     * @throws NoSuchAlgorithmException MD5がサポートされていない場合
     */
    public static String toMD5(final String s)
        throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(s.getBytes("ASCII"));
        return hexToString(digest.digest());
    }

    /**
     * 指定されたContextのアプリケーションがDozeモードが有効になっているかを確認する.
     * <p>
     * Android M以前のOSでは、常にtrueを返却します。
     * </p>
     * @param context コンテキスト
     * @return Dozeモードが有効の場合はtrue、それ以外はfalse
     */
    public static boolean isDozeMode(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
    }

    /**
     * Dozeモードの解除要求を行う.
     * <p>
     * Dozeモードの解除には必ずユーザの許諾が必要になります。
     * </p>
     * <p>
     * Android M以前のOSの場合には、このメソッドは処理を行いません。
     * </p>
     * @param context コンテキスト
     */
    public static void startConfirmIgnoreDozeMode(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }

    /**
     * Dozeモードの設定画面を開きます.
     * <p>
     * Dozeモードの解除には必ずユーザの許諾が必要になります。
     * </p>
     * <p>
     * Android M以前のOSの場合には、このメソッドは処理を行いません。
     * </p>
     * @param context コンテキスト
     */
    public static void startDozeModeSettingActivity(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * プラグインのアイコン画像を読み込みます.
     * @param context コンテキスト
     * @param plugin
     * @return プラグインのアイコン画像
     */
    public static Drawable loadPluginIcon(final Context context, final DevicePlugin plugin) {
        return loadPluginIcon(context, plugin.getPackageName(), plugin.getPluginIconId());
    }

    /**
     * プラグインのアイコン画像を読み込みます.
     * @param context コンテキスト
     * @param packageName プラグインのパッケージ名
     * @param iconId アイコンのリソースID
     * @return プラグインのアイコン画像
     */
    public static Drawable loadPluginIcon(final Context context,
                                          final String packageName,
                                          final Integer iconId) {
        PackageManager pkgMgr = context.getPackageManager();
        Drawable icon;
        if (iconId != null) {
            icon = ResourcesCompat.getDrawable(context.getResources(), iconId, null);
        } else {
            try {
                ApplicationInfo info = pkgMgr.getApplicationInfo(packageName, 0);
                icon = pkgMgr.getApplicationIcon(info.packageName);
            } catch (PackageManager.NameNotFoundException e) {
                icon = null;
                if (BuildConfig.DEBUG) {
                    Log.d("Manager", "Icon is not found.");
                }
            }
        }
        return icon;
    }


    /**
     * リクエスト用のIntentをデバッグ用の文字列に変換します.
     * @param intent リクエスト用のIntent
     * @return デバッグ用の文字列
     */
    public static String convertRequestToString(final Intent intent) {
        StringBuilder sb = new StringBuilder();

        String action = intent.getAction();
        String api = intent.getStringExtra("api");
        String profile = intent.getStringExtra("profile");
        String inter = intent.getStringExtra("interface");
        String attr = intent.getStringExtra("attribute");

        if (IntentDConnectMessage.ACTION_GET.equals(action)) {
            sb.append("GET ");
        } else if (IntentDConnectMessage.ACTION_PUT.equals(action)) {
            sb.append("PUT ");
        } else if (IntentDConnectMessage.ACTION_POST.equals(action)) {
            sb.append("POST ");
        } else if (IntentDConnectMessage.ACTION_DELETE.equals(action)) {
            sb.append("DELETE ");
        }

        if (api != null) {
            sb.append("/");
            sb.append(api);
        }
        if (profile != null) {
            sb.append("/");
            sb.append(profile);
        }
        if (inter != null) {
            sb.append("/");
            sb.append(inter);
        }
        if (attr != null) {
            sb.append("/");
            sb.append(attr);
        }

        return sb.toString();
    }
}
