/*
 CapabilityUtil.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.util.Size;

import org.deviceconnect.android.activity.IntentHandlerActivity;
import org.deviceconnect.android.activity.PermissionUtility;

import java.util.ArrayList;
import java.util.List;

public final class CapabilityUtil {
    private CapabilityUtil() {
    }

    public static void requestPermissions(final Context context, final Handler handler, final PermissionUtility.PermissionRequestCallback callback) {
        PermissionUtility.requestPermissions(context, handler, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, callback);
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

    public static Size getSupportedMaxSize(String mimeType) {
        List<Size> sizeList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaCodecList list = new MediaCodecList(MediaCodecList.ALL_CODECS);
            for (MediaCodecInfo codecInfo : list.getCodecInfos()) {
                if (codecInfo.isEncoder() &&
                        isHardware(codecInfo) &&
                        isMediaCodecInfo(codecInfo, mimeType, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)) {
                    Size size = printCodecInfo(codecInfo, mimeType);
                    if (size != null) {
                        sizeList.add(size);
                    }
                }
            }
        } else {
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
                if (codecInfo.isEncoder() &&
                        isHardware(codecInfo) &&
                        isMediaCodecInfo(codecInfo, mimeType, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)) {
                    Size size = printCodecInfo(codecInfo, mimeType);
                    if (size != null) {
                        sizeList.add(size);
                    }
                }
            }
        }

        Size size = null;
        int max = 0;

        for (Size s : sizeList) {
            int d = s.getWidth() * s.getHeight();
            if (max < d) {
                max = d;
                size = s;
            }
        }

        return size;
    }

    private static Size printCodecInfo(MediaCodecInfo codecInfo, String mimeType) {
        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (!type.startsWith(mimeType)) {
                continue;
            }

            try {
                MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                MediaCodecInfo.VideoCapabilities videoCapabilities = capabilities.getVideoCapabilities();
                if (videoCapabilities != null) {
                    int w = videoCapabilities.getSupportedWidths().getUpper();
                    int h = videoCapabilities.getSupportedHeights().getUpper();
                    return new Size(w, h);
                }
            } catch (Exception e) {
                // ignore.
            }
        }
        return null;
    }

    /**
     * 指定された MediaCodecInfo のマイムタイプとカラーフォーマットが一致するか確認します.
     *
     * @param codecInfo 確認する MediaCodecInfo
     * @param mimeType マイムタイプ
     * @param colorFormat カラーフォーマット
     * @return 一致する場合はtrue、それ以外はfalse
     */
    private static boolean isMediaCodecInfo(MediaCodecInfo codecInfo, String mimeType, int colorFormat) {
        if (!codecInfo.isEncoder()) {
            return false;
        }

        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (!type.equalsIgnoreCase(mimeType)) {
                continue;
            }

            try {
                MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                for (int i = 0; i < capabilities.colorFormats.length; i++) {
                    int format = capabilities.colorFormats[i];
                    if (colorFormat == format) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // ignore.
            }
        }

        return false;
    }

    private static boolean isHardware(MediaCodecInfo info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return info.isHardwareAccelerated();
        } else {
            // エンコーダ名が OMX.qcom. または OMX.Exynos. から始まる場合はハードウェアエンコーダ
            // エンコーダ名が OMX.google. から始まる場合はソフトウェアエンコーダ
            String name = info.getName();
            return name.startsWith("OMX.qcom.") || name.startsWith("OMX.Exynos.");
        }
    }
}
