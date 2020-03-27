package org.deviceconnect.android.libmedia.streaming.camera2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * カメラ操作を行うためのユーティリティクラス.
 */
final class Camera2Helper {
    private Camera2Helper() {}

    static void debugInfo(final CameraManager cameraManager, final String cameraId) {
        CameraCharacteristics characteristics;
        try {
            characteristics = cameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            return;
        }

        Log.d("Camera2", "---- Camera Info ----");

        int[] availableOpticalStabilization = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
        if (availableOpticalStabilization != null && availableOpticalStabilization.length > 0) {
            Log.d("Camera2", "LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION");
            for (int stabilization : availableOpticalStabilization) {
                if (stabilization == CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON) {
                    Log.d("Camera2", "  OPTICAL_STABILIZATION: LENS_OPTICAL_STABILIZATION_MODE_ON");
                } else {
                    Log.d("Camera2", "  OPTICAL_STABILIZATION: LENS_OPTICAL_STABILIZATION_MODE_OFF");
                }
            }
        } else {
            Log.d("Camera2", "Not support a LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION.");
        }

        int[] availableVideoStabilization = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
        if (availableVideoStabilization != null && availableVideoStabilization.length > 0) {
            Log.d("Camera2", "CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES");
            for (int stabilization : availableVideoStabilization) {
                if (stabilization == CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON) {
                    Log.d("Camera2", "  VIDEO_STABILIZATION: CONTROL_VIDEO_STABILIZATION_MODE_ON");
                } else {
                    Log.d("Camera2", "  VIDEO_STABILIZATION: CONTROL_VIDEO_STABILIZATION_MODE_OFF");
                }
            }
        } else {
            Log.d("Camera2", "Not support a CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES.");
        }

        Range<Long> exposureRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        if (exposureRange != null) {
            Log.d("Camera2", "EXPOSURE: " + exposureRange.toString());
        }

        Range<Integer> sensiivityRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        if (sensiivityRange != null) {
            Log.d("Camera2", "ISO: " + sensiivityRange.toString());
        }

        Long frameDuration = characteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION);
        if (frameDuration != null) {
            Log.d("Camera2", "MAX_FRAME_DURATION: " + frameDuration);
        }

        Range<Integer> compensationRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        if (compensationRange != null) {
            Log.d("Camera2", "COMPENSATION: " + compensationRange.toString());
        }

        Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        Log.d("Camera2", "Support Auto Flash: " + (available == null ? false : available));


        Integer level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (level != null) {
            switch (level) {
                case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                    Log.d("Camera2", "LEVEL: INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY");
                    break;
                case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                    Log.d("Camera2", "LEVEL: INFO_SUPPORTED_HARDWARE_LEVEL_FULL");
                    break;
            }
        } else {
            Log.d("Camera2", "LEVEL: Not supported.");
        }

        Range<Integer>[] fpsRange = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        if (fpsRange != null) {
            Log.d("Camera2", "Support FPS");
            for (Range<Integer> range : fpsRange) {
                Log.d("Camera2", "  " + range.toString());
            }
        }

        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            Size[] pictureSizes = map.getOutputSizes(ImageFormat.JPEG);
            if (pictureSizes != null) {
                Log.d("Camera2", "PictureSizes: " + pictureSizes.length);
                for (Size size : pictureSizes) {
                    Log.d("Camera2", "  " + size.toString());
                }
            }

            Size[] previewSizes = map.getOutputSizes(SurfaceTexture.class);
            if (previewSizes != null) {
                Log.d("Camera2", "PreviewSizes: " + previewSizes.length);
                for (Size size : previewSizes) {
                    Log.d("Camera2", "  " + size.toString());
                }
            }

            Size[] highSpeedVideoSizes = map.getHighSpeedVideoSizes();
            if (highSpeedVideoSizes != null) {
                Log.d("Camera2", "HighSpeedVideoSizes: " + highSpeedVideoSizes.length);
                for (Size size : highSpeedVideoSizes) {
                    Log.d("Camera2", "  " + size.toString());
                }
            }

            Range<Integer>[] highSpeedFps = map.getHighSpeedVideoFpsRanges();
            if (highSpeedFps != null) {
                Log.d("Camera2", "highSpeedFps " + highSpeedFps.length);
                for (Range<Integer> range : highSpeedFps) {
                    Log.d("Camera2", "  " + range.toString());
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int[] formats = map.getOutputFormats();
                if (formats != null) {
                    Log.d("Camera2", "OutputFormat List");
                    for (int format : formats) {
                        Log.d("Camera2", "  Format: " + format);
                        Size[] highResolutionSizes = map.getHighResolutionOutputSizes(format);
                        if (highResolutionSizes != null) {
                            for (Size size : highResolutionSizes) {
                                Log.d("Camera2", "    SIZE: " + size.toString());
                            }
                        }
                    }
                }
            }
        }
        Log.d("Camera2", "---------------------");
    }

    /**
     * Auto-Focus の状態を文字列に変換します。
     *
     * @param afState Auto Focus の状態
     * @return 文字列
     */
    static String debugAFState(Integer afState) {
        if (afState == null) {
            return "NULL";
        }

        switch (afState) {
            default:
                return "UNKNOWN";
            case CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN:
                return "CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN";
            case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED:
                return "CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED";
            case CaptureResult.CONTROL_AF_STATE_INACTIVE:
                return "CaptureResult.CONTROL_AF_STATE_INACTIVE";
            case CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                return "CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED";
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                return "CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED";
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN:
                return "CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN";
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
                return "CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED";
        }
    }

    /**
     * Auto-Exposure の状態を文字列に変換します。
     *
     * @param asState Auto Exposure の状態
     * @return 文字列
     */
    static String debugAEState(Integer asState) {
        if (asState == null) {
            return "NULL";
        }

        switch (asState) {
            default:
                return "UNKNOWN";
            case CaptureResult.CONTROL_AE_STATE_CONVERGED:
                return "CaptureResult.CONTROL_AE_STATE_CONVERGED";
            case CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED:
                return "CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED";
            case CaptureResult.CONTROL_AE_STATE_INACTIVE:
                return "CaptureResult.CONTROL_AE_STATE_INACTIVE";
            case CaptureResult.CONTROL_AE_STATE_LOCKED:
                return "CaptureResult.CONTROL_AE_STATE_LOCKED";
            case CaptureResult.CONTROL_AE_STATE_PRECAPTURE:
                return "CaptureResult.CONTROL_AE_STATE_PRECAPTURE";
            case CaptureResult.CONTROL_AE_STATE_SEARCHING:
                return "CaptureResult.CONTROL_AE_STATE_SEARCHING";
        }
    }

    /**
     * 指定されたカメラの取り付けられた向きを取得します.
     *
     * @param cameraManager カメラマネージャ
     * @param cameraId カメラID
     * @return カメラの取り付けられた向き(0, 90, 180, 270)
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
                Collections.sort(previewSizes, SIZE_COMPARATOR);
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
                Collections.sort(pictureSizes, SIZE_COMPARATOR);
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
        return ImageReader.newInstance(width, height, imageFormat, 1);
    }

    /**
     * 画面の向きを取得します.
     *
     * @param context コンテキスト
     * @return 画面の向き(Surface.ROTATION_0, Surface.ROTATION_90, Surface.ROTATION_180, Surface.ROTATION_270)
     */
    static int getDisplayRotation(final Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm == null ? 0 : wm.getDefaultDisplay().getRotation();
    }

    /**
     * サイズの小さい方からソートを行うための比較演算子.
     */
    private static final Comparator<Size> SIZE_COMPARATOR = (lhs, rhs) -> {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    };
}
