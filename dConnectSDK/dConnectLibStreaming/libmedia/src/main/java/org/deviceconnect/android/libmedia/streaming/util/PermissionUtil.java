package org.deviceconnect.android.libmedia.streaming.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public final class PermissionUtil {

    private PermissionUtil() {
    }

    /**
     * 指定されたパーミッションに許可が下りていないリストを作成します.
     *
     * <p>
     * 返却されるリストが空の場合には、全てのパーミッションに許可が下りています。
     * </p>
     *
     * @param context コンテキスト
     * @param permissions 確認するパーミッションの配列
     * @return パーミッションが下りていないリスト
     */
    public static List<String> checkPermissions(final Context context, final List<String> permissions) {
        return checkPermissions(context, permissions.toArray(new String[0]));
    }

    /**
     * 指定されたパーミッションに許可が下りていないリストを作成します.
     *
     * <p>
     * 返却されるリストが空の場合には、全てのパーミッションに許可が下りています。
     * </p>
     *
     * @param context コンテキスト
     * @param permissions 確認するパーミッションの配列
     * @return パーミッションが下りていないリスト
     */
    public static List<String> checkPermissions(final Context context, final String[] permissions) {
        List<String> denies = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                int permissionCheck = context.checkSelfPermission(permission);
                if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                    denies.add(permission);
                }
            }
        }
        return denies;
    }

    /**
     * パーミッションを要求します.
     *
     * @param activity リクエストレスポンスを受け取るActivity
     * @param permissions 許可を求めるパーミッションの配列
     * @param requestCode リクエストコード
     */
    public static void requestPermissions(final Activity activity, final List<String> permissions, final int requestCode) {
        requestPermissions(activity, permissions.toArray(new String[0]), requestCode);
    }

    /**
     * パーミッションを要求します.
     *
     * @param activity リクエストレスポンスを受け取るActivity
     * @param permissions 許可を求めるパーミッションの配列
     * @param requestCode リクエストコード
     */
    public static void requestPermissions(final Activity activity, final String[] permissions, final int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(permissions, requestCode);
        }
    }

    /**
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])} に返却されたパーミッションの許可を確認します。
     *
     * <p>
     * 返却されるリストが空の場合には、全てのパーミッションに許可が下りています。
     * </p>
     *
     * @param permissions パーミッションの配列
     * @param grantResults 許可
     * @return 許可が下りていないパーミッションのリスト.
     */
    public static List<String> checkRequestPermissionsResult(final String[] permissions, final int[] grantResults) {
        List<String> denies = new ArrayList<>();
        for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                denies.add(permissions[i]);
            }
        }
        return denies;
    }
}
