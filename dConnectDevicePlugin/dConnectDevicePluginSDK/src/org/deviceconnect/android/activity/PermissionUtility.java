package org.deviceconnect.android.activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class to make a permission request.
 *
 * @author NTT DOCOMO, INC.
 */
public final class PermissionUtility {

    private PermissionUtility() {
    }

    /**
     * パーミッションの許諾リクエストを要求する.
     *
     * <p>
     * Android OS 6.0以上の端末では、このメソッドが呼び出されると内部でActivityが起動して、ユーザに対してパーミッションの許諾確認を行う。<br>
     * ユーザから許可された場合には、{@link org.deviceconnect.android.activity.PermissionUtility.PermissionRequestCallback#onSuccess}が呼び出され、
     * 拒否された場合には、{@link org.deviceconnect.android.activity.PermissionUtility.PermissionRequestCallback#onFail(String)}が呼び出される。
     * </p>
     * <p>
     * Android OS 6.0未満の端末では、{@link org.deviceconnect.android.activity.PermissionUtility.PermissionRequestCallback#onSuccess}が常に呼び出される。
     * </p>
     * <br>
     * サンプルコード:
     * <pre>
     * {@code
     * String[] permissions = new String[] {
     *     Manifest.permission.ACCESS_COARSE_LOCATION,
     *     Manifest.permission.ACCESS_FINE_LOCATION
     * }
     * 
     * PermissionUtility.requestPermissions(getActivity(), new Handler(),
     *         permissions,
     *         new PermissionUtility.PermissionRequestCallback() {
     *             public void onSuccess() {
     *                 // 許可された時の処理
     *             }
     *             public void onFail(final String deniedPermission) {
     *                 // 拒否された時の処理
     *             }
     *         });
     * }
     * </pre>
     * @param context コンテキスト
     * @param handler ハンドラー
     * @param permissions 許可を求めるパーミッション群
     * @param callback 許諾通知を行うコールバック
     */
    public static void requestPermissions(@NonNull final Context context, @NonNull final Handler handler,
            @NonNull final String[] permissions, @NonNull final PermissionRequestCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callback.onSuccess();
        } else {
            Set<String> mPermissionSet = new HashSet<>(Arrays.asList(permissions));

            if (mPermissionSet.size() == 0) {
                callback.onSuccess();
                return;
            }
            Set<String> tmp = new HashSet<>(mPermissionSet);
            for (final String permission : tmp) {
                if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                    mPermissionSet.remove(permission);
                }
            }
            if (mPermissionSet.size() == 0) {
                callback.onSuccess();
            } else {
                String[] missingPermissions = mPermissionSet.toArray(new String[mPermissionSet.size()]);
                PermissionRequestActivity.requestPermissions(context, missingPermissions, new ResultReceiver(handler) {
                    @Override
                    protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                        String[] retPermissions = resultData.getStringArray(PermissionRequestActivity.EXTRA_PERMISSIONS);
                        int[] retGrantResults = resultData.getIntArray(PermissionRequestActivity.EXTRA_GRANT_RESULTS);
                        if (retPermissions == null || retGrantResults == null) {
                            callback.onFail(null);
                            return;
                        }
                        for (int i = 0; i < retPermissions.length; ++i) {
                            if (retGrantResults[i] == PackageManager.PERMISSION_DENIED) {
                                callback.onFail(retPermissions[i]);
                                return;
                            }
                        }
                        callback.onSuccess();
                    }
                });
            }
        }
    }

    /**
     * パーミッションの許諾リクエストの返答を通知するコールバック.
     */
    public interface PermissionRequestCallback {
        /**
         * パーミッションが許可された場合に呼び出される.
         */
        void onSuccess();

        /**
         * パーミッションが拒否されたときに呼び出される.
         * @param deniedPermission 拒否されたパーミッション
         */
        void onFail(@NonNull String deniedPermission);
    }
}
