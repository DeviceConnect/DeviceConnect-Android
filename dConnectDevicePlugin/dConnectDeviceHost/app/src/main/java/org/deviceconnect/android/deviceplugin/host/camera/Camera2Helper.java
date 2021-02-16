/*
 Camera2Helper.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.util.Range;
import android.util.Size;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * カメラ操作を行うためのユーティリティクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public final class Camera2Helper {
    private Camera2Helper() {}

    /**
     * 指定されたカメラの取り付けられた向きを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return カメラの取り付けられた向き
     */
    public static int getSensorOrientation(final CameraManager cameraManager, final String cameraId) {
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
    public static int getFacing(final CameraManager cameraManager, final String id) throws CameraAccessException {
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
    public static String getCameraId(final CameraManager cameraManager, final int facing) throws CameraAccessException {
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
     * 指定された機能が利用可能か確認します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @param capability 機能
     * @return 機能が利用可能な場合は true、それ以外は false
     */
    private static boolean availableCapability(final CameraManager cameraManager, final String cameraId, final int capability) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            int[] capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
            if (capabilities != null) {
                for (int caps : capabilities) {
                    if (caps == capability) {
                        return true;
                    }
                }
            }
        } catch (CameraAccessException e) {
            // ignore.
        }
        return false;
    }

    /**
     * デプス機能が利用可能か確認します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return 機能が利用可能な場合は true、それ以外は false
     */
    public static boolean availableDepth(final CameraManager cameraManager, final String cameraId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        return availableCapability(cameraManager, cameraId, CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT);
    }

    /**
     * デプスとカメラ映像が排他的か確認します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return 排他的な場合はtrue、それ以外はfalse
     */
    public static boolean exclusiveDepth(final CameraManager cameraManager, final String cameraId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }

        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Boolean exclusive = characteristics.get(CameraCharacteristics.DEPTH_DEPTH_IS_EXCLUSIVE);
            if (exclusive != null) {
                return exclusive;
            }
        } catch (CameraAccessException e) {
            // ignore.
        }
        return false;
    }

    /**
     * カメラID に対応したカメラデバイスがサポートしているプレビューサイズのリストを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return サポートしているプレビューサイズのリスト
     */
    @NonNull
    public static List<Size> getSupportedPreviewSizes(final CameraManager cameraManager, final String cameraId) {
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
    public static List<Size> getSupportedPictureSizes(final CameraManager cameraManager, final String cameraId) {
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
     * カメラがサポートしている FPS の一覧を取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return サポートしている FPS のリスト
     */
    @NonNull
    static List<Range<Integer>> getSupportedFps(final CameraManager cameraManager, final String cameraId) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Range<Integer>[] fps = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            if (fps != null) {
                return Arrays.asList(fps);
            }
        } catch (CameraAccessException e) {
            // ignore.
        }
        return new ArrayList<>();
    }

    private static List<Integer> getSupportedParam(final CameraManager cameraManager, final String cameraId, CameraCharacteristics.Key<int[]> key) {
        List<Integer> list = new ArrayList<>();
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            int[] modes = characteristics.get(key);
            if (modes != null) {
                for (int mode : modes) {
                    list.add(mode);
                }
            }
        } catch (CameraAccessException e) {
            // ignore.
        }
        return list;
    }

    /**
     * デフォルトで使用する自動フォーカスモードを取得します.
     *
     * @param context コンテキスト
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return デフォルトで使用する自動フォーカスモード
     */
    static Integer choiceAutoFocusMode(final Context context, final CameraManager cameraManager, final String cameraId) {
        PackageManager pkgMgr = context.getPackageManager();
        if (!pkgMgr.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            return null;
        }

        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            int[] afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            if (afModes == null) {
                return null;
            }
            for (int afMode : afModes) {
                if (afMode == CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE) {
                    return afMode;
                }
            }
            return null;
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /**
     * デフォルトで使用する自動露出モードを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return デフォルトで使用する自動露出モード
     */
    static Integer choiceAutoExposureMode(final CameraManager cameraManager, final String cameraId) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            int[] aeModes = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
            if (aeModes == null) {
                return null;
            }
            for (int aeMode : aeModes) {
                if (aeMode == CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH) {
                    return aeMode;
                }
            }
            for (int aeMode : aeModes) {
                if (aeMode == CameraMetadata.CONTROL_AE_MODE_ON) {
                    return aeMode;
                }
            }
            return null;
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /**
     * カメラがサポートしている自動フォーカスモードの一覧を取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return サポートしている自動フォーカスモードのリスト
     */
    @NonNull
    static List<Integer> getSupportedAutoFocusMode(final CameraManager cameraManager, final String cameraId) {
        return getSupportedParam(cameraManager, cameraId, CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
    }

    /**
     * カメラがサポートしているホワイトバランスモードの一覧を取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return サポートしているホワイトバランスモードのリスト
     */
    @NonNull
    static List<Integer> getSupportedAWB(final CameraManager cameraManager, final String cameraId) {
        return getSupportedParam(cameraManager, cameraId, CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
    }

    /**
     * カメラがサポートしている自動露出モードを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return サポートしている自動露出モードのリスト
     */
    @NonNull
    static List<Integer> getSupportedAutoExposureMode(final CameraManager cameraManager, final String cameraId) {
        return getSupportedParam(cameraManager, cameraId, CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
    }

    /**
     * カメラがサポートしている手ぶれ補正モードを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return サポートしている手ぶれ補正モードのリスト
     */
    @NonNull
    static List<Integer> getSupportedStabilization(final CameraManager cameraManager, final String cameraId) {
        return getSupportedParam(cameraManager, cameraId, CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
    }

    /**
     * カメラがサポートしている光学手ぶれ補正モードを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return サポートしている光学手ぶれ補正モードのリスト
     */
    @NonNull
    static List<Integer> getSupportedOpticalStabilization(final CameraManager cameraManager, final String cameraId) {
        return getSupportedParam(cameraManager, cameraId, CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
    }

    /**
     * カメラがサポートしているノイズ低減モードを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return サポートしているノイズ低減モードのリスト
     */
    @NonNull
    static List<Integer> getSupportedNoiseReductionMode(final CameraManager cameraManager, final String cameraId) {
        return getSupportedParam(cameraManager, cameraId, CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES);
    }

    /**
     * 有効になっているカメラのサイズを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return カメラのサイズ
     */
    static Rect getActiveArraySize(final CameraManager cameraManager, final String cameraId) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            return characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /**
     * 自動フォーカスの最大範囲を取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return 自動フォーカスの最大範囲
     */
    static Integer getMaxMeteringArea(final CameraManager cameraManager, final String cameraId) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            return characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /**
     * デジタルズームの最大値を取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return デジタルズームの最大値
     */
    static Float getMaxDigitalZoom(final CameraManager cameraManager, final String cameraId) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            return characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /**
     * 露出時間の範囲を取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return 露出時間の範囲
     */
    static Range<Long> getSupportedSensorExposureTime(final CameraManager cameraManager, final String cameraId) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            return characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /**
     * ISO感度の範囲を取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return ISO感度の範囲
     */
    static Range<Integer> getSupportedSensorSensitivity(final CameraManager cameraManager, final String cameraId) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            return characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /**
     * フレーム期間の最大値を取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return フレーム期間の最大値
     */
    static Long getMaxSensorFrameDuration(final CameraManager cameraManager, final String cameraId) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            return characteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION);
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /**
     * 焦点距離のリストを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return 焦点距離のリスト
     */
    static List<Float> getSupportedFocalLengthList(final CameraManager cameraManager, final String cameraId) {
        List<Float> list = new ArrayList<>();
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            float[] modes = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            if (modes != null) {
                for (float mode : modes) {
                    list.add(mode);
                }
            }
        } catch (CameraAccessException e) {
            // ignore.
        }
        return list;
    }

    /**
     * ImageReader のインスタンスを作成します.
     *
     * @param width 横幅
     * @param height 縦幅
     * @param imageFormat 写真のフォーマット
     * @return ImageReader のインスタンス
     */
    public static ImageReader createImageReader(final int width, final int height, final int imageFormat) {
        return ImageReader.newInstance(width, height, imageFormat, 1);
    }

    /**
     * サイズの小さい方からソートを行うための比較演算子.
     */
    private static final Comparator<Size> SizeComparator = (lhs, rhs) -> {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    };
}
