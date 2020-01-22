package org.deviceconnect.android.libmedia.streaming.camera2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

public class Camera2WrapperManager {

    /**
     * カメラが取り付けられている向きを取得します.
     *
     * @param context コンテキスト
     * @param facing カメラのタイプ
     * @return カメラの向き(0, 90, 180, 270)
     */
    public static int getSensorOrientation(Context context, int facing) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) {
            throw new UnsupportedOperationException("Not supported a Camera.");
        }

        try {
            String cameraId = Camera2Helper.getCameraId(manager, facing);
            if (cameraId != null) {
                return Camera2Helper.getSensorOrientation(manager, cameraId);
            }
        } catch (CameraAccessException e) {
            // ignore.
        }

        throw new UnsupportedOperationException("Not supported a Camera.");
    }

    /**
     * ディスプレイの向きを取得します.
     *
     * @param context コンテキスト
     * @param rotation カメラの向き指定
     * @return ディスプレイの向き(Surface.ROTATION_0, Surface.ROTATION_90, Surface.ROTATION_180, Surface.ROTATION_270)
     */
    public static int getDisplayRotation(Context context, Camera2Wrapper.Rotation rotation) {
        switch (rotation) {
            default:
            case FREE:
                return Camera2Helper.getDisplayRotation(context);
            case ROTATION_0:
                return Surface.ROTATION_0;
            case ROTATION_90:
                return Surface.ROTATION_90;
            case ROTATION_180:
                return Surface.ROTATION_180;
            case ROTATION_270:
                return Surface.ROTATION_270;
        }
    }

    /**
     * カメラの取り付けられた向きと画面の向きから縦横のスワップが必要か確認します.
     *
     * @return スワップが必要な場合はtrue、それ以外はfalse
     */
    public static boolean isSwappedDimensions(Context context, int facing, Camera2Wrapper.Rotation rotation) {
        int sensorOrientation = getSensorOrientation(context, facing);
        switch (getDisplayRotation(context, rotation)) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    return true;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * Camera2Wrapper のインスタンスを作成します.
     *
     * <p>
     * バックにあるカメラを優先的に探して、サポートされていない場合にはフロントのカメラのインスタンスを作成します。
     * </p>
     *
     * @param context コンテキスト
     * @return Camera2Wrapper
     */
    public static Camera2Wrapper createCamera(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) {
            throw new UnsupportedOperationException("Not supported a Camera.");
        }

        try {
            String cameraId = Camera2Helper.getCameraId(manager, CameraCharacteristics.LENS_FACING_BACK);
            if (cameraId == null) {
                cameraId = Camera2Helper.getCameraId(manager, CameraCharacteristics.LENS_FACING_FRONT);
            }
            return new Camera2Wrapper(context, cameraId);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Not supported a Camera.");
        }
    }

    /**
     * Camera2Wrapper のインスタンスを作成します.
     *
     * <p>
     * 引数の camera には、以下の値が指定できます。
     * <ul>
     *     <li>CameraCharacteristics.LENS_FACING_BACK</li>
     *     <li>CameraCharacteristics.LENS_FACING_FRONT</li>
     *     <li>CameraCharacteristics.LENS_FACING_EXTERNAL</li>
     * </ul>
     * サポートされているタイプは、{@link #supportCameraIds}で確認できます。
     * また、サポートされていないタイプが指定された場合には例外が発生します。
     * </p>
     *
     * @param context コンテキスト
     * @param facing カメラのタイプ
     * @return Camera2Wrapper
     */
    public static Camera2Wrapper createCamera(Context context, int facing) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) {
            throw new UnsupportedOperationException("Not supported a Camera.");
        }

        try {
            String cameraId = Camera2Helper.getCameraId(manager, facing);
            if (cameraId == null) {
                throw new UnsupportedOperationException("Not supported a Camera. facing=" + facing);
            }
            return new Camera2Wrapper(context, cameraId);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Not supported a Camera.");
        }
    }

    /**
     * サポートしているカメラのタイプ一覧を取得します.
     *
     * @param context コンテキスト
     * @return カメラのタイプ一覧
     */
    public static List<Integer> supportCameraIds(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) {
            throw new UnsupportedOperationException("Not supported a Camera.");
        }

        List<Integer> list = new ArrayList<>();
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null) {
                    switch (facing) {
                        case CameraCharacteristics.LENS_FACING_BACK:
                            list.add(CameraCharacteristics.LENS_FACING_BACK);
                            break;
                        case CameraCharacteristics.LENS_FACING_FRONT:
                            list.add(CameraCharacteristics.LENS_FACING_FRONT);
                            break;
                        default:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                list.add(CameraCharacteristics.LENS_FACING_EXTERNAL);
                            }
                            break;
                    }
                }
            }
        } catch (CameraAccessException e) {
            // ignore.
        }
        return list;
    }

    /**
     * カメラのパーミッショに許可が下りているか確認します.
     * <p>
     * 端末の SDK レベルが 23 未満の場合には、パーミッションが不要なので常にtrueを返却します。
     * </p>
     * @param context コンテキスト
     * @return カメラのパーミッションに許可が下りている場合はtrue、それ以外はfalse
     */
    public static boolean checkCameraPermission(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = context.checkSelfPermission(Manifest.permission.CAMERA);
            return (permissionCheck == PackageManager.PERMISSION_GRANTED);
        } else {
            return true;
        }
    }

    /**
     * カメラのパーミッションを要求します.
     *
     * @param activity リクエストレスポンスを受け取るActivity
     * @param requestCode リクエストコード
     */
    public static void requestCameraPermission(final Activity activity, final int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.CAMERA
            }, requestCode);
        }
    }

    public static boolean checkRequestPermissionsResult(String[] permissions, int[] grantResults) {
        for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
            String permission = permissions[i];
            if (Manifest.permission.CAMERA.equals(permission)) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }
}
