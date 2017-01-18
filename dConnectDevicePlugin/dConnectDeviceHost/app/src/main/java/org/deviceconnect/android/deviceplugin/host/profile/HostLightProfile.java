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
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorderManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

import static android.hardware.camera2.CameraCharacteristics.*;

/**
 * Light Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostLightProfile extends LightProfile {
    /** ライトID. */
    private static final String HOST_LIGHT_ID = "1";

    /** ライト名称の初期値. */
    private static final String HOST_DEFAULT_LIGHT_NAME = "Host Light";

    /** HostDeviceRecorderManager instance. */
    private final HostDeviceRecorderManager mRecorderMgr;

    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCamCharacteristics;
    private CameraCaptureSession mSession;

    private CaptureRequest.Builder mBuilder;

    private Camera camera = null;

    private CameraManager mCamMgr;
    private String mCameraId = null;
    private boolean isOn = false;

    private final DConnectApi mGetLightApi = new GetApi() {

        @Override
        public boolean onRequest(Intent request, Intent response) {
            String serviceId = getServiceID(request);
            Bundle lightParam = new Bundle();
            setName(lightParam, HOST_DEFAULT_LIGHT_NAME);
            setConfig(lightParam, "");
            // TODO: カメラのライト状態を取得して設定
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                setLightId(lightParam, HOST_LIGHT_ID);
                camera = Camera.open();
                Camera.Parameters param = camera.getParameters();
                String mode = param.getFlashMode();
                isOn = mode.equals(Camera.Parameters.FLASH_MODE_TORCH);
                camera.release();
            } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                setLightId(lightParam, HOST_LIGHT_ID);
                switch (mBuilder.get(CaptureRequest.FLASH_MODE)) {
                    case CameraMetadata.FLASH_MODE_TORCH:
                        isOn = true;
                        break;
                    case CameraMetadata.FLASH_MODE_OFF:
                    default:
                        isOn = false;
                        break;
                }
            } else {
                setLightId(lightParam, mCameraId);
            }
            setOn(lightParam, isOn);
            List<Bundle> lightParams = new ArrayList<>();
            lightParams.add(lightParam);
            setLights(response, lightParams);
            setResult(response, DConnectMessage.RESULT_OK);
            sendResponse(response);
            return true;
        }
    };

    private final DConnectApi mPostLightApi = new PostApi() {
        @Override
        public boolean onRequest(Intent request, Intent response) {
            String serviceId = getServiceID(request);
            String lightId = getLightId(request);
            long[] flashing = getFlashing(request);

            if (lightId != null && lightId.length() == 0) {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                return true;
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                if (lightId != null && !(lightId.equals(HOST_LIGHT_ID))) {
                    MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                    return true;
                }
            } else {
                if (lightId != null && !(lightId.equals(mCameraId))) {
                    MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                    return true;
                }
            }


            if (flashing != null) {
                // TODO: 点滅制御追加
                //

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                // TODO:点灯制御
                isOn = true;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    camera = Camera.open();
                    Camera.Parameters param = camera.getParameters();
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(param);
                    camera.release();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (mCameraId != null) {
                        try {
                            mCamMgr.setTorchMode(mCameraId, true);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    try {
                        mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                        mSession.setRepeatingRequest(mBuilder.build(), null, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        }
    };

    private final DConnectApi mDeleteLightApi = new DeleteApi() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onRequest(Intent request, Intent response) {
            String serviceId = getServiceID(request);
            String lightId = getLightId(request);

            if (lightId != null && lightId.length() == 0) {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                return true;
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                if (lightId != null && !(lightId.equals(HOST_LIGHT_ID))) {
                    MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                    return true;
                }
            } else {
                if (lightId != null && !(lightId.equals(mCameraId))) {
                    MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                    return true;
                }
            }

            // TODO:消灯制御
            isOn = false;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                camera = Camera.open();
                Camera.Parameters param = camera.getParameters();
                param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(param);
                camera.release();
                return true;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mCameraId != null) {
                    try {
                        mCamMgr.setTorchMode(mCameraId, false);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                try {
                    mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    mSession.setRepeatingRequest(mBuilder.build(), null, null);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    public HostLightProfile(final Context context, final HostDeviceRecorderManager mgr) {
        mRecorderMgr = mgr;
        addApi(mGetLightApi);
        addApi(mPostLightApi);
        addApi(mDeleteLightApi);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamMgr = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCamMgr.registerTorchCallback(new CameraManager.TorchCallback() {
                    @Override
                    public void onTorchModeChanged(String cameraId, boolean enabled) {
                        mCameraId = cameraId;
                        isOn = enabled;
                    }
                }, new Handler());
            } else {
                try {
                    String[] id = mCamMgr.getCameraIdList();
                    if (id.length > 0) {
                        mCamCharacteristics = mCamMgr.getCameraCharacteristics(id[0]);
                        boolean isFlash = mCamCharacteristics.get(FLASH_INFO_AVAILABLE);
                        if (isFlash) {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            mCamMgr.openCamera(id[0], new MyCameraDeviceStateCallback(), null);
                        }
                    }
                }
                catch (CameraAccessException e)
                {
                    e.printStackTrace();
                }
            }
        } else {
            mCamMgr = null;
        }
    }

    class MyCameraDeviceStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            // get builder
            try {
                mBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                List<Surface> list = new ArrayList<Surface>();
                SurfaceTexture mSurfaceTexture = new SurfaceTexture(1);
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
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    }

    private Size getSmallestSize(String cameraId) throws CameraAccessException {
        Size[] outputSizes = mCamMgr.getCameraCharacteristics(cameraId).get(SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(SurfaceTexture.class);
        if (outputSizes == null || outputSizes.length == 0) {
            throw new IllegalStateException("Camera " + cameraId + "doesn't support any outputSize.");
        }
        Size chosen = outputSizes[0];
        for (Size s : outputSizes) {
            if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
                chosen = s;
            }
        }
        return chosen;
    }

    class MyCameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            mSession = session;
            try {
                mSession.setRepeatingRequest(mBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    }
}
