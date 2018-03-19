/*
 CapabilityUtil.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;

import org.deviceconnect.android.activity.IntentHandlerActivity;
import org.deviceconnect.android.activity.PermissionUtility;

public final class CapabilityUtil {
    private CapabilityUtil() {
    }

    public static void requestPermissions(final Context context, final Handler handler, final PermissionUtility.PermissionRequestCallback callback) {
        PermissionUtility.requestPermissions(context, handler, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO}, callback);
    }

    public static void requestPermissions(final Context context, final PermissionUtility.PermissionRequestCallback callback) {
        requestPermissions(context, new Handler(Looper.getMainLooper()), callback);
    }

    public static void checkCapability(final Context context, final Handler handler, final Callback callback) {
        final ResultReceiver cameraCapabilityCallback = new ResultReceiver(handler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        callback.onSuccess();
                    } else {
                        callback.onFail();
                    }
                } catch (Throwable throwable) {
                    callback.onFail();
                }
            }
        };
        final ResultReceiver overlayDrawingCapabilityCallback = new ResultReceiver(handler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        CapabilityUtil.checkCameraCapability(context, cameraCapabilityCallback);
                    } else {
                        callback.onFail();
                    }
                } catch (Throwable throwable) {
                    callback.onFail();
                }
            }
        };
        CapabilityUtil.checkOverlayDrawingCapability(context, handler, overlayDrawingCapabilityCallback);
    }

    /**
     * オーバーレイ表示のパーミッションを確認します.
     *
     * @param context コンテキスト
     * @param handler ハンドラー
     * @param resultReceiver 確認を受けるレシーバ
     */
    @TargetApi(23)
    private static void checkOverlayDrawingCapability(final Context context, final Handler handler, final ResultReceiver resultReceiver) {
        if (Settings.canDrawOverlays(context)) {
            resultReceiver.send(Activity.RESULT_OK, null);
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            IntentHandlerActivity.startActivityForResult(context, intent, new ResultReceiver(handler) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (Settings.canDrawOverlays(context)) {
                        resultReceiver.send(Activity.RESULT_OK, null);
                    } else {
                        resultReceiver.send(Activity.RESULT_CANCELED, null);
                    }
                }
            });
        }
    }

    /**
     * カメラのパーミッションを確認します.
     *
     * @param context コンテキスト
     * @param resultReceiver 確認を受けるレシーバ
     */
    private static void checkCameraCapability(final Context context, final ResultReceiver resultReceiver) {
        PermissionUtility.requestPermissions(context, new Handler(), new String[]{Manifest.permission.CAMERA},
                new PermissionUtility.PermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        resultReceiver.send(Activity.RESULT_OK, null);
                    }

                    @Override
                    public void onFail(final String deniedPermission) {
                        resultReceiver.send(Activity.RESULT_CANCELED, null);
                    }
                });
    }

    /**
     * Overlayの表示結果を通知するコールバック.
     */
    public interface Callback {
        /**
         * 表示できたことを通知します.
         */
        void onSuccess();

        /**
         * 表示できなかったことを通知します.
         */
        void onFail();
    }
}
