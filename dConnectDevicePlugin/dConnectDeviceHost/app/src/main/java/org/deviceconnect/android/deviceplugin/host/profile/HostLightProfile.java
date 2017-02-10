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
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.profile.utils.FlashingExecutor;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorderManager;
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

    /** Camera クラスインスタンス.  */
    private Camera mCamera = null;

    /** ライト点灯/消灯状態. */
    private boolean isOn = false;

    /** Contextインスタンス. */
    private Context mContext = null;

    /** HostDeviceRecorderManagerインスタンス. */
    private HostDeviceRecorderManager mMgr = null;

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

                            isOn = mMgr.getCameraDevice().isFlashLightState();
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
                                mMgr.getCameraDevice().turnOnFlashLight();
                                setResult(response, DConnectMessage.RESULT_OK);
                            }
                            sendResponse(response);
                        }

                        @Override
                        public void onFail(@NonNull String deniedPermission) {
                            MessageUtils.setIllegalDeviceStateError(response,
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
                            mMgr.getCameraDevice().turnOffFlashLight();
                            mMgr.getCameraDevice().clean();
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
     * @param manager HostDeviceRecorderManager.
     */
    public HostLightProfile(final Context context, final HostDeviceRecorderManager manager) {
        mContext = context;
        mMgr = manager;
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
        mMgr.initialize();
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
            public void changeLight(boolean isState, FlashingExecutor.CompleteListener listener) {
                if (isState) {
                    isOn = true;
                    mMgr.getCameraDevice().turnOnFlashLight();
                } else {
                    isOn = false;
                    mMgr.getCameraDevice().turnOffFlashLight();
                    mMgr.getCameraDevice().clean();
                }
                listener.onComplete();
            }
        });
        exe.start(flashing);
    }
}
