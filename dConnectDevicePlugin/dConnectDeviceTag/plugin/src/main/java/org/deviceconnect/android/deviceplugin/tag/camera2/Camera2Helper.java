/*
 Camera2Helper.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.camera2;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * カメラ操作を行うためのユーティリティクラス.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
final class Camera2Helper {
    private Camera2Helper() {}

    /**
     * 指定されたカメラの取り付けられた向きを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return カメラの取り付けられた向き
     */
    static int getSensorOrientation(final CameraManager cameraManager, final String cameraId) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Integer sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            return sensorOrientation == null ? 0 : sensorOrientation;
        } catch (CameraAccessException e) {
            return 0;
        }
    }

    /**
     * 指定されたカメラIDの向きを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param id カメラID
     * @return 向き
     * @throws CameraAccessException カメラの操作に失敗した場合に発生
     */
    static int getFacing(final CameraManager cameraManager, final String id) throws CameraAccessException {
        for (String cameraId : cameraManager.getCameraIdList()) {
            if (cameraId.equals(id)) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null) {
                    return facing;
                }
            }
        }
        return -1;
    }

    /**
     * 指定された facing に対応するカメラIDを取得します.
     * <p>
     * facing に対応したカメラが発見できない場合には null を返却します。
     * </p>
     * @param cameraManager カメラマネージャ
     * @param facing カメラの向き
     * @return カメラID
     * @throws CameraAccessException カメラの操作に失敗した場合に発生
     */
    static String getCameraId(final CameraManager cameraManager, final int facing) throws CameraAccessException {
        for (String cameraId : cameraManager.getCameraIdList()) {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Integer supportFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (supportFacing != null && supportFacing == facing) {
                return cameraId;
            }
        }
        return null;
    }

    /**
     * カメラID に対応したカメラデバイスがサポートしているプレビューサイズのリストを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return サポートしているプレビューサイズのリスト
     */
    @NonNull
    static List<Size> getSupportedPreviewSizes(final CameraManager cameraManager, final String cameraId) {
        List<Size> previewSizes = new ArrayList<>();
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                previewSizes = Arrays.asList(map.getOutputSizes(SurfaceTexture.class));
                Collections.sort(previewSizes, SizeComparator);
            }
        } catch (CameraAccessException e) {
            // ignore.
        }
        return previewSizes;
    }

    /**
     * カメラID に対応したカメラデバイスがサポートしている写真サイズのリストを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return サポートしている写真サイズのリスト
     */
    @NonNull
    static List<Size> getSupportedPictureSizes(final CameraManager cameraManager, final String cameraId) {
        List<Size> pictureSizes = new ArrayList<>();
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if(map != null) {
                pictureSizes = Arrays.asList(map.getOutputSizes(ImageFormat.JPEG));
                Collections.sort(pictureSizes, SizeComparator);
            }
        } catch (CameraAccessException e) {
            // ignore.
        }
        return pictureSizes;
    }

    /**
     * ImageReader のインスタンスを作成します.
     *
     * @param width 横幅
     * @param height 縦幅
     * @param imageFormat 写真のフォーマット
     * @return ImageReader のインスタンス
     */
    static ImageReader createImageReader(final int width, final int height, final int imageFormat) {
        return ImageReader.newInstance(height, width, imageFormat, 1);
    }

    /**
     * スクリーンの向きを取得します.
     *
     * @param context コンテキスト
     * @return スクリーンの向き
     */
    static int getScreenOrientation(final Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    /**
     * 画面の向きを取得します.
     *
     * @param context コンテキスト
     * @return 画面の向き
     */
    static int getDisplayRotation(final Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm == null ? 0 : wm.getDefaultDisplay().getRotation();
    }

    /**
     * 画面の向きを取得します.
     *
     * @param context コンテキスト
     * @return 画面の向き
     */
    static int getDisplayRotation2(final Context context) {
        switch (getDisplayRotation(context)) {
            default:
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
    }

    /**
     * サイズの小さい方からソートを行うための比較演算子.
     */
    private static final Comparator<Size> SizeComparator = (lhs, rhs) -> {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    };
}
