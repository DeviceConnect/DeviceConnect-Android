/*
 HostLightProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.profile.utils.FlashingExecutor;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Light Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostLightProfile extends LightProfile {
    /** ライトID. */
    private static final String HOST_LIGHT_ID = "0";

    /** ライト名称の初期値. */
    private static final String HOST_DEFAULT_LIGHT_NAME = "Host Light";

    /** 点滅制御用Map. */
    private Map<String, FlashingExecutor> mFlashingMap = new HashMap<>();

    /** Camera クラスインスタンス（OS 4.x用）.  */
    private Camera mCamera = null;

    /** Camera 制御用クラスインスタンス (OS 5.x以上用). */
    private FlashLightUtilForLollipop mFlashUtil = null;

    /** ライト点灯/消灯状態. */
    private boolean isOn = false;

    /** Contextインスタンス. */
    private Context mContext = null;
    /**
     * Get Light API.
     */
    private final DConnectApi mGetLightApi = new GetApi() {

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            PermissionUtility.requestPermissions(mContext,
                    new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.CAMERA},
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {
                            initCameraInstance();

                            String serviceId = getServiceID(request);
                            Bundle lightParam = new Bundle();
                            setName(lightParam, HOST_DEFAULT_LIGHT_NAME);
                            setConfig(lightParam, "");
                            setLightId(lightParam, HOST_LIGHT_ID);

                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                                Camera.Parameters param = mCamera.getParameters();
                                String mode = param.getFlashMode();
                                isOn = mode.equals(Camera.Parameters.FLASH_MODE_TORCH);
                            } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                                switch (mFlashUtil.mBuilder.get(CaptureRequest.FLASH_MODE)) {
                                    case CameraMetadata.FLASH_MODE_TORCH:
                                        isOn = true;
                                        break;
                                    case CameraMetadata.FLASH_MODE_OFF:
                                    default:
                                        isOn = false;
                                        break;
                                }
                            }
                            setOn(lightParam, isOn);

                            List<Bundle> lightParams = new ArrayList<>();
                            lightParams.add(lightParam);
                            setLights(response, lightParams);
                            setResult(response, DConnectMessage.RESULT_OK);
                            sendResponse(response);
                        }

                        @Override
                        public void onFail(@NonNull String deniedPermission) {
                            MessageUtils.setIllegalServerStateError(response,
                                    "CAMERA permission not granted.");
                            sendResponse(response);
                        }
                    });

            return false;
        }
    };

    /**
     * Post Light API.
     */
    private final DConnectApi mPostLightApi = new PostApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            final String lightId = getLightId(request);
            final long[] flashing = getFlashing(request);

            if (lightId != null && lightId.length() == 0) {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                return true;
            }

            if (lightId != null && !(lightId.equals(HOST_LIGHT_ID))) {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                return true;
            }

            PermissionUtility.requestPermissions(mContext,
                    new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.CAMERA},
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {
                            initCameraInstance();
                            if (flashing != null) {
                                flashing(HOST_LIGHT_ID, flashing);
                                setResult(response, DConnectMessage.RESULT_OK);
                            } else {
                                isOn = true;
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                                    Camera.Parameters param = mCamera.getParameters();
                                    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                    mCamera.setParameters(param);
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    mFlashUtil.setFlashLight(isOn);
                                }
                                setResult(response, DConnectMessage.RESULT_OK);
                            }
                            sendResponse(response);
                        }

                        @Override
                        public void onFail(@NonNull String deniedPermission) {
                            MessageUtils.setIllegalServerStateError(response,
                                    "CAMERA permission not granted.");
                            sendResponse(response);
                        }
                    });

            return false;
        }
    };

    /**
     * Delete Light API.
     */
    private final DConnectApi mDeleteLightApi = new DeleteApi() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            String lightId = getLightId(request);

            if (lightId != null && lightId.length() == 0) {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                return true;
            }

            if (lightId != null && !(lightId.equals(HOST_LIGHT_ID))) {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                return true;
            }

            PermissionUtility.requestPermissions(mContext,
                    new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.CAMERA},
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {
                            initCameraInstance();
                            isOn = false;
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                                Camera.Parameters param = mCamera.getParameters();
                                param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                mCamera.setParameters(param);
                                mCamera.release();
                                mCamera = null;
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                mFlashUtil.setFlashLight(isOn);
                            }
                            setResult(response, DConnectMessage.RESULT_OK);
                            sendResponse(response);
                        }

                        @Override
                        public void onFail(@NonNull String deniedPermission) {
                            MessageUtils.setIllegalServerStateError(response,
                                    "CAMERA permission not granted.");
                            sendResponse(response);
                        }
                    });

            return false;
        }
    };

    /**
     * Constructor.
     * @param context context.
     */
    public HostLightProfile(final Context context) {
        mContext = context;
        addApi(mGetLightApi);
        addApi(mPostLightApi);
        addApi(mDeleteLightApi);

        PermissionUtility.requestPermissions(mContext,
                new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.CAMERA},
                new PermissionUtility.PermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        initCameraInstance();
                    }

                    @Override
                    public void onFail(@NonNull String deniedPermission) {

                    }
                });

    }

    /**
     * Cameraインスタンス生成.
     */
    private void initCameraInstance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mFlashUtil == null) {
                mFlashUtil = new FlashLightUtilForLollipop(mContext);
            }
        } else {
            if (mCamera == null) {
                mFlashUtil = null;
                mCamera = Camera.open();
                mCamera.startPreview();
            }
        }
    }

    /**
     * 点滅制御.
     * @param id ライトID.
     * @param flashing 点滅パターン.
     */
    private void flashing(String id, long[] flashing) {
        FlashingExecutor exe = mFlashingMap.get(id);
        if (exe == null) {
            exe = new FlashingExecutor();
            mFlashingMap.put(id, exe);
        }
        exe.setLightControllable(new FlashingExecutor.LightControllable() {
            @Override
            public void changeLight(boolean isOn, FlashingExecutor.CompleteListener listener) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    if (mCamera == null) {
                        mCamera = Camera.open();
                        mCamera.startPreview();
                    }
                    Camera.Parameters param = mCamera.getParameters();
                    if (isOn) {
                        param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(param);
                    } else {
                        param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(param);
                        mCamera.release();
                        mCamera = null;
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mFlashUtil.setFlashLight(isOn);
                }
                listener.onComplete();
            }
        });
        exe.start(flashing);
    }

    /**
     * Camera 制御用クラス (OS 5.x以上用).
     */
    public class FlashLightUtilForLollipop {
        private CameraCaptureSession mSession;
        private CaptureRequest.Builder mBuilder;
        private CameraDevice mCameraDevice;
        private CameraManager mCameraManager;
        private SurfaceTexture mSurfaceTexture;

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public FlashLightUtilForLollipop(final Context context) {
            PermissionUtility.requestPermissions(context,
                    new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.CAMERA},
                    new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    try {
                        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                        CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(HOST_LIGHT_ID);
                        boolean flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        if (flashAvailable) {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            mCameraManager.openCamera(HOST_LIGHT_ID, new MyCameraDeviceStateCallback(), null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(@NonNull String deniedPermission) {

                }
            });
        }

        class MyCameraDeviceStateCallback extends CameraDevice.StateCallback {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                mCameraDevice = camera;
                try {
                    mBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    //flash on, default is on
                    mBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                    mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    List<Surface> list = new ArrayList<Surface>();
                    mSurfaceTexture = new SurfaceTexture(1);
                    Size size = getSmallestSize(mCameraDevice.getId());
                    mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                    Surface mSurface = new Surface(mSurfaceTexture);
                    list.add(mSurface);
                    mBuilder.addTarget(mSurface);
                    camera.createCaptureSession(list, new MyCameraCaptureSessionStateCallback(), null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        }

        private Size getSmallestSize(String cameraId) throws CameraAccessException {
            Size[] outputSizes = mCameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(SurfaceTexture.class);
            if (outputSizes == null || outputSizes.length == 0) {
                throw new IllegalStateException(
                        "Camera " + cameraId + "doesn't support any outputSize.");
            }
            Size chosen = outputSizes[0];
            for (Size s : outputSizes) {
                if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
                    chosen = s;
                }
            }
            return chosen;
        }

        /**
         * session callback
         */
        class MyCameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                mSession = session;
                try {
                    mSession.setRepeatingRequest(mBuilder.build(), null, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void setFlashLight(final boolean isOn) {
            try {
                if (isOn) {
                    mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                } else {
                    mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                }
                mSession.setRepeatingRequest(mBuilder.build(), null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void close() {
            if (mCameraDevice == null || mSession == null) {
                return;
            }
            mSession.close();
            mCameraDevice.close();
            mCameraDevice = null;
            mSession = null;
        }
    }
}
