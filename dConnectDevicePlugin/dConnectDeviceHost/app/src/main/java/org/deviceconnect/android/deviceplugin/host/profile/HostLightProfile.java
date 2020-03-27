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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.profile.utils.FlashingExecutor;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
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

import androidx.annotation.NonNull;

/**
 * Light Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostLightProfile extends LightProfile {
    /**
     * ライトID.
     */
    private static final String HOST_LIGHT_ID = "0";

    /**
     * ライト名称の初期値.
     */
    private static final String HOST_DEFAULT_LIGHT_NAME = "Host Light";

    /**
     * 点滅制御用Map.
     */
    private Map<String, FlashingExecutor> mFlashingMap = new HashMap<>();

    /**
     * Contextインスタンス.
     */
    private Context mContext;

    /**
     * HostDevicePhotoRecorderインスタンス.
     */
    private HostDevicePhotoRecorder mPhotoRec;

    /**
     * レスポンスを返すハンドラー.
     */
    private final Handler mResponseHandler;

    /**
     * 点滅を制御するハンドラー.
     */
    private final Handler mFlashingHandler;

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
                            Bundle lightParam = new Bundle();
                            setName(lightParam, HOST_DEFAULT_LIGHT_NAME);
                            setConfig(lightParam, "");
                            setLightId(lightParam, HOST_LIGHT_ID);
                            setOn(lightParam, mPhotoRec.isFlashLightState());

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
                            if (flashing != null) {
                                flashing(HOST_LIGHT_ID, flashing);
                                setResult(response, DConnectMessage.RESULT_OK);
                                sendResponse(response);
                            } else {
                                mPhotoRec.turnOnFlashLight(new HostDevicePhotoRecorder.TurnOnFlashLightListener() {
                                    @Override
                                    public void onRequested() {
                                        setResult(response, DConnectMessage.RESULT_OK);
                                        sendResponse(response);
                                    }

                                    @Override
                                    public void onTurnOn() {
                                        // NOTE: カメラに正常にリクエストできた時点でレスポンスを返すので、
                                        // ここでは何もしない
                                    }

                                    @Override
                                    public void onError(final HostDevicePhotoRecorder.Error error) {
                                        setPhotoRecorderError(response, error);
                                        sendResponse(response);
                                    }
                                }, mResponseHandler);
                            }
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
                            mPhotoRec.turnOffFlashLight(new HostDevicePhotoRecorder.TurnOffFlashLightListener() {
                                @Override
                                public void onRequested() {
                                    setResult(response, DConnectMessage.RESULT_OK);
                                    sendResponse(response);
                                }

                                @Override
                                public void onTurnOff() {
                                    // NOTE: カメラに正常にリクエストできた時点でレスポンスを返すので、
                                    // ここでは何もしない
                                }

                                @Override
                                public void onError(final HostDevicePhotoRecorder.Error error) {
                                    setPhotoRecorderError(response, error);
                                    sendResponse(response);
                                }
                            }, mResponseHandler);
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

    private static void setPhotoRecorderError(final Intent response,
                                              final HostDevicePhotoRecorder.Error error) {
        switch (error) {
            case UNSUPPORTED:
                MessageUtils.setInvalidRequestParameterError(response,
                        "this camera has no flash light.");
                break;
            case FATAL_ERROR:
                MessageUtils.setIllegalDeviceStateError(response,
                        "this camera is not available.");
                break;
            default:
                MessageUtils.setUnknownError(response);
                break;
        }
    }

    /**
     * Constructor.
     *
     * @param context context.
     * @param manager HostMediaRecorderManager.
     */
    public HostLightProfile(final Context context, final HostMediaRecorderManager manager) {
        mContext = context;
        manager.initialize();
        mPhotoRec = manager.getCameraRecorder(null);

        mResponseHandler = createHandler("light");
        mFlashingHandler = createHandler("light-flashing");

        addApi(mGetLightApi);
        addApi(mPostLightApi);
        addApi(mDeleteLightApi);
    }

    private Handler createHandler(final String name) {
        HandlerThread thread = new HandlerThread(name);
        thread.start();
        return new Handler(thread.getLooper());
    }

    /**
     * 点滅制御.
     *
     * @param id       ライトID.
     * @param flashing 点滅パターン.
     */
    private void flashing(final String id, final long[] flashing) {
        FlashingExecutor exe = mFlashingMap.get(id);
        if (exe == null) {
            exe = new FlashingExecutor();
            mFlashingMap.put(id, exe);
        }
        exe.setLightControllable((isOn, listener) -> {
            final long startTime = currentTime();
            if (isOn) {
                mPhotoRec.turnOnFlashLight(new HostDevicePhotoRecorder.TurnOnFlashLightListener() {
                    @Override
                    public void onRequested() {
                        // NOP.
                    }

                    @Override
                    public void onTurnOn() {
                        listener.onComplete(delayTime(startTime));
                    }

                    @Override
                    public void onError(final HostDevicePhotoRecorder.Error error) {
                        listener.onFatalError(); // エラーが発生した場合、点滅を中断
                    }
                }, mFlashingHandler);
            } else {
                mPhotoRec.turnOffFlashLight(new HostDevicePhotoRecorder.TurnOffFlashLightListener() {
                    @Override
                    public void onRequested() {
                        // NOP.
                    }

                    @Override
                    public void onTurnOff() {
                        listener.onComplete(delayTime(startTime));
                    }

                    @Override
                    public void onError(final HostDevicePhotoRecorder.Error error) {
                        listener.onFatalError(); // エラーが発生した場合、点滅を中断
                    }
                }, mFlashingHandler);
            }
        });
        exe.start(flashing);
    }

    private long delayTime(final long startTime) {
        return currentTime() - startTime;
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }
}
